package com.example.Impression.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoldeDTO {
    private BigDecimal totalEntrees;
    private BigDecimal totalSorties;
    private BigDecimal soldeNet;
}








