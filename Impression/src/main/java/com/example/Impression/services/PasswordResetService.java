package com.example.Impression.services;

import com.example.Impression.entities.PasswordResetCode;
import com.example.Impression.entities.Utilisateur;

import com.example.Impression.repositories.UtilisateurRepository;
import com.example.Impression.repositories.PasswordResetCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 10;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void sendResetCode(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            // For security, do not reveal whether email exists
            return;
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Invalidate previous active codes
        List<PasswordResetCode> actives = passwordResetCodeRepository
                .findByUtilisateur_IdAndUtiliseFalse(utilisateur.getId());
        for (PasswordResetCode c : actives) {
            c.setUtilise(true);
        }
        if (!actives.isEmpty()) {
            passwordResetCodeRepository.saveAll(actives);
        }

        String code = generateNumericCode(CODE_LENGTH);

        PasswordResetCode prc = new PasswordResetCode();
        prc.setUtilisateur(utilisateur);
        prc.setCode(code);
        prc.setDateExpiration(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        prc.setUtilise(false);
        passwordResetCodeRepository.save(prc);

        String subject = "RiadNear - Code de réinitialisation";
        String message = "Bonjour " + utilisateur.getPrenom() + ",\n\n" +
                "Voici votre code de réinitialisation du mot de passe: " + code +
                "\nCe code est valable " + CODE_TTL_MINUTES + " minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.";
        emailService.envoyerEmailSimple(utilisateur.getEmail(), subject, message);
    }

    public boolean verifyCode(String email, String code) {
        if (code != null) {
            code = code.trim();
        }
        Optional<PasswordResetCode> opt = passwordResetCodeRepository
                .findByUtilisateur_EmailAndCodeAndUtiliseFalse(email, code);
        if (opt.isEmpty()) {
            return false;
        }
        PasswordResetCode prc = opt.get();
        if (prc.getDateExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    public boolean resetPassword(String email, String code, String newPassword) {
        if (code != null) {
            code = code.trim();
        }
        Optional<PasswordResetCode> opt = passwordResetCodeRepository
                .findByUtilisateur_EmailAndCodeAndUtiliseFalse(email, code);
        if (opt.isEmpty()) {
            return false;
        }
        PasswordResetCode prc = opt.get();
        if (prc.getDateExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            return false;
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setMotDePasseHash(passwordEncoder.encode(newPassword));
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        // Mark this code as used and invalidate any other active codes
        prc.setUtilise(true);
        passwordResetCodeRepository.save(prc);

        List<PasswordResetCode> others = passwordResetCodeRepository
                .findByUtilisateur_IdAndUtiliseFalse(utilisateur.getId());
        for (PasswordResetCode c : others) {
            c.setUtilise(true);
        }
        if (!others.isEmpty()) {
            passwordResetCodeRepository.saveAll(others);
        }

        return true;
    }

    private String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, length);
        int min = (int) Math.pow(10, length - 1);
        int number = random.nextInt(bound - min) + min;
        return String.valueOf(number);
    }
}
