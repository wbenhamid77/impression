package com.example.Impression.services;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    /**
     * Lit une image depuis le disque et la convertit en byte[]
     */
    public byte[] lireImageDepuisDisque(String cheminImage) {
        try {
            // Utiliser File pour gérer les chemins Windows
            File file = new File(cheminImage);

            if (file.exists() && file.isFile()) {
                return Files.readAllBytes(file.toPath());
            } else {
                System.err.println("Fichier non trouvé: " + cheminImage);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier: " + cheminImage + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Lit une liste d'images depuis le disque et les convertit en byte[]
     */
    public List<byte[]> lireImagesDepuisDisque(List<String> cheminsImages) {
        List<byte[]> imagesBytes = new ArrayList<>();

        if (cheminsImages != null) {
            for (String chemin : cheminsImages) {
                byte[] imageBytes = lireImageDepuisDisque(chemin);
                if (imageBytes != null) {
                    imagesBytes.add(imageBytes);
                }
            }
        }

        return imagesBytes;
    }

    /**
     * Vérifie si un fichier existe
     */
    public boolean fichierExiste(String chemin) {
        return Files.exists(Paths.get(chemin));
    }

    /**
     * Obtient la taille d'un fichier
     */
    public long getTailleFichier(String chemin) {
        try {
            return Files.size(Paths.get(chemin));
        } catch (IOException e) {
            return -1;
        }
    }
}