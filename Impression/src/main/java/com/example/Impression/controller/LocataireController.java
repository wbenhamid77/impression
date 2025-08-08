package com.example.Impression.controller;

import com.example.Impression.dto.CreationLocataireDTO;
import com.example.Impression.dto.ModificationLocataireDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.dto.AnnonceDTO;
import com.example.Impression.services.LocataireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/locataires")
@CrossOrigin(origins = "*")
public class LocataireController {

    @Autowired
    private LocataireService locataireService;

    // POST - Créer un locataire
    @PostMapping
    public ResponseEntity<?> creerLocataire(@Valid @RequestBody CreationLocataireDTO dto) {
        try {
            UtilisateurDTO locataireCree = locataireService.creerLocataire(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(locataireCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer tous les locataires
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> obtenirTousLesLocataires() {
        List<UtilisateurDTO> locataires = locataireService.trouverTousLesLocataires();
        return ResponseEntity.ok(locataires);
    }

    // GET - Récupérer un locataire par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirLocataireParId(@PathVariable UUID id) {
        Optional<UtilisateurDTO> locataire = locataireService.trouverLocataireParId(id);
        return locataire.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer un locataire par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirLocataireParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> locataire = locataireService.trouverLocataireParEmail(email);
        return locataire.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Modifier un locataire
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierLocataire(@PathVariable UUID id,
            @Valid @RequestBody ModificationLocataireDTO dto) {
        try {
            Optional<UtilisateurDTO> locataireModifie = locataireService.modifierLocataire(id, dto);
            return locataireModifie.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Supprimer un locataire (désactivation)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerLocataire(@PathVariable UUID id) {
        boolean supprime = locataireService.supprimerLocataire(id);
        if (supprime) {
            return ResponseEntity.ok("Locataire supprimé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Activer un compte locataire
    @PostMapping("/{id}/activer")
    public ResponseEntity<String> activerCompteLocataire(@PathVariable UUID id) {
        boolean active = locataireService.activerCompteLocataire(id);
        if (active) {
            return ResponseEntity.ok("Compte locataire activé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Désactiver un compte locataire
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverCompteLocataire(@PathVariable UUID id) {
        boolean desactive = locataireService.desactiverCompteLocataire(id);
        if (desactive) {
            return ResponseEntity.ok("Compte locataire désactivé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Compter les locataires
    @GetMapping("/statistiques/count")
    public ResponseEntity<Long> compterLocataires() {
        long nombre = locataireService.compterLocataires();
        return ResponseEntity.ok(nombre);
    }

    // ========== ENDPOINTS POUR LES FAVORIS ==========

    // POST - Ajouter une annonce aux favoris
    @PostMapping("/{locataireId}/favoris/{annonceId}")
    public ResponseEntity<String> ajouterFavori(
            @PathVariable UUID locataireId,
            @PathVariable UUID annonceId) {
        try {
            boolean ajoute = locataireService.ajouterFavori(locataireId, annonceId);
            if (ajoute) {
                return ResponseEntity.ok("Annonce ajoutée aux favoris avec succès");
            } else {
                return ResponseEntity.ok("L'annonce était déjà dans les favoris");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Retirer une annonce des favoris
    @DeleteMapping("/{locataireId}/favoris/{annonceId}")
    public ResponseEntity<String> retirerFavori(
            @PathVariable UUID locataireId,
            @PathVariable UUID annonceId) {
        try {
            boolean retire = locataireService.retirerFavori(locataireId, annonceId);
            if (retire) {
                return ResponseEntity.ok("Annonce retirée des favoris avec succès");
            } else {
                return ResponseEntity.ok("L'annonce n'était pas dans les favoris");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer toutes les annonces favorites d'un locataire
    @GetMapping("/{locataireId}/favoris")
    public ResponseEntity<List<AnnonceDTO>> getAnnoncesFavorites(@PathVariable UUID locataireId) {
        try {
            List<AnnonceDTO> favoris = locataireService.getAnnoncesFavorites(locataireId);
            return ResponseEntity.ok(favoris);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET - Vérifier si une annonce est dans les favoris
    @GetMapping("/{locataireId}/favoris/{annonceId}/check")
    public ResponseEntity<Boolean> estFavori(
            @PathVariable UUID locataireId,
            @PathVariable UUID annonceId) {
        try {
            boolean estFavori = locataireService.estFavori(locataireId, annonceId);
            return ResponseEntity.ok(estFavori);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}