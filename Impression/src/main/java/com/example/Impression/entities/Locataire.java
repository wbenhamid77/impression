package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import com.example.Impression.entities.Reservation;

@Entity
@Table(name = "locataires")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Locataire extends Utilisateur {

    @Column
    private int nombreReservations = 0;

    @Column
    private double noteMoyenne = 0.0;

    @Column
    private boolean estVerifie = false;

    // Informations professionnelles
    @Column(length = 100)
    private String profession;

    @Column
    private Double revenuAnnuel;

    @Column(length = 100)
    private String employeur;

    @Column
    private LocalDate dateEmbauche;

    @ElementCollection
    @CollectionTable(name = "locataire_favoris", joinColumns = @JoinColumn(name = "locataire_id"))
    @Column(name = "favori_id")
    private List<UUID> favoris;

    // Relation avec les réservations
    @OneToMany(mappedBy = "locataire", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    // Constructeur pour créer un locataire avec les informations de base
    public Locataire(String nom, String prenom, String email, String motDePasseHash) {
        super();
        this.setNom(nom);
        this.setPrenom(prenom);
        this.setEmail(email);
        this.setMotDePasseHash(motDePasseHash);
        this.setRole(com.example.Impression.enums.Role.LOCATAIRE);
    }

    // Constructeur complet pour créer un locataire avec toutes les informations
    public Locataire(String nom, String prenom, String email, String motDePasseHash,
            String telephone, String profession, Double revenuAnnuel,
            String employeur, LocalDate dateEmbauche) {
        this(nom, prenom, email, motDePasseHash);
        this.setTelephone(telephone);
        this.profession = profession;
        this.revenuAnnuel = revenuAnnuel;
        this.employeur = employeur;
        this.dateEmbauche = dateEmbauche;
    }
}