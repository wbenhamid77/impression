package com.example.Impression.entities;

import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.StatutPaiement;
import com.example.Impression.enums.TypePaiement;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relation avec la réservation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // Montant et type de paiement
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePaiement typePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement modePaiement;

    // Informations de transaction
    @Column(unique = true)
    private String numeroTransaction;

    @Column
    private String referenceExterne;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Dates importantes
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column
    private LocalDateTime datePaiement;

    @Column
    private LocalDateTime dateExpiration;

    @Column
    private LocalDateTime dateEchec;

    // Informations de remboursement
    @Column
    private String numeroRemboursement;

    @Column
    private LocalDateTime dateRemboursement;

    @Column(columnDefinition = "TEXT")
    private String raisonRemboursement;

    // Métadonnées
    @Column(columnDefinition = "TEXT")
    private String metadonnees;

    // Constructeur principal
    public Paiement(Reservation reservation, BigDecimal montant, TypePaiement typePaiement,
            ModePaiement modePaiement, String description) {
        this.reservation = reservation;
        this.montant = montant;
        this.typePaiement = typePaiement;
        this.modePaiement = modePaiement;
        this.description = description;
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.dateExpiration = LocalDateTime.now().plusHours(24); // Expiration dans 24h
    }

    // Méthodes métier
    public void marquerCommePaye(String numeroTransaction) {
        this.statut = StatutPaiement.PAYE;
        this.numeroTransaction = numeroTransaction;
        this.datePaiement = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public void marquerCommeEnCours() {
        this.statut = StatutPaiement.EN_COURS;
        this.dateModification = LocalDateTime.now();
    }

    public void marquerCommeEchec(String raison) {
        this.statut = StatutPaiement.ECHEC;
        this.dateEchec = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (raison != null) {
            this.description = (this.description != null ? this.description + " | " : "") + "Échec: " + raison;
        }
    }

    public void marquerCommeAnnule(String raison) {
        this.statut = StatutPaiement.ANNULE;
        this.dateModification = LocalDateTime.now();
        if (raison != null) {
            this.description = (this.description != null ? this.description + " | " : "") + "Annulé: " + raison;
        }
    }

    public void marquerCommeExpire() {
        this.statut = StatutPaiement.EXPIRE;
        this.dateModification = LocalDateTime.now();
    }

    public void rembourser(String numeroRemboursement, String raison) {
        this.statut = StatutPaiement.REMBOURSE;
        this.numeroRemboursement = numeroRemboursement;
        this.raisonRemboursement = raison;
        this.dateRemboursement = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public boolean estExpire() {
        return LocalDateTime.now().isAfter(this.dateExpiration);
    }

    public boolean peutEtreAnnule() {
        return statut == StatutPaiement.EN_ATTENTE || statut == StatutPaiement.EN_COURS;
    }

    public boolean peutEtreRembourse() {
        return statut == StatutPaiement.PAYE;
    }

    public boolean estPaye() {
        return statut == StatutPaiement.PAYE;
    }

    public boolean estEnAttente() {
        return statut == StatutPaiement.EN_ATTENTE;
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (statut == null) {
            statut = StatutPaiement.EN_ATTENTE;
        }
        if (dateExpiration == null) {
            dateExpiration = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
