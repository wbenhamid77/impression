package com.example.Impression.services;

import com.example.Impression.entities.Utilisateur;
import com.example.Impression.entities.VerificationToken;
import com.example.Impression.repositories.UtilisateurRepository;
import com.example.Impression.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VerificationService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public VerificationToken genererTokenVerification(Utilisateur utilisateur) {
        // Invalider anciens tokens non utilisés (optionnel)
        // Générer un nouveau token unique
        String token = UUID.randomUUID().toString().replace("-", "");

        VerificationToken vt = new VerificationToken();
        vt.setUtilisateur(utilisateur);
        vt.setToken(token);
        vt.setDateExpiration(LocalDateTime.now().plusHours(24));
        vt.setUtilise(false);

        return verificationTokenRepository.save(vt);
    }

    public boolean envoyerEmailVerification(Utilisateur utilisateur) {
        VerificationToken vt = genererTokenVerification(utilisateur);
        String lien = frontendBaseUrl + "/login?verifyToken=" + vt.getToken();

        String sujet = "RiadNear - Vérifiez votre adresse e-mail";
        String texte = "Bonjour " + utilisateur.getPrenom() +
                ", bienvenue sur RiadNear. Pour activer votre compte (lien valable 24h), cliquez sur : " +
                lien +
                ". Si vous n'êtes pas à l'origine de cette inscription, ignorez cet e-mail. RiadNear";

        emailService.envoyerEmailSimple(utilisateur.getEmail(), sujet, texte);
        return true;
    }

    public Optional<Utilisateur> validerToken(String token) {
        if (token != null) {
            token = token.trim().toLowerCase();
        }
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty())
            return Optional.empty();

        VerificationToken vt = tokenOpt.get();
        if (vt.isUtilise())
            return Optional.empty();
        if (vt.getDateExpiration().isBefore(LocalDateTime.now()))
            return Optional.empty();

        Utilisateur utilisateur = vt.getUtilisateur();
        utilisateur.setEmailVerifie(true);
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        vt.setUtilise(true);
        verificationTokenRepository.save(vt);

        return Optional.of(utilisateur);
    }
}
