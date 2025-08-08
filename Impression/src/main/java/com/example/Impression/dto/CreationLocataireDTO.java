package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationLocataireDTO {

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

    // Attributs spécifiques au Locataire
    @Size(max = 100, message = "La profession ne peut pas dépasser 100 caractères")
    private String profession;

    @Positive(message = "Le revenu annuel doit être positif")
    private Double revenuAnnuel;

    @Size(max = 100, message = "L'employeur ne peut pas dépasser 100 caractères")
    private String employeur;

    private LocalDate dateEmbauche;
}