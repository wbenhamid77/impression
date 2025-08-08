package com.example.Impression.services;

import com.example.Impression.dto.AnnonceDTO;
import com.example.Impression.dto.CreerAnnonceDTO;
import com.example.Impression.dto.AdresseDTO;
import com.example.Impression.dto.LocateurInfoDTO;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Adresse;
import com.example.Impression.entities.Locateur;
import com.example.Impression.enums.TypeMaison;
import com.example.Impression.exception.AnnonceException;
import com.example.Impression.repositories.AdresseRepository;
import com.example.Impression.repositories.AnnonceRepository;
import com.example.Impression.repositories.LocateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnnonceService {

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private LocateurRepository locateurRepository;

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private ImageService imageService;

    // Créer une annonce
    public AnnonceDTO creerAnnonce(CreerAnnonceDTO creerAnnonceDTO) {
        Locateur locateur = locateurRepository.findById(creerAnnonceDTO.getLocateurId())
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Locateur non trouvé"));

        Annonce annonce = new Annonce();
        annonce.setTitre(creerAnnonceDTO.getTitre());
        annonce.setDescription(creerAnnonceDTO.getDescription());
        // Créer l'adresse à partir des données fournies
        Adresse adresse = convertirAdresseDTO(creerAnnonceDTO.getAdresse());
        adresse.setLocateur(locateur);
        adresse.setEstActive(true);
        Adresse adresseSauvegardee = adresseRepository.save(adresse);
        annonce.setAdresse(adresseSauvegardee);
        annonce.setPrixParNuit(creerAnnonceDTO.getPrixParNuit());
        annonce.setPrixParSemaine(creerAnnonceDTO.getPrixParSemaine());
        annonce.setPrixParMois(creerAnnonceDTO.getPrixParMois());
        annonce.setCapacite(creerAnnonceDTO.getCapacite());
        annonce.setNombreChambres(creerAnnonceDTO.getNombreChambres());
        annonce.setNombreSallesDeBain(creerAnnonceDTO.getNombreSallesDeBain());
        annonce.setTypeMaison(creerAnnonceDTO.getTypeMaison());
        annonce.setEquipements(creerAnnonceDTO.getEquipements());
        annonce.setRegles(creerAnnonceDTO.getRegles());

        // Stocker les chemins d'images
        annonce.setImages(creerAnnonceDTO.getImages());

        // Lire les images depuis le disque et les stocker comme BLOB
        List<byte[]> imagesBytes = imageService.lireImagesDepuisDisque(creerAnnonceDTO.getImages());

        // Assigner les images à la liste BLOB
        if (imagesBytes != null && !imagesBytes.isEmpty()) {
            annonce.setImagesBlob(imagesBytes);
        }

        annonce.setStadePlusProche(creerAnnonceDTO.getStadePlusProche());
        annonce.setDistanceStade(creerAnnonceDTO.getDistanceStade());
        annonce.setAdresseStade(creerAnnonceDTO.getAdresseStade());
        annonce.setLatitude(creerAnnonceDTO.getLatitude());
        annonce.setLongitude(creerAnnonceDTO.getLongitude());
        annonce.setLocateur(locateur);
        annonce.setEstActive(true);

        Annonce annonceSauvegardee = annonceRepository.save(annonce);
        return convertirEnDTO(annonceSauvegardee);
    }

    // Récupérer toutes les annonces actives
    public List<AnnonceDTO> getAllAnnoncesActives() {
        return annonceRepository.findByEstActiveTrue()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer une annonce par ID
    public Optional<AnnonceDTO> getAnnonceById(UUID id) {
        return annonceRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    // Récupérer les annonces d'un locateur
    public List<AnnonceDTO> getAnnoncesByLocateur(UUID locateurId) {
        Locateur locateur = locateurRepository.findById(locateurId)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Locateur non trouvé"));

        return annonceRepository.findByLocateur(locateur)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Mettre à jour une annonce sans vérification d'autorisation
    public AnnonceDTO mettreAJourAnnonceSansVerification(UUID id, CreerAnnonceDTO creerAnnonceDTO) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Annonce non trouvée"));

        annonce.setTitre(creerAnnonceDTO.getTitre());
        annonce.setDescription(creerAnnonceDTO.getDescription());

        // Mettre à jour l'adresse existante
        Adresse adresseExistante = annonce.getAdresse();
        adresseExistante.setRue(creerAnnonceDTO.getAdresse().getRue());
        adresseExistante.setNumero(creerAnnonceDTO.getAdresse().getNumero());
        adresseExistante.setCodePostal(creerAnnonceDTO.getAdresse().getCodePostal());
        adresseExistante.setVille(creerAnnonceDTO.getAdresse().getVille());
        adresseExistante.setPays(creerAnnonceDTO.getAdresse().getPays());
        adresseExistante.setComplement(creerAnnonceDTO.getAdresse().getComplement());
        adresseExistante.setEstActive(true);

        // Sauvegarder l'adresse mise à jour
        Adresse adresseSauvegardee = adresseRepository.save(adresseExistante);
        annonce.setAdresse(adresseSauvegardee);

        annonce.setPrixParNuit(creerAnnonceDTO.getPrixParNuit());
        annonce.setPrixParSemaine(creerAnnonceDTO.getPrixParSemaine());
        annonce.setPrixParMois(creerAnnonceDTO.getPrixParMois());
        annonce.setCapacite(creerAnnonceDTO.getCapacite());
        annonce.setNombreChambres(creerAnnonceDTO.getNombreChambres());
        annonce.setNombreSallesDeBain(creerAnnonceDTO.getNombreSallesDeBain());
        annonce.setTypeMaison(creerAnnonceDTO.getTypeMaison());
        annonce.setEquipements(creerAnnonceDTO.getEquipements());
        annonce.setRegles(creerAnnonceDTO.getRegles());

        // Stocker les chemins d'images
        annonce.setImages(creerAnnonceDTO.getImages());

        // Lire les images depuis le disque et les stocker comme BLOB
        List<byte[]> imagesBytes = imageService.lireImagesDepuisDisque(creerAnnonceDTO.getImages());

        // Assigner les images à la liste BLOB
        if (imagesBytes != null && !imagesBytes.isEmpty()) {
            annonce.setImagesBlob(imagesBytes);
        }

        annonce.setStadePlusProche(creerAnnonceDTO.getStadePlusProche());
        annonce.setDistanceStade(creerAnnonceDTO.getDistanceStade());
        annonce.setAdresseStade(creerAnnonceDTO.getAdresseStade());
        annonce.mettreAJour();

        Annonce annonceSauvegardee = annonceRepository.save(annonce);
        return convertirEnDTO(annonceSauvegardee);
    }

    // Mettre à jour une annonce
    public AnnonceDTO mettreAJourAnnonce(UUID id, CreerAnnonceDTO creerAnnonceDTO, UUID locateurId) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Annonce non trouvée"));

        // Vérifier que l'annonce appartient au locateur
        if (!annonce.getLocateur().getId().equals(locateurId)) {
            throw new AnnonceException.AnnonceUnauthorizedException(
                    "Vous n'êtes pas autorisé à modifier cette annonce");
        }

        annonce.setTitre(creerAnnonceDTO.getTitre());
        annonce.setDescription(creerAnnonceDTO.getDescription());

        // Mettre à jour l'adresse existante
        Adresse adresseExistante = annonce.getAdresse();
        adresseExistante.setRue(creerAnnonceDTO.getAdresse().getRue());
        adresseExistante.setNumero(creerAnnonceDTO.getAdresse().getNumero());
        adresseExistante.setCodePostal(creerAnnonceDTO.getAdresse().getCodePostal());
        adresseExistante.setVille(creerAnnonceDTO.getAdresse().getVille());
        adresseExistante.setPays(creerAnnonceDTO.getAdresse().getPays());
        adresseExistante.setComplement(creerAnnonceDTO.getAdresse().getComplement());
        adresseExistante.setEstActive(true);

        // Sauvegarder l'adresse mise à jour
        Adresse adresseSauvegardee = adresseRepository.save(adresseExistante);
        annonce.setAdresse(adresseSauvegardee);

        annonce.setPrixParNuit(creerAnnonceDTO.getPrixParNuit());
        annonce.setPrixParSemaine(creerAnnonceDTO.getPrixParSemaine());
        annonce.setPrixParMois(creerAnnonceDTO.getPrixParMois());
        annonce.setCapacite(creerAnnonceDTO.getCapacite());
        annonce.setNombreChambres(creerAnnonceDTO.getNombreChambres());
        annonce.setNombreSallesDeBain(creerAnnonceDTO.getNombreSallesDeBain());
        annonce.setTypeMaison(creerAnnonceDTO.getTypeMaison());
        annonce.setEquipements(creerAnnonceDTO.getEquipements());
        annonce.setRegles(creerAnnonceDTO.getRegles());

        // Stocker les chemins d'images
        annonce.setImages(creerAnnonceDTO.getImages());

        // Lire les images depuis le disque et les stocker comme BLOB
        List<byte[]> imagesBytes = imageService.lireImagesDepuisDisque(creerAnnonceDTO.getImages());

        // Assigner les images à la liste BLOB
        if (imagesBytes != null && !imagesBytes.isEmpty()) {
            annonce.setImagesBlob(imagesBytes);
        }

        annonce.setStadePlusProche(creerAnnonceDTO.getStadePlusProche());
        annonce.setDistanceStade(creerAnnonceDTO.getDistanceStade());
        annonce.setAdresseStade(creerAnnonceDTO.getAdresseStade());
        annonce.mettreAJour();

        Annonce annonceSauvegardee = annonceRepository.save(annonce);
        return convertirEnDTO(annonceSauvegardee);
    }

    // Désactiver une annonce
    public boolean desactiverAnnonce(UUID id, UUID locateurId) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Annonce non trouvée"));

        if (!annonce.getLocateur().getId().equals(locateurId)) {
            throw new AnnonceException.AnnonceUnauthorizedException(
                    "Vous n'êtes pas autorisé à désactiver cette annonce");
        }

        annonce.desactiver();
        annonceRepository.save(annonce);
        return true;
    }

    // Supprimer une annonce
    public boolean supprimerAnnonce(UUID id, UUID locateurId) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Annonce non trouvée"));

        if (!annonce.getLocateur().getId().equals(locateurId)) {
            throw new AnnonceException.AnnonceUnauthorizedException(
                    "Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        annonce.supprimer();
        annonceRepository.save(annonce);
        return true;
    }

    // Supprimer une annonce sans vérification d'autorisation
    public boolean supprimerAnnonceSansVerification(UUID id) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceException.AnnonceNotFoundException("Annonce non trouvée"));

        annonce.supprimer();
        annonceRepository.save(annonce);
        return true;
    }

    // Recherche d'annonces
    public List<AnnonceDTO> rechercherAnnonces(String ville, TypeMaison typeMaison,
            BigDecimal prixMax, Integer capaciteMin, Double noteMin) {
        return annonceRepository.findByCritereRecherche(ville, typeMaison, prixMax, capaciteMin, noteMin)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Recherche par stade
    public List<AnnonceDTO> rechercherParStade(String stade) {
        return annonceRepository.findByStadeContaining(stade)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // ========== RECHERCHES GÉOGRAPHIQUES ==========

    // Recherche par rayon autour d'un point
    public List<Annonce> rechercherParRayon(BigDecimal latitude, BigDecimal longitude, double rayonKm) {
        return annonceRepository.findByRayonGeographique(latitude, longitude, rayonKm);
    }

    // Recherche par zone géographique
    public List<Annonce> rechercherParZone(BigDecimal latMin, BigDecimal latMax, BigDecimal lonMin, BigDecimal lonMax) {
        return annonceRepository.findByZoneGeographique(latMin, latMax, lonMin, lonMax);
    }

    // Recherche par proximité
    public List<Annonce> rechercherParProximite(BigDecimal latitude, BigDecimal longitude) {
        List<Object[]> resultats = annonceRepository.findByProximite(latitude, longitude);
        return resultats.stream()
                .map(resultat -> (Annonce) resultat[0])
                .collect(Collectors.toList());
    }

    // Recherche par proximité avec limite de distance
    public List<Annonce> rechercherParProximiteAvecLimite(BigDecimal latitude, BigDecimal longitude,
            double distanceMax) {
        return annonceRepository.findByProximiteAvecLimite(latitude, longitude, distanceMax);
    }

    // ========== VERSIONS DTO DES RECHERCHES GÉOGRAPHIQUES ==========

    // Recherche par rayon autour d'un point (retourne des DTOs)
    public List<AnnonceDTO> rechercherParRayonDTO(BigDecimal latitude, BigDecimal longitude, double rayonKm) {
        return rechercherParRayon(latitude, longitude, rayonKm)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Recherche par zone géographique (retourne des DTOs)
    public List<AnnonceDTO> rechercherParZoneDTO(BigDecimal latMin, BigDecimal latMax, BigDecimal lonMin,
            BigDecimal lonMax) {
        return rechercherParZone(latMin, latMax, lonMin, lonMax)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Recherche par proximité (retourne des DTOs)
    public List<AnnonceDTO> rechercherParProximiteDTO(BigDecimal latitude, BigDecimal longitude) {
        return rechercherParProximite(latitude, longitude)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Recherche par proximité avec limite de distance (retourne des DTOs)
    public List<AnnonceDTO> rechercherParProximiteAvecLimiteDTO(BigDecimal latitude, BigDecimal longitude,
            double distanceMax) {
        return rechercherParProximiteAvecLimite(latitude, longitude, distanceMax)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de conversion
    private AnnonceDTO convertirEnDTO(Annonce annonce) {
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
        dto.setStadePlusProche(annonce.getStadePlusProche());
        dto.setDistanceStade(annonce.getDistanceStade());
        dto.setAdresseStade(annonce.getAdresseStade());
        dto.setLatitude(annonce.getLatitude());
        dto.setLongitude(annonce.getLongitude());
        return dto;
    }

    private Adresse convertirAdresseDTO(AdresseDTO adresseDTO) {
        Adresse adresse = new Adresse();
        adresse.setRue(adresseDTO.getRue());
        adresse.setNumero(adresseDTO.getNumero());
        adresse.setCodePostal(adresseDTO.getCodePostal());
        adresse.setVille(adresseDTO.getVille());
        adresse.setPays(adresseDTO.getPays());
        adresse.setComplement(adresseDTO.getComplement());
        // latitude/longitude supprimés
        return adresse;
    }

    private AdresseDTO convertirAdresseEnDTO(Adresse adresse) {
        AdresseDTO dto = new AdresseDTO();
        dto.setRue(adresse.getRue());
        dto.setNumero(adresse.getNumero());
        dto.setCodePostal(adresse.getCodePostal());
        dto.setVille(adresse.getVille());
        dto.setPays(adresse.getPays());
        dto.setComplement(adresse.getComplement());
        // latitude/longitude supprimés
        return dto;
    }
}