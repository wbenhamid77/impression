## Plateforme de location - Référence API (Backend Spring)

Cette documentation décrit les APIs disponibles pour construire un frontend de plateforme de location. Elle couvre les endpoints, corps JSON d’entrée/sortie, paramètres et relations entre ressources.

### Rôles et concepts
- **ADMIN**: gère le système et les utilisateurs
- **LOCATEUR**: publie et gère des `annonces`
- **LOCATAIRE**: recherche, réserve des `annonces`, gère ses favoris
- **Annonce**: logement publiable par un locateur
- **Réservation**: demande/contrat entre locataire et annonce
- **Favoris**: association locataire ↔ annonce

### Authentification & Session
Deux contrôleurs exposent des routes d’authentification. Utilisez celui qui convient à votre stratégie (token JWT vs login simple).

- Base: `/api/auth`

1) AuthController (JWT)
- POST `/api/auth/login`
  - Body: `LoginRequestDTO`
```json
{
  "email": "john@doe.com",
  "password": "secret123"
}
```
  - 200: `LoginResponseDTO`
```json
{
  "token": "<JWT>",
  "refreshToken": "<REFRESH>",
  "userId": "2c2d1f57-...",
  "email": "john@doe.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "LOCATAIRE",
  "expirationDate": "2025-01-31T12:00:00",
  "message": "Authentification réussie"
}
```
  - 400: `LoginResponseDTO` avec `message`

- POST `/api/auth/refresh?refreshToken=<token>`
  - 200: `{ "token": "<NEW_JWT>" }`
  - 400: `{ "message": "Token de rafraîchissement invalide" }`

- POST `/api/auth/validate?token=<jwt>`
  - 200: `{ "valid": true }`
  - 400: `{ "valid": false }`

- PUT `/api/auth/modifier-mot-de-passe`
  - Body: `ModificationMotDePasseDTO`
```json
{
  "email": "john@doe.com",
  "ancienMotDePasse": "secret123",
  "nouveauMotDePasse": "newSecret456"
}
```
  - 200 / 400: `ReponseModificationMotDePasseDTO`

2) AuthentificationController (auth simple + vérification)
- POST `/api/auth/connexion`
  - Body: `ConnexionDTO`
```json
{ "email": "john@doe.com", "motDePasse": "secret123" }
```
  - 200: `UtilisateurDTO` | 400: message

- POST `/api/auth/deconnexion?email=john@doe.com`
  - 200/400: message

- POST `/api/auth/verifier`
  - Body: `ConnexionDTO` (email, motDePasse)
  - 200: `true | false`

- PUT `/api/auth/changer-mot-de-passe`
  - Body: `ModificationMotDePasseDTO`
  - 200 / 400: `ReponseModificationMotDePasseDTO`


### Utilisateurs génériques
- Base: `/api/utilisateurs`

- POST `/api/utilisateurs`
  - Body: `CreationUtilisateurDTO`
```json
{
  "nom": "Jane",
  "prenom": "Doe",
  "email": "jane@doe.com",
  "motDePasse": "secret123",
  "telephone": "+33 6 00 00 00 00",
  "role": "LOCATEUR",
  "matricule": null,
  "departement": null,
  "description": "Hôte super"
}
```
  - 201: `UtilisateurDTO` | 400: message

- GET `/api/utilisateurs`
  - 200: `UtilisateurDTO[]`

- GET `/api/utilisateurs/{id}`
  - 200: `UtilisateurDTO` | 404

- GET `/api/utilisateurs/email/{email}`
  - 200: `UtilisateurDTO` | 404

- GET `/api/utilisateurs/role/{role}`
  - 200: `UtilisateurDTO[]` (role ∈ `ADMIN|LOCATEUR|LOCATAIRE`)

- GET `/api/utilisateurs/recherche?terme=doe`
  - 200: `UtilisateurDTO[]`

- PUT `/api/utilisateurs/{id}`
  - Body: `ModificationUtilisateurDTO`
  - 200: `UtilisateurDTO` | 404 | 400

- DELETE `/api/utilisateurs/{id}`
  - 200: message | 404

- POST `/api/utilisateurs/{id}/activer` | POST `/api/utilisateurs/{id}/desactiver`
  - 200: message | 404

- GET `/api/utilisateurs/statistiques/role/{role}`
  - 200: `Long`

- PUT `/api/utilisateurs/{id}/profil`
  - Body: `ModificationProfilDTO`
  - 200 / 400: `ReponseModificationProfilDTO`


### Profil (raccourcis)
- Base: `/api/profil`

