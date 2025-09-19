package com.example.Impression.controller;

import com.example.Impression.dto.LoginResponseDTO;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.security.JwtTokenProvider;
import com.example.Impression.services.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/verification")
@CrossOrigin(origins = "*")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // GET /api/auth/verification/confirm?token=...
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmer(@RequestParam("token") String token) {
        Optional<Utilisateur> utilisateurOpt = verificationService.validerToken(token);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token invalide ou expiré"));
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        String jwt = jwtTokenProvider.generateToken(utilisateur.getId(), utilisateur.getEmail(),
                utilisateur.getRole().name());
        String refresh = jwtTokenProvider.generateRefreshToken(utilisateur.getId());

        LoginResponseDTO resp = new LoginResponseDTO(
                jwt,
                refresh,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole().name(),
                LocalDateTime.now().plusHours(24),
                "Email vérifié et connexion réussie");

        return ResponseEntity.ok(resp);
    }
}
