package com.example.Impression.entities;

import com.example.Impression.enums.RibType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ribs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rib {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RibType type;

    // Titulaires possibles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id")
    private Locataire locataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locateur_id")
    private Locateur locateur;

    // Si plateforme, aucun titulaire utilisateur
    @Column(nullable = false)
    private String iban;

    @Column
    private String bic;

    @Column(nullable = false)
    private String titulaireNom;

    @Column
    private String banque;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "principal", nullable = false)
    private boolean defautCompte = false; // compte par défaut du titulaire/type (colonne DB: principal)

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

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
