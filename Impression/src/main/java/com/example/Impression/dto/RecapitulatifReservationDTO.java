package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecapitulatifReservationDTO {

    // Informations du logement
    private UUID annonceId;
    private String titreAnnonce;
    private String adresseAnnonce;
    private String nomLocateur;
    private String emailLocateur;

    // Informations des dates
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private int nombreNuits;

    // Informations des prix
    private BigDecimal prixParNuit;
    private BigDecimal prixTotal;
    private BigDecimal fraisService;
    private BigDecimal fraisNettoyage;
    private BigDecimal fraisDepot;
    private BigDecimal montantTotal;

    // Informations du voyageur
    private int nombreVoyageurs;
    private String messageProprietaire;

    // Informations de paiement
    private String modePaiement;
    private boolean paiementEnLigne;

    // Calcul du récapitulatif
    public void calculerRecapitulatif() {
        if (dateArrivee != null && dateDepart != null && prixParNuit != null) {
            this.nombreNuits = (int) java.time.temporal.ChronoUnit.DAYS.between(dateArrivee, dateDepart);
            if (nombreNuits <= 0) {
                nombreNuits = 1; // Au moins une nuit
            }

            this.prixTotal = prixParNuit.multiply(BigDecimal.valueOf(nombreNuits));

            // Calcul du montant total avec frais
            BigDecimal fraisTotal = BigDecimal.ZERO;
            if (fraisService != null)
                fraisTotal = fraisTotal.add(fraisService);
            if (fraisNettoyage != null)
                fraisTotal = fraisTotal.add(fraisNettoyage);
            if (fraisDepot != null)
                fraisTotal = fraisTotal.add(fraisDepot);

            this.montantTotal = prixTotal.add(fraisTotal);
        }
    }
}