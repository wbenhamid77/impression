package com.example.Impression.dto;

import com.example.Impression.enums.Role;
import com.example.Impression.enums.StatutKYC;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private UUID id;
    private Role role;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private StatutKYC statutKyc;
    private LocalDateTime dateInscription;
    private LocalDateTime derniereConnexion;
    private boolean estActif;
    private String photoProfil;
    private LocalDateTime dateModification;
}