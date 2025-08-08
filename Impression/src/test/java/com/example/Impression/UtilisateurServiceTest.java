package com.example.Impression;

import com.example.Impression.dto.CreationUtilisateurDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.enums.Role;
import com.example.Impression.services.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UtilisateurServiceTest {

    @Autowired
    private UtilisateurService utilisateurService;

    @Test
    public void testCreerUtilisateur() {
        // Créer un DTO pour un locataire
        CreationUtilisateurDTO dto = new CreationUtilisateurDTO();
        dto.setNom("Dupont");
        dto.setPrenom("Jean");
        dto.setEmail("jean.dupont@test.com");
        dto.setMotDePasse("password123");
        dto.setTelephone("0123456789");
        dto.setRole(Role.LOCATAIRE);

        // Créer l'utilisateur
        UtilisateurDTO utilisateurCree = utilisateurService.creerUtilisateur(dto);

        // Vérifications
        assertNotNull(utilisateurCree);
        assertEquals("Dupont", utilisateurCree.getNom());
        assertEquals("Jean", utilisateurCree.getPrenom());
        assertEquals("jean.dupont@test.com", utilisateurCree.getEmail());
        assertEquals(Role.LOCATAIRE, utilisateurCree.getRole());
        assertTrue(utilisateurCree.isEstActif());
    }

    @Test
    public void testTrouverParEmail() {
        // Créer un utilisateur d'abord
        CreationUtilisateurDTO dto = new CreationUtilisateurDTO();
        dto.setNom("Martin");
        dto.setPrenom("Marie");
        dto.setEmail("marie.martin@test.com");
        dto.setMotDePasse("password123");
        dto.setRole(Role.LOCATEUR);

        utilisateurService.creerUtilisateur(dto);

        // Rechercher par email
        Optional<UtilisateurDTO> utilisateur = utilisateurService.trouverParEmail("marie.martin@test.com");

        // Vérifications
        assertTrue(utilisateur.isPresent());
        assertEquals("Martin", utilisateur.get().getNom());
        assertEquals("Marie", utilisateur.get().getPrenom());
    }

    @Test
    public void testTrouverParRole() {
        // Créer plusieurs utilisateurs avec différents rôles
        CreationUtilisateurDTO dto1 = new CreationUtilisateurDTO();
        dto1.setNom("Admin1");
        dto1.setPrenom("Admin");
        dto1.setEmail("admin1@test.com");
        dto1.setMotDePasse("password123");
        dto1.setRole(Role.ADMIN);

        CreationUtilisateurDTO dto2 = new CreationUtilisateurDTO();
        dto2.setNom("Locateur1");
        dto2.setPrenom("Locateur");
        dto2.setEmail("locateur1@test.com");
        dto2.setMotDePasse("password123");
        dto2.setRole(Role.LOCATEUR);

        utilisateurService.creerUtilisateur(dto1);
        utilisateurService.creerUtilisateur(dto2);

        // Rechercher les admins
        List<UtilisateurDTO> admins = utilisateurService.trouverParRole(Role.ADMIN);

        // Vérifications
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().allMatch(u -> u.getRole() == Role.ADMIN));
    }
}