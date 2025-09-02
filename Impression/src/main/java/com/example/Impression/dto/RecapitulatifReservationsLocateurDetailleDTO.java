package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecapitulatifReservationsLocateurDetailleDTO {

    // Informations du locateur
    private UUID locateurId;
    private String nomLocateur;
    private String prenomLocateur;
    private String emailLocateur;

    // Réservations détaillées par statut
    private List<ReservationLocateurDetailleeDTO> reservationsEnAttente;
    private List<ReservationLocateurDetailleeDTO> reservationsConfirmees;
    private List<ReservationLocateurDetailleeDTO> reservationsEnCours;
    private List<ReservationLocateurDetailleeDTO> reservationsTerminees;
    private List<ReservationLocateurDetailleeDTO> reservationsAnnulees;

    // Statistiques
    private int totalReservations;
    private int totalEnAttente;
    private int totalConfirmees;
    private int totalEnCours;
    private int totalTerminees;
    private int totalAnnulees;
}