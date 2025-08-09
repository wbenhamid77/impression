package com.example.Impression.entities;

import com.example.Impression.enums.ModeTransport;
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
@Table(name = "annonce_stade_distances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnonceStadeDistance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relation avec l'annonce
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    // Relation avec le stade
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stade_id", nullable = false)
    private Stade stade;

    // Distance en kilomètres
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    // Temps de trajet estimé en minutes
    @Column(nullable = false)
    private Integer tempsTrajetMinutes;

    // Mode de transport (VOITURE, TRANSPORT_PUBLIC, MARCHE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeTransport modeTransport = ModeTransport.VOITURE;

    // Indique si c'est le stade le plus proche
    @Column(nullable = false)
    private Boolean estLePlusProche = false;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column
    private LocalDateTime dateModification;

    // Constructeur pour faciliter la création
    public AnnonceStadeDistance(Annonce annonce, Stade stade, BigDecimal distance, Integer tempsTrajetMinutes) {
        this.annonce = annonce;
        this.stade = stade;
        this.distance = distance;
        this.tempsTrajetMinutes = tempsTrajetMinutes;
    }

    // Constructeur de convenance avec tous les paramètres
    public AnnonceStadeDistance(Annonce annonce, Stade stade, BigDecimal distance, Integer tempsTrajetMinutes,
            ModeTransport modeTransport, Boolean estLePlusProche) {
        this.annonce = annonce;
        this.stade = stade;
        this.distance = distance;
        this.tempsTrajetMinutes = tempsTrajetMinutes;
        this.modeTransport = modeTransport;
        this.estLePlusProche = estLePlusProche;
    }
}