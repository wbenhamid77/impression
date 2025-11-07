package com.example.Impression.repositories;

import com.example.Impression.entities.Rib;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Locateur;
import com.example.Impression.enums.RibType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RibRepository extends JpaRepository<Rib, UUID> {
    List<Rib> findByLocataireAndActif(Locataire locataire, boolean actif);

    List<Rib> findByLocateurAndActif(Locateur locateur, boolean actif);

    Optional<Rib> findFirstByTypeAndDefautCompteTrue(RibType type);

    Optional<Rib> findFirstByLocataireAndDefautCompteTrue(Locataire locataire);

    Optional<Rib> findFirstByLocateurAndDefautCompteTrue(Locateur locateur);

    Optional<Rib> findFirstByType(RibType type);
}
