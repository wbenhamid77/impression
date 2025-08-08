package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String rue;

    @Column(length = 10)
    private String numero;

    @Column(nullable = false, length = 10)
    private String codePostal;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(nullable = false, length = 100)
    private String pays = "France";

    @Column(length = 255)
    private String complement;

    @Column(precision = 8, scale = 2)
    private BigDecimal surface; // Surface en m²

    // Relation avec le locateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locateur_id", nullable = false)
    private Locateur locateur;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private boolean estActive = true;

    public String getAdresseComplete() {
        StringBuilder adresse = new StringBuilder();
        if (numero != null && !numero.isEmpty()) {
            adresse.append(numero).append(" ");
        }
        if (rue != null && !rue.isEmpty()) {
            adresse.append(rue).append(", ");
        }
        if (codePostal != null && !codePostal.isEmpty()) {
            adresse.append(codePostal).append(" ");
        }
        if (ville != null && !ville.isEmpty()) {
            adresse.append(ville);
        }
        if (pays != null && !pays.isEmpty()) {
            adresse.append(", ").append(pays);
        }
        return adresse.toString();
    }

    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}