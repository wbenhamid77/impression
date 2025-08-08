package com.example.Impression.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificationMotDePasseDTO {

    @NotNull(message = "L'ID utilisateur est requis")
    private UUID utilisateurId;

    @NotBlank(message = "L'ancien mot de passe est requis")
    private String ancienMotDePasse;

    @NotBlank(message = "Le nouveau mot de passe est requis")
    private String nouveauMotDePasse;
}