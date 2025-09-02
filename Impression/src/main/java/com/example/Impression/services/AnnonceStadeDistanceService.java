package com.example.Impression.services;

import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.AnnonceStadeDistance;
import com.example.Impression.entities.Stade;
import com.example.Impression.enums.ModeTransport;
import com.example.Impression.repositories.AnnonceStadeDistanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class AnnonceStadeDistanceService {

    @Autowired
    private AnnonceStadeDistanceRepository annonceStadeDistanceRepository;

    @Autowired
    private StadeService stadeService;

    @Autowired(required = false)
    private GoogleDistanceMatrixService googleDistanceMatrixService;

    public void calculerEtSauvegarderDistances(Annonce annonce) {
        // Si les coordonnées ne sont pas renseignées, on ne calcule rien
        log.info("[Distances] Début calcul pour annonce {} (lat={}, lon={})", annonce.getId(), annonce.getLatitude(),
                annonce.getLongitude());
        if (annonce.getLatitude() == null || annonce.getLongitude() == null) {
            log.warn("[Distances] Abandon: coordonnées manquantes pour annonce {}", annonce.getId());
            return;
        }
        // Supprimer les anciennes distances pour cette annonce
        annonceStadeDistanceRepository.deleteByAnnonce(annonce);

        List<Stade> stadesActifs = stadeService.obtenirTousLesStades();
        log.info("[Distances] Stades actifs trouvés: {}", stadesActifs != null ? stadesActifs.size() : 0);

        boolean utiliseGoogle = googleDistanceMatrixService != null && googleDistanceMatrixService.isEnabled();
        List<StadeEtDistance> mesures = new java.util.ArrayList<>();

        if (utiliseGoogle) {
            log.info("[Distances] Google Distance Matrix activé, tentative de calcul via API");
            var resultats = googleDistanceMatrixService.calculerDistancesEtTemps(
                    annonce.getLatitude(), annonce.getLongitude(), stadesActifs);
            for (var r : resultats) {
                mesures.add(new StadeEtDistance(r.getStade(), r.getDistanceKm(), r.getDureeMinutes()));
            }
            log.info("[Distances] Mesures via Google: {}", mesures.size());
        }

        if (!utiliseGoogle || mesures.isEmpty()) {
            // Repli: calcul Haversine + estimation
            var stadesAvecDistances = stadeService.calculerDistancesAvecTousLesStades(
                    annonce.getLatitude(), annonce.getLongitude());
            for (var s : stadesAvecDistances) {
                BigDecimal distance = s.getDistance();
                Integer duree = distance != null ? distance.multiply(BigDecimal.valueOf(1.5)).intValue() : null;
                mesures.add(new StadeEtDistance(s.getStade(), distance, duree));
            }
            log.info("[Distances] Mesures via fallback (Haversine): {}", mesures.size());
        }

        // Trier par distance croissante
        mesures.sort((a, b) -> {
            if (a.distance == null && b.distance == null)
                return 0;
            if (a.distance == null)
                return 1;
            if (b.distance == null)
                return -1;
            return a.distance.compareTo(b.distance);
        });

        boolean premierStade = true;
        int sauvegardees = 0;
        for (var m : mesures) {
            // Ne pas persister des valeurs incomplètes
            if (m.distance == null || m.dureeMinutes == null) {
                log.warn("[Distances] Mesure ignorée (distance/durée null) pour stade={} distance={} duree={}",
                        m.stade != null ? m.stade.getNom() : null, m.distance, m.dureeMinutes);
                continue;
            }
            AnnonceStadeDistance asd = new AnnonceStadeDistance(
                    annonce,
                    m.stade,
                    m.distance,
                    m.dureeMinutes,
                    ModeTransport.VOITURE,
                    premierStade);
            annonceStadeDistanceRepository.save(asd);
            premierStade = false;
            sauvegardees++;
        }
        // S'assurer que les insertions sont poussées en base avant toute lecture
        annonceStadeDistanceRepository.flush();
        log.info("[Distances] Distances sauvegardées pour annonce {}: {}", annonce.getId(), sauvegardees);
    }

    public void mettreAJourDistances(Annonce annonce) {
        // Cette méthode fait la même chose que calculerEtSauvegarderDistances
        // car nous recalculons toutes les distances
        calculerEtSauvegarderDistances(annonce);
    }

    public List<AnnonceStadeDistance> getDistancesParAnnonce(Annonce annonce) {
        List<AnnonceStadeDistance> distances = annonceStadeDistanceRepository.findByAnnonceOrderByDistanceAsc(annonce);
        if ((distances == null || distances.isEmpty()) && annonce.getLatitude() != null
                && annonce.getLongitude() != null) {
            log.warn("[Distances] Aucune distance trouvée en BDD pour annonce {}. Recalcul et persistance...",
                    annonce.getId());
            try {
                calculerEtSauvegarderDistances(annonce);
                annonceStadeDistanceRepository.flush();
                distances = annonceStadeDistanceRepository.findByAnnonceOrderByDistanceAsc(annonce);
                log.info("[Distances] Distances recalculées retrouvées: {}", distances != null ? distances.size() : 0);
            } catch (Exception ex) {
                log.error("[Distances] Échec du recalcul: {}", ex.getMessage(), ex);
            }
        }
        return distances;
    }

    public Optional<AnnonceStadeDistance> getStadeLePlusProche(Annonce annonce) {
        return annonceStadeDistanceRepository.findStadeLePlusProche(annonce);
    }

    public void supprimerDistancesParAnnonce(Annonce annonce) {
        annonceStadeDistanceRepository.deleteByAnnonce(annonce);
    }
}

class StadeEtDistance {
    public final Stade stade;
    public final BigDecimal distance;
    public final Integer dureeMinutes;

    StadeEtDistance(Stade stade, BigDecimal distance, Integer dureeMinutes) {
        this.stade = stade;
        this.distance = distance;
        this.dureeMinutes = dureeMinutes;
    }
}