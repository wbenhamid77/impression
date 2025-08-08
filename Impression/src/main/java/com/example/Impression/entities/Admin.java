package com.example.Impression.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends Utilisateur {

    @Column(length = 50)
    private String matricule;

    @Column(length = 100)
    private String departement;

    public Admin(String nom, String prenom, String email, String motDePasseHash) {
        super();
        this.setNom(nom);
        this.setPrenom(prenom);
        this.setEmail(email);
        this.setMotDePasseHash(motDePasseHash);
        this.setRole(com.example.Impression.enums.Role.ADMIN);
    }
}