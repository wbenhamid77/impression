package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories_stade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieStade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Catégorie de place (ex: 1, 2, 3, 4)
    @Column(nullable = false)
    private Integer categorie;

    // Nom optionnel (ex: "Cat 1")
    @Column(length = 100)
    private String nom;

    // Nombre de places disponibles pour cette catégorie
    @Column(nullable = false)
    private Integer nombrePlaces;

    // Prix par place pour cette catégorie
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stade_id", nullable = false)
    private Stade stade;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column
    private LocalDateTime dateModification;
}
