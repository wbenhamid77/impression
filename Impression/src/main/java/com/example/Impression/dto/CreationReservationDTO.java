package com.example.Impression.dto;

import com.example.Impression.enums.ModePaiement;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationReservationDTO {

    @NotNull(message = "L'ID de l'annonce est obligatoire")
    private UUID annonceId;

    @NotNull(message = "La date d'arrivée est obligatoire")
    @Future(message = "La date d'arrivée doit être dans le futur")
    private LocalDate dateArrivee;

    @NotNull(message = "La date de départ est obligatoire")
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDate dateDepart;

    @NotNull(message = "Le nombre de voyageurs est obligatoire")
    @Min(value = 1, message = "Le nombre de voyageurs doit être au moins de 1")
    @Max(value = 20, message = "Le nombre de voyageurs ne peut pas dépasser 20")
    private Integer nombreVoyageurs;

    private ModePaiement modePaiement = ModePaiement.PAIEMENT_SUR_PLACE;

    @Size(max = 1000, message = "Le message ne peut pas dépasser 1000 caractères")
    private String messageProprietaire;

    // Frais optionnels
    private BigDecimal fraisService = BigDecimal.ZERO;
    private BigDecimal fraisNettoyage = BigDecimal.ZERO;
    private BigDecimal fraisDepot = BigDecimal.ZERO;
}