package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "locateurs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Locateur extends Utilisateur {

    @Column(precision = 10, scale = 2)
    private BigDecimal soldePortefeuille = BigDecimal.ZERO;

    @Column
    private int nombreAnnonces = 0;

    @Column
    private double noteMoyenne = 0.0;

    @Column
    private boolean estVerifie = false;

    @Column(length = 1000)
    private String description;

    // Informations professionnelles
    @Column(length = 14)
    private String numeroSiret;

    @Column(length = 100)
    private String raisonSociale;

    @Column(length = 500)
    private String adresseProfessionnelle;

    @ElementCollection
    @CollectionTable(name = "locateur_documents", joinColumns = @JoinColumn(name = "locateur_id"))
    @Column(name = "document")
    private List<String> documents;

    // Constructeur pour créer un locateur avec les informations de base
    public Locateur(String nom, String prenom, String email, String motDePasseHash) {
        super();
        this.setNom(nom);
        this.setPrenom(prenom);
        this.setEmail(email);
        this.setMotDePasseHash(motDePasseHash);
        this.setRole(com.example.Impression.enums.Role.LOCATEUR);
    }

    // Constructeur complet pour créer un locateur avec toutes les informations
    public Locateur(String nom, String prenom, String email, String motDePasseHash,
            String telephone, String description, String numeroSiret,
            String raisonSociale, String adresseProfessionnelle) {
        this(nom, prenom, email, motDePasseHash);
        this.setTelephone(telephone);
        this.description = description;
        this.numeroSiret = numeroSiret;
        this.raisonSociale = raisonSociale;
        this.adresseProfessionnelle = adresseProfessionnelle;
    }
}