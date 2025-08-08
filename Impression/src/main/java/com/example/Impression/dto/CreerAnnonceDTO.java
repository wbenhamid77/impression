package com.example.Impression.dto;

import com.example.Impression.enums.TypeMaison;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreerAnnonceDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "L'adresse est obligatoire")
    private AdresseDTO adresse;

    @NotNull(message = "Le prix par nuit est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix par nuit doit être positif")
    private BigDecimal prixParNuit;

    @DecimalMin(value = "0.01", message = "Le prix par semaine doit être positif")
    private BigDecimal prixParSemaine;

    @DecimalMin(value = "0.01", message = "Le prix par mois doit être positif")
    private BigDecimal prixParMois;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins de 1")
    private Integer capacite;

    @NotNull(message = "Le nombre de chambres est obligatoire")
    @Min(value = 0, message = "Le nombre de chambres ne peut pas être négatif")
    private Integer nombreChambres;

    @NotNull(message = "Le nombre de salles de bain est obligatoire")
    @Min(value = 0, message = "Le nombre de salles de bain ne peut pas être négatif")
    private Integer nombreSallesDeBain;

    @NotNull(message = "Le type de maison est obligatoire")
    private TypeMaison typeMaison;

    private List<String> equipements;
    private List<String> regles;
    private List<String> images;

    // Informations sur le stade
    private String stadePlusProche;
    private BigDecimal distanceStade;
    private String adresseStade;

    // Coordonnées géographiques
    @DecimalMin(value = "-90.0", message = "La latitude doit être entre -90 et 90")
    @DecimalMax(value = "90.0", message = "La latitude doit être entre -90 et 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être entre -180 et 180")
    @DecimalMax(value = "180.0", message = "La longitude doit être entre -180 et 180")
    private BigDecimal longitude;

    @NotNull(message = "L'ID du locateur est obligatoire")
    private UUID locateurId;
}