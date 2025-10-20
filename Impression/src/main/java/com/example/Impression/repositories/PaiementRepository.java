package com.example.Impression.repositories;

import com.example.Impression.entities.Paiement;
import com.example.Impression.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID> {

    // Trouver les paiements d'une réservation
    List<Paiement> findByReservationIdOrderByDateCreationDesc(UUID reservationId);

    // Trouver les paiements par statut
    List<Paiement> findByStatutOrderByDateCreationDesc(StatutPaiement statut);

    // Trouver les paiements en attente qui vont expirer
    @Query("SELECT p FROM Paiement p WHERE p.statut = :statut AND p.dateExpiration <= :dateLimite")
    List<Paiement> findPaiementsExpires(@Param("statut") StatutPaiement statut,
            @Param("dateLimite") LocalDateTime dateLimite);

    // Trouver les paiements d'un locataire (via réservation)
    @Query("SELECT p FROM Paiement p WHERE p.reservation.locataire.id = :locataireId ORDER BY p.dateCreation DESC")
    List<Paiement> findByLocataireId(@Param("locataireId") UUID locataireId);

    // Trouver les paiements d'un locateur (via réservation)
    @Query("SELECT p FROM Paiement p WHERE p.reservation.annonce.locateur.id = :locateurId ORDER BY p.dateCreation DESC")
    List<Paiement> findByLocateurId(@Param("locateurId") UUID locateurId);

    // Trouver les paiements en attente d'une réservation
    @Query("SELECT p FROM Paiement p WHERE p.reservation.id = :reservationId AND p.statut = :statut")
    Optional<Paiement> findPaiementEnAttenteByReservationId(@Param("reservationId") UUID reservationId,
            @Param("statut") StatutPaiement statut);

    // Trouver les paiements payés d'une réservation
    @Query("SELECT p FROM Paiement p WHERE p.reservation.id = :reservationId AND p.statut = :statut")
    List<Paiement> findPaiementsPayesByReservationId(@Param("reservationId") UUID reservationId,
            @Param("statut") StatutPaiement statut);

    // Compter les paiements par statut
    long countByStatut(StatutPaiement statut);

    // Trouver les paiements par numéro de transaction
    Optional<Paiement> findByNumeroTransaction(String numeroTransaction);

    // Trouver les paiements créés entre deux dates
    @Query("SELECT p FROM Paiement p WHERE p.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY p.dateCreation DESC")
    List<Paiement> findPaiementsParPeriode(@Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    // Trouver les paiements expirés non traités
    @Query("SELECT p FROM Paiement p WHERE p.statut IN :statuts AND p.dateExpiration < :maintenant")
    List<Paiement> findPaiementsExpiresNonTraites(@Param("statuts") List<StatutPaiement> statuts,
            @Param("maintenant") LocalDateTime maintenant);

    // Trouver les paiements d'une annonce (via réservation)
    @Query("SELECT p FROM Paiement p WHERE p.reservation.annonce.id = :annonceId ORDER BY p.dateCreation DESC")
    List<Paiement> findByAnnonceId(@Param("annonceId") UUID annonceId);

    // Trouver les paiements en attente pour un locataire spécifique
    @Query("SELECT p FROM Paiement p WHERE p.reservation.locataire.id = :locataireId AND p.statut = :statut ORDER BY p.dateCreation DESC")
    List<Paiement> findPaiementsEnAttenteByLocataireId(@Param("locataireId") UUID locataireId,
            @Param("statut") StatutPaiement statut);

    // Trouver les paiements en attente pour un locateur spécifique
    @Query("SELECT p FROM Paiement p WHERE p.reservation.annonce.locateur.id = :locateurId AND p.statut = :statut ORDER BY p.dateCreation DESC")
    List<Paiement> findPaiementsEnAttenteByLocateurId(@Param("locateurId") UUID locateurId,
            @Param("statut") StatutPaiement statut);
}
