package com.example.Impression.config;

import com.example.Impression.entities.Stade;
import com.example.Impression.repositories.StadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class StadeDataLoader implements CommandLineRunner {

    @Autowired
    private StadeRepository stadeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si les stades sont déjà chargés
        if (stadeRepository.count() == 0) {
            chargerStadesCAN2025();
        }
    }

    private void chargerStadesCAN2025() {
        List<Stade> stades = Arrays.asList(

                // Stade Mohammed V - Casablanca
                new Stade(
                        "Stade Mohammed V",
                        "Casablanca",
                        "Boulevard Mohamed Zerktouni, Casablanca",
                        new BigDecimal("33.5292"), // Latitude
                        new BigDecimal("-7.4612"), // Longitude
                        45000),

                // Stade Prince Moulay Abdellah - Rabat
                new Stade(
                        "Stade Prince Moulay Abdellah",
                        "Rabat",
                        "Avenue Annakhil, Hay Riad, Rabat",
                        new BigDecimal("33.9556"), // Latitude
                        new BigDecimal("-6.8341"), // Longitude
                        52000),

                // Stade Adrar - Agadir
                new Stade(
                        "Stade Adrar",
                        "Agadir",
                        "Boulevard Mohamed V, Agadir",
                        new BigDecimal("30.3928"), // Latitude
                        new BigDecimal("-9.5378"), // Longitude
                        45000),

                // Stade de Fès
                new Stade(
                        "Stade de Fès",
                        "Fès",
                        "Route de Sefrou, Fès",
                        new BigDecimal("34.0181"), // Latitude
                        new BigDecimal("-5.0078"), // Longitude
                        45000),

                // Stade Ibn Batouta - Tanger
                new Stade(
                        "Stade Ibn Batouta",
                        "Tanger",
                        "Route de Tétouan, Tanger",
                        new BigDecimal("35.7595"), // Latitude
                        new BigDecimal("-5.8134"), // Longitude
                        65000),

                // Stade de Marrakech
                new Stade(
                        "Stade de Marrakech",
                        "Marrakech",
                        "Avenue Mohammed VI, Marrakech",
                        new BigDecimal("31.6063"), // Latitude
                        new BigDecimal("-8.0417"), // Longitude
                        45000));

        // Ajouter des descriptions
        stades.get(0).setDescription("Stade principal de Casablanca, rénové pour la CAN 2025");
        stades.get(1).setDescription("Stade national du Maroc, siège de la finale de la CAN 2025");
        stades.get(2).setDescription("Stade moderne d'Agadir avec vue sur l'océan Atlantique");
        stades.get(3).setDescription("Stade historique de Fès, cité impériale");
        stades.get(4).setDescription("Plus grand stade du Maroc, porte d'entrée de l'Afrique");
        stades.get(5).setDescription("Stade de la ville rouge, au pied de l'Atlas");

        // Sauvegarder tous les stades
        stadeRepository.saveAll(stades);

        System.out.println("✅ " + stades.size() + " stades de la CAN 2025 chargés avec succès !");
    }
}