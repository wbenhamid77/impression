package com.example.Impression.service;

import com.example.Impression.dto.CreateUserRequest;
import com.example.Impression.dto.UserResponse;
import com.example.Impression.entity.User;
import com.example.Impression.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getUsername());

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur '" + request.getUsername() + "' existe déjà");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("L'email '" + request.getEmail() + "' existe déjà");
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", savedUser.getUsername());

        return UserResponse.fromUser(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        log.info("Récupération de l'utilisateur avec l'ID: {}", id);
        return userRepository.findById(id)
                .map(UserResponse::fromUser);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByUsername(String username) {
        log.info("Récupération de l'utilisateur avec le nom d'utilisateur: {}", username);
        return userRepository.findByUsername(username)
                .map(UserResponse::fromUser);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {
        log.info("Récupération de l'utilisateur avec l'email: {}", email);
        return userRepository.findByEmail(email)
                .map(UserResponse::fromUser);
    }

    public UserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("Mise à jour de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau nom d'utilisateur existe déjà (sauf pour l'utilisateur
        // actuel)
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur '" + request.getUsername() + "' existe déjà");
        }

        // Vérifier si le nouvel email existe déjà (sauf pour l'utilisateur actuel)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("L'email '" + request.getEmail() + "' existe déjà");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès: {}", updatedUser.getUsername());

        return UserResponse.fromUser(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("Utilisateur supprimé avec succès");
    }

    public UserResponse toggleUserStatus(Long id) {
        log.info("Changement du statut de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        user.setEnabled(!user.isEnabled());
        User updatedUser = userRepository.save(user);

        log.info("Statut de l'utilisateur {} changé à: {}", updatedUser.getUsername(), updatedUser.isEnabled());

        return UserResponse.fromUser(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        log.info("Récupération des utilisateurs avec le rôle: {}", role);
        return userRepository.findByRole(role)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.info("Récupération des utilisateurs actifs");
        return userRepository.findByEnabled(true)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        log.info("Recherche d'utilisateurs avec le mot-clé: {}", keyword);
        return userRepository.findByKeyword(keyword)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchActiveUsers(String keyword) {
        log.info("Recherche d'utilisateurs actifs avec le mot-clé: {}", keyword);
        return userRepository.findActiveUsersByKeyword(keyword)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }
}