package com.example.Impression.repositories;

import com.example.Impression.entities.Locataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocataireRepository extends JpaRepository<Locataire, UUID> {

    List<Locataire> findByEstVerifieTrue();

    List<Locataire> findByEstVerifieFalse();

    @Query("SELECT l FROM Locataire l WHERE l.noteMoyenne >= :noteMin")
    List<Locataire> findByNoteMoyenneSuperieure(@Param("noteMin") double noteMin);

    @Query("SELECT l FROM Locataire l WHERE l.nombreReservations >= :nombreMin")
    List<Locataire> findByNombreReservationsSuperieur(@Param("nombreMin") int nombreMin);
}