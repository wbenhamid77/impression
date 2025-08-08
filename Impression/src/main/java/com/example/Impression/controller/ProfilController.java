package com.example.Impression.controller;

import com.example.Impression.dto.ModificationProfilDTO;
import com.example.Impression.dto.ReponseModificationProfilDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/profil")
@CrossOrigin(origins = "*")
public class ProfilController {

    @Autowired
    private UtilisateurService utilisateurService;

    // GET - Récupérer le profil d'un utilisateur
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirProfil(@PathVariable UUID id) {
        Optional<UtilisateurDTO> utilisateur = utilisateurService.trouverParId(id);
        return utilisateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Modifier le profil d'un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<ReponseModificationProfilDTO> modifierProfil(@PathVariable UUID id,
            @Valid @RequestBody ModificationProfilDTO modificationProfilDTO) {

        ReponseModificationProfilDTO response = utilisateurService.modifierProfil(id, modificationProfilDTO);

        if (response.isSucces()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // GET - Récupérer le profil par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirProfilParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> utilisateur = utilisateurService.trouverParEmail(email);
        return utilisateur.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}