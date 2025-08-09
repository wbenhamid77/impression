package com.example.Impression.entities;

import com.example.Impression.enums.TypeMaison;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Entity
@Table(name = "annonces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Relation avec l'adresse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParNuit;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixParSemaine;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixParMois;

    @Column(nullable = false)
    private int capacite;

    @Column(nullable = false)
    private int nombreChambres;

    @Column(nullable = false)
    private int nombreSallesDeBain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMaison typeMaison;

    @Column(nullable = false)
    private boolean estActive = true;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    // Liste d'équipements stockée comme JSON dans la base de données
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> equipements = new ArrayList<>();

    // Liste de règles stockée comme JSON dans la base de données
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> regles = new ArrayList<>();

    // Liste d'images stockée comme JSON de chemins dans la base de données
    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> images = new ArrayList<>();

    // Images stockées comme liste de fichiers binaires dans la base de données
    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = ByteArrayListConverter.class)
    private List<byte[]> imagesBlob = new ArrayList<>();

    @Column
    private Double noteMoyenne = 0.0;

    @Column(nullable = false)
    private int nombreAvis = 0;

    // Relation avec le locateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locateur_id", nullable = false)
    private Locateur locateur;

    // Coordonnées géographiques
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // Relation avec les distances vers les stades
    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnnonceStadeDistance> distancesStades = new ArrayList<>();

    // Relation avec les réservations
    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.example.Impression.entities.Reservation> reservations = new ArrayList<>();

    // Méthodes métier
    public void publier() {
        this.estActive = true;
        this.dateModification = LocalDateTime.now();
    }

    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }

    public void desactiver() {
        this.estActive = false;
        this.dateModification = LocalDateTime.now();
    }

    public void supprimer() {
        this.estActive = false;
        this.dateModification = LocalDateTime.now();
    }

    public BigDecimal calculerPrixTotal(LocalDateTime debut, LocalDateTime fin) {
        // Logique de calcul du prix total selon la durée
        long jours = java.time.Duration.between(debut, fin).toDays();

        if (jours <= 7) {
            return prixParNuit.multiply(BigDecimal.valueOf(jours));
        } else if (jours <= 30) {
            return prixParSemaine.multiply(BigDecimal.valueOf(jours / 7));
        } else {
            return prixParMois.multiply(BigDecimal.valueOf(jours / 30));
        }
    }

    public boolean verifierDisponibilite(java.time.LocalDate dateArrivee, java.time.LocalDate dateDepart) {
        // Vérifier s'il n'y a pas de réservations actives pour cette période
        return reservations.stream()
                .filter(reservation -> reservation.estActive())
                .noneMatch(reservation -> reservation.estEnConflit(dateArrivee, dateDepart));
    }

    public List<com.example.Impression.entities.Reservation> getReservationsActives() {
        return reservations.stream()
                .filter(reservation -> reservation.estActive())
                .collect(java.util.stream.Collectors.toList());
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        // Initialiser les listes si elles sont null
        if (equipements == null) {
            equipements = new ArrayList<>();
        }
        if (regles == null) {
            regles = new ArrayList<>();
        }
        if (images == null) {
            images = new ArrayList<>();
        }
        if (distancesStades == null) {
            distancesStades = new ArrayList<>();
        }
        if (reservations == null) {
            reservations = new ArrayList<>();
        }
        if (reservations == null) {
            reservations = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        // S'assurer que les listes ne sont pas null
        if (equipements == null) {
            equipements = new ArrayList<>();
        }
        if (regles == null) {
            regles = new ArrayList<>();
        }
        if (images == null) {
            images = new ArrayList<>();
        }
        if (distancesStades == null) {
            distancesStades = new ArrayList<>();
        }
    }
}