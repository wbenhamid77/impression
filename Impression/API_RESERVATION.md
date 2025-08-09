# API de Réservation - Documentation

## Vue d'ensemble

Cette API permet de gérer le processus complet de réservation selon le cahier des charges fourni. Elle implémente toutes les étapes du processus de réservation, de la vérification de disponibilité à la confirmation finale.

## Architecture

### Entités principales

1. **Reservation** : Entité principale qui lie une annonce à un locataire avec des dates spécifiques
2. **Annonce** : Logement disponible à la location
3. **Locataire** : Utilisateur qui effectue la réservation
4. **Locateur** : Propriétaire du logement

### Relations

- Une **Annonce** peut avoir plusieurs **Reservations** (selon les dates)
- Un **Locataire** peut avoir plusieurs **Reservations**
- Une **Reservation** appartient à une seule **Annonce** et un seul **Locataire**

## Processus de réservation

### 1️⃣ Ouverture d'une page de réservation

**Endpoint :** `GET /api/annonces/{id}` (existant)

**Fonctionnalités :**
- Afficher les détails de l'annonce (titre, prix, photos, localisation)
- Vérifier la disponibilité en temps réel
- Calculer le prix total selon les dates

### 2️⃣ Sélection des dates

**Endpoint :** `GET /api/reservations/disponibilite`

**Paramètres :**
- `annonceId` : UUID de l'annonce
- `dateArrivee` : Date d'arrivée (format: YYYY-MM-DD)
- `dateDepart` : Date de départ (format: YYYY-MM-DD)

**Réponse :**
```json
{
  "disponible": true
}
```

### 3️⃣ Confirmation des informations

**Endpoint :** `POST /api/reservations/recapitulatif`

**Corps de la requête :**
```json
{
  "annonceId": "uuid-de-l-annonce",
  "dateArrivee": "2024-06-15",
  "dateDepart": "2024-06-20",
  "nombreVoyageurs": 2,
  "modePaiement": "PAIEMENT_SUR_PLACE",
  "messageProprietaire": "Bonjour, nous arrivons vers 14h",
  "fraisService": 25.00,
  "fraisNettoyage": 50.00,
  "fraisDepot": 200.00
}
```

**Réponse :**
```json
{
  "annonceId": "uuid-de-l-annonce",
  "titreAnnonce": "Appartement moderne au centre-ville",
  "adresseAnnonce": "123 Rue de la Paix, 75001 Paris, France",
  "nomLocateur": "Jean Dupont",
  "emailLocateur": "jean.dupont@email.com",
  "dateArrivee": "2024-06-15",
  "dateDepart": "2024-06-20",
  "nombreNuits": 5,
  "prixParNuit": 100.00,
  "prixTotal": 500.00,
  "fraisService": 25.00,
  "fraisNettoyage": 50.00,
  "fraisDepot": 200.00,
  "montantTotal": 775.00,
  "nombreVoyageurs": 2,
  "messageProprietaire": "Bonjour, nous arrivons vers 14h",
  "modePaiement": "Paiement sur place",
  "paiementEnLigne": false
}
```

### 4️⃣ Création de la réservation

**Endpoint :** `POST /api/reservations`

**Paramètres :**
- `locataireId` : UUID du locataire connecté

**Corps de la requête :** Même format que pour le récapitulatif

**Réponse :**
```json
{
  "id": "uuid-de-la-reservation",
  "annonceId": "uuid-de-l-annonce",
  "locataireId": "uuid-du-locataire",
  "dateArrivee": "2024-06-15",
  "dateDepart": "2024-06-20",
  "nombreNuits": 5,
  "prixParNuit": 100.00,
  "prixTotal": 500.00,
  "montantTotal": 775.00,
  "statut": "EN_ATTENTE",
  "modePaiement": "PAIEMENT_SUR_PLACE",
  "messageProprietaire": "Bonjour, nous arrivons vers 14h",
  "dateCreation": "2024-01-15T10:30:00",
  "nombreVoyageurs": 2,
  "titreAnnonce": "Appartement moderne au centre-ville",
  "adresseAnnonce": "123 Rue de la Paix, 75001 Paris, France",
  "nomLocateur": "Jean Dupont",
  "emailLocateur": "jean.dupont@email.com",
  "nomLocataire": "Marie Martin",
  "prenomLocataire": "Marie",
  "emailLocataire": "marie.martin@email.com"
}
```

