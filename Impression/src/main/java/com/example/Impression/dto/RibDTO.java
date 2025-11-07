package com.example.Impression.dto;

import com.example.Impression.enums.RibType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RibDTO {
    private UUID id;
    private RibType type;
    private UUID locataireId;
    private UUID locateurId;
    private String iban;
    private String bic;
    private String titulaireNom;
    private String banque;
    private boolean actif;
    private boolean defautCompte;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
