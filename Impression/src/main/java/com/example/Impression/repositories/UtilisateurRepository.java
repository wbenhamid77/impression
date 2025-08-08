package com.example.Impression.repositories;

import com.example.Impression.entities.Utilisateur;
import com.example.Impression.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByEmailAndEstActifTrue(String email);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByEstActifTrue();

    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email AND u.motDePasseHash = :motDePasseHash")
    Optional<Utilisateur> findByEmailAndMotDePasseHash(@Param("email") String email,
            @Param("motDePasseHash") String motDePasseHash);

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT u FROM Utilisateur u WHERE u.nom LIKE %:recherche% OR u.prenom LIKE %:recherche% OR u.email LIKE %:recherche%")
    List<Utilisateur> rechercherUtilisateurs(@Param("recherche") String recherche);
}