# Système de Paiement - Impression

## Vue d'ensemble

Le système de paiement a été intégré à l'application Impression pour gérer automatiquement les paiements des réservations. Quand un locateur confirme une réservation, un paiement est automatiquement créé et doit être effectué par le locataire dans les 24 heures.

## Fonctionnalités principales

### 1. Création automatique de paiement
- Quand une réservation est confirmée par le locateur, un paiement est automatiquement créé
- Le paiement expire dans 24 heures
- Si le paiement n'est pas effectué dans les délais, la réservation est automatiquement annulée

### 2. Types de paiement
- **ACOMPTE** : Paiement partiel
- **SOLDE** : Paiement du solde restant
- **TOTAL** : Paiement complet (utilisé par défaut)
- **REMBOURSEMENT** : Remboursement d'un paiement

### 3. Statuts de paiement
- **EN_ATTENTE** : Paiement créé, en attente de traitement
- **EN_COURS** : Paiement en cours de traitement
- **PAYE** : Paiement effectué avec succès
- **ECHEC** : Échec du paiement
- **ANNULE** : Paiement annulé
- **REMBOURSE** : Paiement remboursé
- **EXPIRE** : Paiement expiré (après 24h)

### 4. Modes de paiement supportés
- Carte bancaire
- PayPal
- Virement bancaire
- Paiement sur place
- Chèque

## API Endpoints

### Paiements
- `POST /api/paiements` - Créer un paiement
- `PUT /api/paiements/{id}/confirmer` - Confirmer un paiement
- `PUT /api/paiements/{id}/en-cours` - Marquer comme en cours
- `PUT /api/paiements/{id}/echec` - Marquer comme échec
- `PUT /api/paiements/{id}/annuler` - Annuler un paiement
- `PUT /api/paiements/{id}/rembourser` - Rembourser un paiement
- `GET /api/paiements/{id}` - Obtenir un paiement par ID
- `GET /api/paiements/reservation/{reservationId}` - Paiements d'une réservation
- `GET /api/paiements/locataire/{locataireId}` - Paiements d'un locataire
- `GET /api/paiements/locateur/{locateurId}` - Paiements d'un locateur
- `GET /api/paiements/locataire/{locataireId}/en-attente` - Paiements en attente d'un locataire
- `GET /api/paiements/locateur/{locateurId}/en-attente` - Paiements en attente d'un locateur
- `GET /api/paiements/expires` - Paiements expirés
- `POST /api/paiements/marquer-expires` - Marquer les paiements expirés

### Réservations (endpoints mis à jour)
- `GET /api/reservations/{id}/paiements` - Paiements d'une réservation

## Flux de travail

### 1. Confirmation de réservation
1. Le locateur confirme une réservation via `PUT /api/reservations/{id}/confirmer`
2. Un paiement est automatiquement créé avec le montant total de la réservation
3. Le paiement est visible pour le locataire et le locateur

### 2. Paiement par le locataire
1. Le locataire peut voir ses paiements en attente via `GET /api/paiements/locataire/{id}/en-attente`
2. Le locataire effectue le paiement (intégration avec passerelle de paiement)
3. Le paiement est confirmé via `PUT /api/paiements/{id}/confirmer`

### 3. Gestion des expirations
1. Un scheduler vérifie toutes les 30 minutes les paiements expirés
2. Les paiements non payés dans les 24h sont marqués comme expirés
3. Les réservations associées sont automatiquement annulées

## Configuration du scheduler

Le scheduler est configuré pour :
- Vérifier les paiements expirés toutes les 30 minutes
- Vérifier les paiements critiques toutes les heures
- Marquer automatiquement les paiements expirés
- Annuler les réservations non payées

## Sécurité

- Les paiements sont liés à des réservations spécifiques
- Seuls les propriétaires des réservations peuvent voir leurs paiements
- Les paiements expirés sont automatiquement traités
- Logs détaillés pour le suivi des paiements

## Intégration future

Le système est conçu pour être facilement intégré avec :
- Passerelles de paiement (Stripe, PayPal, etc.)
- Systèmes de notification (email, SMS)
- Systèmes de reporting financier
- APIs de remboursement

## Exemples d'utilisation

### Créer un paiement manuellement
```json
POST /api/paiements
{
  "reservationId": "uuid-de-la-reservation",
  "montant": 150.00,
  "typePaiement": "TOTAL",
  "modePaiement": "CARTE_BANCAIRE",
  "description": "Paiement pour réservation"
}
```

### Confirmer un paiement
```json
PUT /api/paiements/{id}/confirmer
{
  "numeroTransaction": "TXN123456789",
  "referenceExterne": "REF123456",
  "metadonnees": "{\"gateway\": \"stripe\"}"
}
```

### Rembourser un paiement
```json
PUT /api/paiements/{id}/rembourser
{
  "numeroRemboursement": "REF123456789",
  "raisonRemboursement": "Annulation par le client",
  "metadonnees": "{\"gateway\": \"stripe\"}"
}
```
