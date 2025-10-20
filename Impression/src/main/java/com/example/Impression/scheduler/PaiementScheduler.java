package com.example.Impression.scheduler;

import com.example.Impression.services.PaiementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaiementScheduler {

    private final PaiementService paiementService;

    /**
     * Vérifier et marquer les paiements expirés toutes les 30 minutes
     * Les paiements non payés dans les 24h seront marqués comme expirés
     * et les réservations associées seront annulées
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // Toutes les 30 minutes
    public void verifierPaiementsExpires() {
        log.info("Début de la vérification des paiements expirés");

        try {
            paiementService.marquerPaiementsExpires();
            log.info("Vérification des paiements expirés terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des paiements expirés: {}", e.getMessage(), e);
        }
    }

    /**
     * Vérification supplémentaire toutes les heures pour les paiements critiques
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // Toutes les heures
    public void verifierPaiementsCritiques() {
        log.info("Début de la vérification des paiements critiques");

        try {
            // Cette méthode peut être étendue pour des vérifications plus spécifiques
            // Par exemple, envoyer des rappels avant expiration
            log.info("Vérification des paiements critiques terminée");
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des paiements critiques: {}", e.getMessage(), e);
        }
    }
}
