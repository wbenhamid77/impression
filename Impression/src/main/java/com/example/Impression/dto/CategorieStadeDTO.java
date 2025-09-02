package com.example.Impression.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieStadeDTO {
    private UUID id;
    private Integer categorie;
    private String nom;
    private Integer nombrePlaces;
    private BigDecimal prix;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
