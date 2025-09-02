package com.example.Impression.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ModePaiement {
    CARTE_BANCAIRE("Carte bancaire", "CARTE", "CARTE_BANCAIRE"),
    PAYPAL("PayPal", "PAYPAL"),
    VIREMENT("Virement bancaire", "VIREMENT", "VIREMENT_BANCAIRE"),
    PAIEMENT_SUR_PLACE("Paiement sur place", "PAIEMENT_SUR_PLACE", "SUR_PLACE"),
    CHEQUE("Chèque", "CHEQUE");

    private final String libelle;
    private final String[] alias;

    ModePaiement(String libelle, String... alias) {
        this.libelle = libelle;
        this.alias = alias;
    }

    public String getLibelle() {
        return libelle;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static ModePaiement fromString(String value) {
        if (value == null) {
            return null;
        }

        // Essayer d'abord par le nom exact
        try {
            return ModePaiement.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Essayer par les alias
            for (ModePaiement mode : values()) {
                for (String alias : mode.alias) {
                    if (alias.equalsIgnoreCase(value)) {
                        return mode;
                    }
                }
            }
            throw new IllegalArgumentException("Mode de paiement non reconnu: " + value);
        }
    }
}