- GET `/api/profil/{id}` → `UtilisateurDTO`
- PUT `/api/profil/{id}` → Body `ModificationProfilDTO` → `ReponseModificationProfilDTO`
- GET `/api/profil/email/{email}` → `UtilisateurDTO`


### Admins
- Base: `/api/admins`

CRUD + stats: mêmes schémas que Utilisateurs mais via `CreationAdminDTO`/`ModificationAdminDTO`.
- POST `/api/admins` → 201 `UtilisateurDTO`
- GET `/api/admins` → `UtilisateurDTO[]`
- GET `/api/admins/{id}` | `/api/admins/email/{email}`
- PUT `/api/admins/{id}` → `UtilisateurDTO`
- DELETE `/api/admins/{id}` → message
- POST `/api/admins/{id}/activer` | `/desactiver`
- GET `/api/admins/statistiques/count` → `Long`


### Locateurs
- Base: `/api/locateurs`

- POST `/api/locateurs` → Body `CreationLocateurDTO` → 201 `UtilisateurDTO`
- GET `/api/locateurs` → `UtilisateurDTO[]`
- GET `/api/locateurs/{id}` | `/email/{email}` → `UtilisateurDTO`
- PUT `/api/locateurs/{id}` → Body `ModificationLocateurDTO` → `UtilisateurDTO`
- DELETE `/api/locateurs/{id}` → message
- POST `/api/locateurs/{id}/activer` | `/desactiver`
- GET `/api/locateurs/statistiques/count` → `Long`

Réservations d’un locateur (aggrégations détaillées pour ses annonces)
- GET `/api/locateurs/{id}/reservations` → `ReservationLocateurDetailleeDTO[]`
- GET `/api/locateurs/{id}/reservations/statut/{statut}` → `ReservationLocateurDetailleeDTO[]` (statut ∈ `EN_ATTENTE|CONFIRMEE|EN_COURS|TERMINEE|ANNULEE`)
- GET `/api/locateurs/{id}/reservations/recapitulatif` → `RecapitulatifReservationsLocateurDetailleDTO`
- GET `/api/locateurs/{id}/reservations/en-attente|confirmees|en-cours|terminees|annulees` → `ReservationLocateurDetailleeDTO[]`


### Locataires
- Base: `/api/locataires`

- POST `/api/locataires` → Body `CreationLocataireDTO` → 201 `UtilisateurDTO`
- GET `/api/locataires` → `UtilisateurDTO[]`
- GET `/api/locataires/{id}` | `/email/{email}` → `UtilisateurDTO`
- PUT `/api/locataires/{id}` → Body `ModificationLocataireDTO` → `UtilisateurDTO`
- DELETE `/api/locataires/{id}` → message
- POST `/api/locataires/{id}/activer` | `/desactiver`
- GET `/api/locataires/statistiques/count` → `Long`

Favoris
- POST `/api/locataires/{locataireId}/favoris/{annonceId}` → 200 message (idempotent)
- DELETE `/api/locataires/{locataireId}/favoris/{annonceId}` → 200 message (idempotent)
- GET `/api/locataires/{locataireId}/favoris` → `AnnonceDTO[]`
- GET `/api/locataires/{locataireId}/favoris/{annonceId}/check` → `true|false`


### Annonces
- Base: `/api/annonces`

- GET `/api/annonces` → `AnnonceDTO[]` (annonces actives)
- GET `/api/annonces/{id}` → `AnnonceDTO` | 404
- POST `/api/annonces` → Body `CreerAnnonceDTO` → 201 `AnnonceDTO`
- PUT `/api/annonces/{id}` → Body `CreerAnnonceDTO` → `AnnonceDTO`
- DELETE `/api/annonces/{id}?locateurId=<UUID>` → 204 | 404
- PATCH `/api/annonces/{id}/desactiver?locateurId=<UUID>` → 200
- GET `/api/annonces/locateur/{locateurId}` → `AnnonceDTO[]`

Recherche
- GET `/api/annonces/recherche?ville=&typeMaison=&prixMax=&capaciteMin=&noteMin=` → `AnnonceDTO[]`
- GET `/api/annonces/recherche/stade?stade=` → `AnnonceDTO[]`
- GET `/api/annonces/recherche/rayon?latitude=&longitude=&rayonKm=` → `AnnonceDTO[]`
- GET `/api/annonces/recherche/zone?latMin=&latMax=&lonMin=&lonMax=` → `AnnonceDTO[]`
- GET `/api/annonces/recherche/proximite?latitude=&longitude=&distanceMax=` → `AnnonceDTO[]`

