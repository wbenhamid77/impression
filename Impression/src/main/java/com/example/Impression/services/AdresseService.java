package com.example.Impression.services;

import com.example.Impression.dto.AdresseDTO;
import com.example.Impression.entities.Adresse;
import com.example.Impression.entities.Locateur;
import com.example.Impression.repositories.AdresseRepository;
import com.example.Impression.repositories.LocateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdresseService {

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private LocateurRepository locateurRepository;

    // Créer une adresse
    public AdresseDTO creerAdresse(AdresseDTO adresseDTO, UUID locateurId) {
        Locateur locateur = locateurRepository.findById(locateurId)
                .orElseThrow(() -> new RuntimeException("Locateur non trouvé"));

        Adresse adresse = new Adresse();
        adresse.setRue(adresseDTO.getRue());
        adresse.setNumero(adresseDTO.getNumero());
        adresse.setCodePostal(adresseDTO.getCodePostal());
        adresse.setVille(adresseDTO.getVille());
        adresse.setPays(adresseDTO.getPays());
        adresse.setComplement(adresseDTO.getComplement());
        adresse.setSurface(adresseDTO.getSurface());
        adresse.setLocateur(locateur);
        adresse.setEstActive(true);

        Adresse adresseSauvegardee = adresseRepository.save(adresse);
        return convertirEnDTO(adresseSauvegardee);
    }

    // Récupérer toutes les adresses d'un locateur
    public List<AdresseDTO> getAdressesByLocateur(UUID locateurId) {
        Locateur locateur = locateurRepository.findById(locateurId)
                .orElseThrow(() -> new RuntimeException("Locateur non trouvé"));

        return adresseRepository.findByLocateurAndEstActiveTrue(locateur)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer une adresse par ID
    public Optional<AdresseDTO> getAdresseById(UUID id) {
        return adresseRepository.findById(id)
                .map(this::convertirEnDTO);
    }

    // Mettre à jour une adresse
    public AdresseDTO mettreAJourAdresse(UUID id, AdresseDTO adresseDTO, UUID locateurId) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));

        // Vérifier que l'adresse appartient au locateur
        if (!adresse.getLocateur().getId().equals(locateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette adresse");
        }

        adresse.setRue(adresseDTO.getRue());
        adresse.setNumero(adresseDTO.getNumero());
        adresse.setCodePostal(adresseDTO.getCodePostal());
        adresse.setVille(adresseDTO.getVille());
        adresse.setPays(adresseDTO.getPays());
        adresse.setComplement(adresseDTO.getComplement());
        adresse.setSurface(adresseDTO.getSurface());
        adresse.mettreAJour();

        Adresse adresseSauvegardee = adresseRepository.save(adresse);
        return convertirEnDTO(adresseSauvegardee);
    }

    // Désactiver une adresse
    public boolean desactiverAdresse(UUID id, UUID locateurId) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));

        if (!adresse.getLocateur().getId().equals(locateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à désactiver cette adresse");
        }

        adresse.setEstActive(false);
        adresseRepository.save(adresse);
        return true;
    }

    // Recherche d'adresses par ville
    public List<AdresseDTO> rechercherParVille(String ville) {
        return adresseRepository.findByVilleContaining(ville)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Recherche d'adresses par surface
    public List<AdresseDTO> rechercherParSurface(BigDecimal surfaceMin, BigDecimal surfaceMax) {
        return adresseRepository.findBySurfaceBetweenAndEstActiveTrue(surfaceMin, surfaceMax)
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de conversion
    private AdresseDTO convertirEnDTO(Adresse adresse) {
        AdresseDTO dto = new AdresseDTO();
        dto.setId(adresse.getId());
        dto.setRue(adresse.getRue());
        dto.setNumero(adresse.getNumero());
        dto.setCodePostal(adresse.getCodePostal());
        dto.setVille(adresse.getVille());
        dto.setPays(adresse.getPays());
        dto.setComplement(adresse.getComplement());
        dto.setSurface(adresse.getSurface());
        dto.setLocateurId(adresse.getLocateur().getId());
        dto.setNomLocateur(adresse.getLocateur().getNom() + " " + adresse.getLocateur().getPrenom());
        dto.setDateCreation(adresse.getDateCreation());
        dto.setDateModification(adresse.getDateModification());
        dto.setEstActive(adresse.isEstActive());
        return dto;
    }
}