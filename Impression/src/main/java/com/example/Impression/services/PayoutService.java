package com.example.Impression.services;

import com.example.Impression.dto.TransactionInstructionDTO;
import com.example.Impression.dto.SoldeDTO;
import com.example.Impression.entities.*;
import com.example.Impression.enums.RibType;
import com.example.Impression.enums.TransactionStatus;
import com.example.Impression.enums.TransactionType;
import com.example.Impression.enums.StatutPaiement;
import com.example.Impression.exception.ResourceNotFoundException;
import com.example.Impression.repositories.RibRepository;
import com.example.Impression.repositories.TransactionInstructionRepository;
import com.example.Impression.repositories.PaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayoutService {

        private final RibRepository ribRepository;
        private final TransactionInstructionRepository instructionRepository;
        private final PaiementRepository paiementRepository;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public List<TransactionInstructionDTO> generateSplitOnPayment(Paiement paiement) {
                Reservation reservation = paiement.getReservation();
                Locateur locateur = reservation.getAnnonce().getLocateur();

                Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                                .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));

                Rib locateurRib = ribRepository.findFirstByLocateurAndDefautCompteTrue(locateur)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "RIB par défaut du locateur introuvable"));

                BigDecimal montant = paiement.getMontant();
                BigDecimal quatreVingt = montant.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal vingt = montant.subtract(quatreVingt);

                List<TransactionInstruction> instructions = new ArrayList<>();

                // 80% -> locateur (depuis plateforme)
                instructions.add(buildInstruction(paiement, reservation, platformRib, locateurRib, quatreVingt,
                                TransactionType.PAYOUT_LOCATEUR, "Split 80% au locateur"));

                // 20% -> reste sur plateforme (on l'enregistre comme commission)
                instructions.add(buildInstruction(paiement, reservation, platformRib, platformRib, vingt,
                                TransactionType.COMMISSION_PLATEFORME, "Commission 20% plateforme"));

                instructions = instructionRepository.saveAll(instructions);
                return instructions.stream().map(this::toDTO).collect(Collectors.toList());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public List<TransactionInstructionDTO> generateSplitForReservationAmount(Reservation reservation,
                        Paiement paiementRef,
                        BigDecimal montantTotal) {
                Locateur locateur = reservation.getAnnonce().getLocateur();

                Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                                .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));

                Rib locateurRib = ribRepository.findFirstByLocateurAndDefautCompteTrue(locateur)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "RIB par défaut du locateur introuvable"));

                BigDecimal quatreVingt = montantTotal.multiply(new BigDecimal("0.80")).setScale(2,
                                RoundingMode.HALF_UP);
                BigDecimal vingt = montantTotal.subtract(quatreVingt);

                List<TransactionInstruction> instructions = new ArrayList<>();

                TransactionInstruction toLocateur = buildInstruction(paiementRef, reservation, platformRib, locateurRib,
                                quatreVingt, TransactionType.PAYOUT_LOCATEUR, "Split 80% au locateur");
                TransactionInstruction commission = buildInstruction(paiementRef, reservation, platformRib, platformRib,
                                vingt, TransactionType.COMMISSION_PLATEFORME, "Commission 20% plateforme");

                instructions.add(toLocateur);
                instructions.add(commission);

                instructions = instructionRepository.saveAll(instructions);

                // Renseigner les RIBs de payout sur les paiements de la réservation
                try {
                        List<Paiement> paiementsReservation = paiementRepository
                                        .findByReservationIdOrderByDateCreationDesc(reservation.getId());
                        for (Paiement p : paiementsReservation) {
                                String desc = p.getDescription() != null ? p.getDescription() : "";
                                if (desc.contains("LOCATEUR")) {
                                        p.setPayoutFromRib(platformRib);
                                        p.setPayoutToRib(locateurRib);
                                } else if (desc.contains("PLATEFORME")) {
                                        p.setPayoutFromRib(platformRib);
                                        p.setPayoutToRib(platformRib);
                                }
                        }
                        paiementRepository.saveAll(paiementsReservation);
                } catch (Exception ignore) {
                        // on n'échoue pas la génération d'instructions si l'update de paiement échoue
                }
                return instructions.stream().map(this::toDTO).collect(Collectors.toList());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public TransactionInstructionDTO generatePayinOnPayment(Paiement paiement, String reference) {
                Reservation reservation = paiement.getReservation();
                Locataire locataire = reservation.getLocataire();

                Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                                .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));

                Rib locataireRib = ribRepository.findFirstByLocataireAndDefautCompteTrue(locataire)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "RIB par défaut du locataire introuvable pour payin"));

                TransactionInstruction payin = new TransactionInstruction();
                payin.setPaiement(paiement);
                payin.setReservation(reservation);
                payin.setFromRib(locataireRib);
                payin.setToRib(platformRib);
                payin.setMontant(paiement.getMontant());
                payin.setType(TransactionType.PAYIN_PLATEFORME);
                payin.setStatut(TransactionStatus.EXECUTED);
                payin.setReference(reference);
                payin.setDateExecution(java.time.LocalDateTime.now());

                payin = instructionRepository.save(payin);

                // Renseigner les RIBs de payin (locataire -> plateforme) sur tous les paiements
                // de la réservation
                try {
                        List<Paiement> paiementsReservation = paiementRepository
                                        .findByReservationIdOrderByDateCreationDesc(reservation.getId());
                        for (Paiement p : paiementsReservation) {
                                p.setPayinFromRib(locataireRib);
                                p.setPayinToRib(platformRib);
                        }
                        paiementRepository.saveAll(paiementsReservation);
                } catch (Exception ignore) {
                        // ne pas bloquer le flux si l'enrichissement échoue
                }
                return toDTO(payin);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void markAllInstructionsExecutedForPaiement(Paiement paiement, String reference) {
                List<TransactionInstruction> instructions = instructionRepository
                                .findByPaiementIdOrderByDateCreationAsc(paiement.getId());
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                for (TransactionInstruction ti : instructions) {
                        ti.setStatut(TransactionStatus.EXECUTED);
                        if (reference != null && (ti.getReference() == null || ti.getReference().isEmpty())) {
                                ti.setReference(reference);
                        }
                        ti.setDateExecution(now);
                }
                instructionRepository.saveAll(instructions);
        }

        public java.math.BigDecimal computeIncomingForRibs(java.util.List<java.util.UUID> ribIds) {
                return instructionRepository
                                .findByToRibIdInAndStatutOrderByDateCreationAsc(ribIds, TransactionStatus.EXECUTED)
                                .stream()
                                .map(TransactionInstruction::getMontant)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        }

        public java.math.BigDecimal computeOutgoingForRibs(java.util.List<java.util.UUID> ribIds) {
                return instructionRepository
                                .findByFromRibIdInAndStatutOrderByDateCreationAsc(ribIds, TransactionStatus.EXECUTED)
                                .stream()
                                .map(TransactionInstruction::getMontant)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        }

        public List<TransactionInstructionDTO> generateRefundInstructionsForCancellation(Reservation reservation,
                        Paiement paiement) {
                // Calcule la fen eatre: maintenant vs dateArrivee
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime arrivee = reservation.getDateArrivee().atStartOfDay();
                long hoursBefore = ChronoUnit.HOURS.between(now, arrivee);

                BigDecimal total = paiement.getMontant();

                Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                                .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));

                Locateur locateur = reservation.getAnnonce().getLocateur();
                Rib locateurRib = ribRepository.findFirstByLocateurAndDefautCompteTrue(locateur)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "RIB par défaut du locateur introuvable"));

                Locataire locataire = reservation.getLocataire();
                Rib locataireRib = ribRepository.findFirstByLocataireAndDefautCompteTrue(locataire)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "RIB par défaut du locataire introuvable pour remboursement"));

                List<TransactionInstruction> instructions = new ArrayList<>();

                if (hoursBefore < 24) {
                        // 0% remboursement
                        log.info("Annulation <24h, aucun remboursement");
                        return new ArrayList<>();
                } else if (hoursBefore < 48) {
                        // 50% total: 40% locateur + 10% plateforme
                        BigDecimal quarante = total.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal dix = total.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);

                        instructions.add(buildInstruction(paiement, reservation, locateurRib, locataireRib, quarante,
                                        TransactionType.REFUND_LOCATAIRE_FROM_LOCATEUR,
                                        "Remboursement 40% depuis locateur"));
                        instructions.add(buildInstruction(paiement, reservation, platformRib, locataireRib, dix,
                                        TransactionType.REFUND_LOCATAIRE_FROM_PLATEFORME,
                                        "Remboursement 10% depuis plateforme"));
                } else {
                        // 100% total: 80% locateur + 20% plateforme
                        BigDecimal quatreVingt = total.multiply(new BigDecimal("0.80")).setScale(2,
                                        RoundingMode.HALF_UP);
                        BigDecimal vingt = total.subtract(quatreVingt);

                        instructions.add(buildInstruction(paiement, reservation, locateurRib, locataireRib, quatreVingt,
                                        TransactionType.REFUND_LOCATAIRE_FROM_LOCATEUR,
                                        "Remboursement 80% depuis locateur"));
                        instructions.add(buildInstruction(paiement, reservation, platformRib, locataireRib, vingt,
                                        TransactionType.REFUND_LOCATAIRE_FROM_PLATEFORME,
                                        "Remboursement 20% depuis plateforme"));
                }

                instructions = instructionRepository.saveAll(instructions);

                // Si des instructions de remboursement ont été créées, marquer les paiements
                // comme REMBOURSE
                if (!instructions.isEmpty()) {
                        try {
                                List<Paiement> paiementsReservation = paiementRepository
                                                .findByReservationIdOrderByDateCreationDesc(reservation.getId());
                                java.time.LocalDateTime now2 = java.time.LocalDateTime.now();
                                for (Paiement p : paiementsReservation) {
                                        if (p.getStatut() == StatutPaiement.PAYE) {
                                                p.setStatut(StatutPaiement.REMBOURSE);
                                                p.setDateRemboursement(now2);
                                                p.setDateModification(now2);
                                        }
                                }
                                paiementRepository.saveAll(paiementsReservation);
                        } catch (Exception ignore) {
                                // on ne bloque pas le flux si la mise à jour de statut échoue
                        }
                }

                return instructions.stream().map(this::toDTO).collect(Collectors.toList());
        }

        public List<TransactionInstructionDTO> listPending() {
                return instructionRepository.findByStatutOrderByDateCreationAsc(TransactionStatus.PENDING)
                                .stream().map(this::toDTO).collect(Collectors.toList());
        }

        public List<TransactionInstructionDTO> getInstructionsByReservation(UUID reservationId) {
                return instructionRepository.findByReservationIdOrderByDateCreationAsc(reservationId)
                                .stream().map(this::toDTO).collect(Collectors.toList());
        }

        public List<TransactionInstructionDTO> getInstructionsByPaiement(UUID paiementId) {
                return instructionRepository.findByPaiementIdOrderByDateCreationAsc(paiementId)
                                .stream().map(this::toDTO).collect(Collectors.toList());
        }

        public TransactionInstructionDTO markExecuted(UUID instructionId, String reference) {
                TransactionInstruction ti = instructionRepository.findById(instructionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Instruction introuvable"));
                ti.setStatut(TransactionStatus.EXECUTED);
                ti.setReference(reference);
                ti.setDateExecution(LocalDateTime.now());
                return toDTO(instructionRepository.save(ti));
        }

        public TransactionInstructionDTO cancel(UUID instructionId, String notes) {
                TransactionInstruction ti = instructionRepository.findById(instructionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Instruction introuvable"));
                ti.setStatut(TransactionStatus.CANCELLED);
                ti.setNotes(notes);
                return toDTO(instructionRepository.save(ti));
        }

        public SoldeDTO getSoldeForLocataire(Locataire locataire) {
                List<Rib> ribs = ribRepository.findByLocataireAndActif(locataire, true);
                List<UUID> ribIds = ribs.stream().map(Rib::getId).collect(Collectors.toList());
                java.math.BigDecimal entrees = computeIncomingForRibs(ribIds);
                java.math.BigDecimal sorties = computeOutgoingForRibs(ribIds);
                return new SoldeDTO(entrees, sorties, entrees.subtract(sorties));
        }

        public SoldeDTO getSoldeForLocateur(Locateur locateur) {
                List<Rib> ribs = ribRepository.findByLocateurAndActif(locateur, true);
                List<UUID> ribIds = ribs.stream().map(Rib::getId).collect(Collectors.toList());
                java.math.BigDecimal entrees = computeIncomingForRibs(ribIds);
                java.math.BigDecimal sorties = computeOutgoingForRibs(ribIds);
                return new SoldeDTO(entrees, sorties, entrees.subtract(sorties));
        }

        public SoldeDTO getSoldeForPlateforme() {
                Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                                .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));
                java.util.List<java.util.UUID> ids = java.util.List.of(platformRib.getId());
                java.math.BigDecimal entrees = computeIncomingForRibs(ids);
                java.math.BigDecimal sorties = computeOutgoingForRibs(ids);
                return new SoldeDTO(entrees, sorties, entrees.subtract(sorties));
        }

        public List<TransactionInstructionDTO> encaissementsForRibIds(List<UUID> ribIds) {
                return instructionRepository
                                .findByToRibIdInAndStatutOrderByDateCreationAsc(ribIds, TransactionStatus.EXECUTED)
                                .stream().map(this::toDTO).collect(Collectors.toList());
        }

        private TransactionInstruction buildInstruction(Paiement paiement,
                        Reservation reservation,
                        Rib from,
                        Rib to,
                        BigDecimal montant,
                        TransactionType type,
                        String notes) {
                TransactionInstruction ti = new TransactionInstruction();
                ti.setPaiement(paiement);
                ti.setReservation(reservation);
                ti.setFromRib(from);
                ti.setToRib(to);
                ti.setMontant(montant);
                ti.setType(type);
                ti.setNotes(notes);
                return ti;
        }

        private TransactionInstructionDTO toDTO(TransactionInstruction ti) {
                TransactionInstructionDTO dto = new TransactionInstructionDTO();
                dto.setId(ti.getId());
                dto.setReservationId(ti.getReservation() != null ? ti.getReservation().getId() : null);
                dto.setPaiementId(ti.getPaiement() != null ? ti.getPaiement().getId() : null);
                dto.setType(ti.getType());
                dto.setStatut(ti.getStatut());
                dto.setFromRibId(ti.getFromRib() != null ? ti.getFromRib().getId() : null);
                dto.setToRibId(ti.getToRib() != null ? ti.getToRib().getId() : null);
                dto.setMontant(ti.getMontant());
                dto.setReference(ti.getReference());
                dto.setNotes(ti.getNotes());
                dto.setDateCreation(ti.getDateCreation());
                dto.setDateModification(ti.getDateModification());
                dto.setDateExecution(ti.getDateExecution());
                return dto;
        }
}
