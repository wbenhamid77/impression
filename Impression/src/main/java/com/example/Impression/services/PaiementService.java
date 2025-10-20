package com.example.Impression.services;

import com.example.Impression.dto.ConfirmationPaiementDTO;
import com.example.Impression.dto.CreationPaiementDTO;
import com.example.Impression.dto.PaiementDTO;
import com.example.Impression.dto.RemboursementPaiementDTO;
import com.example.Impression.entities.Paiement;
import com.example.Impression.entities.Reservation;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Créer un nouveau paiement pour une réservation
     */
    public PaiementDTO creerPaiement(CreationPaiementDTO creationDTO) {
        log.info("Création d'un paiement pour la réservation: {}", creationDTO.getReservationId());

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

        // Créer le paiement
        Paiement paiement = new Paiement(
                reservation,
                creationDTO.getMontant(),
                creationDTO.getTypePaiement(),
                creationDTO.getModePaiement(),
                creationDTO.getDescription());

        if (creationDTO.getMetadonnees() != null) {
            paiement.setMetadonnees(creationDTO.getMetadonnees());
        }

        paiement = paiementRepository.save(paiement);

        log.info("Paiement créé avec succès: {}", paiement.getId());

        return convertirEnDTO(paiement);
    }

    /**
     * Confirmer un paiement
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

        paiement.marquerCommePaye(confirmationDTO.getNumeroTransaction());

        if (confirmationDTO.getReferenceExterne() != null) {
            paiement.setReferenceExterne(confirmationDTO.getReferenceExterne());
        }

        if (confirmationDTO.getMetadonnees() != null) {
            paiement.setMetadonnees(confirmationDTO.getMetadonnees());
        }

        paiement = paiementRepository.save(paiement);

        // Mettre à jour le statut de la réservation si nécessaire
        mettreAJourStatutReservation(paiement.getReservation());

        log.info("Paiement confirmé avec succès: {}", paiement.getId());

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

        paiement.rembourser(remboursementDTO.getNumeroRemboursement(), remboursementDTO.getRaisonRemboursement());

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
     */
    public List<PaiementDTO> getPaiementsLocataire(UUID locataireId) {
        log.info("Récupération des paiements pour le locataire: {}", locataireId);

        List<Paiement> paiements = paiementRepository.findByLocataireId(locataireId);
        return paiements.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
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

        // Informations de la réservation
        Reservation reservation = paiement.getReservation();
        dto.setTitreAnnonce(reservation.getAnnonce().getTitre());
        dto.setAdresseAnnonce(formatAdresse(reservation.getAnnonce().getAdresse()));
        dto.setNomLocataire(reservation.getLocataire().getNom());
        dto.setPrenomLocataire(reservation.getLocataire().getPrenom());
        dto.setEmailLocataire(reservation.getLocataire().getEmail());
        dto.setNomLocateur(reservation.getAnnonce().getLocateur().getNom());
        dto.setPrenomLocateur(reservation.getAnnonce().getLocateur().getPrenom());
        dto.setEmailLocateur(reservation.getAnnonce().getLocateur().getEmail());
        dto.setDateArrivee(reservation.getDateArrivee().atStartOfDay());
        dto.setDateDepart(reservation.getDateDepart().atStartOfDay());
        dto.setNombreNuits(reservation.getNombreNuits());

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
