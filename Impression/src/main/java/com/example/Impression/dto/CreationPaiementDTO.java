package com.example.Impression.dto;

import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.TypePaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreationPaiementDTO {
    private UUID reservationId;
    private BigDecimal montant;
    private TypePaiement typePaiement;
    private ModePaiement modePaiement;
    private String description;
    private String metadonnees;
}
