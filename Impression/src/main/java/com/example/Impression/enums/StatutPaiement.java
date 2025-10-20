package com.example.Impression.enums;

public enum StatutPaiement {
    EN_ATTENTE("En attente de paiement"),
    EN_COURS("Paiement en cours"),
    PAYE("Payé"),
    ECHEC("Échec du paiement"),
    ANNULE("Annulé"),
    REMBOURSE("Remboursé"),
    EXPIRE("Expiré");

    private final String libelle;

    StatutPaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
