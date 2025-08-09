package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(nullable = false, length = 255)
    private String ville;

    @Column(length = 500)
    private String adresseComplete;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column
    private Integer capacite;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean estActif = true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column
    private LocalDateTime dateModification;

    // Constructeur pour faciliter la création
    public Stade(String nom, String ville, String adresseComplete, BigDecimal latitude, BigDecimal longitude,
            Integer capacite) {
        this.nom = nom;
        this.ville = ville;
        this.adresseComplete = adresseComplete;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacite = capacite;
    }
}