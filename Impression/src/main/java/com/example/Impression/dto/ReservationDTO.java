package com.example.Impression.dto;

import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.StatutReservation;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private UUID id;
    private UUID annonceId;
    private UUID locataireId;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private int nombreNuits;
    private BigDecimal prixParNuit;
    private BigDecimal prixTotal;
    private BigDecimal fraisService;
    private BigDecimal fraisNettoyage;
    private BigDecimal fraisDepot;
    private BigDecimal montantTotal;
    private StatutReservation statut;
    private ModePaiement modePaiement;
    private String messageProprietaire;
    private String numeroTransaction;
    private LocalDateTime datePaiement;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateConfirmation;
    private LocalDateTime dateAnnulation;
    private String raisonAnnulation;
    private int nombreVoyageurs;

    // Informations de l'annonce (pour l'affichage)
    private String titreAnnonce;
    private String adresseAnnonce;
    private String nomLocateur;
    private String emailLocateur;

    // Informations du locataire (pour l'affichage)
    private String nomLocataire;
    private String prenomLocataire;
    private String emailLocataire;
}