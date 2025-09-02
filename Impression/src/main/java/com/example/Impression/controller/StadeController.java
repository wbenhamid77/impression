package com.example.Impression.controller;

import com.example.Impression.dto.StadeDTO;
import com.example.Impression.dto.CategorieStadeDTO;
import com.example.Impression.entities.Stade;
import com.example.Impression.entities.CategorieStade;
import com.example.Impression.services.StadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stades")
@CrossOrigin(origins = "*")
public class StadeController {

    @Autowired
    private StadeService stadeService;

    @GetMapping
    public ResponseEntity<List<StadeDTO>> getTousLesStades() {
        List<Stade> stades = stadeService.obtenirTousLesStades();
        List<StadeDTO> dto = stades.stream().map(this::convertirEnDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    private StadeDTO convertirEnDTO(Stade stade) {
        if (stade == null)
            return null;
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
        dto.setSurfaceMetresCarres(stade.getSurfaceMetresCarres());
        dto.setCategories(stade.getCategories());
        dto.setCategoriesPlaces(convertirCategories(stade.getCategoriesPlaces()));
        dto.setPrixMin(stade.getPrixMin());
        dto.setPrixMax(stade.getPrixMax());
        dto.setImages(stade.getImages());
        dto.setImagesBlob(stade.getImagesBlob());
        dto.setSurfaceType(stade.getSurfaceType());
        dto.setDimensions(stade.getDimensions());
        dto.setSiteWeb(stade.getSiteWeb());
        dto.setTelephone(stade.getTelephone());
        return dto;
    }

    private List<CategorieStadeDTO> convertirCategories(List<CategorieStade> categories) {
        if (categories == null)
            return java.util.Collections.emptyList();
        return categories.stream().map(c -> {
            CategorieStadeDTO dto = new CategorieStadeDTO();
            dto.setId(c.getId());
            dto.setCategorie(c.getCategorie());
            dto.setNom(c.getNom());
            dto.setNombrePlaces(c.getNombrePlaces());
            dto.setPrix(c.getPrix());
            dto.setDateCreation(c.getDateCreation());
            dto.setDateModification(c.getDateModification());
            return dto;
        }).collect(Collectors.toList());
    }
}
