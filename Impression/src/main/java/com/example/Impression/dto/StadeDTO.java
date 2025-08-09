package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StadeDTO {
    private UUID id;
    private String nom;
    private String ville;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacite;
    private String description;
    private boolean estActif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}