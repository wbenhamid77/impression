package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
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

    // Surface du stade en mètres carrés
    @Column
    private Integer surfaceMetresCarres;

    // Catégories/tags du stade (stockées en JSON via convertisseur)
    @Convert(converter = StringListConverter.class)
    @Column(length = 2000)
    private List<String> categories;

    // Prix indicatifs (ex: location d'espaces, visite, etc.)
    @Column(precision = 10, scale = 2)
    private BigDecimal prixMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMax;

    // Catégories (places/prix) associées au stade
    @OneToMany(mappedBy = "stade", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategorieStade> categoriesPlaces;

    // Liens d'images (chemins/URLs)
    @Convert(converter = StringListConverter.class)
    @Column(length = 4000)
    private List<String> images;

    // Images binaires (BLOB encodé base64 via convertisseur)
    @Convert(converter = ByteArrayListConverter.class)
    @Lob
    private List<byte[]> imagesBlob;

    // Informations complémentaires
    @Column(length = 100)
    private String surfaceType; // ex: Gazon naturel, Synthétique

    @Column(length = 50)
    private String dimensions; // ex: 105x68 m

    @Column(length = 255)
    private String siteWeb;

    @Column(length = 50)
    private String telephone;

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