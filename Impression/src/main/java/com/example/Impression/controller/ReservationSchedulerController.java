package com.example.Impression.controller;

import com.example.Impression.services.ReservationSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion du scheduler des réservations
 * Permet de déclencher manuellement la gestion automatique des statuts
 */
@RestController
@RequestMapping("/api/reservations/scheduler")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReservationSchedulerController {

    private final ReservationSchedulerService reservationSchedulerService;

    /**
     * Déclencher manuellement la gestion automatique des statuts
     * Utile pour les tests et le debugging
     */
    @PostMapping("/declencher")
    public ResponseEntity<String> declencherGestionAutomatique() {
        log.info("Déclenchement manuel de la gestion automatique des statuts");

        try {
            reservationSchedulerService.declencherGestionManuelle();
            return ResponseEntity.ok("Gestion automatique des statuts déclenchée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du déclenchement de la gestion automatique", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors du déclenchement: " + e.getMessage());
        }
    }

    /**
     * Vérifier l'état du scheduler
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Scheduler des réservations actif - Exécution toutes les heures");
    }
}
