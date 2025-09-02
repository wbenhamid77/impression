package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecapitulatifReservationsLocateurDTO {

    private UUID locateurId;
    private String nomLocateur;
    private String prenomLocateur;
    private String emailLocateur;

    // Réservations groupées par statut
    private List<ReservationLocateurDTO> reservationsEnAttente;
    private List<ReservationLocateurDTO> reservationsConfirmees;
    private List<ReservationLocateurDTO> reservationsEnCours;
    private List<ReservationLocateurDTO> reservationsTerminees;
    private List<ReservationLocateurDTO> reservationsAnnulees;

    // Statistiques
    private long totalReservations;
    private long totalEnAttente;
    private long totalConfirmees;
    private long totalEnCours;
    private long totalTerminees;
    private long totalAnnulees;
}