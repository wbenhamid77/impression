package com.example.Impression.entities;

import com.example.Impression.enums.Role;
import com.example.Impression.enums.StatutKYC;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutKYC statutKyc = StatutKYC.NON_VÉRIFIÉ;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateInscription;

    @Column
    private LocalDateTime derniereConnexion;

    @Column(nullable = false)
    private boolean estActif = true;

    @Column(name = "email_verifie", nullable = false)
    private boolean emailVerifie = false;

    @Column(length = 500)
    private String photoProfil;

    @UpdateTimestamp
    @Column
    private LocalDateTime dateModification;
}