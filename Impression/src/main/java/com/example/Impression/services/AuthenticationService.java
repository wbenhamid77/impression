package com.example.Impression.services;

import com.example.Impression.dto.LoginRequestDTO;
import com.example.Impression.dto.LoginResponseDTO;
import com.example.Impression.dto.ModificationMotDePasseDTO;
import com.example.Impression.dto.ReponseModificationMotDePasseDTO;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.repositories.UtilisateurRepository;
import com.example.Impression.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private VerificationService verificationService;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository
                .findByEmailAndEstActifTrue(loginRequest.getEmail());

        if (utilisateurOpt.isEmpty()) {
            return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                    "Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(loginRequest.getPassword(), utilisateur.getMotDePasseHash())) {
            return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                    "Email ou mot de passe incorrect");
        }

        // Vérifier si le compte est actif
        if (!utilisateur.isEstActif()) {
            return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                    "Compte désactivé");
        }

        // Si l'email n'est pas encore vérifié, accepter un token de vérification
        // optionnel
        if (!utilisateur.isEmailVerifie()) {
            String verificationToken = loginRequest.getVerificationToken();
            if (verificationToken != null) {
                verificationToken = verificationToken.trim().toLowerCase();
            }
            if (verificationToken == null || verificationToken.isBlank()) {
                return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                        "Email non vérifié. Fournissez le token de vérification envoyé par email.");
            }

            // Valider le token (le service marque le compte comme vérifié et le token comme
            // utilisé)
            Optional<Utilisateur> utilisateurTokenOpt = verificationService.validerToken(verificationToken);
            if (utilisateurTokenOpt.isEmpty()) {
                return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                        "Token de vérification invalide ou expiré");
            }

            // Par sécurité, vérifier que le token correspond bien à l'utilisateur qui tente
            // de se connecter
            if (!utilisateurTokenOpt.get().getId().equals(utilisateur.getId())) {
                return new LoginResponseDTO(null, null, null, null, null, null, null, null,
                        "Token de vérification ne correspond pas à cet utilisateur");
            }

            // Recharger l'utilisateur pour refléter l'état vérifié
            utilisateur = utilisateurTokenOpt.get();
        }

        // Mettre à jour la dernière connexion
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        // Générer les tokens
        String token = jwtTokenProvider.generateToken(utilisateur.getId(), utilisateur.getEmail(),
                utilisateur.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(utilisateur.getId());

        // Calculer la date d'expiration
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(24); // 24 heures

        return new LoginResponseDTO(
                token,
                refreshToken,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole().name(),
                expirationDate,
                "Connexion réussie");
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isTokenExpired(token);
    }

    public String refreshToken(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken) && !jwtTokenProvider.isTokenExpired(refreshToken)) {
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(userId);

            if (utilisateurOpt.isPresent() && utilisateurOpt.get().isEstActif()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                return jwtTokenProvider.generateToken(utilisateur.getId(), utilisateur.getEmail(),
                        utilisateur.getRole().name());
            }
        }
        return null;
    }

    /**
     * Modifie le mot de passe d'un utilisateur (locataire ou locateur)
     * 
     * @param dto DTO contenant l'ID utilisateur, l'ancien mot de passe et le
     *            nouveau mot de passe
     * @return Réponse indiquant le succès ou l'échec de l'opération
     */
    public ReponseModificationMotDePasseDTO modifierMotDePasse(ModificationMotDePasseDTO dto) {
        try {
            // Vérifier si l'utilisateur existe
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(dto.getUtilisateurId());

            if (utilisateurOpt.isEmpty()) {
                return new ReponseModificationMotDePasseDTO(false, "Utilisateur non trouvé");
            }

            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier si le compte est actif
            if (!utilisateur.isEstActif()) {
                return new ReponseModificationMotDePasseDTO(false, "Compte désactivé");
            }

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(dto.getAncienMotDePasse(), utilisateur.getMotDePasseHash())) {
                return new ReponseModificationMotDePasseDTO(false, "Ancien mot de passe incorrect");
            }

            // Encoder le nouveau mot de passe
            String nouveauMotDePasseHash = passwordEncoder.encode(dto.getNouveauMotDePasse());

            // Mettre à jour le mot de passe
            utilisateur.setMotDePasseHash(nouveauMotDePasseHash);
            utilisateur.setDateModification(LocalDateTime.now());

            utilisateurRepository.save(utilisateur);

            return new ReponseModificationMotDePasseDTO(
                    true,
                    "Mot de passe modifié avec succès",
                    utilisateur.getId().toString(),
                    utilisateur.getEmail());

        } catch (Exception e) {
            return new ReponseModificationMotDePasseDTO(false,
                    "Erreur lors de la modification du mot de passe: " + e.getMessage());
        }
    }
}