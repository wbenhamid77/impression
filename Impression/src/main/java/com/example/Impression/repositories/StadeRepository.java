package com.example.Impression.repositories;

import com.example.Impression.entities.Stade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadeRepository extends JpaRepository<Stade, UUID> {

    // Trouver tous les stades actifs
    List<Stade> findByEstActifTrue();

    // Trouver un stade par nom
    Optional<Stade> findByNomAndEstActifTrue(String nom);

    // Trouver les stades par ville
    List<Stade> findByVilleAndEstActifTrue(String ville);

    // Trouver le stade par nom exact
    Optional<Stade> findByNom(String nom);

    // Compter le nombre de stades actifs
    long countByEstActifTrue();

    // Requête personnalisée pour calculer la distance
    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(?2)) + sin(radians(?1)) * " +
            "sin(radians(latitude)))) AS distance " +
            "FROM stades WHERE est_actif = true " +
            "ORDER BY distance", nativeQuery = true)
    List<Object[]> findStadesOrderedByDistance(double latitude, double longitude);
}