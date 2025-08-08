package com.example.Impression.controller;

import com.example.Impression.dto.LoginRequestDTO;
import com.example.Impression.dto.LoginResponseDTO;
import com.example.Impression.dto.ModificationMotDePasseDTO;
import com.example.Impression.dto.ReponseModificationMotDePasseDTO;
import com.example.Impression.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    // POST - Se connecter
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authenticationService.login(loginRequest);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST - Rafraîchir le token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        String newToken = authenticationService.refreshToken(refreshToken);

        if (newToken != null) {
            return ResponseEntity.ok().body("{\"token\": \"" + newToken + "\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Token de rafraîchissement invalide\"}");
        }
    }

    // POST - Valider un token
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = authenticationService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok().body("{\"valid\": true}");
        } else {
            return ResponseEntity.badRequest().body("{\"valid\": false}");
        }
    }

    // PUT - Modifier le mot de passe
    @PutMapping("/modifier-mot-de-passe")
    public ResponseEntity<ReponseModificationMotDePasseDTO> modifierMotDePasse(
            @Valid @RequestBody ModificationMotDePasseDTO modificationMotDePasseDTO) {

        ReponseModificationMotDePasseDTO response = authenticationService.modifierMotDePasse(modificationMotDePasseDTO);

        if (response.isSucces()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}