package com.example.Impression.services;

import com.example.Impression.dto.CreationUtilisateurDTO;
import com.example.Impression.dto.ModificationUtilisateurDTO;
import com.example.Impression.dto.ModificationProfilDTO;
import com.example.Impression.dto.ReponseModificationProfilDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.entities.Admin;
import com.example.Impression.entities.Locateur;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.enums.Role;
import com.example.Impression.repositories.AdminRepository;
import com.example.Impression.repositories.LocateurRepository;
import com.example.Impression.repositories.LocataireRepository;
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
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LocateurRepository locateurRepository;

    @Autowired
    private LocataireRepository locataireRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Méthodes de création
    public UtilisateurDTO creerUtilisateur(CreationUtilisateurDTO dto) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Encoder le mot de passe
        String motDePasseHash = passwordEncoder.encode(dto.getMotDePasse());

        Utilisateur utilisateur = null;

        switch (dto.getRole()) {
            case ADMIN:
                Admin admin = new Admin(dto.getNom(), dto.getPrenom(), dto.getEmail(), motDePasseHash);
                admin.setTelephone(dto.getTelephone());
                admin.setMatricule(dto.getMatricule());
                admin.setDepartement(dto.getDepartement());
                utilisateur = adminRepository.save(admin);
                break;

            case LOCATEUR:
                Locateur locateur = new Locateur(dto.getNom(), dto.getPrenom(), dto.getEmail(), motDePasseHash);
                locateur.setTelephone(dto.getTelephone());
                locateur.setDescription(dto.getDescription());
                utilisateur = locateurRepository.save(locateur);
                break;

            case LOCATAIRE:
                Locataire locataire = new Locataire(dto.getNom(), dto.getPrenom(), dto.getEmail(), motDePasseHash);
                locataire.setTelephone(dto.getTelephone());
                utilisateur = locataireRepository.save(locataire);
                break;
        }

        return convertirEnDTO(utilisateur);
    }

    // Méthodes de récupération
    public Optional<UtilisateurDTO> trouverParId(UUID id) {
        return utilisateurRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    public Optional<UtilisateurDTO> trouverParEmail(String email) {
        return utilisateurRepository.findByEmailAndEstActifTrue(email)
                .map(this::convertirEnDTO);
    }

    public List<UtilisateurDTO> trouverTousLesUtilisateurs() {
        return utilisateurRepository.findByEstActifTrue()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurDTO> trouverParRole(Role role) {
        return utilisateurRepository.findByRole(role)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurDTO> rechercherUtilisateurs(String recherche) {
        return utilisateurRepository.rechercherUtilisateurs(recherche)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de modification
    public Optional<UtilisateurDTO> modifierUtilisateur(UUID id, ModificationUtilisateurDTO dto) {
        return utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    if (dto.getNom() != null)
                        utilisateur.setNom(dto.getNom());
                    if (dto.getPrenom() != null)
                        utilisateur.setPrenom(dto.getPrenom());
                    if (dto.getEmail() != null) {
                        // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                        Optional<Utilisateur> utilisateurExistant = utilisateurRepository.findByEmail(dto.getEmail());
                        if (utilisateurExistant.isPresent() && !utilisateurExistant.get().getId().equals(id)) {
                            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                        }
                        utilisateur.setEmail(dto.getEmail());
                    }
                    if (dto.getTelephone() != null)
                        utilisateur.setTelephone(dto.getTelephone());
                    if (dto.getPhotoProfil() != null)
                        utilisateur.setPhotoProfil(dto.getPhotoProfil());

                    // Modifier les champs spécifiques selon le type d'utilisateur
                    if (utilisateur instanceof Admin && dto.getMatricule() != null) {
                        ((Admin) utilisateur).setMatricule(dto.getMatricule());
                    }
                    if (utilisateur instanceof Admin && dto.getDepartement() != null) {
                        ((Admin) utilisateur).setDepartement(dto.getDepartement());
                    }
                    if (utilisateur instanceof Locateur && dto.getDescription() != null) {
                        ((Locateur) utilisateur).setDescription(dto.getDescription());
                    }

                    utilisateur.setDateModification(LocalDateTime.now());
                    return convertirEnDTO(utilisateurRepository.save(utilisateur));
                });
    }

    // Méthodes de suppression
    public boolean supprimerUtilisateur(UUID id) {
        return utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    utilisateur.setEstActif(false);
                    utilisateur.setDateModification(LocalDateTime.now());
                    utilisateurRepository.save(utilisateur);
                    return true;
                })
                .orElse(false);
    }

    // Méthodes d'authentification
    public Optional<UtilisateurDTO> authentifierUtilisateur(String email, String motDePasse) {
        return utilisateurRepository.findByEmailAndEstActifTrue(email)
                .filter(utilisateur -> passwordEncoder.matches(motDePasse, utilisateur.getMotDePasseHash()))
                .map(utilisateur -> {
                    utilisateur.setDerniereConnexion(LocalDateTime.now());
                    utilisateurRepository.save(utilisateur);
                    return convertirEnDTO(utilisateur);
                });
    }

    // Méthodes de gestion des comptes
    public boolean activerCompte(UUID id) {
        return utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    utilisateur.setEstActif(true);
                    utilisateur.setDateModification(LocalDateTime.now());
                    utilisateurRepository.save(utilisateur);
                    return true;
                })
                .orElse(false);
    }

    public boolean desactiverCompte(UUID id) {
        return utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    utilisateur.setEstActif(false);
                    utilisateur.setDateModification(LocalDateTime.now());
                    utilisateurRepository.save(utilisateur);
                    return true;
                })
                .orElse(false);
    }

    // Méthodes utilitaires
    private UtilisateurDTO convertirEnDTO(Utilisateur utilisateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setRole(utilisateur.getRole());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setStatutKyc(utilisateur.getStatutKyc());
        dto.setDateInscription(utilisateur.getDateInscription());
        dto.setDerniereConnexion(utilisateur.getDerniereConnexion());
        dto.setEstActif(utilisateur.isEstActif());
        dto.setPhotoProfil(utilisateur.getPhotoProfil());
        dto.setDateModification(utilisateur.getDateModification());
        return dto;
    }

    public long compterUtilisateursParRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }

    /**
     * Modifie le profil d'un utilisateur (locataire, locateur ou admin)
     * 
     * @param utilisateurId ID de l'utilisateur à modifier
     * @param dto           DTO contenant les nouvelles informations du profil
     * @return Réponse indiquant le succès ou l'échec de l'opération
     */
    public ReponseModificationProfilDTO modifierProfil(UUID utilisateurId, ModificationProfilDTO dto) {
        try {
            // Vérifier si l'utilisateur existe
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);

            if (utilisateurOpt.isEmpty()) {
                return new ReponseModificationProfilDTO(false, "Utilisateur non trouvé");
            }

            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier si le compte est actif
            if (!utilisateur.isEstActif()) {
                return new ReponseModificationProfilDTO(false, "Compte désactivé");
            }

            // Modifier les champs de base
            if (dto.getNom() != null) {
                utilisateur.setNom(dto.getNom());
            }
            if (dto.getPrenom() != null) {
                utilisateur.setPrenom(dto.getPrenom());
            }
            if (dto.getTelephone() != null) {
                utilisateur.setTelephone(dto.getTelephone());
            }
            if (dto.getPhotoProfil() != null) {
                utilisateur.setPhotoProfil(dto.getPhotoProfil());
            }

            // Vérifier et modifier l'email si fourni
            if (dto.getEmail() != null && !dto.getEmail().equals(utilisateur.getEmail())) {
                // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                Optional<Utilisateur> utilisateurExistant = utilisateurRepository.findByEmail(dto.getEmail());
                if (utilisateurExistant.isPresent() && !utilisateurExistant.get().getId().equals(utilisateurId)) {
                    return new ReponseModificationProfilDTO(false,
                            "Cet email est déjà utilisé par un autre utilisateur");
                }
                utilisateur.setEmail(dto.getEmail());
            }

            // Modifier les champs spécifiques selon le type d'utilisateur
            if (utilisateur instanceof Admin) {
                Admin admin = (Admin) utilisateur;
                if (dto.getMatricule() != null) {
                    admin.setMatricule(dto.getMatricule());
                }
                if (dto.getDepartement() != null) {
                    admin.setDepartement(dto.getDepartement());
                }
            }

            if (utilisateur instanceof Locateur) {
                Locateur locateur = (Locateur) utilisateur;
                if (dto.getDescription() != null) {
                    locateur.setDescription(dto.getDescription());
                }
                if (dto.getNumeroSiret() != null) {
                    locateur.setNumeroSiret(dto.getNumeroSiret());
                }
                if (dto.getRaisonSociale() != null) {
                    locateur.setRaisonSociale(dto.getRaisonSociale());
                }
                if (dto.getAdresseProfessionnelle() != null) {
                    locateur.setAdresseProfessionnelle(dto.getAdresseProfessionnelle());
                }
            }

            if (utilisateur instanceof Locataire) {
                Locataire locataire = (Locataire) utilisateur;
                if (dto.getProfession() != null) {
                    locataire.setProfession(dto.getProfession());
                }
                if (dto.getRevenuAnnuel() != null) {
                    locataire.setRevenuAnnuel(dto.getRevenuAnnuel());
                }
                if (dto.getEmployeur() != null) {
                    locataire.setEmployeur(dto.getEmployeur());
                }
                if (dto.getDateEmbauche() != null) {
                    locataire.setDateEmbauche(dto.getDateEmbauche());
                }
            }

            // Mettre à jour la date de modification
            utilisateur.setDateModification(LocalDateTime.now());

            // Sauvegarder les modifications
            Utilisateur utilisateurModifie = utilisateurRepository.save(utilisateur);

            // Créer la réponse
            ReponseModificationProfilDTO response = new ReponseModificationProfilDTO(
                    true,
                    "Profil modifié avec succès",
                    utilisateurModifie.getId(),
                    utilisateurModifie.getEmail());

            // Ajouter les informations mises à jour
            response.setNom(utilisateurModifie.getNom());
            response.setPrenom(utilisateurModifie.getPrenom());
            response.setTelephone(utilisateurModifie.getTelephone());
            response.setPhotoProfil(utilisateurModifie.getPhotoProfil());
            response.setDateModification(utilisateurModifie.getDateModification());

            return response;

        } catch (Exception e) {
            return new ReponseModificationProfilDTO(false,
                    "Erreur lors de la modification du profil: " + e.getMessage());
        }
    }
}