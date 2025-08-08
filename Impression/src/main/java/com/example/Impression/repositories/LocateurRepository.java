package com.example.Impression.repositories;

import com.example.Impression.entities.Locateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocateurRepository extends JpaRepository<Locateur, UUID> {

    List<Locateur> findByEstVerifieTrue();

    List<Locateur> findByEstVerifieFalse();

    @Query("SELECT l FROM Locateur l WHERE l.noteMoyenne >= :noteMin")
    List<Locateur> findByNoteMoyenneSuperieure(@Param("noteMin") double noteMin);

    @Query("SELECT l FROM Locateur l WHERE l.soldePortefeuille >= :soldeMin")
    List<Locateur> findBySoldeSuperieur(@Param("soldeMin") BigDecimal soldeMin);

    @Query("SELECT l FROM Locateur l WHERE l.nombreAnnonces >= :nombreMin")
    List<Locateur> findByNombreAnnoncesSuperieur(@Param("nombreMin") int nombreMin);
}