Exemple `CreerAnnonceDTO`
```json
{
  "titre": "Studio proche centre",
  "description": "Studio lumineux",
  "adresse": {
    "rue": "12 Rue des Fleurs",
    "numero": "12",
    "codePostal": "75001",
    "ville": "Paris",
    "pays": "France"
  },
  "prixParNuit": 75.0,
  "prixParSemaine": 450.0,
  "prixParMois": 1500.0,
  "capacite": 2,
  "nombreChambres": 1,
  "nombreSallesDeBain": 1,
  "typeMaison": "STUDIO",
  "equipements": ["WIFI", "TV"],
  "regles": ["Non fumeur"],
  "images": [],
  "stadePlusProche": "Stade Jean Bouin",
  "distanceStade": 2.5,
  "adresseStade": "1 Av. du Général Sarrail, Paris",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "locateurId": "2c2d1f57-..."
}
```


### Réservations
- Base: `/api/reservations`

Création
- POST `/api/reservations/recapitulatif` → Body `CreationReservationDTO` → 200 `RecapitulatifReservationDTO`
- POST `/api/reservations?locataireId=<UUID>` → Body `CreationReservationDTO` → 201 `ReservationDTO`

Gestion du statut
- PUT `/api/reservations/{id}/confirmer` → `ReservationDTO`
- PUT `/api/reservations/{id}/annuler?raison=` → `ReservationDTO`
- PUT `/api/reservations/{id}/statut?statut=EN_COURS|CONFIRMEE|...` → `ReservationDTO`
- PUT `/api/reservations/{id}/terminer` → `ReservationDTO`
- PUT `/api/reservations/{id}/mettre-en-cours` → `ReservationDTO`

Consultation
- GET `/api/reservations/locataire/{locataireId}` → `ReservationDTO[]`
- GET `/api/reservations/locateur/{locateurId}` → `ReservationDTO[]`
- GET `/api/reservations/annonce/{annonceId}` → `ReservationDTO[]`
- GET `/api/reservations/{id}` → `ReservationDTO`
- GET `/api/reservations/en-attente` → `ReservationDTO[]`

Disponibilités & périodes
- GET `/api/reservations/disponibilite?annonceId=&dateArrivee=&dateDepart=` → `true|false`
- GET `/api/reservations/annonce/{annonceId}/periodes` → `PeriodeReserveeDTO[]`
- GET `/api/reservations/annonce/{annonceId}/periodes-futures?statuts=EN_ATTENTE,CONFIRMEE,EN_COURS` → `PeriodeReserveeDTO[]`
- GET `/api/reservations/annonce/{annonceId}/jours-reserves?statuts=...` → `LocalDate[]`

Segments temporels côté locataire (dérivés)
- GET `/api/reservations/locataire/{locataireId}/futures` → `ReservationDTO[]`
- GET `/api/reservations/locataire/{locataireId}/passees` → `ReservationDTO[]`
- GET `/api/reservations/locataire/{locataireId}/en-cours` → `ReservationDTO[]`

Exemple `CreationReservationDTO`
```json
{
  "annonceId": "6c02b5af-...",
  "dateArrivee": "2025-09-15",
  "dateDepart": "2025-09-18",
  "nombreVoyageurs": 2,
  "modePaiement": "PAIEMENT_SUR_PLACE",
  "messageProprietaire": "Arrivée vers 19h",
  "fraisService": 10.0,
  "fraisNettoyage": 15.0,
  "fraisDepot": 100.0
}
```

Réponse type `ReservationDTO`
```json
{
  "id": "a7b3...",
  "annonceId": "6c02...",
  "locataireId": "2c2d...",
  "dateArrivee": "2025-09-15",
  "dateDepart": "2025-09-18",
  "nombreNuits": 3,
  "prixParNuit": 75.0,
  "prixTotal": 225.0,
  "fraisService": 10.0,
  "fraisNettoyage": 15.0,
  "fraisDepot": 100.0,
  "montantTotal": 350.0,
  "statut": "EN_ATTENTE",
  "modePaiement": "PAIEMENT_SUR_PLACE",
  "messageProprietaire": "Arrivée vers 19h",
  "numeroTransaction": null,
  "datePaiement": null,
  "dateCreation": "2025-08-31T12:00:00",
  "dateModification": "2025-08-31T12:00:00",
  "dateConfirmation": null,
  "dateAnnulation": null,
  "raisonAnnulation": null,
  "nombreVoyageurs": 2,
  "titreAnnonce": "Studio proche centre",
  "adresseAnnonce": "12 Rue des Fleurs, 75001 Paris, France",
  "nomLocateur": "Doe",
  "emailLocateur": "host@doe.com",
  "nomLocataire": "Doe",
  "prenomLocataire": "John",
  "emailLocataire": "john@doe.com"
}
```


