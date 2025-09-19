package com.example.Impression.entities;

import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relation avec l'annonce
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    // Relation avec le locataire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id", nullable = false)
    private Locataire locataire;

    // Dates de réservation
    @Column(nullable = false)
    private LocalDate dateArrivee;

    @Column(nullable = false)
    private LocalDate dateDepart;

    // Calcul automatique du nombre de nuits
    @Column(nullable = false)
    private int nombreNuits;

    // Prix et frais
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParNuit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal fraisService = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal fraisNettoyage = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal fraisDepot = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    // Statut et mode de paiement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement modePaiement = ModePaiement.PAIEMENT_SUR_PLACE;

    // Message optionnel au propriétaire
    @Column(columnDefinition = "TEXT")
    private String messageProprietaire;

    // Informations de paiement
    @Column
    private String numeroTransaction;

    @Column
    private LocalDateTime datePaiement;

    // Dates de création et modification
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column
    private LocalDateTime dateConfirmation;

    @Column
    private LocalDateTime dateAnnulation;

    // Raison de l'annulation (si applicable)
    @Column(columnDefinition = "TEXT")
    private String raisonAnnulation;

    // Nombre de voyageurs
    @Column(nullable = false)
    private int nombreVoyageurs;

    // Constructeur principal
    public Reservation(Annonce annonce, Locataire locataire, LocalDate dateArrivee,
            LocalDate dateDepart, int nombreVoyageurs) {
        this.annonce = annonce;
        this.locataire = locataire;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.nombreVoyageurs = nombreVoyageurs;
        this.prixParNuit = annonce.getPrixParNuit();
        calculerNuitsEtPrix();
    }

    // Méthodes métier
    public void calculerNuitsEtPrix() {
        if (dateArrivee != null && dateDepart != null) {
            this.nombreNuits = (int) java.time.temporal.ChronoUnit.DAYS.between(dateArrivee, dateDepart);
            if (nombreNuits <= 0) {
                throw new IllegalArgumentException("La date de départ doit être postérieure à la date d'arrivée");
            }

            // Calcul du prix de base
            this.prixTotal = prixParNuit.multiply(BigDecimal.valueOf(nombreNuits));

            // Calcul du montant total avec frais
            this.montantTotal = prixTotal.add(fraisService).add(fraisNettoyage).add(fraisDepot);
        }
    }

    public void confirmer() {
        this.statut = StatutReservation.CONFIRMEE;
        this.dateConfirmation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public void annuler(String raison) {
        this.statut = StatutReservation.ANNULEE;
        this.raisonAnnulation = raison;
        this.dateAnnulation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public void terminer() {
        this.statut = StatutReservation.TERMINEE;
        this.dateModification = LocalDateTime.now();
    }

    public void mettreEnCours() {
        this.statut = StatutReservation.EN_COURS;
        this.dateModification = LocalDateTime.now();
    }

    public boolean peutEtreAnnulee() {
        // Logique pour déterminer si la réservation peut être annulée
        // Permet l'annulation tant que la réservation n'est pas déjà annulée ou terminée
        // et que la date d'arrivée n'est pas encore passée
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime arrivee = dateArrivee.atStartOfDay();
        return maintenant.isBefore(arrivee) &&
                statut != StatutReservation.ANNULEE &&
                statut != StatutReservation.TERMINEE;
    }

    public boolean estEnConflit(LocalDate autreArrivee, LocalDate autreDepart) {
        // Vérifier s'il y a un conflit de dates avec une autre réservation
        return !(dateDepart.isBefore(autreArrivee) || autreDepart.isBefore(dateArrivee));
    }

    public boolean estActive() {
        return statut == StatutReservation.CONFIRMEE ||
                statut == StatutReservation.EN_COURS;
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (statut == null) {
            statut = StatutReservation.EN_ATTENTE;
        }
        if (modePaiement == null) {
            modePaiement = ModePaiement.PAIEMENT_SUR_PLACE;
        }
        calculerNuitsEtPrix();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        calculerNuitsEtPrix();
    }
}