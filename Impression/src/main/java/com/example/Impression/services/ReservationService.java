package com.example.Impression.services;

import com.example.Impression.dto.CreationReservationDTO;
import com.example.Impression.dto.RecapitulatifReservationDTO;
import com.example.Impression.dto.ReservationDTO;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Reservation;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.exception.ResourceNotFoundException;
import com.example.Impression.exception.ReservationException;
import com.example.Impression.repositories.AnnonceRepository;
import com.example.Impression.repositories.LocataireRepository;
import com.example.Impression.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AnnonceRepository annonceRepository;
    private final LocataireRepository locataireRepository;

    /**
     * 1️⃣ Créer un récapitulatif de réservation
     */
    public RecapitulatifReservationDTO creerRecapitulatif(CreationReservationDTO creationDTO) {
        log.info("Création du récapitulatif pour l'annonce: {}", creationDTO.getAnnonceId());

        // Vérifier que l'annonce existe
        Annonce annonce = annonceRepository.findById(creationDTO.getAnnonceId())
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

        // Vérifier la disponibilité
        if (!verifierDisponibilite(creationDTO.getAnnonceId(), creationDTO.getDateArrivee(),
                creationDTO.getDateDepart())) {
            throw new ReservationException("L'annonce n'est pas disponible pour les dates sélectionnées");
        }

        // Créer le récapitulatif
        RecapitulatifReservationDTO recapitulatif = new RecapitulatifReservationDTO();
        recapitulatif.setAnnonceId(annonce.getId());
        recapitulatif.setTitreAnnonce(annonce.getTitre());
        recapitulatif.setAdresseAnnonce(formatAdresse(annonce.getAdresse()));
        recapitulatif.setNomLocateur(annonce.getLocateur().getNom() + " " + annonce.getLocateur().getPrenom());
        recapitulatif.setEmailLocateur(annonce.getLocateur().getEmail());
        recapitulatif.setDateArrivee(creationDTO.getDateArrivee());
        recapitulatif.setDateDepart(creationDTO.getDateDepart());
        recapitulatif.setPrixParNuit(annonce.getPrixParNuit());
        recapitulatif.setFraisService(creationDTO.getFraisService());
        recapitulatif.setFraisNettoyage(creationDTO.getFraisNettoyage());
        recapitulatif.setFraisDepot(creationDTO.getFraisDepot());
        recapitulatif.setNombreVoyageurs(creationDTO.getNombreVoyageurs());
        recapitulatif.setMessageProprietaire(creationDTO.getMessageProprietaire());
        recapitulatif.setModePaiement(creationDTO.getModePaiement().getLibelle());
        recapitulatif.setPaiementEnLigne(!creationDTO.getModePaiement().name().equals("PAIEMENT_SUR_PLACE"));

        recapitulatif.calculerRecapitulatif();

        return recapitulatif;
    }

    /**
     * 2️⃣ Créer une réservation
     */
    public ReservationDTO creerReservation(CreationReservationDTO creationDTO, UUID locataireId) {
        log.info("Création de réservation pour le locataire: {} et l'annonce: {}", locataireId,
                creationDTO.getAnnonceId());

        // Vérifier que l'annonce et le locataire existent
        Annonce annonce = annonceRepository.findById(creationDTO.getAnnonceId())
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

        Locataire locataire = locataireRepository.findById(locataireId)
                .orElseThrow(() -> new ResourceNotFoundException("Locataire non trouvé"));

        // Vérifier la disponibilité
        if (!verifierDisponibilite(creationDTO.getAnnonceId(), creationDTO.getDateArrivee(),
                creationDTO.getDateDepart())) {
            throw new ReservationException("L'annonce n'est pas disponible pour les dates sélectionnées");
        }

        // Créer la réservation
        Reservation reservation = new Reservation(
                annonce,
                locataire,
                creationDTO.getDateArrivee(),
                creationDTO.getDateDepart(),
                creationDTO.getNombreVoyageurs());

        // Définir les frais
        reservation.setFraisService(creationDTO.getFraisService());
        reservation.setFraisNettoyage(creationDTO.getFraisNettoyage());
        reservation.setFraisDepot(creationDTO.getFraisDepot());
        reservation.setModePaiement(creationDTO.getModePaiement());
        reservation.setMessageProprietaire(creationDTO.getMessageProprietaire());

        // Sauvegarder la réservation
        reservation = reservationRepository.save(reservation);

        // Mettre à jour le nombre de réservations du locataire
        locataire.setNombreReservations(locataire.getNombreReservations() + 1);
        locataireRepository.save(locataire);

        log.info("Réservation créée avec succès: {}", reservation.getId());

        return convertirEnDTO(reservation);
    }

    /**
     * 3️⃣ Confirmer une réservation
     */
    public ReservationDTO confirmerReservation(UUID reservationId) {
        log.info("Confirmation de la réservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatut() != StatutReservation.EN_ATTENTE) {
            throw new ReservationException("La réservation ne peut pas être confirmée dans son état actuel");
        }

        reservation.confirmer();
        reservation = reservationRepository.save(reservation);

        // TODO: Envoyer une notification au propriétaire
        envoyerNotificationProprietaire(reservation);

        return convertirEnDTO(reservation);
    }

    /**
     * 4️⃣ Annuler une réservation
     */
    public ReservationDTO annulerReservation(UUID reservationId, String raison) {
        log.info("Annulation de la réservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (!reservation.peutEtreAnnulee()) {
            throw new ReservationException("La réservation ne peut pas être annulée");
        }

        reservation.annuler(raison);
        reservation = reservationRepository.save(reservation);

        return convertirEnDTO(reservation);
    }

    /**
     * 5️⃣ Obtenir les réservations d'un locataire
     */
    public List<ReservationDTO> getReservationsLocataire(UUID locataireId) {
        log.info("Récupération des réservations pour le locataire: {}", locataireId);

        List<Reservation> reservations = reservationRepository.findByLocataireIdOrderByDateCreationDesc(locataireId);
        return reservations.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * 6️⃣ Obtenir les réservations d'une annonce
     */
    public List<ReservationDTO> getReservationsAnnonce(UUID annonceId) {
        log.info("Récupération des réservations pour l'annonce: {}", annonceId);

        List<Reservation> reservations = reservationRepository.findByAnnonceIdOrderByDateCreationDesc(annonceId);
        return reservations.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * 7️⃣ Vérifier la disponibilité d'une annonce
     */
    public boolean verifierDisponibilite(UUID annonceId, LocalDate dateArrivee, LocalDate dateDepart) {
        log.info("Vérification de disponibilité pour l'annonce: {} du {} au {}", annonceId, dateArrivee, dateDepart);

        List<Reservation> conflits = reservationRepository.findConflitsReservation(annonceId, dateArrivee, dateDepart);
        return conflits.isEmpty();
    }

    /**
     * 8️⃣ Obtenir une réservation par ID
     */
    public ReservationDTO getReservationById(UUID reservationId) {
        log.info("Récupération de la réservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        return convertirEnDTO(reservation);
    }

    /**
     * 9️⃣ Obtenir les réservations en attente de confirmation
     */
    public List<ReservationDTO> getReservationsEnAttente() {
        log.info("Récupération des réservations en attente de confirmation");

        List<Reservation> reservations = reservationRepository
                .findByStatutOrderByDateCreationAsc(StatutReservation.EN_ATTENTE);
        return reservations.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔟 Mettre à jour le statut d'une réservation
     */
    public ReservationDTO mettreAJourStatut(UUID reservationId, StatutReservation nouveauStatut) {
        log.info("Mise à jour du statut de la réservation: {} vers {}", reservationId, nouveauStatut);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        switch (nouveauStatut) {
            case CONFIRMEE:
                reservation.confirmer();
                break;
            case ANNULEE:
                reservation.annuler("Annulation par l'administrateur");
                break;
            case TERMINEE:
                reservation.terminer();
                break;
            case EN_COURS:
                reservation.mettreEnCours();
                break;
            default:
                throw new ReservationException("Statut non autorisé");
        }

        reservation = reservationRepository.save(reservation);
        return convertirEnDTO(reservation);
    }

    /**
     * 1️⃣0️⃣ Obtenir les périodes déjà réservées d'une annonce (réservations
     * confirmées/en cours)
     */
    public java.util.List<com.example.Impression.dto.PeriodeReserveeDTO> getPeriodesReserveesAnnonce(
            java.util.UUID annonceId) {
        log.info("Récupération des périodes réservées pour l'annonce: {}", annonceId);
        java.util.List<com.example.Impression.entities.Reservation> actives = reservationRepository
                .findByAnnonceIdAndStatutOrderByDateCreationDesc(annonceId,
                        com.example.Impression.enums.StatutReservation.CONFIRMEE);
        actives.addAll(reservationRepository
                .findByAnnonceIdAndStatutOrderByDateCreationDesc(annonceId,
                        com.example.Impression.enums.StatutReservation.EN_COURS));
        return actives.stream()
                .map(r -> new com.example.Impression.dto.PeriodeReserveeDTO(r.getDateArrivee(), r.getDateDepart()))
                .sorted(java.util.Comparator.comparing(com.example.Impression.dto.PeriodeReserveeDTO::getDateArrivee))
                .toList();
    }

    /**
     * 1️⃣5️⃣ Obtenir les réservations de toutes les annonces d'un locateur
     */
    public java.util.List<com.example.Impression.dto.ReservationDTO> getReservationsLocateur(
            java.util.UUID locateurId) {
        log.info("Récupération des réservations pour le locateur: {}", locateurId);
        java.util.List<com.example.Impression.entities.Reservation> reservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);
        return reservations.stream().map(this::convertirEnDTO).toList();
    }

    // Méthodes utilitaires privées

    private ReservationDTO convertirEnDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setAnnonceId(reservation.getAnnonce().getId());
        dto.setLocataireId(reservation.getLocataire().getId());
        dto.setDateArrivee(reservation.getDateArrivee());
        dto.setDateDepart(reservation.getDateDepart());
        dto.setNombreNuits(reservation.getNombreNuits());
        dto.setPrixParNuit(reservation.getPrixParNuit());
        dto.setPrixTotal(reservation.getPrixTotal());
        dto.setFraisService(reservation.getFraisService());
        dto.setFraisNettoyage(reservation.getFraisNettoyage());
        dto.setFraisDepot(reservation.getFraisDepot());
        dto.setMontantTotal(reservation.getMontantTotal());
        dto.setStatut(reservation.getStatut());
        dto.setModePaiement(reservation.getModePaiement());
        dto.setMessageProprietaire(reservation.getMessageProprietaire());
        dto.setNumeroTransaction(reservation.getNumeroTransaction());
        dto.setDatePaiement(reservation.getDatePaiement());
        dto.setDateCreation(reservation.getDateCreation());
        dto.setDateModification(reservation.getDateModification());
        dto.setDateConfirmation(reservation.getDateConfirmation());
        dto.setDateAnnulation(reservation.getDateAnnulation());
        dto.setRaisonAnnulation(reservation.getRaisonAnnulation());
        dto.setNombreVoyageurs(reservation.getNombreVoyageurs());

        // Informations de l'annonce
        dto.setTitreAnnonce(reservation.getAnnonce().getTitre());
        dto.setAdresseAnnonce(formatAdresse(reservation.getAnnonce().getAdresse()));
        dto.setNomLocateur(reservation.getAnnonce().getLocateur().getNom() + " "
                + reservation.getAnnonce().getLocateur().getPrenom());
        dto.setEmailLocateur(reservation.getAnnonce().getLocateur().getEmail());

        // Informations du locataire
        dto.setNomLocataire(reservation.getLocataire().getNom());
        dto.setPrenomLocataire(reservation.getLocataire().getPrenom());
        dto.setEmailLocataire(reservation.getLocataire().getEmail());

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

    private void envoyerNotificationProprietaire(Reservation reservation) {
        // TODO: Implémenter l'envoi de notification au propriétaire
        log.info("Notification envoyée au propriétaire {} pour la réservation {}",
                reservation.getAnnonce().getLocateur().getEmail(),
                reservation.getId());
    }
}