package com.example.Impression.services;

import com.example.Impression.dto.CreationLocataireDTO;
import com.example.Impression.dto.ModificationLocataireDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.dto.AnnonceDTO;
import com.example.Impression.dto.AdresseDTO;
import com.example.Impression.dto.LocateurInfoDTO;
import com.example.Impression.dto.StadeDTO;
import com.example.Impression.dto.AnnonceStadeDistanceDTO;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Adresse;
import com.example.Impression.entities.AnnonceStadeDistance;
import com.example.Impression.repositories.LocataireRepository;
import com.example.Impression.repositories.UtilisateurRepository;
import com.example.Impression.repositories.AnnonceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocataireService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LocataireRepository locataireRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private AnnonceStadeDistanceService annonceStadeDistanceService;

    // Créer un locataire
    public UtilisateurDTO creerLocataire(CreationLocataireDTO dto) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Encoder le mot de passe
        String motDePasseHash = passwordEncoder.encode(dto.getMotDePasse());

        // Créer le locataire avec toutes les informations
        Locataire locataire = new Locataire(
                dto.getNom(),
                dto.getPrenom(),
                dto.getEmail(),
                motDePasseHash,
                dto.getTelephone(),
                dto.getProfession(),
                dto.getRevenuAnnuel(),
                dto.getEmployeur(),
                dto.getDateEmbauche());

        locataire.setPhotoProfil(dto.getPhotoProfil());

        // Sauvegarder
        Locataire locataireSauvegarde = locataireRepository.save(locataire);

        return convertirEnDTO(locataireSauvegarde);
    }

    // Récupérer tous les locataires
    public List<UtilisateurDTO> trouverTousLesLocataires() {
        return locataireRepository.findAll()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer un locataire par ID
    public Optional<UtilisateurDTO> trouverLocataireParId(UUID id) {
        return locataireRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    // Récupérer un locataire par email
    public Optional<UtilisateurDTO> trouverLocataireParEmail(String email) {
        return locataireRepository.findAll()
                .stream()
                .filter(locataire -> locataire.getEmail().equals(email) && locataire.isEstActif())
                .findFirst()
                .map(this::convertirEnDTO);
    }

    // Modifier un locataire
    public Optional<UtilisateurDTO> modifierLocataire(UUID id, ModificationLocataireDTO dto) {
        return locataireRepository.findById(id)
                .map(locataire -> {
                    if (dto.getNom() != null)
                        locataire.setNom(dto.getNom());
                    if (dto.getPrenom() != null)
                        locataire.setPrenom(dto.getPrenom());
                    if (dto.getEmail() != null) {
                        // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                        Optional<Utilisateur> utilisateurExistant = utilisateurRepository.findByEmail(dto.getEmail());
                        if (utilisateurExistant.isPresent() && !utilisateurExistant.get().getId().equals(id)) {
                            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                        }
                        locataire.setEmail(dto.getEmail());
                    }
                    if (dto.getTelephone() != null)
                        locataire.setTelephone(dto.getTelephone());
                    if (dto.getPhotoProfil() != null)
                        locataire.setPhotoProfil(dto.getPhotoProfil());

                    locataire.setDateModification(LocalDateTime.now());
                    return convertirEnDTO(locataireRepository.save(locataire));
                });
    }

    // Supprimer un locataire (désactivation)
    public boolean supprimerLocataire(UUID id) {
        return locataireRepository.findById(id)
                .map(locataire -> {
                    locataire.setEstActif(false);
                    locataire.setDateModification(LocalDateTime.now());
                    locataireRepository.save(locataire);
                    return true;
                })
                .orElse(false);
    }

    // Activer un compte locataire
    public boolean activerCompteLocataire(UUID id) {
        return locataireRepository.findById(id)
                .map(locataire -> {
                    locataire.setEstActif(true);
                    locataire.setDateModification(LocalDateTime.now());
                    locataireRepository.save(locataire);
                    return true;
                })
                .orElse(false);
    }

    // Désactiver un compte locataire
    public boolean desactiverCompteLocataire(UUID id) {
        return locataireRepository.findById(id)
                .map(locataire -> {
                    locataire.setEstActif(false);
                    locataire.setDateModification(LocalDateTime.now());
                    locataireRepository.save(locataire);
                    return true;
                })
                .orElse(false);
    }

    // Compter les locataires
    public long compterLocataires() {
        return locataireRepository.count();
    }

    // Ajouter une annonce aux favoris
    public boolean ajouterFavori(UUID locataireId, UUID annonceId) {
        Locataire locataire = locataireRepository.findById(locataireId)
                .orElseThrow(() -> new RuntimeException("Locataire non trouvé"));

        // Vérifier que l'annonce existe
        if (!annonceRepository.existsById(annonceId)) {
            throw new RuntimeException("Annonce non trouvée");
        }

        // Initialiser la liste des favoris si elle est null
        if (locataire.getFavoris() == null) {
            locataire.setFavoris(new ArrayList<>());
        }

        // Vérifier si l'annonce n'est pas déjà dans les favoris
        if (!locataire.getFavoris().contains(annonceId)) {
            locataire.getFavoris().add(annonceId);
            locataireRepository.save(locataire);
            return true;
        }

        return false; // L'annonce était déjà dans les favoris
    }

    // Retirer une annonce des favoris
    public boolean retirerFavori(UUID locataireId, UUID annonceId) {
        Locataire locataire = locataireRepository.findById(locataireId)
                .orElseThrow(() -> new RuntimeException("Locataire non trouvé"));

        if (locataire.getFavoris() != null && locataire.getFavoris().contains(annonceId)) {
            locataire.getFavoris().remove(annonceId);
            locataireRepository.save(locataire);
            return true;
        }

        return false; // L'annonce n'était pas dans les favoris
    }

    // Récupérer toutes les annonces favorites d'un locataire
    public List<AnnonceDTO> getAnnoncesFavorites(UUID locataireId) {
        Locataire locataire = locataireRepository.findById(locataireId)
                .orElseThrow(() -> new RuntimeException("Locataire non trouvé"));

        if (locataire.getFavoris() == null || locataire.getFavoris().isEmpty()) {
            return new ArrayList<>();
        }

        return annonceRepository.findAllById(locataire.getFavoris())
                .stream()
                .map(this::convertirAnnonceEnDTO)
                .collect(Collectors.toList());
    }

    // Vérifier si une annonce est dans les favoris
    public boolean estFavori(UUID locataireId, UUID annonceId) {
        Locataire locataire = locataireRepository.findById(locataireId)
                .orElseThrow(() -> new RuntimeException("Locataire non trouvé"));

        return locataire.getFavoris() != null && locataire.getFavoris().contains(annonceId);
    }

    // Méthode utilitaire pour convertir en DTO
    private UtilisateurDTO convertirEnDTO(Locataire locataire) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(locataire.getId());
        dto.setRole(locataire.getRole());
        dto.setNom(locataire.getNom());
        dto.setPrenom(locataire.getPrenom());
        dto.setEmail(locataire.getEmail());
        dto.setTelephone(locataire.getTelephone());
        dto.setStatutKyc(locataire.getStatutKyc());
        dto.setDateInscription(locataire.getDateInscription());
        dto.setDerniereConnexion(locataire.getDerniereConnexion());
        dto.setEstActif(locataire.isEstActif());
        dto.setPhotoProfil(locataire.getPhotoProfil());
        dto.setDateModification(locataire.getDateModification());
        return dto;
    }

    // Méthode utilitaire pour convertir une annonce en DTO
    private AnnonceDTO convertirAnnonceEnDTO(Annonce annonce) {
        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(annonce.getId());
        dto.setTitre(annonce.getTitre());
        dto.setDescription(annonce.getDescription());
        dto.setAdresse(convertirAdresseEnDTO(annonce.getAdresse()));
        dto.setPrixParNuit(annonce.getPrixParNuit());
        dto.setPrixParSemaine(annonce.getPrixParSemaine());
        dto.setPrixParMois(annonce.getPrixParMois());
        dto.setCapacite(annonce.getCapacite());
        dto.setNombreChambres(annonce.getNombreChambres());
        dto.setNombreSallesDeBain(annonce.getNombreSallesDeBain());
        dto.setTypeMaison(annonce.getTypeMaison());
        dto.setEstActive(annonce.isEstActive());
        dto.setDateCreation(annonce.getDateCreation());
        dto.setDateModification(annonce.getDateModification());
        dto.setEquipements(annonce.getEquipements());
        dto.setRegles(annonce.getRegles());
        dto.setImages(annonce.getImages());
        dto.setImagesBlob(annonce.getImagesBlob());
        dto.setNoteMoyenne(annonce.getNoteMoyenne());
        dto.setNombreAvis(annonce.getNombreAvis());

        // Créer les informations du locateur
        LocateurInfoDTO locateurInfo = new LocateurInfoDTO();
        locateurInfo.setId(annonce.getLocateur().getId());
        locateurInfo.setNom(annonce.getLocateur().getNom());
        locateurInfo.setPrenom(annonce.getLocateur().getPrenom());
        locateurInfo.setEmail(annonce.getLocateur().getEmail());
        locateurInfo.setTelephone(annonce.getLocateur().getTelephone());
        locateurInfo.setPhotoProfil(annonce.getLocateur().getPhotoProfil());
        locateurInfo.setDescription(annonce.getLocateur().getDescription());
        locateurInfo.setNoteMoyenne(annonce.getLocateur().getNoteMoyenne());
        locateurInfo.setNombreAnnonces(annonce.getLocateur().getNombreAnnonces());
        locateurInfo.setEstVerifie(annonce.getLocateur().isEstVerifie());
        locateurInfo.setRaisonSociale(annonce.getLocateur().getRaisonSociale());

        dto.setLocateur(locateurInfo);
        dto.setLatitude(annonce.getLatitude());
        dto.setLongitude(annonce.getLongitude());

        // Convertir les distances avec les stades
        List<AnnonceStadeDistance> distances = annonceStadeDistanceService.getDistancesParAnnonce(annonce);
        List<AnnonceStadeDistanceDTO> distancesDTO = distances.stream()
                .map(this::convertirAnnonceStadeDistanceEnDTO)
                .collect(Collectors.toList());

        dto.setDistancesStades(distancesDTO);

        // Trouver le stade le plus proche
        Optional<AnnonceStadeDistance> stadeLePlusProche = annonceStadeDistanceService.getStadeLePlusProche(annonce);
        if (stadeLePlusProche.isPresent()) {
            dto.setStadeLePlusProche(convertirAnnonceStadeDistanceEnDTO(stadeLePlusProche.get()));
        }

        return dto;
    }

    private AnnonceStadeDistanceDTO convertirAnnonceStadeDistanceEnDTO(AnnonceStadeDistance distance) {
        AnnonceStadeDistanceDTO dto = new AnnonceStadeDistanceDTO();
        dto.setId(distance.getId());
        dto.setStade(convertirStadeEnDTO(distance.getStade()));
        dto.setDistance(distance.getDistance());
        dto.setTempsTrajetMinutes(distance.getTempsTrajetMinutes());
        dto.setModeTransport(distance.getModeTransport().name());
        dto.setEstLePlusProche(distance.getEstLePlusProche());
        dto.setDateCreation(distance.getDateCreation());
        dto.setDateModification(distance.getDateModification());
        return dto;
    }

    // Méthode utilitaire pour convertir une adresse en DTO
    private AdresseDTO convertirAdresseEnDTO(Adresse adresse) {
        AdresseDTO dto = new AdresseDTO();
        dto.setRue(adresse.getRue());
        dto.setNumero(adresse.getNumero());
        dto.setCodePostal(adresse.getCodePostal());
        dto.setVille(adresse.getVille());
        dto.setPays(adresse.getPays());
        dto.setComplement(adresse.getComplement());
        return dto;
    }

    // Méthode utilitaire pour convertir un stade en DTO
    private StadeDTO convertirStadeEnDTO(com.example.Impression.entities.Stade stade) {
        if (stade == null) {
            return null;
        }

        StadeDTO dto = new StadeDTO();
        dto.setId(stade.getId());
        dto.setNom(stade.getNom());
        dto.setVille(stade.getVille());
        dto.setAdresseComplete(stade.getAdresseComplete());
        dto.setLatitude(stade.getLatitude());
        dto.setLongitude(stade.getLongitude());
        dto.setCapacite(stade.getCapacite());
        dto.setDescription(stade.getDescription());
        dto.setEstActif(stade.isEstActif());
        dto.setDateCreation(stade.getDateCreation());
        dto.setDateModification(stade.getDateModification());

        return dto;
    }
}