### 5️⃣ Confirmation par le propriétaire

**Endpoint :** `PUT /api/reservations/{id}/confirmer`

**Réponse :**
```json
{
  "id": "uuid-de-la-reservation",
  "statut": "CONFIRMEE",
  "dateConfirmation": "2024-01-15T11:00:00",
  // ... autres champs
}
```

### 6️⃣ Gestion des réservations

#### Obtenir les réservations d'un locataire
**Endpoint :** `GET /api/reservations/locataire/{locataireId}`

#### Obtenir les réservations d'une annonce
**Endpoint :** `GET /api/reservations/annonce/{annonceId}`

#### Obtenir une réservation spécifique
**Endpoint :** `GET /api/reservations/{id}`

#### Annuler une réservation
**Endpoint :** `PUT /api/reservations/{id}/annuler?raison=Changement de plans`

## Statuts de réservation

- **EN_ATTENTE** : Réservation créée, en attente de confirmation du propriétaire
- **CONFIRMEE** : Réservation confirmée par le propriétaire
- **EN_COURS** : Séjour en cours
- **TERMINEE** : Séjour terminé
- **ANNULEE** : Réservation annulée

## Modes de paiement

- **CARTE_BANCAIRE** : Paiement par carte bancaire
- **PAYPAL** : Paiement via PayPal
- **VIREMENT** : Virement bancaire
- **PAIEMENT_SUR_PLACE** : Paiement sur place (par défaut)
- **CHEQUE** : Paiement par chèque

## Validation des données

### Règles de validation

1. **Dates :**
   - Date d'arrivée et de départ obligatoires
   - Dates doivent être dans le futur
   - Date de départ > Date d'arrivée

2. **Voyageurs :**
   - Nombre de voyageurs entre 1 et 20
   - Ne peut pas dépasser la capacité de l'annonce

3. **Disponibilité :**
   - Vérification automatique des conflits de dates
   - Une seule réservation active par période pour une annonce

### Gestion des erreurs

**Erreur de validation :**
```json
{
  "status": 400,
  "error": "Erreur de validation",
  "message": "Les données fournies ne sont pas valides",
  "timestamp": "2024-01-15T10:30:00",
  "fieldErrors": {
    "dateArrivee": "La date d'arrivée doit être dans le futur",
    "nombreVoyageurs": "Le nombre de voyageurs doit être au moins de 1"
  }
}
```

**Erreur de réservation :**
```json
{
  "status": 400,
  "error": "Erreur de réservation",
  "message": "L'annonce n'est pas disponible pour les dates sélectionnées",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Fonctionnalités avancées

### Vérification de disponibilité en temps réel
L'API vérifie automatiquement les conflits de dates avec les réservations existantes.

### Calcul automatique des prix
- Prix de base selon le nombre de nuits
- Ajout des frais optionnels (service, nettoyage, dépôt)
- Calcul du montant total

### Notifications
- Notification automatique au propriétaire lors de la création d'une réservation
- Possibilité d'ajouter des messages personnalisés

### Gestion des annulations
- Vérification des conditions d'annulation (24h avant l'arrivée)
- Enregistrement de la raison d'annulation
- Mise à jour automatique du statut

## Sécurité

- Validation des données côté serveur
- Vérification des permissions utilisateur
- Protection contre les réservations multiples sur la même période
- Logs détaillés pour audit

## Exemples d'utilisation

### Frontend - Processus de réservation

```javascript
// 1. Vérifier la disponibilité
const checkAvailability = async (annonceId, dateArrivee, dateDepart) => {
  const response = await fetch(`/api/reservations/disponibilite?annonceId=${annonceId}&dateArrivee=${dateArrivee}&dateDepart=${dateDepart}`);
  return response.json();
};

// 2. Créer le récapitulatif
const createRecap = async (reservationData) => {
  const response = await fetch('/api/reservations/recapitulatif', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(reservationData)
  });
  return response.json();
};

// 3. Confirmer la réservation
const confirmReservation = async (reservationData, locataireId) => {
  const response = await fetch(`/api/reservations?locataireId=${locataireId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(reservationData)
  });
  return response.json();
};
```

Cette API respecte entièrement le cahier des charges et permet une gestion complète du processus de réservation avec toutes les fonctionnalités demandées. 