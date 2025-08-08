package com.example.Impression.services;

import com.example.Impression.dto.CreationLocateurDTO;
import com.example.Impression.dto.ModificationLocateurDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.entities.Locateur;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.repositories.LocateurRepository;
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
public class LocateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LocateurRepository locateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Créer un locateur
    public UtilisateurDTO creerLocateur(CreationLocateurDTO dto) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Encoder le mot de passe
        String motDePasseHash = passwordEncoder.encode(dto.getMotDePasse());

        // Créer le locateur avec toutes les informations
        Locateur locateur = new Locateur(
                dto.getNom(),
                dto.getPrenom(),
                dto.getEmail(),
                motDePasseHash,
                dto.getTelephone(),
                dto.getDescription(),
                dto.getNumeroSiret(),
                dto.getRaisonSociale(),
                dto.getAdresseProfessionnelle());

        locateur.setPhotoProfil(dto.getPhotoProfil());

        // Sauvegarder
        Locateur locateurSauvegarde = locateurRepository.save(locateur);

        return convertirEnDTO(locateurSauvegarde);
    }

    // Récupérer tous les locateurs
    public List<UtilisateurDTO> trouverTousLesLocateurs() {
        return locateurRepository.findAll()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer un locateur par ID
    public Optional<UtilisateurDTO> trouverLocateurParId(UUID id) {
        return locateurRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    // Récupérer un locateur par email
    public Optional<UtilisateurDTO> trouverLocateurParEmail(String email) {
        return locateurRepository.findAll()
                .stream()
                .filter(locateur -> locateur.getEmail().equals(email) && locateur.isEstActif())
                .findFirst()
                .map(this::convertirEnDTO);
    }

    // Modifier un locateur
    public Optional<UtilisateurDTO> modifierLocateur(UUID id, ModificationLocateurDTO dto) {
        return locateurRepository.findById(id)
                .map(locateur -> {
                    if (dto.getNom() != null)
                        locateur.setNom(dto.getNom());
                    if (dto.getPrenom() != null)
                        locateur.setPrenom(dto.getPrenom());
                    if (dto.getEmail() != null) {
                        // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                        Optional<Utilisateur> utilisateurExistant = utilisateurRepository.findByEmail(dto.getEmail());
                        if (utilisateurExistant.isPresent() && !utilisateurExistant.get().getId().equals(id)) {
                            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                        }
                        locateur.setEmail(dto.getEmail());
                    }
                    if (dto.getTelephone() != null)
                        locateur.setTelephone(dto.getTelephone());
                    if (dto.getPhotoProfil() != null)
                        locateur.setPhotoProfil(dto.getPhotoProfil());
                    if (dto.getDescription() != null)
                        locateur.setDescription(dto.getDescription());

                    locateur.setDateModification(LocalDateTime.now());
                    return convertirEnDTO(locateurRepository.save(locateur));
                });
    }

    // Supprimer un locateur (désactivation)
    public boolean supprimerLocateur(UUID id) {
        return locateurRepository.findById(id)
                .map(locateur -> {
                    locateur.setEstActif(false);
                    locateur.setDateModification(LocalDateTime.now());
                    locateurRepository.save(locateur);
                    return true;
                })
                .orElse(false);
    }

    // Activer un compte locateur
    public boolean activerCompteLocateur(UUID id) {
        return locateurRepository.findById(id)
                .map(locateur -> {
                    locateur.setEstActif(true);
                    locateur.setDateModification(LocalDateTime.now());
                    locateurRepository.save(locateur);
                    return true;
                })
                .orElse(false);
    }

    // Désactiver un compte locateur
    public boolean desactiverCompteLocateur(UUID id) {
        return locateurRepository.findById(id)
                .map(locateur -> {
                    locateur.setEstActif(false);
                    locateur.setDateModification(LocalDateTime.now());
                    locateurRepository.save(locateur);
                    return true;
                })
                .orElse(false);
    }

    // Compter les locateurs
    public long compterLocateurs() {
        return locateurRepository.count();
    }

    // Méthode utilitaire pour convertir en DTO
    private UtilisateurDTO convertirEnDTO(Locateur locateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(locateur.getId());
        dto.setRole(locateur.getRole());
        dto.setNom(locateur.getNom());
        dto.setPrenom(locateur.getPrenom());
        dto.setEmail(locateur.getEmail());
        dto.setTelephone(locateur.getTelephone());
        dto.setStatutKyc(locateur.getStatutKyc());
        dto.setDateInscription(locateur.getDateInscription());
        dto.setDerniereConnexion(locateur.getDerniereConnexion());
        dto.setEstActif(locateur.isEstActif());
        dto.setPhotoProfil(locateur.getPhotoProfil());
        dto.setDateModification(locateur.getDateModification());
        return dto;
    }
}