### Health & Maison
- GET `/` → String
- GET `/test` → String
- GET `/health` → `"OK"`
- GET `/api/health` → `"API OK"`


### DTOs principaux (schémas)
- `UtilisateurDTO`
```json
{
  "id": "uuid",
  "role": "ADMIN|LOCATEUR|LOCATAIRE",
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "telephone": "string",
  "statutKyc": "NON_VÉRIFIÉ|EN_ATTENTE|VALIDÉ|REJETÉ",
  "dateInscription": "date-time",
  "derniereConnexion": "date-time",
  "estActif": true,
  "photoProfil": "url",
  "dateModification": "date-time"
}
```

- `AnnonceDTO` (extrait)
```json
{
  "id": "uuid",
  "titre": "string",
  "description": "string",
  "adresse": { "rue": "string", "codePostal": "string", "ville": "string", "pays": "string" },
  "prixParNuit": 0,
  "capacite": 1,
  "typeMaison": "APPARTEMENT|MAISON|STUDIO|...",
  "estActive": true,
  "equipements": ["string"],
  "images": ["url"],
  "latitude": 0,
  "longitude": 0,
  "locateur": { "id": "uuid", "nom": "string", "noteMoyenne": 4.7 }
}
```

- `CreationLocateurDTO` / `CreationLocataireDTO` / `CreationAdminDTO`: champs d’identité + spécifiques (voir contrôleurs)
- `ModificationProfilDTO` / `Modification*DTO`: champs partiels facultatifs
- `PeriodeReserveeDTO`: `{ "dateArrivee": "date", "dateDepart": "date" }`


### Enums
- `Role`: `ADMIN`, `LOCATEUR`, `LOCATAIRE`
- `StatutReservation`: `EN_ATTENTE`, `CONFIRMEE`, `ANNULEE`, `TERMINEE`, `EN_COURS`
- `ModePaiement`: `CARTE_BANCAIRE`, `PAYPAL`, `VIREMENT`, `PAIEMENT_SUR_PLACE`, `CHEQUE`
- `TypeMaison`: `APPARTEMENT`, `MAISON`, `STUDIO`, `LOFT`, `VILLA`, `CHALET`, `...`
- `StatutKYC`: `NON_VÉRIFIÉ`, `EN_ATTENTE`, `VALIDÉ`, `REJETÉ`


### Relations et règles métier clés
- Un `Locateur` possède plusieurs `Annonces`.
- Un `Locataire` peut réserver n’importe quelle `Annonce` disponible et gérer des favoris.
- Une `Réservation` relie un `Locataire` à une `Annonce` avec un `Statut` et des montants calculés.
- Disponibilité: vérifiée par `/api/reservations/disponibilite` + périodes/jours réservés par annonce.
- Mise à jour des statuts via endpoints dédiés (`confirmer`, `annuler`, `mettre-en-cours`, `terminer`, `statut`).


### Parcours type Frontend
- Navigation non connectée: recherche d’annonces, détails, vérification disponibilité.
- Auth: login → stocker `token` (si JWT) → appels authentifiés (si applicable dans votre backend).
- Locataire: créer réservation (récapitulatif → création), consulter mes réservations (futures, en cours, passées), gérer favoris.
- Locateur: créer/éditer mes annonces, lister réservations liées à mes annonces par statuts, gérer activation/désactivation d’annonces.
- Admin: gestion utilisateurs, statistiques simples via endpoints dédiés.


### Notes d’intégration
- Tous les endpoints exposent `@CrossOrigin(origins = "*")`: appels cross-domain autorisés.
- Identifiants: `UUID` (string). Dates: `yyyy-MM-dd`, DateTime: ISO-8601.
- Enum en entrée: utilisez les valeurs exactes (ex: `PAIEMENT_SUR_PLACE`).
- Certains endpoints exigent des `query params` (ex: `locateurId` pour mise à jour/suppression d’annonce).
- Les réponses d’erreur peuvent être `400` avec message, `403` (annonces non autorisées), `404` (non trouvé), `500` (erreur interne).

### CAN 2025 – Parcours et besoins utilisateurs

Cette section synthétise les besoins du front pendant un évènement à forte affluence (CAN 2025), en s’appuyant sur les endpoints déjà disponibles. L’objectif est d’offrir un parcours rapide: recherche près d’un stade, vérification disponibilité, réservation express et suivi.

