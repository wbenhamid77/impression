package com.example.Impression.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseModificationMotDePasseDTO {

    private boolean succes;
    private String message;
    private String utilisateurId;
    private String email;

    public ReponseModificationMotDePasseDTO(boolean succes, String message) {
        this.succes = succes;
        this.message = message;
    }
}