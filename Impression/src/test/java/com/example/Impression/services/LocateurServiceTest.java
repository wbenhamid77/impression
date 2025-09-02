package com.example.Impression.services;

import com.example.Impression.dto.ReservationLocateurDTO;
import com.example.Impression.dto.RecapitulatifReservationsLocateurDTO;
import com.example.Impression.entities.Locateur;
import com.example.Impression.entities.Reservation;
import com.example.Impression.entities.Annonce;
import com.example.Impression.entities.Locataire;
import com.example.Impression.entities.Adresse;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.repositories.LocateurRepository;
import com.example.Impression.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocateurServiceTest {

    @Mock
    private LocateurRepository locateurRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LocateurService locateurService;

    private Locateur locateur;
    private Reservation reservation;
    private Annonce annonce;
    private Locataire locataire;
    private Adresse adresse;

    @BeforeEach
    void setUp() {
        // Créer un locateur de test
        locateur = new Locateur();
        locateur.setId(UUID.randomUUID());
        locateur.setNom("Martin");
        locateur.setPrenom("Pierre");
        locateur.setEmail("pierre.martin@email.com");

        // Créer une adresse de test
        adresse = new Adresse();
        adresse.setNumero("123");
        adresse.setRue("Rue de la Paix");
        adresse.setCodePostal("75001");
        adresse.setVille("Paris");

        // Créer une annonce de test
        annonce = new Annonce();
        annonce.setId(UUID.randomUUID());
        annonce.setTitre("Appartement avec vue");
        annonce.setAdresse(adresse);
        annonce.setLocateur(locateur);

        // Créer un locataire de test
        locataire = new Locataire();
        locataire.setId(UUID.randomUUID());
        locataire.setNom("Dupont");
        locataire.setPrenom("Jean");
        locataire.setEmail("jean.dupont@email.com");
        locataire.setTelephone("0123456789");

        // Créer une réservation de test
        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setAnnonce(annonce);
        reservation.setLocataire(locataire);
        reservation.setDateArrivee(LocalDate.of(2024, 6, 1));
        reservation.setDateDepart(LocalDate.of(2024, 6, 5));
        reservation.setNombreNuits(4);
        reservation.setNombreVoyageurs(2);
        reservation.setPrixParNuit(new BigDecimal("150.00"));
        reservation.setPrixTotal(new BigDecimal("600.00"));
        reservation.setFraisService(new BigDecimal("25.00"));
        reservation.setFraisNettoyage(new BigDecimal("50.00"));
        reservation.setFraisDepot(new BigDecimal("200.00"));
        reservation.setMontantTotal(new BigDecimal("875.00"));
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservation.setMessageProprietaire("Arrivée prévue vers 14h");
        reservation.setDateCreation(LocalDateTime.now());
        reservation.setDateModification(LocalDateTime.now());
        reservation.setDateConfirmation(LocalDateTime.now());
    }

    @Test
    void testObtenirReservationsLocateur() {
        // Arrange
        when(reservationRepository.findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateur.getId()))
                .thenReturn(Arrays.asList(reservation));

        // Act
        List<ReservationLocateurDTO> resultat = locateurService.obtenirReservationsLocateur(locateur.getId());

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());

        ReservationLocateurDTO dto = resultat.get(0);
        assertEquals(reservation.getId(), dto.getId());
        assertEquals(annonce.getId(), dto.getAnnonceId());
        assertEquals("Appartement avec vue", dto.getTitreAnnonce());
        assertEquals("123 Rue de la Paix, 75001 Paris", dto.getAdresseAnnonce());
        assertEquals(locataire.getId(), dto.getLocataireId());
        assertEquals("Dupont", dto.getNomLocataire());
        assertEquals("Jean", dto.getPrenomLocataire());
        assertEquals(StatutReservation.CONFIRMEE, dto.getStatut());
        assertEquals("Confirmée", dto.getLibelleStatut());
    }

    @Test
    void testObtenirRecapitulatifReservations() {
        // Arrange
        when(locateurRepository.findById(locateur.getId())).thenReturn(Optional.of(locateur));
        when(reservationRepository.findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateur.getId()))
                .thenReturn(Arrays.asList(reservation));

        // Act
        RecapitulatifReservationsLocateurDTO resultat = locateurService
                .obtenirRecapitulatifReservations(locateur.getId());

        // Assert
        assertNotNull(resultat);
        assertEquals(locateur.getId(), resultat.getLocateurId());
        assertEquals("Martin", resultat.getNomLocateur());
        assertEquals("Pierre", resultat.getPrenomLocateur());
        assertEquals("pierre.martin@email.com", resultat.getEmailLocateur());

        assertEquals(1, resultat.getTotalReservations());
        assertEquals(0, resultat.getTotalEnAttente());
        assertEquals(1, resultat.getTotalConfirmees());
        assertEquals(0, resultat.getTotalEnCours());
        assertEquals(0, resultat.getTotalTerminees());
        assertEquals(0, resultat.getTotalAnnulees());

        assertEquals(1, resultat.getReservationsConfirmees().size());
        assertEquals(0, resultat.getReservationsEnAttente().size());
    }

    @Test
    void testObtenirRecapitulatifReservations_LocateurNonTrouve() {
        // Arrange
        when(locateurRepository.findById(locateur.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            locateurService.obtenirRecapitulatifReservations(locateur.getId());
        });
    }
}