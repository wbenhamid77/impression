package com.example.Impression.enums;

public enum ModePaiement {
    CARTE_BANCAIRE("Carte bancaire"),
    PAYPAL("PayPal"),
    VIREMENT("Virement bancaire"),
    PAIEMENT_SUR_PLACE("Paiement sur place"),
    CHEQUE("Chèque");

    private final String libelle;

    ModePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}