package com.example.Impression.repositories;

import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Locateur;
import com.example.Impression.enums.TypeMaison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, UUID> {

        // Trouver toutes les annonces actives
        List<Annonce> findByEstActiveTrue();

        // Trouver les annonces d'un locateur
        List<Annonce> findByLocateur(Locateur locateur);

        // Trouver les annonces actives d'un locateur
        List<Annonce> findByLocateurAndEstActiveTrue(Locateur locateur);

        // Recherche par type de maison
        List<Annonce> findByTypeMaisonAndEstActiveTrue(TypeMaison typeMaison);

        // Recherche par ville
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true AND a.adresse.ville LIKE %:ville%")
        List<Annonce> findByVilleContaining(@Param("ville") String ville);

        // Recherche par prix maximum
        List<Annonce> findByPrixParNuitLessThanEqualAndEstActiveTrue(BigDecimal prixMax);

        // Recherche par capacité minimum
        List<Annonce> findByCapaciteGreaterThanEqualAndEstActiveTrue(int capaciteMin);

        // Recherche par nombre de chambres
        List<Annonce> findByNombreChambresAndEstActiveTrue(int nombreChambres);

        // Recherche par note minimum
        List<Annonce> findByNoteMoyenneGreaterThanEqualAndEstActiveTrue(Double noteMin);

        // Recherche par distance du stade
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true AND a.distanceStade <= :distanceMax")
        List<Annonce> findByDistanceStadeLessThanEqual(@Param("distanceMax") BigDecimal distanceMax);

        // Recherche combinée
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true " +
                        "AND (:ville IS NULL OR a.adresse.ville LIKE %:ville%) " +
                        "AND (:typeMaison IS NULL OR a.typeMaison = :typeMaison) " +
                        "AND (:prixMax IS NULL OR a.prixParNuit <= :prixMax) " +
                        "AND (:capaciteMin IS NULL OR a.capacite >= :capaciteMin) " +
                        "AND (:noteMin IS NULL OR a.noteMoyenne >= :noteMin)")
        List<Annonce> findByCritereRecherche(
                        @Param("ville") String ville,
                        @Param("typeMaison") TypeMaison typeMaison,
                        @Param("prixMax") BigDecimal prixMax,
                        @Param("capaciteMin") Integer capaciteMin,
                        @Param("noteMin") Double noteMin);

        // Trouver les annonces par stade
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true AND a.stadePlusProche LIKE %:stade%")
        List<Annonce> findByStadeContaining(@Param("stade") String stade);

        // Compter les annonces d'un locateur
        long countByLocateur(Locateur locateur);

        // Compter les annonces actives d'un locateur
        long countByLocateurAndEstActiveTrue(Locateur locateur);

        // ========== RECHERCHES GÉOGRAPHIQUES ==========

        // Recherche par rayon autour d'un point (en kilomètres)
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true " +
                        "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
                        "cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(a.latitude)))) <= :rayonKm")
        List<Annonce> findByRayonGeographique(
                        @Param("latitude") BigDecimal latitude,
                        @Param("longitude") BigDecimal longitude,
                        @Param("rayonKm") double rayonKm);

        // Recherche par zone géographique (rectangle)
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true " +
                        "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL " +
                        "AND a.latitude BETWEEN :latMin AND :latMax " +
                        "AND a.longitude BETWEEN :lonMin AND :lonMax")
        List<Annonce> findByZoneGeographique(
                        @Param("latMin") BigDecimal latMin,
                        @Param("latMax") BigDecimal latMax,
                        @Param("lonMin") BigDecimal lonMin,
                        @Param("lonMax") BigDecimal lonMax);

        // Recherche par proximité d'un point (triée par distance)
        @Query("SELECT a, (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
                        "cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(a.latitude)))) as distance " +
                        "FROM Annonce a WHERE a.estActive = true " +
                        "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL " +
                        "ORDER BY distance")
        List<Object[]> findByProximite(
                        @Param("latitude") BigDecimal latitude,
                        @Param("longitude") BigDecimal longitude);

        // Recherche par proximité avec limite de distance
        @Query("SELECT a FROM Annonce a WHERE a.estActive = true " +
                        "AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
                        "cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(a.latitude)))) <= :distanceMax " +
                        "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
                        "cos(radians(a.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(a.latitude))))")
        List<Annonce> findByProximiteAvecLimite(
                        @Param("latitude") BigDecimal latitude,
                        @Param("longitude") BigDecimal longitude,
                        @Param("distanceMax") double distanceMax);
}