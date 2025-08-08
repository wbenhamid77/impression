package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificationProfilDTO {

    // Champs de base
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 500, message = "L'URL de la photo de profil ne peut pas dépasser 500 caractères")
    private String photoProfil;

    // Champs spécifiques pour Admin
    private String matricule;
    private String departement;

    // Champs spécifiques pour Locateur
    private String description;
    private String numeroSiret;
    private String raisonSociale;
    private String adresseProfessionnelle;

    // Champs spécifiques pour Locataire
    private String profession;
    private Double revenuAnnuel;
    private String employeur;
    private LocalDate dateEmbauche;
}