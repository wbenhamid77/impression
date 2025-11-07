package com.example.Impression.controller;

import com.example.Impression.dto.ForgotPasswordRequestDTO;
import com.example.Impression.dto.ResetPasswordDTO;
import com.example.Impression.dto.VerifyResetCodeDTO;
import com.example.Impression.services.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.sendResetCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si un compte existe, un code a été envoyé."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyResetCodeDTO request) {
        boolean valid = passwordResetService.verifyCode(request.getEmail(), request.getCode());
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Code invalide ou expiré"));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        boolean success = passwordResetService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNouveauMotDePasse());
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("message", "Impossible de réinitialiser le mot de passe"));
        }
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}












