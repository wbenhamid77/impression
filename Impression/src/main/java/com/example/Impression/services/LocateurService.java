package com.example.Impression.services;

import com.example.Impression.dto.CreationLocateurDTO;
import com.example.Impression.dto.ModificationLocateurDTO;
import com.example.Impression.dto.UtilisateurDTO;
import com.example.Impression.dto.ReservationLocateurDTO;
import com.example.Impression.dto.RecapitulatifReservationsLocateurDTO;
import com.example.Impression.dto.ReservationLocateurDetailleeDTO;
import com.example.Impression.dto.RecapitulatifReservationsLocateurDetailleDTO;
import com.example.Impression.dto.AnnonceDTO;
import com.example.Impression.dto.AdresseDTO;
import com.example.Impression.dto.LocateurInfoDTO;
import com.example.Impression.entities.Locateur;
import com.example.Impression.entities.Utilisateur;
import com.example.Impression.entities.Reservation;
import com.example.Impression.enums.StatutReservation;
import com.example.Impression.repositories.LocateurRepository;
import com.example.Impression.repositories.UtilisateurRepository;
import com.example.Impression.repositories.ReservationRepository;
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
    private ReservationRepository reservationRepository;

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

    // Récupérer toutes les réservations d'un locateur
    public List<ReservationLocateurDTO> obtenirReservationsLocateur(UUID locateurId) {
        List<Reservation> reservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);
        return reservations.stream()
                .map(this::convertirReservationEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer toutes les réservations d'un locateur avec informations détaillées
    public List<ReservationLocateurDetailleeDTO> obtenirReservationsLocateurDetaillees(UUID locateurId) {
        List<Reservation> reservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);
        return reservations.stream()
                .map(this::convertirReservationEnDetailleeDTO)
                .collect(Collectors.toList());
    }

    // Récupérer les réservations d'un locateur par statut
    public List<ReservationLocateurDTO> obtenirReservationsLocateurParStatut(UUID locateurId,
            StatutReservation statut) {
        List<Reservation> reservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);
        return reservations.stream()
                .filter(reservation -> reservation.getStatut() == statut)
                .map(this::convertirReservationEnDTO)
                .collect(Collectors.toList());
    }

    // Récupérer les réservations d'un locateur par statut avec informations
    // détaillées
    public List<ReservationLocateurDetailleeDTO> obtenirReservationsLocateurParStatutDetaillees(UUID locateurId,
            StatutReservation statut) {
        List<Reservation> reservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);
        return reservations.stream()
                .filter(reservation -> reservation.getStatut() == statut)
                .map(this::convertirReservationEnDetailleeDTO)
                .collect(Collectors.toList());
    }

    // Récupérer le récapitulatif complet des réservations d'un locateur
    public RecapitulatifReservationsLocateurDTO obtenirRecapitulatifReservations(UUID locateurId) {
        Optional<Locateur> locateurOpt = locateurRepository.findById(locateurId);
        if (locateurOpt.isEmpty()) {
            throw new RuntimeException("Locateur non trouvé");
        }

        Locateur locateur = locateurOpt.get();
        List<Reservation> toutesReservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);

        RecapitulatifReservationsLocateurDTO recapitulatif = new RecapitulatifReservationsLocateurDTO();
        recapitulatif.setLocateurId(locateur.getId());
        recapitulatif.setNomLocateur(locateur.getNom());
        recapitulatif.setPrenomLocateur(locateur.getPrenom());
        recapitulatif.setEmailLocateur(locateur.getEmail());

        // Grouper les réservations par statut
        recapitulatif.setReservationsEnAttente(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                        .map(this::convertirReservationEnDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsConfirmees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                        .map(this::convertirReservationEnDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsEnCours(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.EN_COURS)
                        .map(this::convertirReservationEnDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsTerminees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.TERMINEE)
                        .map(this::convertirReservationEnDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsAnnulees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                        .map(this::convertirReservationEnDTO)
                        .collect(Collectors.toList()));

        // Calculer les statistiques
        recapitulatif.setTotalReservations(toutesReservations.size());
        recapitulatif.setTotalEnAttente(recapitulatif.getReservationsEnAttente().size());
        recapitulatif.setTotalConfirmees(recapitulatif.getReservationsConfirmees().size());
        recapitulatif.setTotalEnCours(recapitulatif.getReservationsEnCours().size());
        recapitulatif.setTotalTerminees(recapitulatif.getReservationsTerminees().size());
        recapitulatif.setTotalAnnulees(recapitulatif.getReservationsAnnulees().size());

        return recapitulatif;
    }

    // Récupérer le récapitulatif complet des réservations d'un locateur avec
    // informations détaillées
    public RecapitulatifReservationsLocateurDetailleDTO obtenirRecapitulatifReservationsDetaille(UUID locateurId) {
        Optional<Locateur> locateurOpt = locateurRepository.findById(locateurId);
        if (locateurOpt.isEmpty()) {
            throw new RuntimeException("Locateur non trouvé");
        }

        Locateur locateur = locateurOpt.get();
        List<Reservation> toutesReservations = reservationRepository
                .findByAnnonce_Locateur_IdOrderByDateCreationDesc(locateurId);

        RecapitulatifReservationsLocateurDetailleDTO recapitulatif = new RecapitulatifReservationsLocateurDetailleDTO();
        recapitulatif.setLocateurId(locateur.getId());
        recapitulatif.setNomLocateur(locateur.getNom());
        recapitulatif.setPrenomLocateur(locateur.getPrenom());
        recapitulatif.setEmailLocateur(locateur.getEmail());

        // Grouper les réservations par statut avec informations détaillées
        recapitulatif.setReservationsEnAttente(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                        .map(this::convertirReservationEnDetailleeDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsConfirmees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                        .map(this::convertirReservationEnDetailleeDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsEnCours(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.EN_COURS)
                        .map(this::convertirReservationEnDetailleeDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsTerminees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.TERMINEE)
                        .map(this::convertirReservationEnDetailleeDTO)
                        .collect(Collectors.toList()));

        recapitulatif.setReservationsAnnulees(
                toutesReservations.stream()
                        .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                        .map(this::convertirReservationEnDetailleeDTO)
                        .collect(Collectors.toList()));

        // Calculer les statistiques
        recapitulatif.setTotalReservations(toutesReservations.size());
        recapitulatif.setTotalEnAttente(recapitulatif.getReservationsEnAttente().size());
        recapitulatif.setTotalConfirmees(recapitulatif.getReservationsConfirmees().size());
        recapitulatif.setTotalEnCours(recapitulatif.getReservationsEnCours().size());
        recapitulatif.setTotalTerminees(recapitulatif.getReservationsTerminees().size());
        recapitulatif.setTotalAnnulees(recapitulatif.getReservationsAnnulees().size());

        return recapitulatif;
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

    // Méthode utilitaire pour convertir une réservation en DTO
    private ReservationLocateurDTO convertirReservationEnDTO(Reservation reservation) {
        ReservationLocateurDTO dto = new ReservationLocateurDTO();

        dto.setId(reservation.getId());
        dto.setAnnonceId(reservation.getAnnonce().getId());
        dto.setTitreAnnonce(reservation.getAnnonce().getTitre());
        dto.setAdresseAnnonce(formatAdresse(reservation.getAnnonce().getAdresse()));

        // Informations du locataire
        dto.setLocataireId(reservation.getLocataire().getId());
        dto.setNomLocataire(reservation.getLocataire().getNom());
        dto.setPrenomLocataire(reservation.getLocataire().getPrenom());
        dto.setEmailLocataire(reservation.getLocataire().getEmail());
        dto.setTelephoneLocataire(reservation.getLocataire().getTelephone());

        // Dates et durée
        dto.setDateArrivee(reservation.getDateArrivee());
        dto.setDateDepart(reservation.getDateDepart());
        dto.setNombreNuits(reservation.getNombreNuits());
        dto.setNombreVoyageurs(reservation.getNombreVoyageurs());

        // Prix et montants
        dto.setPrixParNuit(reservation.getPrixParNuit());
        dto.setPrixTotal(reservation.getPrixTotal());
        dto.setFraisService(reservation.getFraisService());
        dto.setFraisNettoyage(reservation.getFraisNettoyage());
        dto.setFraisDepot(reservation.getFraisDepot());
        dto.setMontantTotal(reservation.getMontantTotal());

        // Statut et informations
        dto.setStatut(reservation.getStatut());
        dto.setLibelleStatut(reservation.getStatut().getLibelle());
        dto.setMessageProprietaire(reservation.getMessageProprietaire());

        // Dates de création et modification
        dto.setDateCreation(reservation.getDateCreation());
        dto.setDateModification(reservation.getDateModification());
        dto.setDateConfirmation(reservation.getDateConfirmation());
        dto.setDateAnnulation(reservation.getDateAnnulation());

        // Raison d'annulation si applicable
        dto.setRaisonAnnulation(reservation.getRaisonAnnulation());

        return dto;
    }

    // Méthode utilitaire pour formater l'adresse
    private String formatAdresse(com.example.Impression.entities.Adresse adresse) {
        if (adresse == null)
            return "Adresse non disponible";

        StringBuilder sb = new StringBuilder();
        if (adresse.getNumero() != null)
            sb.append(adresse.getNumero()).append(" ");
        if (adresse.getRue() != null)
            sb.append(adresse.getRue()).append(", ");
        if (adresse.getCodePostal() != null)
            sb.append(adresse.getCodePostal()).append(" ");
        if (adresse.getVille() != null)
            sb.append(adresse.getVille());

        String resultat = sb.toString().trim();
        return resultat.endsWith(",") ? resultat.substring(0, resultat.length() - 1) : resultat;
    }

    // Méthode utilitaire pour convertir une réservation en DTO détaillé
    private ReservationLocateurDetailleeDTO convertirReservationEnDetailleeDTO(Reservation reservation) {
        ReservationLocateurDetailleeDTO dto = new ReservationLocateurDetailleeDTO();

        dto.setId(reservation.getId());

        // Convertir l'annonce complète en DTO
        dto.setAnnonce(convertirAnnonceEnDTO(reservation.getAnnonce()));

        // Convertir le locataire complet en DTO
        dto.setLocataire(convertirUtilisateurEnDTO(reservation.getLocataire()));

        // Dates et durée
        dto.setDateArrivee(reservation.getDateArrivee());
        dto.setDateDepart(reservation.getDateDepart());
        dto.setNombreNuits(reservation.getNombreNuits());
        dto.setNombreVoyageurs(reservation.getNombreVoyageurs());

        // Prix et montants
        dto.setPrixParNuit(reservation.getPrixParNuit());
        dto.setPrixTotal(reservation.getPrixTotal());
        dto.setFraisService(reservation.getFraisService());
        dto.setFraisNettoyage(reservation.getFraisNettoyage());
        dto.setFraisDepot(reservation.getFraisDepot());
        dto.setMontantTotal(reservation.getMontantTotal());

        // Statut et informations
        dto.setStatut(reservation.getStatut());
        dto.setLibelleStatut(reservation.getStatut().getLibelle());
        dto.setMessageProprietaire(reservation.getMessageProprietaire());

        // Dates de création et modification
        dto.setDateCreation(reservation.getDateCreation());
        dto.setDateModification(reservation.getDateModification());
        dto.setDateConfirmation(reservation.getDateConfirmation());
        dto.setDateAnnulation(reservation.getDateAnnulation());

        // Raison d'annulation si applicable
        dto.setRaisonAnnulation(reservation.getRaisonAnnulation());

        return dto;
    }

    // Méthode utilitaire pour convertir une annonce en DTO
    private AnnonceDTO convertirAnnonceEnDTO(com.example.Impression.entities.Annonce annonce) {
        if (annonce == null)
            return null;

        AnnonceDTO dto = new AnnonceDTO();
        dto.setId(annonce.getId());
        dto.setTitre(annonce.getTitre());
        dto.setDescription(annonce.getDescription());
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
        dto.setLatitude(annonce.getLatitude());
        dto.setLongitude(annonce.getLongitude());

        // Convertir l'adresse
        if (annonce.getAdresse() != null) {
            AdresseDTO adresseDTO = new AdresseDTO();
            adresseDTO.setId(annonce.getAdresse().getId());
            adresseDTO.setNumero(annonce.getAdresse().getNumero());
            adresseDTO.setRue(annonce.getAdresse().getRue());
            adresseDTO.setCodePostal(annonce.getAdresse().getCodePostal());
            adresseDTO.setVille(annonce.getAdresse().getVille());
            adresseDTO.setPays(annonce.getAdresse().getPays());
            adresseDTO.setComplement(annonce.getAdresse().getComplement());
            adresseDTO.setSurface(annonce.getAdresse().getSurface());
            adresseDTO.setLocateurId(annonce.getAdresse().getLocateur().getId());
            adresseDTO.setNomLocateur(annonce.getAdresse().getLocateur().getNom());
            adresseDTO.setDateCreation(annonce.getAdresse().getDateCreation());
            adresseDTO.setDateModification(annonce.getAdresse().getDateModification());
            adresseDTO.setEstActive(annonce.getAdresse().isEstActive());
            dto.setAdresse(adresseDTO);
        }

        // Convertir le locateur
        if (annonce.getLocateur() != null) {
            LocateurInfoDTO locateurInfo = new LocateurInfoDTO();
            locateurInfo.setId(annonce.getLocateur().getId());
            locateurInfo.setNom(annonce.getLocateur().getNom());
            locateurInfo.setPrenom(annonce.getLocateur().getPrenom());
            dto.setLocateur(locateurInfo);
        }

        return dto;
    }

    // Méthode utilitaire pour convertir un utilisateur en DTO
    private UtilisateurDTO convertirUtilisateurEnDTO(com.example.Impression.entities.Utilisateur utilisateur) {
        if (utilisateur == null)
            return null;

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
}