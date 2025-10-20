package com.example.Impression.dto;

import lombok.Data;

@Data
public class ConfirmationPaiementDTO {
    private String numeroTransaction;
    private String referenceExterne;
    private String metadonnees;
}
