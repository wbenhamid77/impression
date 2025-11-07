package com.example.Impression.dto;

import com.example.Impression.enums.RibType;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateRibDTO {
    private RibType type; // LOCATAIRE, LOCATEUR, PLATEFORME
    private UUID locataireId; // requis si type LOCATAIRE
    private UUID locateurId; // requis si type LOCATEUR
    private String iban;
    private String bic;
    private String titulaireNom;
    private String banque;
    private boolean defautCompte;
}
