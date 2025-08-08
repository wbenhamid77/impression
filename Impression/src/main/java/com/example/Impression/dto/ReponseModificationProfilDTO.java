package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseModificationProfilDTO {

    private boolean succes;
    private String message;
    private UUID utilisateurId;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String photoProfil;
    private LocalDateTime dateModification;

    public ReponseModificationProfilDTO(boolean succes, String message) {
        this.succes = succes;
        this.message = message;
    }

    public ReponseModificationProfilDTO(boolean succes, String message, UUID utilisateurId, String email) {
        this.succes = succes;
        this.message = message;
        this.utilisateurId = utilisateurId;
        this.email = email;
    }
}