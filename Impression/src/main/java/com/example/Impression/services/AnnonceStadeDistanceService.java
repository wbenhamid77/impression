package com.example.Impression.services;

import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.AnnonceStadeDistance;
import com.example.Impression.entities.Stade;
import com.example.Impression.enums.ModeTransport;
import com.example.Impression.repositories.AnnonceStadeDistanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnnonceStadeDistanceService {

    @Autowired
    private AnnonceStadeDistanceRepository annonceStadeDistanceRepository;

    @Autowired
    private StadeService stadeService;

    public void calculerEtSauvegarderDistances(Annonce annonce) {
        // Supprimer les anciennes distances pour cette annonce
        annonceStadeDistanceRepository.deleteByAnnonce(annonce);

        List<StadeService.StadeAvecDistance> stadesAvecDistances = stadeService.calculerDistancesAvecTousLesStades(
                annonce.getLatitude(), annonce.getLongitude());

        boolean premierStade = true;
        for (StadeService.StadeAvecDistance stadeAvecDistance : stadesAvecDistances) {
            Stade stade = stadeAvecDistance.getStade();
            BigDecimal distance = stadeAvecDistance.getDistance();

            // Estimation simple du temps de trajet (ex: 1 min par km en voiture)
            Integer tempsTrajetMinutes = distance != null ? distance.multiply(BigDecimal.valueOf(1.5)).intValue()
                    : null;

            AnnonceStadeDistance asd = new AnnonceStadeDistance(
                    annonce,
                    stade,
                    distance,
                    tempsTrajetMinutes,
                    ModeTransport.VOITURE, // Mode de transport par défaut
                    premierStade // Le premier est le plus proche
            );
            annonceStadeDistanceRepository.save(asd);
            premierStade = false; // Après le premier, les autres ne sont plus "le plus proche"
        }
    }

    public void mettreAJourDistances(Annonce annonce) {
        // Cette méthode fait la même chose que calculerEtSauvegarderDistances
        // car nous recalculons toutes les distances
        calculerEtSauvegarderDistances(annonce);
    }

    public List<AnnonceStadeDistance> getDistancesParAnnonce(Annonce annonce) {
        return annonceStadeDistanceRepository.findByAnnonceOrderByDistanceAsc(annonce);
    }

    public Optional<AnnonceStadeDistance> getStadeLePlusProche(Annonce annonce) {
        return annonceStadeDistanceRepository.findStadeLePlusProche(annonce);
    }

    public void supprimerDistancesParAnnonce(Annonce annonce) {
        annonceStadeDistanceRepository.deleteByAnnonce(annonce);
    }
}