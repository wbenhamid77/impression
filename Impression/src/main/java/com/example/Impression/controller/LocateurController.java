package com.example.Impression.controller;

import com.example.Impression.dto.CreationLocateurDTO;
import com.example.Impression.dto.ModificationLocateurDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.dto.ReservationLocateurDTO;
import com.example.Impression.dto.RecapitulatifReservationsLocateurDTO;
import com.example.Impression.dto.ReservationLocateurDetailleeDTO;
import com.example.Impression.dto.RecapitulatifReservationsLocateurDetailleDTO;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.services.LocateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/locateurs")
@CrossOrigin(origins = "*")
public class LocateurController {

    @Autowired
    private LocateurService locateurService;

    // POST - Créer un locateur
    @PostMapping
    public ResponseEntity<?> creerLocateur(@Valid @RequestBody CreationLocateurDTO dto) {
        try {
            UtilisateurDTO locateurCree = locateurService.creerLocateur(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(locateurCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer tous les locateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> obtenirTousLesLocateurs() {
        List<UtilisateurDTO> locateurs = locateurService.trouverTousLesLocateurs();
        return ResponseEntity.ok(locateurs);
    }

    // GET - Récupérer un locateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirLocateurParId(@PathVariable UUID id) {
        Optional<UtilisateurDTO> locateur = locateurService.trouverLocateurParId(id);
        return locateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer un locateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirLocateurParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> locateur = locateurService.trouverLocateurParEmail(email);
        return locateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Modifier un locateur
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierLocateur(@PathVariable UUID id,
            @Valid @RequestBody ModificationLocateurDTO dto) {
        try {
            Optional<UtilisateurDTO> locateurModifie = locateurService.modifierLocateur(id, dto);
            return locateurModifie.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Supprimer un locateur (désactivation)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerLocateur(@PathVariable UUID id) {
        boolean supprime = locateurService.supprimerLocateur(id);
        if (supprime) {
            return ResponseEntity.ok("Locateur supprimé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Activer un compte locateur
    @PostMapping("/{id}/activer")
    public ResponseEntity<String> activerCompteLocateur(@PathVariable UUID id) {
        boolean active = locateurService.activerCompteLocateur(id);
        if (active) {
            return ResponseEntity.ok("Compte locateur activé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Désactiver un compte locateur
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverCompteLocateur(@PathVariable UUID id) {
        boolean desactive = locateurService.desactiverCompteLocateur(id);
        if (desactive) {
            return ResponseEntity.ok("Compte locateur désactivé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Compter les locateurs
    @GetMapping("/statistiques/count")
    public ResponseEntity<Long> compterLocateurs() {
        long nombre = locateurService.compterLocateurs();
        return ResponseEntity.ok(nombre);
    }

    // ===== NOUVEAUX ENDPOINTS POUR LES RÉSERVATIONS =====

    // GET - Récupérer toutes les réservations d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations")
    public ResponseEntity<?> obtenirReservationsLocateur(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurDetaillees(locateurId);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations d'un locateur par statut avec informations
    // détaillées
    @GetMapping("/{id}/reservations/statut/{statut}")
    public ResponseEntity<?> obtenirReservationsLocateurParStatut(
            @PathVariable String id,
            @PathVariable StatutReservation statut) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(locateurId,
                            statut);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer le récapitulatif complet des réservations d'un locateur avec
    // informations détaillées
    @GetMapping("/{id}/reservations/recapitulatif")
    public ResponseEntity<?> obtenirRecapitulatifReservations(
            @PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            RecapitulatifReservationsLocateurDetailleDTO recapitulatif = locateurService
                    .obtenirRecapitulatifReservationsDetaille(locateurId);
            return ResponseEntity.ok(recapitulatif);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations en attente d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations/en-attente")
    public ResponseEntity<?> obtenirReservationsEnAttente(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(locateurId,
                            StatutReservation.EN_ATTENTE);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations confirmées d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations/confirmees")
    public ResponseEntity<?> obtenirReservationsConfirmees(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(locateurId,
                            StatutReservation.CONFIRMEE);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations en cours d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations/en-cours")
    public ResponseEntity<?> obtenirReservationsEnCours(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(
                            locateurId,
                            StatutReservation.EN_COURS);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations terminées d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations/terminees")
    public ResponseEntity<?> obtenirReservationsTerminees(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(locateurId,
                            StatutReservation.TERMINEE);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }

    // GET - Récupérer les réservations annulées d'un locateur avec informations
    // détaillées
    @GetMapping("/{id}/reservations/annulees")
    public ResponseEntity<?> obtenirReservationsAnnulees(@PathVariable String id) {
        try {
            UUID locateurId = UUID.fromString(id);
            List<ReservationLocateurDetailleeDTO> reservations = locateurService
                    .obtenirReservationsLocateurParStatutDetaillees(locateurId,
                            StatutReservation.ANNULEE);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Format d'ID invalide",
                    "message", "L'ID doit être au format UUID valide (ex: 5ADBD152-B7F0-4010-A8FE-50D28FB55CD6)",
                    "idFourni", id,
                    "formatAttendu", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la récupération",
                    "message", e.getMessage()));
        }
    }
}