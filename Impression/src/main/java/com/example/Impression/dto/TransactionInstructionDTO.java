package com.example.Impression.dto;

import com.example.Impression.enums.TransactionStatus;
import com.example.Impression.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInstructionDTO {
    private UUID id;
    private UUID reservationId;
    private UUID paiementId;
    private TransactionType type;
    private TransactionStatus statut;
    private UUID fromRibId;
    private UUID toRibId;
    private BigDecimal montant;
    private String reference;
    private String notes;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateExecution;
}
