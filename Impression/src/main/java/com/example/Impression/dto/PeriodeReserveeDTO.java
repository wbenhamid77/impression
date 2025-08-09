package com.example.Impression.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodeReserveeDTO {
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
}