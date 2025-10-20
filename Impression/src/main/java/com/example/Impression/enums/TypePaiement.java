package com.example.Impression.enums;

public enum TypePaiement {
    ACOMPTE("Acompte"),
    SOLDE("Solde"),
    TOTAL("Paiement total"),
    REMBOURSEMENT("Remboursement");

    private final String libelle;

    TypePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
