package com.example.Impression.controller;

import com.example.Impression.dto.AnnonceDTO;
import com.example.Impression.dto.CreerAnnonceDTO;
import com.example.Impression.enums.TypeMaison;
import com.example.Impression.exception.AnnonceException;
import com.example.Impression.services.AnnonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "*")
public class AnnonceController {

    @Autowired
    private AnnonceService annonceService;

    // GET /api/annonces - Récupérer toutes les annonces actives
    @GetMapping
    public ResponseEntity<List<AnnonceDTO>> getAllAnnonces() {
        try {
            List<AnnonceDTO> annonces = annonceService.getAllAnnoncesActives();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/{id} - Récupérer une annonce par ID
    @GetMapping("/{id}")
    public ResponseEntity<AnnonceDTO> getAnnonceById(@PathVariable UUID id) {
        try {
            Optional<AnnonceDTO> annonce = annonceService.getAnnonceById(id);
            return annonce.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/annonces - Créer une nouvelle annonce
    @PostMapping
    public ResponseEntity<AnnonceDTO> creerAnnonce(@RequestBody CreerAnnonceDTO creerAnnonceDTO) {
        try {
            AnnonceDTO nouvelleAnnonce = annonceService.creerAnnonce(creerAnnonceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleAnnonce);
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/{id}/distances - Récupérer distances et stades pour une
    // annonce
    @GetMapping("/{id}/distances")
    public ResponseEntity<List<com.example.Impression.dto.AnnonceStadeDistanceDTO>> getDistancesPourAnnonce(
            @PathVariable UUID id) {
        try {
            List<com.example.Impression.dto.AnnonceStadeDistanceDTO> distances = annonceService.getDistancesAnnonce(id);
            return ResponseEntity.ok(distances);
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/annonces/{id} - Mettre à jour une annonce
    @PutMapping("/{id}")
    public ResponseEntity<AnnonceDTO> mettreAJourAnnonce(
            @PathVariable UUID id,
            @RequestBody CreerAnnonceDTO creerAnnonceDTO,
            @RequestParam UUID locateurId) {
        try {
            AnnonceDTO annonceMiseAJour = annonceService.mettreAJourAnnonce(id, creerAnnonceDTO, locateurId);
            return ResponseEntity.ok(annonceMiseAJour);
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AnnonceException.AnnonceUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/annonces/{id} - Supprimer une annonce
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAnnonce(
            @PathVariable UUID id,
            @RequestParam UUID locateurId) {
        try {
            boolean supprimee = annonceService.supprimerAnnonce(id, locateurId);
            return supprimee ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AnnonceException.AnnonceUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/locateur/{locateurId} - Récupérer les annonces d'un
    // locateur
    @GetMapping("/locateur/{locateurId}")
    public ResponseEntity<List<AnnonceDTO>> getAnnoncesByLocateur(@PathVariable UUID locateurId) {
        try {
            List<AnnonceDTO> annonces = annonceService.getAnnoncesByLocateur(locateurId);
            return ResponseEntity.ok(annonces);
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/recherche - Recherche d'annonces avec critères
    @GetMapping("/recherche")
    public ResponseEntity<List<AnnonceDTO>> rechercherAnnonces(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) TypeMaison typeMaison,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer capaciteMin,
            @RequestParam(required = false) Double noteMin) {
        try {
            List<AnnonceDTO> annonces = annonceService.rechercherAnnonces(ville, typeMaison, prixMax, capaciteMin,
                    noteMin);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/recherche/stade - Recherche par stade
    @GetMapping("/recherche/stade")
    public ResponseEntity<List<AnnonceDTO>> rechercherParStade(@RequestParam String stade) {
        try {
            List<AnnonceDTO> annonces = annonceService.rechercherParStade(stade);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/recherche/rayon - Recherche par rayon géographique
    @GetMapping("/recherche/rayon")
    public ResponseEntity<List<AnnonceDTO>> rechercherParRayon(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam double rayonKm) {
        try {
            List<AnnonceDTO> annonces = annonceService.rechercherParRayonDTO(latitude, longitude, rayonKm);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/recherche/zone - Recherche par zone géographique
    @GetMapping("/recherche/zone")
    public ResponseEntity<List<AnnonceDTO>> rechercherParZone(
            @RequestParam BigDecimal latMin,
            @RequestParam BigDecimal latMax,
            @RequestParam BigDecimal lonMin,
            @RequestParam BigDecimal lonMax) {
        try {
            List<AnnonceDTO> annonces = annonceService.rechercherParZoneDTO(latMin, latMax, lonMin, lonMax);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/annonces/recherche/proximite - Recherche par proximité
    @GetMapping("/recherche/proximite")
    public ResponseEntity<List<AnnonceDTO>> rechercherParProximite(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(required = false) Double distanceMax) {
        try {
            List<AnnonceDTO> annonces;
            if (distanceMax != null) {
                annonces = annonceService.rechercherParProximiteAvecLimiteDTO(latitude, longitude, distanceMax);
            } else {
                annonces = annonceService.rechercherParProximiteDTO(latitude, longitude);
            }
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PATCH /api/annonces/{id}/desactiver - Désactiver une annonce
    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverAnnonce(
            @PathVariable UUID id,
            @RequestParam UUID locateurId) {
        try {
            boolean desactivee = annonceService.desactiverAnnonce(id, locateurId);
            return desactivee ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (AnnonceException.AnnonceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AnnonceException.AnnonceUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}