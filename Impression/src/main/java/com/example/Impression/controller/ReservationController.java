package com.example.Impression.controller;

import com.example.Impression.dto.CreationReservationDTO;
import com.example.Impression.dto.RecapitulatifReservationDTO;
import com.example.Impression.dto.ReservationDTO;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 1️⃣ POST /api/reservations/recapitulatif
     * Créer un récapitulatif de réservation
     */
    @PostMapping("/recapitulatif")
    public ResponseEntity<RecapitulatifReservationDTO> creerRecapitulatif(
            @Valid @RequestBody CreationReservationDTO creationDTO) {
        log.info("Demande de création de récapitulatif pour l'annonce: {}", creationDTO.getAnnonceId());

        RecapitulatifReservationDTO recapitulatif = reservationService.creerRecapitulatif(creationDTO);
        return ResponseEntity.ok(recapitulatif);
    }

    /**
     * 2️⃣ POST /api/reservations
     * Créer une réservation
     */
    @PostMapping
    public ResponseEntity<ReservationDTO> creerReservation(
            @Valid @RequestBody CreationReservationDTO creationDTO,
            @RequestParam UUID locataireId) {
        log.info("Demande de création de réservation pour le locataire: {}", locataireId);

        ReservationDTO reservation = reservationService.creerReservation(creationDTO, locataireId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    /**
     * 3️⃣ PUT /api/reservations/{id}/confirmer
     * Confirmer une réservation
     */
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<ReservationDTO> confirmerReservation(@PathVariable UUID id) {
        log.info("Demande de confirmation de la réservation: {}", id);

        ReservationDTO reservation = reservationService.confirmerReservation(id);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 4️⃣ PUT /api/reservations/{id}/annuler
     * Annuler une réservation
     */
    @PutMapping("/{id}/annuler")
    public ResponseEntity<ReservationDTO> annulerReservation(
            @PathVariable UUID id,
            @RequestParam(required = false) String raison) {
        log.info("Demande d'annulation de la réservation: {} avec raison: {}", id, raison);

        String raisonAnnulation = raison != null ? raison : "Annulation par le client";
        ReservationDTO reservation = reservationService.annulerReservation(id, raisonAnnulation);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 5️⃣ GET /api/reservations/locataire/{locataireId}
     * Obtenir les réservations d'un locataire
     */
    @GetMapping("/locataire/{locataireId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsLocataire(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des réservations pour le locataire: {}", locataireId);

        List<ReservationDTO> reservations = reservationService.getReservationsLocataire(locataireId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 6️⃣ GET /api/reservations/annonce/{annonceId}
     * Obtenir les réservations d'une annonce
     */
    @GetMapping("/annonce/{annonceId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsAnnonce(@PathVariable UUID annonceId) {
        log.info("Demande de récupération des réservations pour l'annonce: {}", annonceId);

        List<ReservationDTO> reservations = reservationService.getReservationsAnnonce(annonceId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 7️⃣ GET /api/reservations/{id}
     * Obtenir une réservation par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable UUID id) {
        log.info("Demande de récupération de la réservation: {}", id);

        ReservationDTO reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 8️⃣ GET /api/reservations/disponibilite
     * Vérifier la disponibilité d'une annonce
     */
    @GetMapping("/disponibilite")
    public ResponseEntity<Boolean> verifierDisponibilite(
            @RequestParam UUID annonceId,
            @RequestParam LocalDate dateArrivee,
            @RequestParam LocalDate dateDepart) {
        log.info("Vérification de disponibilité pour l'annonce: {} du {} au {}", annonceId, dateArrivee, dateDepart);

        boolean disponible = reservationService.verifierDisponibilite(annonceId, dateArrivee, dateDepart);
        return ResponseEntity.ok(disponible);
    }

    /**
     * 9️⃣ GET /api/reservations/en-attente
     * Obtenir les réservations en attente de confirmation
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<ReservationDTO>> getReservationsEnAttente() {
        log.info("Demande de récupération des réservations en attente");

        List<ReservationDTO> reservations = reservationService.getReservationsEnAttente();
        return ResponseEntity.ok(reservations);
    }

    /**
     * 🔟 PUT /api/reservations/{id}/statut
     * Mettre à jour le statut d'une réservation
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<ReservationDTO> mettreAJourStatut(
            @PathVariable UUID id,
            @RequestParam StatutReservation statut) {
        log.info("Demande de mise à jour du statut de la réservation: {} vers {}", id, statut);

        ReservationDTO reservation = reservationService.mettreAJourStatut(id, statut);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 1️⃣1️⃣ GET /api/reservations/locataire/{locataireId}/futures
     * Obtenir les réservations futures d'un locataire
     */
    @GetMapping("/locataire/{locataireId}/futures")
    public ResponseEntity<List<ReservationDTO>> getReservationsFutures(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des réservations futures pour le locataire: {}", locataireId);

        // Filtrer les réservations futures depuis toutes les réservations du locataire
        List<ReservationDTO> toutesReservations = reservationService.getReservationsLocataire(locataireId);
        List<ReservationDTO> reservationsFutures = toutesReservations.stream()
                .filter(r -> r.getDateArrivee().isAfter(LocalDate.now()))
                .toList();

        return ResponseEntity.ok(reservationsFutures);
    }

    /**
     * 1️⃣2️⃣ GET /api/reservations/locataire/{locataireId}/passees
     * Obtenir les réservations passées d'un locataire
     */
    @GetMapping("/locataire/{locataireId}/passees")
    public ResponseEntity<List<ReservationDTO>> getReservationsPassees(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des réservations passées pour le locataire: {}", locataireId);

        // Filtrer les réservations passées depuis toutes les réservations du locataire
        List<ReservationDTO> toutesReservations = reservationService.getReservationsLocataire(locataireId);
        List<ReservationDTO> reservationsPassees = toutesReservations.stream()
                .filter(r -> r.getDateDepart().isBefore(LocalDate.now()))
                .toList();

        return ResponseEntity.ok(reservationsPassees);
    }

    /**
     * 1️⃣3️⃣ GET /api/reservations/statistiques
     * Obtenir des statistiques sur les réservations
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Object> getStatistiques() {
        log.info("Demande de statistiques sur les réservations");

        // TODO: Implémenter les statistiques
        return ResponseEntity.ok().build();
    }

    /**
     * 1️⃣4️⃣ GET /api/reservations/annonce/{annonceId}/periodes
     * Obtenir la liste des périodes déjà réservées (confirmées/en cours)
     */
    @GetMapping("/annonce/{annonceId}/periodes")
    public ResponseEntity<List<com.example.Impression.dto.PeriodeReserveeDTO>> getPeriodesReservees(
            @PathVariable UUID annonceId) {
        log.info("Demande des périodes réservées pour l'annonce: {}", annonceId);
        var periodes = reservationService.getPeriodesReserveesAnnonce(annonceId);
        return ResponseEntity.ok(periodes);
    }

    /**
     * 1️⃣5️⃣ GET /api/reservations/locataire/{locataireId}/en-cours
     * Obtenir les réservations en cours d'un locataire
     */
    @GetMapping("/locataire/{locataireId}/en-cours")
    public ResponseEntity<List<ReservationDTO>> getReservationsEnCours(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des réservations en cours pour le locataire: {}", locataireId);

        List<ReservationDTO> toutesReservations = reservationService.getReservationsLocataire(locataireId);
        List<ReservationDTO> reservationsEnCours = toutesReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_COURS)
                .toList();

        return ResponseEntity.ok(reservationsEnCours);
    }

    /**
     * 1️⃣6️⃣ GET /api/reservations/locateur/{locateurId}
     * Obtenir toutes les réservations des annonces d'un locateur
     */
    @GetMapping("/locateur/{locateurId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsLocateur(
            @PathVariable UUID locateurId) {
        log.info("Demande des réservations pour le locateur: {}", locateurId);
        var reservations = reservationService.getReservationsLocateur(locateurId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 1️⃣7️⃣ GET /api/reservations/annonce/{annonceId}/periodes-futures
     * Périodes futures réservées (confirmées/en cours par défaut)
     * Paramètre optionnel: statuts=EN_ATTENTE,CONFIRMEE,EN_COURS
     */
    @GetMapping("/annonce/{annonceId}/periodes-futures")
    public ResponseEntity<List<com.example.Impression.dto.PeriodeReserveeDTO>> getPeriodesFuturesReservees(
            @PathVariable UUID annonceId,
            @RequestParam(required = false) String statuts) {
        List<StatutReservation> filtre = null;
        if (statuts != null && !statuts.isBlank()) {
            filtre = java.util.Arrays.stream(statuts.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(StatutReservation::valueOf)
                    .toList();
        }
        var periodes = reservationService.getPeriodesFuturesReserveesAnnonce(annonceId, filtre);
        return ResponseEntity.ok(periodes);
    }

    /**
     * 1️⃣8️⃣ GET /api/reservations/annonce/{annonceId}/jours-reserves
     * Jours futurs réservés (confirmées/en cours par défaut)
     * Paramètre optionnel: statuts=EN_ATTENTE,CONFIRMEE,EN_COURS
     */
    @GetMapping("/annonce/{annonceId}/jours-reserves")
    public ResponseEntity<List<LocalDate>> getJoursFutursReserves(
            @PathVariable UUID annonceId,
            @RequestParam(required = false) String statuts) {
        List<StatutReservation> filtre = null;
        if (statuts != null && !statuts.isBlank()) {
            filtre = java.util.Arrays.stream(statuts.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(StatutReservation::valueOf)
                    .toList();
        }
        var jours = reservationService.getJoursFutursReserves(annonceId, filtre);
        return ResponseEntity.ok(jours);
    }

    /**
     * 1️⃣9️⃣ PUT /api/reservations/{id}/terminer
     * Marquer une réservation comme terminée
     */
    @PutMapping("/{id}/terminer")
    public ResponseEntity<ReservationDTO> terminerReservation(@PathVariable UUID id) {
        log.info("Demande de finalisation de la réservation: {}", id);

        ReservationDTO reservation = reservationService.mettreAJourStatut(id, StatutReservation.TERMINEE);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 2️⃣0️⃣ PUT /api/reservations/{id}/mettre-en-cours
     * Marquer une réservation comme en cours
     */
    @PutMapping("/{id}/mettre-en-cours")
    public ResponseEntity<ReservationDTO> mettreEnCours(@PathVariable UUID id) {
        log.info("Demande de mise en cours de la réservation: {}", id);

        ReservationDTO reservation = reservationService.mettreAJourStatut(id, StatutReservation.EN_COURS);
        return ResponseEntity.ok(reservation);
    }
}