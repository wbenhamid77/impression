package com.example.Impression.enums;

public enum StatutReservation {
    EN_ATTENTE("En attente de confirmation"),
    CONFIRMEE("Confirmée"),
    ANNULEE("Annulée"),
    TERMINEE("Terminée"),
    EN_COURS("En cours");

    private final String libelle;

    StatutReservation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}