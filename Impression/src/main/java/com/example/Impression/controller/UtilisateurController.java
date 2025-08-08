package com.example.Impression.controller;

import com.example.Impression.dto.CreationUtilisateurDTO;
import com.example.Impression.dto.ModificationUtilisateurDTO;
import com.example.Impression.dto.ModificationProfilDTO;
import com.example.Impression.dto.ReponseModificationProfilDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.enums.Role;
import com.example.Impression.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    // POST - Créer un utilisateur
    @PostMapping
    public ResponseEntity<?> creerUtilisateur(@Valid @RequestBody CreationUtilisateurDTO dto) {
        try {
            UtilisateurDTO utilisateurCree = utilisateurService.creerUtilisateur(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> obtenirTousLesUtilisateurs() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.trouverTousLesUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    // GET - Récupérer un utilisateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirUtilisateurParId(@PathVariable UUID id) {
        Optional<UtilisateurDTO> utilisateur = utilisateurService.trouverParId(id);
        return utilisateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer un utilisateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirUtilisateurParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> utilisateur = utilisateurService.trouverParEmail(email);
        return utilisateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer les utilisateurs par rôle
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDTO>> obtenirUtilisateursParRole(@PathVariable Role role) {
        List<UtilisateurDTO> utilisateurs = utilisateurService.trouverParRole(role);
        return ResponseEntity.ok(utilisateurs);
    }

    // GET - Rechercher des utilisateurs
    @GetMapping("/recherche")
    public ResponseEntity<List<UtilisateurDTO>> rechercherUtilisateurs(@RequestParam String terme) {
        List<UtilisateurDTO> utilisateurs = utilisateurService.rechercherUtilisateurs(terme);
        return ResponseEntity.ok(utilisateurs);
    }

    // PUT - Modifier un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierUtilisateur(@PathVariable UUID id,
            @Valid @RequestBody ModificationUtilisateurDTO dto) {
        try {
            Optional<UtilisateurDTO> utilisateurModifie = utilisateurService.modifierUtilisateur(id, dto);
            return utilisateurModifie.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Supprimer un utilisateur (désactivation)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerUtilisateur(@PathVariable UUID id) {
        boolean supprime = utilisateurService.supprimerUtilisateur(id);
        if (supprime) {
            return ResponseEntity.ok("Utilisateur supprimé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Activer un compte
    @PostMapping("/{id}/activer")
    public ResponseEntity<String> activerCompte(@PathVariable UUID id) {
        boolean active = utilisateurService.activerCompte(id);
        if (active) {
            return ResponseEntity.ok("Compte activé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Désactiver un compte
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverCompte(@PathVariable UUID id) {
        boolean desactive = utilisateurService.desactiverCompte(id);
        if (desactive) {
            return ResponseEntity.ok("Compte désactivé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Compter les utilisateurs par rôle
    @GetMapping("/statistiques/role/{role}")
    public ResponseEntity<Long> compterUtilisateursParRole(@PathVariable Role role) {
        long nombre = utilisateurService.compterUtilisateursParRole(role);
        return ResponseEntity.ok(nombre);
    }

    // PUT - Modifier le profil d'un utilisateur
    @PutMapping("/{id}/profil")
    public ResponseEntity<ReponseModificationProfilDTO> modifierProfil(@PathVariable UUID id,
            @Valid @RequestBody ModificationProfilDTO modificationProfilDTO) {

        ReponseModificationProfilDTO response = utilisateurService.modifierProfil(id, modificationProfilDTO);

        if (response.isSucces()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}