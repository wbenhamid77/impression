package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StadeDistanceDTO {
    private UUID id;
    private String nom;
    private String ville;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacite;
    private String description;
    private BigDecimal distance; // Distance en kilomètres

    // Constructeur de convenance
    public StadeDistanceDTO(StadeDTO stade, BigDecimal distance) {
        this.id = stade.getId();
        this.nom = stade.getNom();
        this.ville = stade.getVille();
        this.adresseComplete = stade.getAdresseComplete();
        this.latitude = stade.getLatitude();
        this.longitude = stade.getLongitude();
        this.capacite = stade.getCapacite();
        this.description = stade.getDescription();
        this.distance = distance;
    }
}