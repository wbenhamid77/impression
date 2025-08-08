package com.example.Impression.services;

import com.example.Impression.dto.CreationAdminDTO;
import com.example.Impression.dto.ModificationAdminDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.entities.Admin;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.repositories.AdminRepository;
import com.example.Impression.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Créer un admin
    public UtilisateurDTO creerAdmin(CreationAdminDTO dto) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Encoder le mot de passe
        String motDePasseHash = passwordEncoder.encode(dto.getMotDePasse());

        // Créer l'admin
        Admin admin = new Admin(dto.getNom(), dto.getPrenom(), dto.getEmail(), motDePasseHash);
        admin.setTelephone(dto.getTelephone());
        admin.setPhotoProfil(dto.getPhotoProfil());
        admin.setMatricule(dto.getMatricule());
        admin.setDepartement(dto.getDepartement());

        // Sauvegarder
        Admin adminSauvegarde = adminRepository.save(admin);

        return convertirEnDTO(adminSauvegarde);
    }

    // Récupérer tous les admins
    public List<UtilisateurDTO> trouverTousLesAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer un admin par ID
    public Optional<UtilisateurDTO> trouverAdminParId(UUID id) {
        return adminRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    // Récupérer un admin par email
    public Optional<UtilisateurDTO> trouverAdminParEmail(String email) {
        return adminRepository.findAll()
                .stream()
                .filter(admin -> admin.getEmail().equals(email) && admin.isEstActif())
                .findFirst()
                .map(this::convertirEnDTO);
    }

    // Modifier un admin
    public Optional<UtilisateurDTO> modifierAdmin(UUID id, ModificationAdminDTO dto) {
        return adminRepository.findById(id)
                .map(admin -> {
                    if (dto.getNom() != null)
                        admin.setNom(dto.getNom());
                    if (dto.getPrenom() != null)
                        admin.setPrenom(dto.getPrenom());
                    if (dto.getEmail() != null) {
                        // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                        Optional<Utilisateur> utilisateurExistant = utilisateurRepository.findByEmail(dto.getEmail());
                        if (utilisateurExistant.isPresent() && !utilisateurExistant.get().getId().equals(id)) {
                            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                        }
                        admin.setEmail(dto.getEmail());
                    }
                    if (dto.getTelephone() != null)
                        admin.setTelephone(dto.getTelephone());
                    if (dto.getPhotoProfil() != null)
                        admin.setPhotoProfil(dto.getPhotoProfil());
                    if (dto.getMatricule() != null)
                        admin.setMatricule(dto.getMatricule());
                    if (dto.getDepartement() != null)
                        admin.setDepartement(dto.getDepartement());

                    admin.setDateModification(LocalDateTime.now());
                    return convertirEnDTO(adminRepository.save(admin));
                });
    }

    // Supprimer un admin (désactivation)
    public boolean supprimerAdmin(UUID id) {
        return adminRepository.findById(id)
                .map(admin -> {
                    admin.setEstActif(false);
                    admin.setDateModification(LocalDateTime.now());
                    adminRepository.save(admin);
                    return true;
                })
                .orElse(false);
    }

    // Activer un compte admin
    public boolean activerCompteAdmin(UUID id) {
        return adminRepository.findById(id)
                .map(admin -> {
                    admin.setEstActif(true);
                    admin.setDateModification(LocalDateTime.now());
                    adminRepository.save(admin);
                    return true;
                })
                .orElse(false);
    }

    // Désactiver un compte admin
    public boolean desactiverCompteAdmin(UUID id) {
        return adminRepository.findById(id)
                .map(admin -> {
                    admin.setEstActif(false);
                    admin.setDateModification(LocalDateTime.now());
                    adminRepository.save(admin);
                    return true;
                })
                .orElse(false);
    }

    // Compter les admins
    public long compterAdmins() {
        return adminRepository.count();
    }

    // Méthode utilitaire pour convertir en DTO
    private UtilisateurDTO convertirEnDTO(Admin admin) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(admin.getId());
        dto.setRole(admin.getRole());
        dto.setNom(admin.getNom());
        dto.setPrenom(admin.getPrenom());
        dto.setEmail(admin.getEmail());
        dto.setTelephone(admin.getTelephone());
        dto.setStatutKyc(admin.getStatutKyc());
        dto.setDateInscription(admin.getDateInscription());
        dto.setDerniereConnexion(admin.getDerniereConnexion());
        dto.setEstActif(admin.isEstActif());
        dto.setPhotoProfil(admin.getPhotoProfil());
        dto.setDateModification(admin.getDateModification());
        return dto;
    }
}