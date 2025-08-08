package com.example.Impression.dto;

import com.example.Impression.enums.TypeMaison;
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
public class AnnonceDTO {
    private UUID id;
    private String titre;
    private String description;
    private AdresseDTO adresse;
    private BigDecimal prixParNuit;
    private BigDecimal prixParSemaine;
    private BigDecimal prixParMois;
    private int capacite;
    private int nombreChambres;
    private int nombreSallesDeBain;
    private TypeMaison typeMaison;
    private boolean estActive;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private List<String> equipements;
    private List<String> regles;
    private List<String> images;
    private List<byte[]> imagesBlob;
    private Double noteMoyenne;
    private int nombreAvis;
    private LocateurInfoDTO locateur;
    private String stadePlusProche;
    private BigDecimal distanceStade;
    private String adresseStade;
    private BigDecimal latitude;
    private BigDecimal longitude;
}