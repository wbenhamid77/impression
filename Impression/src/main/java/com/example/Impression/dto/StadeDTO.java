package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private Integer surfaceMetresCarres;
    private List<String> categories;
    private List<CategorieStadeDTO> categoriesPlaces;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private List<String> images;
    private List<byte[]> imagesBlob;
    private String surfaceType;
    private String dimensions;
    private String siteWeb;
    private String telephone;
}