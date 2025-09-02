package com.example.Impression.dto;

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
public class ReservationLocateurDetailleeDTO {

    private UUID id;

    // Informations complètes de l'annonce
    private AnnonceDTO annonce;

    // Informations complètes du locataire
    private UtilisateurDTO locataire;

    // Dates et durée
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private int nombreNuits;
    private int nombreVoyageurs;

    // Prix et montants
    private BigDecimal prixParNuit;
    private BigDecimal prixTotal;
    private BigDecimal fraisService;
    private BigDecimal fraisNettoyage;
    private BigDecimal fraisDepot;
    private BigDecimal montantTotal;

    // Statut et informations
    private StatutReservation statut;
    private String libelleStatut;
    private String messageProprietaire;

    // Dates de création et modification
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateConfirmation;
    private LocalDateTime dateAnnulation;

    // Raison d'annulation si applicable
    private String raisonAnnulation;
}