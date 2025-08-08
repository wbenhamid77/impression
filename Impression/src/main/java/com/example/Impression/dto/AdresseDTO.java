package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdresseDTO {
    private UUID id;
    private String rue;
    private String numero;
    private String codePostal;
    private String ville;
    private String pays;
    private String complement;
    private java.math.BigDecimal surface; // Surface en m²
    private UUID locateurId;
    private String nomLocateur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean estActive;
}