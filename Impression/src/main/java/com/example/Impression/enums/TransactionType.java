package com.example.Impression.enums;

public enum TransactionType {
    // Encaissement du locataire vers la plateforme (payin)
    PAYIN_PLATEFORME,
    // Split du paiement
    PAYOUT_LOCATEUR,
    COMMISSION_PLATEFORME,

    // Remboursements
    REFUND_LOCATAIRE_FROM_LOCATEUR,
    REFUND_LOCATAIRE_FROM_PLATEFORME
}
