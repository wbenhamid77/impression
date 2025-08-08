package com.example.Impression.controller;

import com.example.Impression.dto.CreationLocateurDTO;
import com.example.Impression.dto.ModificationLocateurDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.services.LocateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/locateurs")
@CrossOrigin(origins = "*")
public class LocateurController {

    @Autowired
    private LocateurService locateurService;

    // POST - Créer un locateur
    @PostMapping
    public ResponseEntity<?> creerLocateur(@Valid @RequestBody CreationLocateurDTO dto) {
        try {
            UtilisateurDTO locateurCree = locateurService.creerLocateur(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(locateurCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer tous les locateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> obtenirTousLesLocateurs() {
        List<UtilisateurDTO> locateurs = locateurService.trouverTousLesLocateurs();
        return ResponseEntity.ok(locateurs);
    }

    // GET - Récupérer un locateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirLocateurParId(@PathVariable UUID id) {
        Optional<UtilisateurDTO> locateur = locateurService.trouverLocateurParId(id);
        return locateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer un locateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirLocateurParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> locateur = locateurService.trouverLocateurParEmail(email);
        return locateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Modifier un locateur
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierLocateur(@PathVariable UUID id,
            @Valid @RequestBody ModificationLocateurDTO dto) {
        try {
            Optional<UtilisateurDTO> locateurModifie = locateurService.modifierLocateur(id, dto);
            return locateurModifie.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Supprimer un locateur (désactivation)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerLocateur(@PathVariable UUID id) {
        boolean supprime = locateurService.supprimerLocateur(id);
        if (supprime) {
            return ResponseEntity.ok("Locateur supprimé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Activer un compte locateur
    @PostMapping("/{id}/activer")
    public ResponseEntity<String> activerCompteLocateur(@PathVariable UUID id) {
        boolean active = locateurService.activerCompteLocateur(id);
        if (active) {
            return ResponseEntity.ok("Compte locateur activé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Désactiver un compte locateur
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverCompteLocateur(@PathVariable UUID id) {
        boolean desactive = locateurService.desactiverCompteLocateur(id);
        if (desactive) {
            return ResponseEntity.ok("Compte locateur désactivé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Compter les locateurs
    @GetMapping("/statistiques/count")
    public ResponseEntity<Long> compterLocateurs() {
        long nombre = locateurService.compterLocateurs();
        return ResponseEntity.ok(nombre);
    }
}