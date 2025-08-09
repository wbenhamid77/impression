package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnonceStadeDistanceDTO {
    private UUID id;
    private StadeDTO stade;
    private BigDecimal distance; // Distance en kilomètres
    private Integer tempsTrajetMinutes; // Temps de trajet en minutes
    private String modeTransport;
    private Boolean estLePlusProche;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Constructeur de convenance
    public AnnonceStadeDistanceDTO(StadeDTO stade, BigDecimal distance, Integer tempsTrajetMinutes,
            Boolean estLePlusProche) {
        this.stade = stade;
        this.distance = distance;
        this.tempsTrajetMinutes = tempsTrajetMinutes;
        this.estLePlusProche = estLePlusProche;
    }

    // Méthode utilitaire pour formater le temps
    public String getTempsTrajetFormate() {
        if (tempsTrajetMinutes == null)
            return null;

        if (tempsTrajetMinutes < 60) {
            return tempsTrajetMinutes + " min";
        } else {
            int heures = tempsTrajetMinutes / 60;
            int minutes = tempsTrajetMinutes % 60;
            return heures + "h" + (minutes > 0 ? " " + minutes + "min" : "");
        }
    }
}