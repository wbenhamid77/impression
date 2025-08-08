package com.example.Impression.controller;

import com.example.Impression.dto.CreationAdminDTO;
import com.example.Impression.dto.ModificationAdminDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // POST - Créer un admin
    @PostMapping
    public ResponseEntity<?> creerAdmin(@Valid @RequestBody CreationAdminDTO dto) {
        try {
            UtilisateurDTO adminCree = adminService.creerAdmin(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(adminCree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // GET - Récupérer tous les admins
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> obtenirTousLesAdmins() {
        List<UtilisateurDTO> admins = adminService.trouverTousLesAdmins();
        return ResponseEntity.ok(admins);
    }

    // GET - Récupérer un admin par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> obtenirAdminParId(@PathVariable UUID id) {
        Optional<UtilisateurDTO> admin = adminService.trouverAdminParId(id);
        return admin.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Récupérer un admin par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> obtenirAdminParEmail(@PathVariable String email) {
        Optional<UtilisateurDTO> admin = adminService.trouverAdminParEmail(email);
        return admin.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT - Modifier un admin
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierAdmin(@PathVariable UUID id,
            @Valid @RequestBody ModificationAdminDTO dto) {
        try {
            Optional<UtilisateurDTO> adminModifie = adminService.modifierAdmin(id, dto);
            return adminModifie.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    // DELETE - Supprimer un admin (désactivation)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerAdmin(@PathVariable UUID id) {
        boolean supprime = adminService.supprimerAdmin(id);
        if (supprime) {
            return ResponseEntity.ok("Admin supprimé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Activer un compte admin
    @PostMapping("/{id}/activer")
    public ResponseEntity<String> activerCompteAdmin(@PathVariable UUID id) {
        boolean active = adminService.activerCompteAdmin(id);
        if (active) {
            return ResponseEntity.ok("Compte admin activé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Désactiver un compte admin
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<String> desactiverCompteAdmin(@PathVariable UUID id) {
        boolean desactive = adminService.desactiverCompteAdmin(id);
        if (desactive) {
            return ResponseEntity.ok("Compte admin désactivé avec succès");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Compter les admins
    @GetMapping("/statistiques/count")
    public ResponseEntity<Long> compterAdmins() {
        long nombre = adminService.compterAdmins();
        return ResponseEntity.ok(nombre);
    }
}