package com.example.Impression.services;

import com.example.Impression.dto.ConfirmationPaiementDTO;
import com.example.Impression.dto.CreationPaiementDTO;
import com.example.Impression.dto.PaiementDTO;
import com.example.Impression.dto.PaiementStatsDTO;
import com.example.Impression.dto.RemboursementPaiementDTO;
import com.example.Impression.entities.Paiement;
import com.example.Impression.entities.Reservation;
import com.example.Impression.entities.Rib;
import com.example.Impression.enums.RibType;
import com.example.Impression.enums.StatutPaiement;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.exception.ResourceNotFoundException;
import com.example.Impression.exception.ReservationException;
import com.example.Impression.repositories.PaiementRepository;
import com.example.Impression.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final PayoutService payoutService;
    private final com.example.Impression.repositories.RibRepository ribRepository;

    /**
     * Créer un nouveau paiement pour une réservation
     * Cette méthode crée automatiquement deux paiements séparés :
     * - Un paiement pour le locateur (80% du montant)
     * - Un paiement pour la plateforme (20% du montant)
     */
    public PaiementDTO creerPaiement(CreationPaiementDTO creationDTO) {
        log.info("Création des paiements pour la réservation: {}", creationDTO.getReservationId());

        // Vérifier que la réservation existe
        Reservation reservation = reservationRepository.findById(creationDTO.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier que la réservation est confirmée
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ReservationException("La réservation doit être confirmée pour créer un paiement");
        }

        // Vérifier qu'il n'y a pas déjà un paiement en attente pour cette réservation
        if (paiementRepository
                .findPaiementEnAttenteByReservationId(creationDTO.getReservationId(), StatutPaiement.EN_ATTENTE)
                .isPresent()) {
            throw new ReservationException("Un paiement est déjà en attente pour cette réservation");
        }

        // Créer les deux paiements séparés (80% locateur, 20% plateforme)
        List<Paiement> paiementsCrees = creerPaiementsSplit(reservation, creationDTO);

        // Retourner le paiement principal (celui du locateur)
        Paiement paiementPrincipal = paiementsCrees.stream()
                .filter(p -> p.getDescription() != null && p.getDescription().contains("LOCATEUR"))
                .findFirst()
                .orElse(paiementsCrees.get(0));

        log.info("Deux paiements créés avec succès: locateur {} et plateforme {}",
                paiementsCrees.get(0).getId(),
                paiementsCrees.size() > 1 ? paiementsCrees.get(1).getId() : "N/A");

        return convertirEnDTO(paiementPrincipal);
    }

    /**
     * Confirmer un paiement
     * Cette méthode confirme automatiquement les deux paiements associés (locateur
     * et plateforme)
     * lorsque l'API /api/paiements/{id}/confirmer est appelée
     */
    public PaiementDTO confirmerPaiement(UUID paiementId, ConfirmationPaiementDTO confirmationDTO) {
        log.info("Confirmation du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (!paiement.peutEtreAnnule()) {
            throw new ReservationException("Le paiement ne peut pas être confirmé dans son état actuel");
        }

        if (paiement.estExpire()) {
            throw new ReservationException("Le paiement a expiré");
        }

        // Récupérer tous les paiements de la même réservation pour confirmer les deux
        // paiements associés
        UUID reservationId = paiement.getReservation().getId();
        List<Paiement> paiementsAssocies = paiementRepository
                .findByReservationIdOrderByDateCreationDesc(reservationId);

        // Filtrer les paiements EN_ATTENTE de la même réservation qui sont liés
        // (LOCATEUR ou PLATEFORME)
        List<Paiement> paiementsAConfirmer = paiementsAssocies.stream()
                .filter(p -> (p.getStatut() == StatutPaiement.EN_ATTENTE || p.getStatut() == StatutPaiement.EN_COURS))
                .filter(p -> p.getId().equals(paiementId) ||
                        (p.getDescription() != null && (p.getDescription().contains("LOCATEUR") ||
                                p.getDescription().contains("PLATEFORME"))))
                .collect(Collectors.toList());

        // S'assurer qu'on a bien les deux paiements (locateur et plateforme)
        if (paiementsAConfirmer.size() < 2) {
            log.warn("Attention: seulement {} paiement(s) trouvé(s) au lieu de 2 pour la réservation {}",
                    paiementsAConfirmer.size(), reservationId);
        }

        // Valider l'unicité du numéro de transaction si fourni (vérifier sur tous les
        // paiements)
        if (confirmationDTO.getNumeroTransaction() != null) {
            String baseNumero = confirmationDTO.getNumeroTransaction();
            for (Paiement p : paiementsAConfirmer) {
                String numeroUnique = baseNumero + "-" + p.getId().toString().substring(0, 8);
                paiementRepository.findByNumeroTransaction(numeroUnique)
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(p.getId())) {
                                throw new ReservationException("numeroTransaction déjà utilisé par un autre paiement");
                            }
                        });
            }
        }

        // Confirmer tous les paiements associés
        for (Paiement p : paiementsAConfirmer) {
            String numeroTransactionUnique = confirmationDTO.getNumeroTransaction() != null
                    ? confirmationDTO.getNumeroTransaction() + "-" + p.getId().toString().substring(0, 8)
                    : null;

            p.marquerCommePaye(numeroTransactionUnique);

            if (confirmationDTO.getReferenceExterne() != null) {
                p.setReferenceExterne(confirmationDTO.getReferenceExterne());
            }

            if (confirmationDTO.getMetadonnees() != null) {
                p.setMetadonnees(confirmationDTO.getMetadonnees());
            }

            paiementRepository.save(p);
            log.info("Paiement confirmé: {} ({} - {})", p.getId(),
                    p.getDescription() != null && p.getDescription().contains("LOCATEUR") ? "LOCATEUR" : "PLATEFORME",
                    p.getMontant());
        }

        // Mettre à jour le statut de la réservation si nécessaire
        mettreAJourStatutReservation(paiement.getReservation());

        // Générer une transaction PAYIN (locataire -> plateforme) et le split 80/20,
        // puis marquer comme exécuté
        try {
            payoutService.generatePayinOnPayment(paiement, confirmationDTO.getNumeroTransaction());
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'instruction PAYIN: {}", e.getMessage());
        }

        try {
            // Marquer comme exécutées les instructions liées à chacun des paiements
            // associés
            for (Paiement p : paiementsAConfirmer) {
                String ref = confirmationDTO.getNumeroTransaction() != null
                        ? confirmationDTO.getNumeroTransaction() + "-" + p.getId().toString().substring(0, 8)
                        : null;
                payoutService.markAllInstructionsExecutedForPaiement(p, ref);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la validation des instructions de split: {}", e.getMessage());
        }

        log.info("Tous les paiements confirmés avec succès pour la réservation: {}", reservationId);

        return convertirEnDTO(paiement);
    }

    /**
     * Marquer un paiement comme en cours
     */
    public PaiementDTO marquerPaiementEnCours(UUID paiementId) {
        log.info("Marquage du paiement en cours: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (paiement.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new ReservationException("Le paiement ne peut pas être marqué comme en cours");
        }

        paiement.marquerCommeEnCours();
        paiement = paiementRepository.save(paiement);

        return convertirEnDTO(paiement);
    }

    /**
     * Marquer un paiement comme échec
     */
    public PaiementDTO marquerPaiementEchec(UUID paiementId, String raison) {
        log.info("Marquage du paiement en échec: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.marquerCommeEchec(raison);
        paiement = paiementRepository.save(paiement);

        return convertirEnDTO(paiement);
    }

    /**
     * Annuler un paiement
     */
    public PaiementDTO annulerPaiement(UUID paiementId, String raison) {
        log.info("Annulation du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (!paiement.peutEtreAnnule()) {
            throw new ReservationException("Le paiement ne peut pas être annulé");
        }

        paiement.marquerCommeAnnule(raison);
        paiement = paiementRepository.save(paiement);

        return convertirEnDTO(paiement);
    }

    /**
     * Rembourser un paiement
     */
    public PaiementDTO rembourserPaiement(UUID paiementId, RemboursementPaiementDTO remboursementDTO) {
        log.info("Remboursement du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (!paiement.peutEtreRembourse()) {
            throw new ReservationException("Le paiement ne peut pas être remboursé");
        }

        // Ne pas changer le statut en REMBOURSE: on trace le remboursement via
        // métadonnées
        paiement.setNumeroRemboursement(remboursementDTO.getNumeroRemboursement());
        paiement.setRaisonRemboursement(remboursementDTO.getRaisonRemboursement());
        paiement.setDateRemboursement(java.time.LocalDateTime.now());
        paiement.setDateModification(java.time.LocalDateTime.now());

        if (remboursementDTO.getMetadonnees() != null) {
            paiement.setMetadonnees(remboursementDTO.getMetadonnees());
        }

        paiement = paiementRepository.save(paiement);

        return convertirEnDTO(paiement);
    }

    /**
     * Obtenir les paiements d'une réservation
     */
    public List<PaiementDTO> getPaiementsReservation(UUID reservationId) {
        log.info("Récupération des paiements pour la réservation: {}", reservationId);

        List<Paiement> paiements = paiementRepository.findByReservationIdOrderByDateCreationDesc(reservationId);
        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les paiements d'un locataire
     * Affiche un seul paiement consolidé avec le montant total pour chaque
     * réservation
     */
    public List<PaiementDTO> getPaiementsLocataire(UUID locataireId) {
        log.info("Récupération des paiements pour le locataire: {}", locataireId);

        List<Paiement> paiements = paiementRepository.findByLocataireId(locataireId);

        // Grouper les paiements par réservation et consolider les paiements LOCATEUR +
        // PLATEFORME
        Map<UUID, List<Paiement>> paiementsParReservation = paiements.stream()
                .collect(Collectors.groupingBy(p -> p.getReservation().getId()));

        List<PaiementDTO> paiementsConsolides = new ArrayList<>();

        for (Map.Entry<UUID, List<Paiement>> entry : paiementsParReservation.entrySet()) {
            List<Paiement> paiementsReservation = entry.getValue();

            // Si la réservation a 2 paiements (LOCATEUR + PLATEFORME), on les consolide
            if (paiementsReservation.size() >= 2) {
                // Trouver le paiement locateur (80%)
                Paiement paiementLocateur = paiementsReservation.stream()
                        .filter(p -> p.getDescription() != null && p.getDescription().contains("LOCATEUR"))
                        .findFirst()
                        .orElse(paiementsReservation.get(0));

                // Trouver le paiement plateforme (20%)
                Paiement paiementPlateforme = paiementsReservation.stream()
                        .filter(p -> p.getDescription() != null && p.getDescription().contains("PLATEFORME"))
                        .findFirst()
                        .orElse(null);

                // Créer un paiement consolidé avec le montant total
                Paiement paiementConsolide = new Paiement(
                        paiementLocateur.getReservation(),
                        paiementLocateur.getMontant()
                                .add(paiementPlateforme != null ? paiementPlateforme.getMontant() : BigDecimal.ZERO),
                        paiementLocateur.getTypePaiement(),
                        paiementLocateur.getModePaiement(),
                        paiementLocateur.getDescription().replace("- LOCATEUR (80%)", ""));

                // Copier les propriétés du paiement principal
                paiementConsolide.setId(paiementLocateur.getId());
                paiementConsolide.setStatut(paiementLocateur.getStatut());
                paiementConsolide.setNumeroTransaction(paiementLocateur.getNumeroTransaction());
                paiementConsolide.setReferenceExterne(paiementLocateur.getReferenceExterne());
                paiementConsolide.setDateCreation(paiementLocateur.getDateCreation());
                paiementConsolide.setDateModification(paiementLocateur.getDateModification());
                paiementConsolide.setDatePaiement(paiementLocateur.getDatePaiement());
                paiementConsolide.setDateExpiration(paiementLocateur.getDateExpiration());
                paiementConsolide.setDateEchec(paiementLocateur.getDateEchec());
                paiementConsolide.setNumeroRemboursement(paiementLocateur.getNumeroRemboursement());
                paiementConsolide.setDateRemboursement(paiementLocateur.getDateRemboursement());
                paiementConsolide.setRaisonRemboursement(paiementLocateur.getRaisonRemboursement());
                paiementConsolide.setMetadonnees(paiementLocateur.getMetadonnees());

                paiementsConsolides.add(convertirEnDTO(paiementConsolide));
            } else {
                // Si un seul paiement, on le retourne tel quel
                paiementsConsolides.add(convertirEnDTO(paiementsReservation.get(0)));
            }
        }

        return paiementsConsolides;
    }

    /**
     * Obtenir les paiements d'un locateur
     */
    public List<PaiementDTO> getPaiementsLocateur(UUID locateurId) {
        log.info("Récupération des paiements pour le locateur: {}", locateurId);

        List<Paiement> paiements = paiementRepository.findByLocateurId(locateurId);
        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir des statistiques des paiements pour un locataire ou un locateur
     */
    public PaiementStatsDTO getPaiementStats(UUID userId, String userType) {
        List<Paiement> paiements;
        if ("locataire".equalsIgnoreCase(userType)) {
            paiements = paiementRepository.findByLocataireId(userId);
        } else if ("locateur".equalsIgnoreCase(userType)) {
            paiements = paiementRepository.findByLocateurId(userId);
        } else {
            throw new ReservationException("userType doit être 'locataire' ou 'locateur'");
        }

        java.math.BigDecimal zero = java.math.BigDecimal.ZERO;
        java.util.function.Function<java.util.stream.Stream<Paiement>, java.math.BigDecimal> sumMontant = s -> s
                .map(Paiement::getMontant)
                .filter(m -> m != null)
                .reduce(zero, java.math.BigDecimal::add);

        long totalCount = paiements.size();
        java.math.BigDecimal totalMontant = sumMontant.apply(paiements.stream());

        java.math.BigDecimal totalPaye = sumMontant.apply(paiements.stream().filter(Paiement::estPaye));
        java.math.BigDecimal totalRembourse = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.REMBOURSE));
        java.math.BigDecimal totalEnAttente = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.EN_ATTENTE));
        java.math.BigDecimal totalEnCours = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.EN_COURS));
        java.math.BigDecimal totalEchec = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.ECHEC));
        java.math.BigDecimal totalExpire = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.EXPIRE));
        java.math.BigDecimal totalAnnule = sumMontant.apply(
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.ANNULE));

        return new PaiementStatsDTO(
                totalCount,
                totalMontant,
                totalPaye,
                totalRembourse,
                totalEnAttente,
                totalEnCours,
                totalEchec,
                totalExpire,
                totalAnnule);
    }

    /**
     * Obtenir les paiements en attente d'un locataire
     */
    public List<PaiementDTO> getPaiementsEnAttenteLocataire(UUID locataireId) {
        log.info("Récupération des paiements en attente pour le locataire: {}", locataireId);

        List<Paiement> paiements = paiementRepository.findPaiementsEnAttenteByLocataireId(locataireId,
                StatutPaiement.EN_ATTENTE);
        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les paiements en attente d'un locateur
     */
    public List<PaiementDTO> getPaiementsEnAttenteLocateur(UUID locateurId) {
        log.info("Récupération des paiements en attente pour le locateur: {}", locateurId);

        List<Paiement> paiements = paiementRepository.findPaiementsEnAttenteByLocateurId(locateurId,
                StatutPaiement.EN_ATTENTE);
        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un paiement par ID
     */
    public PaiementDTO getPaiementById(UUID paiementId) {
        log.info("Récupération du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        return convertirEnDTO(paiement);
    }

    /**
     * Obtenir les paiements expirés
     */
    public List<PaiementDTO> getPaiementsExpires() {
        log.info("Récupération des paiements expirés");

        List<StatutPaiement> statutsExpirables = List.of(StatutPaiement.EN_ATTENTE, StatutPaiement.EN_COURS);
        List<Paiement> paiements = paiementRepository.findPaiementsExpiresNonTraites(statutsExpirables,
                LocalDateTime.now());

        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marquer les paiements expirés
     */
    @Transactional
    public void marquerPaiementsExpires() {
        log.info("Marquage des paiements expirés");

        List<Paiement> paiementsExpires = paiementRepository.findPaiementsExpiresNonTraites(
                List.of(StatutPaiement.EN_ATTENTE, StatutPaiement.EN_COURS),
                LocalDateTime.now());

        for (Paiement paiement : paiementsExpires) {
            paiement.marquerCommeExpire();
            paiementRepository.save(paiement);

            // Annuler la réservation associée
            Reservation reservation = paiement.getReservation();
            if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
                reservation.annuler("Paiement non effectué dans les délais");
                reservationRepository.save(reservation);
            }
        }

        log.info("{} paiements marqués comme expirés", paiementsExpires.size());
    }

    /**
     * Créer deux paiements séparés : 80% pour le locateur et 20% pour la plateforme
     * 
     * @param reservation La réservation associée
     * @param creationDTO Les données de création du paiement
     * @return Liste des deux paiements créés (locateur, puis plateforme)
     */
    private List<Paiement> creerPaiementsSplit(Reservation reservation, CreationPaiementDTO creationDTO) {
        BigDecimal montantTotal = creationDTO.getMontant();

        // Calculer les montants : 80% pour locateur, 20% pour plateforme
        BigDecimal montantLocateur = montantTotal.multiply(new BigDecimal("0.80"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantPlateforme = montantTotal.subtract(montantLocateur);

        // Description de base
        String descriptionBase = creationDTO.getDescription() != null
                ? creationDTO.getDescription()
                : "Paiement réservation";

        // Créer le paiement pour le locateur (80%)
        Paiement paiementLocateur = new Paiement(
                reservation,
                montantLocateur,
                creationDTO.getTypePaiement(),
                creationDTO.getModePaiement(),
                descriptionBase + " - LOCATEUR (80%)");

        if (creationDTO.getMetadonnees() != null) {
            paiementLocateur.setMetadonnees(creationDTO.getMetadonnees());
        }

        // Créer le paiement pour la plateforme (20%)
        Paiement paiementPlateforme = new Paiement(
                reservation,
                montantPlateforme,
                creationDTO.getTypePaiement(),
                creationDTO.getModePaiement(),
                descriptionBase + " - PLATEFORME (20% commission)");

        if (creationDTO.getMetadonnees() != null) {
            paiementPlateforme.setMetadonnees(creationDTO.getMetadonnees());
        }

        // Sauvegarder les deux paiements
        paiementLocateur = paiementRepository.save(paiementLocateur);
        paiementPlateforme = paiementRepository.save(paiementPlateforme);

        log.info("Paiement locateur créé: {} (montant: {})", paiementLocateur.getId(), montantLocateur);
        log.info("Paiement plateforme créé: {} (montant: {})", paiementPlateforme.getId(), montantPlateforme);

        // Renseigner les RIBs (payin/payout) sur les deux paiements créés
        try {
            Rib platformRib = ribRepository.findFirstByType(RibType.PLATEFORME)
                    .orElseThrow(() -> new ResourceNotFoundException("RIB plateforme introuvable"));
            var locateur = reservation.getAnnonce().getLocateur();
            var locataire = reservation.getLocataire();
            Rib locateurRib = ribRepository.findFirstByLocateurAndDefautCompteTrue(locateur)
                    .orElseThrow(() -> new ResourceNotFoundException("RIB par défaut du locateur introuvable"));
            Rib locataireRib = ribRepository.findFirstByLocataireAndDefautCompteTrue(locataire)
                    .orElseThrow(() -> new ResourceNotFoundException("RIB par défaut du locataire introuvable"));

            // PAYIN (locataire -> plateforme) sur les deux paiements
            paiementLocateur.setPayinFromRib(locataireRib);
            paiementLocateur.setPayinToRib(platformRib);
            paiementPlateforme.setPayinFromRib(locataireRib);
            paiementPlateforme.setPayinToRib(platformRib);

            // PAYOUT pour le paiement locateur (plateforme -> locateur)
            paiementLocateur.setPayoutFromRib(platformRib);
            paiementLocateur.setPayoutToRib(locateurRib);

            // Commission (plateforme -> plateforme) pour le paiement plateforme
            paiementPlateforme.setPayoutFromRib(platformRib);
            paiementPlateforme.setPayoutToRib(platformRib);

            paiementLocateur = paiementRepository.save(paiementLocateur);
            paiementPlateforme = paiementRepository.save(paiementPlateforme);
        } catch (Exception e) {
            log.warn("Renseignement des RIBs sur paiements échoué: {}", e.getMessage());
        }

        // Générer les instructions de split 80/20 dès la création (liées au paiement
        // locateur)
        try {
            payoutService.generateSplitForReservationAmount(reservation, paiementLocateur, montantTotal);
        } catch (Exception e) {
            log.error("Erreur lors de la génération des instructions de split: {}", e.getMessage());
        }

        return List.of(paiementLocateur, paiementPlateforme);
    }

    // Méthodes utilitaires privées

    private PaiementDTO convertirEnDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setReservationId(paiement.getReservation().getId());
        dto.setMontant(paiement.getMontant());
        dto.setTypePaiement(paiement.getTypePaiement());
        dto.setStatut(paiement.getStatut());
        dto.setModePaiement(paiement.getModePaiement());
        dto.setNumeroTransaction(paiement.getNumeroTransaction());
        dto.setReferenceExterne(paiement.getReferenceExterne());
        dto.setDescription(paiement.getDescription());
        dto.setDateCreation(paiement.getDateCreation());
        dto.setDateModification(paiement.getDateModification());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setDateExpiration(paiement.getDateExpiration());
        dto.setDateEchec(paiement.getDateEchec());
        dto.setNumeroRemboursement(paiement.getNumeroRemboursement());
        dto.setDateRemboursement(paiement.getDateRemboursement());
        dto.setRaisonRemboursement(paiement.getRaisonRemboursement());
        dto.setMetadonnees(paiement.getMetadonnees());

        // Informations de la réservation (null-safe)
        Reservation reservation = paiement.getReservation();
        if (reservation != null) {
            var annonce = reservation.getAnnonce();
            var locataire = reservation.getLocataire();
            var locateur = (annonce != null) ? annonce.getLocateur() : null;

            dto.setTitreAnnonce(annonce != null ? annonce.getTitre() : null);
            dto.setAdresseAnnonce(annonce != null ? formatAdresse(annonce.getAdresse()) : "Adresse non disponible");

            dto.setNomLocataire(locataire != null ? locataire.getNom() : null);
            dto.setPrenomLocataire(locataire != null ? locataire.getPrenom() : null);
            dto.setEmailLocataire(locataire != null ? locataire.getEmail() : null);

            dto.setNomLocateur(locateur != null ? locateur.getNom() : null);
            dto.setPrenomLocateur(locateur != null ? locateur.getPrenom() : null);
            dto.setEmailLocateur(locateur != null ? locateur.getEmail() : null);

            if (reservation.getDateArrivee() != null) {
                dto.setDateArrivee(reservation.getDateArrivee().atStartOfDay());
            }
            if (reservation.getDateDepart() != null) {
                dto.setDateDepart(reservation.getDateDepart().atStartOfDay());
            }
            dto.setNombreNuits(reservation.getNombreNuits());
        }

        // Informations calculées
        dto.setEstExpire(paiement.estExpire());
        dto.setPeutEtreAnnule(paiement.peutEtreAnnule());
        dto.setPeutEtreRembourse(paiement.peutEtreRembourse());

        if (paiement.getDateExpiration() != null) {
            dto.setHeuresRestantes(ChronoUnit.HOURS.between(LocalDateTime.now(), paiement.getDateExpiration()));
        }

        return dto;
    }

    private String formatAdresse(com.example.Impression.entities.Adresse adresse) {
        if (adresse == null) {
            return "Adresse non disponible";
        }
        return String.format("%s, %s %s, %s",
                adresse.getRue() != null ? adresse.getRue() : "",
                adresse.getCodePostal() != null ? adresse.getCodePostal() : "",
                adresse.getVille() != null ? adresse.getVille() : "",
                adresse.getPays() != null ? adresse.getPays() : "");
    }

    private void mettreAJourStatutReservation(Reservation reservation) {
        // Vérifier si tous les paiements de la réservation sont payés
        List<Paiement> paiementsPayes = paiementRepository.findPaiementsPayesByReservationId(
                reservation.getId(), StatutPaiement.PAYE);

        // Si tous les paiements sont payés, on peut passer la réservation en cours
        // (logique métier à adapter selon vos besoins)
        if (!paiementsPayes.isEmpty()) {
            // Ici vous pouvez ajouter une logique pour déterminer si la réservation
            // peut passer au statut suivant
        }
    }
}
