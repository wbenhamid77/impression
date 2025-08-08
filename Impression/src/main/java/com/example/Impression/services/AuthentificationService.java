package com.example.Impression.services;

import com.example.Impression.dto.ConnexionDTO;
import com.example.Impression.dto.UtilisateurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthentificationService {

    @Autowired
    private UtilisateurService utilisateurService;

    public Optional<UtilisateurDTO> seConnecter(ConnexionDTO connexionDTO) {
        return utilisateurService.authentifierUtilisateur(
                connexionDTO.getEmail(),
                connexionDTO.getMotDePasse());
    }

    public boolean seDeconnecter(String email) {
        // Pour l'instant, on peut juste logger la déconnexion
        // Plus tard, on pourra ajouter la gestion des sessions/tokens
        return true;
    }

    public boolean verifierAuthentification(String email, String motDePasse) {
        return utilisateurService.authentifierUtilisateur(email, motDePasse).isPresent();
    }
}