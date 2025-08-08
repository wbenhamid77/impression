package com.example.Impression.controller;

import com.example.Impression.dto.ConnexionDTO;
import com.example.Impression.dto.ModificationMotDePasseDTO;
import com.example.Impression.dto.ReponseModificationMotDePasseDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.services.AuthentificationService;
import com.example.Impression.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthentificationController {

    @Autowired
    private AuthentificationService authentificationService;

    @Autowired
    private AuthenticationService authenticationService;

    // POST - Se connecter
    @PostMapping("/connexion")
    public ResponseEntity<?> seConnecter(@Valid @RequestBody ConnexionDTO connexionDTO) {
        Optional<UtilisateurDTO> utilisateur = authentificationService.seConnecter(connexionDTO);

        if (utilisateur.isPresent()) {
            return ResponseEntity.ok(utilisateur.get());
        } else {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }

    // POST - Se déconnecter
    @PostMapping("/deconnexion")
    public ResponseEntity<String> seDeconnecter(@RequestParam String email) {
        boolean deconnecte = authentificationService.seDeconnecter(email);
        if (deconnecte) {
            return ResponseEntity.ok("Déconnexion réussie");
        } else {
            return ResponseEntity.badRequest().body("Erreur lors de la déconnexion");
        }
    }

    // POST - Vérifier l'authentification
    @PostMapping("/verifier")
    public ResponseEntity<Boolean> verifierAuthentification(@Valid @RequestBody ConnexionDTO connexionDTO) {
        boolean authentifie = authentificationService.verifierAuthentification(
                connexionDTO.getEmail(),
                connexionDTO.getMotDePasse());
        return ResponseEntity.ok(authentifie);
    }

    // PUT - Modifier le mot de passe (endpoint alternatif)
    @PutMapping("/changer-mot-de-passe")
    public ResponseEntity<ReponseModificationMotDePasseDTO> changerMotDePasse(
            @Valid @RequestBody ModificationMotDePasseDTO modificationMotDePasseDTO) {

        ReponseModificationMotDePasseDTO response = authenticationService.modifierMotDePasse(modificationMotDePasseDTO);

        if (response.isSucces()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}