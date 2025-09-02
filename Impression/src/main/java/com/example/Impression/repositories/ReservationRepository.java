package com.example.Impression.repositories;

import com.example.Impression.entities.Reservation;
import com.example.Impression.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

        // Trouver toutes les réservations d'un locataire
        List<Reservation> findByLocataireIdOrderByDateCreationDesc(UUID locataireId);

        // Trouver toutes les réservations d'une annonce
        List<Reservation> findByAnnonceIdOrderByDateCreationDesc(UUID annonceId);

        // Trouver les réservations par statut
        List<Reservation> findByStatut(StatutReservation statut);

        // Trouver les réservations d'un locataire par statut
        List<Reservation> findByLocataireIdAndStatutOrderByDateCreationDesc(UUID locataireId, StatutReservation statut);

        // Trouver les réservations d'une annonce par statut
        List<Reservation> findByAnnonceIdAndStatutOrderByDateCreationDesc(UUID annonceId, StatutReservation statut);

        // NEW: Trouver toutes les réservations appartenant à un locateur (via ses
        // annonces)
        List<Reservation> findByAnnonce_Locateur_IdOrderByDateCreationDesc(UUID locateurId);

        // Vérifier s'il y a des conflits de dates pour une annonce
        @Query("SELECT r FROM Reservation r WHERE r.annonce.id = :annonceId " +
                        "AND r.statut IN ('CONFIRMEE', 'EN_COURS') " +
                        "AND NOT (r.dateDepart <= :dateArrivee OR r.dateArrivee >= :dateDepart)")
        List<Reservation> findConflitsReservation(@Param("annonceId") UUID annonceId,
                        @Param("dateArrivee") LocalDate dateArrivee,
                        @Param("dateDepart") LocalDate dateDepart);

        // Trouver les réservations actives pour une période donnée
        @Query("SELECT r FROM Reservation r WHERE r.annonce.id = :annonceId " +
                        "AND r.statut IN ('CONFIRMEE', 'EN_COURS') " +
                        "AND r.dateArrivee >= :dateDebut " +
                        "AND r.dateDepart <= :dateFin")
        List<Reservation> findReservationsActivesPourPeriode(@Param("annonceId") UUID annonceId,
                        @Param("dateDebut") LocalDate dateDebut,
                        @Param("dateFin") LocalDate dateFin);

        // Trouver les réservations futures d'un locataire
        @Query("SELECT r FROM Reservation r WHERE r.locataire.id = :locataireId " +
                        "AND r.dateArrivee >= :dateActuelle " +
                        "ORDER BY r.dateArrivee ASC")
        List<Reservation> findReservationsFutures(@Param("locataireId") UUID locataireId,
                        @Param("dateActuelle") LocalDate dateActuelle);

        // Trouver les réservations passées d'un locataire
        @Query("SELECT r FROM Reservation r WHERE r.locataire.id = :locataireId " +
                        "AND r.dateDepart < :dateActuelle " +
                        "ORDER BY r.dateDepart DESC")
        List<Reservation> findReservationsPassees(@Param("locataireId") UUID locataireId,
                        @Param("dateActuelle") LocalDate dateActuelle);

        // Compter le nombre de réservations actives pour une annonce
        @Query("SELECT COUNT(r) FROM Reservation r WHERE r.annonce.id = :annonceId " +
                        "AND r.statut IN ('CONFIRMEE', 'EN_COURS')")
        long countReservationsActives(@Param("annonceId") UUID annonceId);

        // Trouver les réservations en attente de confirmation
        List<Reservation> findByStatutOrderByDateCreationAsc(StatutReservation statut);

        // Trouver les réservations d'un locataire pour une annonce spécifique
        List<Reservation> findByLocataireIdAndAnnonceIdOrderByDateCreationDesc(UUID locataireId, UUID annonceId);

        // NEW: Réservations futures (actives) d'une annonce (dateDepart >= aujourd'hui)
        @Query("SELECT r FROM Reservation r WHERE r.annonce.id = :annonceId " +
                        "AND r.statut IN ('CONFIRMEE', 'EN_COURS') " +
                        "AND r.dateDepart >= :dateActuelle " +
                        "ORDER BY r.dateArrivee ASC")
        List<Reservation> findReservationsActivesFuturesAnnonce(@Param("annonceId") UUID annonceId,
                        @Param("dateActuelle") LocalDate dateActuelle);

        // NEW: Réservations futures avec liste de statuts dynamique
        @Query("SELECT r FROM Reservation r WHERE r.annonce.id = :annonceId " +
                        "AND r.statut IN :statuts " +
                        "AND r.dateDepart >= :dateActuelle " +
                        "ORDER BY r.dateArrivee ASC")
        List<Reservation> findReservationsFuturesAnnonceParStatuts(@Param("annonceId") UUID annonceId,
                        @Param("dateActuelle") LocalDate dateActuelle,
                        @Param("statuts") List<StatutReservation> statuts);
}