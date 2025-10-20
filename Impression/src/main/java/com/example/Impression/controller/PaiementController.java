package com.example.Impression.controller;

import com.example.Impression.dto.ConfirmationPaiementDTO;
import com.example.Impression.dto.CreationPaiementDTO;
import com.example.Impression.dto.PaiementDTO;
import com.example.Impression.dto.RemboursementPaiementDTO;
import com.example.Impression.services.PaiementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Slf4j
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * Créer un nouveau paiement
     */
    @PostMapping
    public ResponseEntity<PaiementDTO> creerPaiement(@RequestBody CreationPaiementDTO creationDTO) {
        log.info("Demande de création de paiement pour la réservation: {}", creationDTO.getReservationId());

        PaiementDTO paiement = paiementService.creerPaiement(creationDTO);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Confirmer un paiement
     */
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<PaiementDTO> confirmerPaiement(@PathVariable UUID id,
            @RequestBody ConfirmationPaiementDTO confirmationDTO) {
        log.info("Demande de confirmation du paiement: {}", id);

        PaiementDTO paiement = paiementService.confirmerPaiement(id, confirmationDTO);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Marquer un paiement comme en cours
     */
    @PutMapping("/{id}/en-cours")
    public ResponseEntity<PaiementDTO> marquerPaiementEnCours(@PathVariable UUID id) {
        log.info("Demande de marquage en cours du paiement: {}", id);

        PaiementDTO paiement = paiementService.marquerPaiementEnCours(id);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Marquer un paiement comme échec
     */
    @PutMapping("/{id}/echec")
    public ResponseEntity<PaiementDTO> marquerPaiementEchec(@PathVariable UUID id,
            @RequestParam(required = false) String raison) {
        log.info("Demande de marquage en échec du paiement: {}", id);

        PaiementDTO paiement = paiementService.marquerPaiementEchec(id, raison);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Annuler un paiement
     */
    @PutMapping("/{id}/annuler")
    public ResponseEntity<PaiementDTO> annulerPaiement(@PathVariable UUID id,
            @RequestParam(required = false) String raison) {
        log.info("Demande d'annulation du paiement: {}", id);

        PaiementDTO paiement = paiementService.annulerPaiement(id, raison);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Rembourser un paiement
     */
    @PutMapping("/{id}/rembourser")
    public ResponseEntity<PaiementDTO> rembourserPaiement(@PathVariable UUID id,
            @RequestBody RemboursementPaiementDTO remboursementDTO) {
        log.info("Demande de remboursement du paiement: {}", id);

        PaiementDTO paiement = paiementService.rembourserPaiement(id, remboursementDTO);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Obtenir un paiement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getPaiementById(@PathVariable UUID id) {
        log.info("Demande de récupération du paiement: {}", id);

        PaiementDTO paiement = paiementService.getPaiementById(id);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Obtenir les paiements d'une réservation
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsReservation(@PathVariable UUID reservationId) {
        log.info("Demande de récupération des paiements pour la réservation: {}", reservationId);

        List<PaiementDTO> paiements = paiementService.getPaiementsReservation(reservationId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Obtenir les paiements d'un locataire
     */
    @GetMapping("/locataire/{locataireId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsLocataire(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des paiements pour le locataire: {}", locataireId);

        List<PaiementDTO> paiements = paiementService.getPaiementsLocataire(locataireId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Obtenir les paiements d'un locateur
     */
    @GetMapping("/locateur/{locateurId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsLocateur(@PathVariable UUID locateurId) {
        log.info("Demande de récupération des paiements pour le locateur: {}", locateurId);

        List<PaiementDTO> paiements = paiementService.getPaiementsLocateur(locateurId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Obtenir les paiements en attente d'un locataire
     */
    @GetMapping("/locataire/{locataireId}/en-attente")
    public ResponseEntity<List<PaiementDTO>> getPaiementsEnAttenteLocataire(@PathVariable UUID locataireId) {
        log.info("Demande de récupération des paiements en attente pour le locataire: {}", locataireId);

        List<PaiementDTO> paiements = paiementService.getPaiementsEnAttenteLocataire(locataireId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Obtenir les paiements en attente d'un locateur
     */
    @GetMapping("/locateur/{locateurId}/en-attente")
    public ResponseEntity<List<PaiementDTO>> getPaiementsEnAttenteLocateur(@PathVariable UUID locateurId) {
        log.info("Demande de récupération des paiements en attente pour le locateur: {}", locateurId);

        List<PaiementDTO> paiements = paiementService.getPaiementsEnAttenteLocateur(locateurId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Obtenir les paiements expirés (pour l'administration)
     */
    @GetMapping("/expires")
    public ResponseEntity<List<PaiementDTO>> getPaiementsExpires() {
        log.info("Demande de récupération des paiements expirés");

        List<PaiementDTO> paiements = paiementService.getPaiementsExpires();
        return ResponseEntity.ok(paiements);
    }

    /**
     * Marquer les paiements expirés (pour le scheduler)
     */
    @PostMapping("/marquer-expires")
    public ResponseEntity<Void> marquerPaiementsExpires() {
        log.info("Demande de marquage des paiements expirés");

        paiementService.marquerPaiementsExpires();
        return ResponseEntity.ok().build();
    }
}
