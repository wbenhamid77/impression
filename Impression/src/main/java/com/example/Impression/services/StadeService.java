package com.example.Impression.services;

import com.example.Impression.entities.Stade;
import com.example.Impression.repositories.StadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Service
@Transactional
public class StadeService {

    @Autowired
    private StadeRepository stadeRepository;

    /**
     * Calcule la distance entre deux points géographiques en utilisant la formule
     * de Haversine
     * 
     * @param lat1 Latitude du premier point
     * @param lon1 Longitude du premier point
     * @param lat2 Latitude du deuxième point
     * @param lon2 Longitude du deuxième point
     * @return Distance en kilomètres
     */
    public BigDecimal calculerDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        final double RAYON_TERRE_KM = 6371.0;

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = RAYON_TERRE_KM * c;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Trouve le stade le plus proche d'une position donnée
     * 
     * @param latitude  Latitude de la position
     * @param longitude Longitude de la position
     * @return Le stade le plus proche avec sa distance
     */
    public StadeAvecDistance trouverStadeLePlusProche(BigDecimal latitude, BigDecimal longitude) {
        List<Stade> stades = stadeRepository.findByEstActifTrue();

        if (stades.isEmpty()) {
            return null;
        }

        Stade stadeLePlusProche = null;
        BigDecimal distanceMin = null;

        for (Stade stade : stades) {
            BigDecimal distance = calculerDistance(latitude, longitude, stade.getLatitude(), stade.getLongitude());
            if (distance != null && (distanceMin == null || distance.compareTo(distanceMin) < 0)) {
                distanceMin = distance;
                stadeLePlusProche = stade;
            }
        }

        return stadeLePlusProche != null ? new StadeAvecDistance(stadeLePlusProche, distanceMin) : null;
    }

    /**
     * Calcule les distances avec tous les stades actifs
     * 
     * @param latitude  Latitude de la position
     * @param longitude Longitude de la position
     * @return Liste de tous les stades avec leurs distances, triée par distance
     */
    public List<StadeAvecDistance> calculerDistancesAvecTousLesStades(BigDecimal latitude, BigDecimal longitude) {
        List<Stade> stades = stadeRepository.findByEstActifTrue();
        if (stades == null || stades.isEmpty()) {
            // Fallback: utiliser tous les stades si aucun n'est marqué actif
            stades = stadeRepository.findAll();
        }
        List<StadeAvecDistance> stadesAvecDistances = new ArrayList<>();

        for (Stade stade : stades) {
            BigDecimal distance = calculerDistance(latitude, longitude, stade.getLatitude(), stade.getLongitude());
            if (distance != null) {
                stadesAvecDistances.add(new StadeAvecDistance(stade, distance));
            }
        }

        // Trier par distance croissante
        stadesAvecDistances.sort((s1, s2) -> s1.getDistance().compareTo(s2.getDistance()));

        return stadesAvecDistances;
    }

    /**
     * Récupère tous les stades actifs
     */
    public List<Stade> obtenirTousLesStades() {
        return stadeRepository.findByEstActifTrue();
    }

    /**
     * Récupère un stade par ID
     */
    public Optional<Stade> obtenirStadeParId(UUID id) {
        return stadeRepository.findById(id);
    }

    /**
     * Récupère un stade par nom
     */
    public Optional<Stade> obtenirStadeParNom(String nom) {
        return stadeRepository.findByNomAndEstActifTrue(nom);
    }

    /**
     * Sauvegarde un stade
     */
    public Stade sauvegarderStade(Stade stade) {
        return stadeRepository.save(stade);
    }

    /**
     * Classe interne pour représenter un stade avec sa distance
     */
    public static class StadeAvecDistance {
        private final Stade stade;
        private final BigDecimal distance;

        public StadeAvecDistance(Stade stade, BigDecimal distance) {
            this.stade = stade;
            this.distance = distance;
        }

        public Stade getStade() {
            return stade;
        }

        public BigDecimal getDistance() {
            return distance;
        }
    }
}