package com.example.Impression.controller;

import com.example.Impression.dto.CreateUserRequest;
import com.example.Impression.dto.UserResponse;
import com.example.Impression.entity.User;
import com.example.Impression.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Requête de création d'utilisateur reçue pour: {}", request.getUsername());
        try {
            UserResponse createdUser = userService.createUser(request);
            log.info("Utilisateur créé avec succès: {}", createdUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Requête de récupération de tous les utilisateurs");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Requête de récupération de l'utilisateur avec l'ID: {}", id);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        log.info("Requête de récupération de l'utilisateur avec le nom d'utilisateur: {}", username);
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Requête de récupération de l'utilisateur avec l'email: {}", email);
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("Requête de mise à jour de l'utilisateur avec l'ID: {}", id);
        try {
            UserResponse updatedUser = userService.updateUser(id, request);
            log.info("Utilisateur mis à jour avec succès: {}", updatedUser.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Requête de suppression de l'utilisateur avec l'ID: {}", id);
        try {
            userService.deleteUser(id);
            log.info("Utilisateur supprimé avec succès");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        log.info("Requête de changement de statut de l'utilisateur avec l'ID: {}", id);
        try {
            UserResponse updatedUser = userService.toggleUserStatus(id);
            log.info("Statut de l'utilisateur changé avec succès: {}", updatedUser.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.error("Erreur lors du changement de statut de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable User.UserRole role) {
        log.info("Requête de récupération des utilisateurs avec le rôle: {}", role);
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        log.info("Requête de récupération des utilisateurs actifs");
        List<UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        log.info("Requête de recherche d'utilisateurs avec le mot-clé: {}", keyword);
        List<UserResponse> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search/active")
    public ResponseEntity<List<UserResponse>> searchActiveUsers(@RequestParam String keyword) {
        log.info("Requête de recherche d'utilisateurs actifs avec le mot-clé: {}", keyword);
        List<UserResponse> users = userService.searchActiveUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check de l'API utilisateurs");
        return ResponseEntity.ok("API Utilisateurs opérationnelle");
    }
}