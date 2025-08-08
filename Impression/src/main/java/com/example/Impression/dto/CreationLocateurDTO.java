package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationLocateurDTO {

    // Attributs de base (hérités de Utilisateur)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 500, message = "L'URL de la photo de profil ne peut pas dépasser 500 caractères")
    private String photoProfil;

    // Attributs spécifiques au Locateur
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Size(max = 14, message = "Le numéro SIRET doit contenir exactement 14 caractères")
    private String numeroSiret;

    @Size(max = 100, message = "La raison sociale ne peut pas dépasser 100 caractères")
    private String raisonSociale;

    @Size(max = 500, message = "L'adresse professionnelle ne peut pas dépasser 500 caractères")
    private String adresseProfessionnelle;
}