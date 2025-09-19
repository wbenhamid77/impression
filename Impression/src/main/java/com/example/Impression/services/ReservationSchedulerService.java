package com.example.Impression.services;

import com.example.Impression.entities.Reservation;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de planification pour la gestion automatique des statuts de
 * réservation
 * 
 * Règles automatiques :
 * 1. EN_ATTENTE → ANNULEE : Si la date de début est dépassée et pas encore
 * confirmée
 * 2. CONFIRMEE → EN_COURS : À la date d'arrivée
 * 3. EN_COURS → TERMINEE : À la date de départ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSchedulerService {

    private final ReservationRepository reservationRepository;

    /**
     * Tâche planifiée qui s'exécute toutes les heures
     * Gère les transitions automatiques des statuts de réservation
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures (3600000 ms)
    @Transactional
    public void gererTransitionsAutomatiques() {
        log.info("Début de la gestion automatique des statuts de réservation");

        LocalDate aujourdhui = LocalDate.now();

        // 1. Annuler automatiquement les réservations en attente après la date de début
        annulerReservationsEnAttenteExpirees(aujourdhui);

        // 2. Passer à EN_COURS les réservations confirmées à la date d'arrivée
        passerReservationsEnCours(aujourdhui);

        // 3. Terminer les réservations en cours à la date de départ
        terminerReservationsExpirees(aujourdhui);

        log.info("Fin de la gestion automatique des statuts de réservation");
    }

    /**
     * Annule automatiquement les réservations en attente dont la date de début est
     * dépassée
     */
    private void annulerReservationsEnAttenteExpirees(LocalDate aujourdhui) {
        log.info("Vérification des réservations en attente expirées");

        List<Reservation> reservationsExpirees = reservationRepository
                .findByStatutAndDateArriveeBefore(StatutReservation.EN_ATTENTE, aujourdhui);

        for (Reservation reservation : reservationsExpirees) {
            log.info("Annulation automatique de la réservation {} - Date d'arrivée dépassée: {}",
                    reservation.getId(), reservation.getDateArrivee());

            reservation.annuler("Annulation automatique - Date de début dépassée sans confirmation");
            reservationRepository.save(reservation);
        }

        if (!reservationsExpirees.isEmpty()) {
            log.info("{} réservation(s) annulée(s) automatiquement", reservationsExpirees.size());
        }
    }

    /**
     * Passe à EN_COURS les réservations confirmées à la date d'arrivée
     */
    private void passerReservationsEnCours(LocalDate aujourdhui) {
        log.info("Vérification des réservations à passer en cours");

        List<Reservation> reservationsADemarrer = reservationRepository
                .findByStatutAndDateArrivee(StatutReservation.CONFIRMEE, aujourdhui);

        for (Reservation reservation : reservationsADemarrer) {
            log.info("Passage en cours de la réservation {} - Date d'arrivée atteinte: {}",
                    reservation.getId(), reservation.getDateArrivee());

            reservation.mettreEnCours();
            reservationRepository.save(reservation);
        }

        if (!reservationsADemarrer.isEmpty()) {
            log.info("{} réservation(s) passée(s) en cours", reservationsADemarrer.size());
        }
    }

    /**
     * Termine les réservations en cours à la date de départ
     */
    private void terminerReservationsExpirees(LocalDate aujourdhui) {
        log.info("Vérification des réservations à terminer");

        List<Reservation> reservationsATerminer = reservationRepository
                .findByStatutAndDateDepartBefore(StatutReservation.EN_COURS, aujourdhui);

        for (Reservation reservation : reservationsATerminer) {
            log.info("Terminaison de la réservation {} - Date de départ atteinte: {}",
                    reservation.getId(), reservation.getDateDepart());

            reservation.terminer();
            reservationRepository.save(reservation);
        }

        if (!reservationsATerminer.isEmpty()) {
            log.info("{} réservation(s) terminée(s)", reservationsATerminer.size());
        }
    }

    /**
     * Méthode de test pour déclencher manuellement la gestion automatique
     * Utile pour les tests et le debugging
     */
    public void declencherGestionManuelle() {
        log.info("Déclenchement manuel de la gestion automatique des statuts");
        gererTransitionsAutomatiques();
    }
}