#### Parcours clés
- Recherche près d’un stade
  - Utiliser: `GET /api/annonces/recherche/stade?stade=<nomStade>`
  - Alternative coordonnées: `GET /api/annonces/recherche/proximite?latitude=<lat>&longitude=<lon>&distanceMax=<km>`
- Filtrer par budget et capacité
  - Utiliser: `GET /api/annonces/recherche?ville=&typeMaison=&prixMax=&capaciteMin=&noteMin=`
- Vérifier la disponibilité aux dates de match
  - Check instantané: `GET /api/reservations/disponibilite?annonceId=&dateArrivee=&dateDepart=`
  - Calendrier UI: `GET /api/reservations/annonce/{annonceId}/periodes-futures?statuts=EN_ATTENTE,CONFIRMEE,EN_COURS`
- Réserver rapidement
  - Devis/récap: `POST /api/reservations/recapitulatif`
  - Création: `POST /api/reservations?locataireId=<UUID>`
- Gérer et suivre
  - Segments côté locataire: `GET /api/reservations/locataire/{locataireId}/futures|en-cours|passees`
  - Favoris (sauvegarde avant achat): `POST/DELETE /api/locataires/{locataireId}/favoris/{annonceId}` + `GET /api/locataires/{locataireId}/favoris`

Notes utiles côté UI: `AnnonceDTO` expose `stadePlusProche` et `distanceStade` (voir exemple de `CreerAnnonceDTO`) pour signaler la proximité au stade.

#### Exemples d’appels prêts à l’emploi

1) Rechercher par stade
```bash
curl -X GET "http://<HOST>/api/annonces/recherche/stade?stade=Stade%20de%20Casablanca"
```

2) Rechercher par proximité (rayon 3 km depuis coordonnées)
```bash
curl -X GET "http://<HOST>/api/annonces/recherche/proximite?latitude=33.5731&longitude=-7.5898&distanceMax=3"
```

3) Affiner par budget et capacité
```bash
curl -X GET "http://<HOST>/api/annonces/recherche?ville=Casablanca&prixMax=100&capaciteMin=2&typeMaison=APPARTEMENT"
```

4) Vérifier disponibilité (période du match)
```bash
curl -X GET "http://<HOST>/api/reservations/disponibilite?annonceId=<ANNONCE_UUID>&dateArrivee=2025-06-20&dateDepart=2025-06-22"
```

5) Afficher un calendrier des périodes bloquées (futures)
```bash
curl -X GET "http://<HOST>/api/reservations/annonce/<ANNONCE_UUID>/periodes-futures?statuts=EN_ATTENTE,CONFIRMEE,EN_COURS"
```

6) Obtenir un récapitulatif de réservation
```bash
curl -X POST "http://<HOST>/api/reservations/recapitulatif" \
  -H "Content-Type: application/json" \
  -d '{
    "annonceId": "<ANNONCE_UUID>",
    "dateArrivee": "2025-06-20",
    "dateDepart": "2025-06-22",
    "nombreVoyageurs": 2,
    "modePaiement": "PAIEMENT_SUR_PLACE",
    "fraisService": 10.0,
    "fraisNettoyage": 15.0,
    "fraisDepot": 100.0
  }'
```

7) Créer la réservation (après récap)
```bash
curl -X POST "http://<HOST>/api/reservations?locataireId=<LOCATAIRE_UUID>" \
  -H "Content-Type: application/json" \
  -d '{
    "annonceId": "<ANNONCE_UUID>",
    "dateArrivee": "2025-06-20",
    "dateDepart": "2025-06-22",
    "nombreVoyageurs": 2,
    "modePaiement": "PAIEMENT_SUR_PLACE",
    "messageProprietaire": "Arrivée vers 19h",
    "fraisService": 10.0,
    "fraisNettoyage": 15.0,
    "fraisDepot": 100.0
  }'
```

#### Bonnes pratiques UI/UX pendant l’évènement
- Mettre en avant la distance au stade (badge "À 800 m du stade").
- Trier par distance et prix; indiquer les créneaux restants (ex: "3 logements disponibles").
- Désactiver/masquer les actions quand `disponibilite=false` pour les dates choisies.
- Proposer l’ajout aux favoris pour comparaison rapide.

#### Conseils d’intégration technique
- Pré-remplir la recherche avec le stade de la rencontre et les dates du match.
- Combiner `recherche/stade` avec `prixMax` et `capaciteMin` pour limiter le trafic.
- Cacher côté front les périodes renvoyées par `periodes-futures` pour un calendrier non réservable.
- Utiliser les segments `futures|en-cours|passees` pour la page "Mes réservations" pendant la CAN.

