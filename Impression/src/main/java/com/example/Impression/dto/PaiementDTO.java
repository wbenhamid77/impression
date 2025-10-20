package com.example.Impression.dto;

import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.StatutPaiement;
import com.example.Impression.enums.TypePaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaiementDTO {
    private UUID id;
    private UUID reservationId;
    private BigDecimal montant;
    private TypePaiement typePaiement;
    private StatutPaiement statut;
    private ModePaiement modePaiement;
    private String numeroTransaction;
    private String referenceExterne;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime datePaiement;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateEchec;
    private String numeroRemboursement;
    private LocalDateTime dateRemboursement;
    private String raisonRemboursement;
    private String metadonnees;

    // Informations de la réservation
    private String titreAnnonce;
    private String adresseAnnonce;
    private String nomLocataire;
    private String prenomLocataire;
    private String emailLocataire;
    private String nomLocateur;
    private String prenomLocateur;
    private String emailLocateur;
    private LocalDateTime dateArrivee;
    private LocalDateTime dateDepart;
    private int nombreNuits;

    // Informations calculées
    private boolean estExpire;
    private boolean peutEtreAnnule;
    private boolean peutEtreRembourse;
    private long heuresRestantes;
}
