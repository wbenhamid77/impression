package com.example.Impression.services;

import com.example.Impression.dto.CreationReservationDTO;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Reservation;
import com.example.Impression.enums.ModePaiement;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.repositories.AnnonceRepository;
import com.example.Impression.repositories.LocataireRepository;
import com.example.Impression.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private LocataireRepository locataireRepository;

    @InjectMocks
    private ReservationService reservationService;

    private UUID annonceId;
    private UUID locataireId;
    private Annonce annonce;
    private Locataire locataire;
    private CreationReservationDTO creationDTO;

    @BeforeEach
    void setUp() {
        annonceId = UUID.randomUUID();
        locataireId = UUID.randomUUID();

        // Créer une adresse de test
        com.example.Impression.entities.Adresse adresse = new com.example.Impression.entities.Adresse();
        adresse.setRue("123 Rue de la Paix");
        adresse.setCodePostal("75001");
        adresse.setVille("Paris");
        adresse.setPays("France");

        // Créer un locateur de test
        com.example.Impression.entities.Locateur locateur = new com.example.Impression.entities.Locateur();
        locateur.setNom("Dupont");
        locateur.setPrenom("Jean");
        locateur.setEmail("jean.dupont@email.com");

        // Créer une annonce de test
        annonce = new Annonce();
        annonce.setId(annonceId);
        annonce.setTitre("Appartement de test");
        annonce.setPrixParNuit(new BigDecimal("100.00"));
        annonce.setAdresse(adresse);
        annonce.setLocateur(locateur);

        // Créer un locataire de test
        locataire = new Locataire();
        locataire.setId(locataireId);
        locataire.setNom("Test");
        locataire.setPrenom("User");

        // Créer un DTO de création de réservation
        creationDTO = new CreationReservationDTO();
        creationDTO.setAnnonceId(annonceId);
        creationDTO.setDateArrivee(LocalDate.now().plusDays(1));
        creationDTO.setDateDepart(LocalDate.now().plusDays(3));
        creationDTO.setNombreVoyageurs(2);
        creationDTO.setModePaiement(ModePaiement.PAIEMENT_SUR_PLACE);
        creationDTO.setMessageProprietaire("Test message");
    }

    @Test
    void testCreerRecapitulatif_Success() {
        // Given
        when(annonceRepository.findById(annonceId)).thenReturn(Optional.of(annonce));
        when(reservationRepository.findConflitsReservation(any(), any(), any()))
                .thenReturn(java.util.Collections.emptyList());

        // When
        var recapitulatif = reservationService.creerRecapitulatif(creationDTO);

        // Then
        assertNotNull(recapitulatif);
        assertEquals(annonceId, recapitulatif.getAnnonceId());
        assertEquals("Appartement de test", recapitulatif.getTitreAnnonce());
        assertEquals(2, recapitulatif.getNombreNuits());
        assertEquals(new BigDecimal("200.00"), recapitulatif.getPrixTotal());
    }

    @Test
    void testVerifierDisponibilite_Disponible() {
        // Given
        when(reservationRepository.findConflitsReservation(any(), any(), any()))
                .thenReturn(java.util.Collections.emptyList());

        // When
        boolean disponible = reservationService.verifierDisponibilite(annonceId, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));

        // Then
        assertTrue(disponible);
    }

    @Test
    void testVerifierDisponibilite_NonDisponible() {
        // Given
        Reservation reservationExistante = new Reservation();
        when(reservationRepository.findConflitsReservation(any(), any(), any()))
                .thenReturn(java.util.Arrays.asList(reservationExistante));

        // When
        boolean disponible = reservationService.verifierDisponibilite(annonceId, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));

        // Then
        assertFalse(disponible);
    }
}