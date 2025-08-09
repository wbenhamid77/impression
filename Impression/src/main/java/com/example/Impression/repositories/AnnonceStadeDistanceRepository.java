package com.example.Impression.repositories;

import com.example.Impression.entities.AnnonceStadeDistance;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Stade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnonceStadeDistanceRepository extends JpaRepository<AnnonceStadeDistance, UUID> {

    // Trouver toutes les distances pour une annonce
    List<AnnonceStadeDistance> findByAnnonceOrderByDistanceAsc(Annonce annonce);

    // Trouver toutes les distances pour un stade
    List<AnnonceStadeDistance> findByStadeOrderByDistanceAsc(Stade stade);

    // Trouver la distance spécifique entre une annonce et un stade
    Optional<AnnonceStadeDistance> findByAnnonceAndStade(Annonce annonce, Stade stade);

    // Trouver le stade le plus proche d'une annonce
    @Query("SELECT asd FROM AnnonceStadeDistance asd WHERE asd.annonce = :annonce AND asd.estLePlusProche = true")
    Optional<AnnonceStadeDistance> findStadeLePlusProche(@Param("annonce") Annonce annonce);

    // Trouver les annonces dans un rayon donné d'un stade
    @Query("SELECT asd FROM AnnonceStadeDistance asd WHERE asd.stade = :stade AND asd.distance <= :rayonKm ORDER BY asd.distance ASC")
    List<AnnonceStadeDistance> findAnnoncesProchesStade(@Param("stade") Stade stade, @Param("rayonKm") Double rayonKm);

    // Supprimer toutes les distances d'une annonce
    void deleteByAnnonce(Annonce annonce);

    // Compter les annonces dans un rayon d'un stade
    @Query("SELECT COUNT(asd) FROM AnnonceStadeDistance asd WHERE asd.stade = :stade AND asd.distance <= :rayonKm")
    long countAnnoncesProchesStade(@Param("stade") Stade stade, @Param("rayonKm") Double rayonKm);

    // Trouver les distances avec temps de trajet maximum
    @Query("SELECT asd FROM AnnonceStadeDistance asd WHERE asd.annonce = :annonce AND asd.tempsTrajetMinutes <= :tempsMaxMinutes ORDER BY asd.tempsTrajetMinutes ASC")
    List<AnnonceStadeDistance> findByAnnonceAndTempsTrajetMaximum(@Param("annonce") Annonce annonce,
            @Param("tempsMaxMinutes") Integer tempsMaxMinutes);

    // Statistiques : distance moyenne vers un stade
    @Query("SELECT AVG(asd.distance) FROM AnnonceStadeDistance asd WHERE asd.stade = :stade")
    Double getDistanceMoyenneVersStade(@Param("stade") Stade stade);
}