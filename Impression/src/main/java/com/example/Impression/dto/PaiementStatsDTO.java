package com.example.Impression.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementStatsDTO {
    private long totalCount;
    private BigDecimal totalMontant;

    private BigDecimal totalPaye;
    private BigDecimal totalRembourse;
    private BigDecimal totalEnAttente;
    private BigDecimal totalEnCours;
    private BigDecimal totalEchec;
    private BigDecimal totalExpire;
    private BigDecimal totalAnnule;
}
