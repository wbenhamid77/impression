package com.example.Impression.repositories;

import com.example.Impression.entities.Adresse;
import com.example.Impression.entities.Locateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, UUID> {

    // Trouver les adresses d'un locateur
    List<Adresse> findByLocateur(Locateur locateur);

    // Trouver les adresses actives d'un locateur
    List<Adresse> findByLocateurAndEstActiveTrue(Locateur locateur);

    // Recherche par ville
    List<Adresse> findByVilleContaining(String ville);

    // Recherche par surface
    List<Adresse> findBySurfaceBetweenAndEstActiveTrue(BigDecimal surfaceMin, BigDecimal surfaceMax);

    // Compter les adresses d'un locateur
    long countByLocateur(Locateur locateur);

    // Compter les adresses actives d'un locateur
    long countByLocateurAndEstActiveTrue(Locateur locateur);
}