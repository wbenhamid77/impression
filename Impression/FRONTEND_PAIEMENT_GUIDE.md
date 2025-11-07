# Guide Frontend - Flux de Paiement et Split 80/20

## Vue d'ensemble

Lorsqu'un locataire effectue un paiement pour une réservation, le système génère automatiquement des instructions de transaction qui répartissent les fonds :
- **80%** du montant va au locateur (propriétaire)
- **20%** du montant reste sur la plateforme (commission)

Ce document explique comment intégrer ce flux dans votre application frontend.

---

## 📋 Flux complet du paiement

```
1. Locataire crée une réservation (statut: EN_ATTENTE)
   ↓
2. Locateur confirme la réservation (statut: CONFIRMEE)
   ↓
3. FRONTEND: Créer un paiement
   → Backend génère automatiquement 2 instructions (80/20) en statut PENDING
   ↓
4. FRONTEND: Locataire effectue le paiement via PSP (Stripe, PayPal, etc.)
   ↓
5. FRONTEND: Confirmer le paiement avec la référence transaction
   → Backend marque le paiement comme PAYE
   → Backend marque les instructions 80/20 comme EXECUTED
   → Backend génère une instruction PAYIN (locataire → plateforme)
```

---

## 🎯 Endpoints à utiliser

### 1. Créer un paiement

**Endpoint:** `POST /api/paiements`

**Quand l'utiliser:** Après que la réservation est confirmée (statut CONFIRMEE), avant de rediriger vers le PSP.

**Body (CreationPaiementDTO):**
```json
{
  "reservationId": "uuid-de-la-reservation",
  "montant": 350.00,
  "typePaiement": "TOTAL",
  "modePaiement": "CARTE_BANCAIRE",
  "description": "Paiement pour réservation Studio Paris 3 nuits",
  "metadonnees": "{\"source\":\"web\",\"device\":\"mobile\"}"
}
```

**Réponse (PaiementDTO):**
```json
{
  "id": "paiement-uuid",
  "reservationId": "reservation-uuid",
  "montant": 350.00,
  "typePaiement": "TOTAL",
  "statut": "EN_ATTENTE",
  "modePaiement": "CARTE_BANCAIRE",
  "numeroTransaction": null,
  "dateCreation": "2025-10-25T14:30:00",
  "dateExpiration": "2025-10-26T14:30:00",
  "heuresRestantes": 24,
  "estExpire": false,
  "peutEtreAnnule": true,
  "peutEtreRembourse": false
}
```

**⚠️ Important:** À cette étape, le backend crée automatiquement 2 instructions de transaction en base :
- Instruction 1: 280€ (80%) pour le locateur (statut: PENDING)
- Instruction 2: 70€ (20%) pour la plateforme (statut: PENDING)

---

### 2. Confirmer le paiement

**Endpoint:** `PUT /api/paiements/{paiementId}/confirmer`

**Quand l'utiliser:** Après que le PSP (Stripe, PayPal, etc.) a validé le paiement et vous a renvoyé une référence de transaction.

**Body (ConfirmationPaiementDTO):**
```json
{
  "numeroTransaction": "TRX-STRIPE-20251025-ABC123",
  "referenceExterne": "pi_3Abc123Def456Ghi789",
  "metadonnees": "{\"psp\":\"stripe\",\"payment_method\":\"card_visa\"}"
}
```

**Réponse (PaiementDTO):**
```json
{
  "id": "paiement-uuid",
  "reservationId": "reservation-uuid",
  "montant": 350.00,
  "typePaiement": "TOTAL",
  "statut": "PAYE",
  "modePaiement": "CARTE_BANCAIRE",
  "numeroTransaction": "TRX-STRIPE-20251025-ABC123",
  "referenceExterne": "pi_3Abc123Def456Ghi789",
  "datePaiement": "2025-10-25T14:35:00",
  "dateCreation": "2025-10-25T14:30:00",
  "dateExpiration": "2025-10-26T14:30:00",
  "estExpire": false,
  "peutEtreAnnule": false,
  "peutEtreRembourse": true
}
```

**⚠️ Important:** À cette étape, le backend :
1. Marque le paiement comme PAYE
2. Marque les 2 instructions 80/20 comme EXECUTED avec la référence transaction
3. Crée une instruction PAYIN (argent du locataire vers la plateforme)

---

## 📊 Vérifier les transactions générées

### Consulter les instructions de transaction d'un paiement

**Endpoint:** `GET /api/payouts/pending` (toutes les instructions en attente)

Ou utilisez les endpoints spécifiques pour voir les encaissements :

**Pour un locateur:**
```bash
GET /api/payouts/encaissements/locateur/{locateurId}
GET /api/payouts/solde/locateur/{locateurId}
```

**Pour la plateforme:**
```bash
GET /api/payouts/encaissements/plateforme
GET /api/payouts/solde/plateforme
```

**Réponse (TransactionInstructionDTO[]):**
```json
[
  {
    "id": "instruction-uuid-1",
    "reservationId": "reservation-uuid",
    "paiementId": "paiement-uuid",
    "type": "PAYOUT_LOCATEUR",
    "statut": "EXECUTED",
    "fromRibId": "rib-plateforme-uuid",
    "toRibId": "rib-locateur-uuid",
    "montant": 280.00,
    "reference": "TRX-STRIPE-20251025-ABC123",
    "notes": "Split 80% au locateur",
    "dateCreation": "2025-10-25T14:30:00",
    "dateExecution": "2025-10-25T14:35:00"
  },
  {
    "id": "instruction-uuid-2",
    "reservationId": "reservation-uuid",
    "paiementId": "paiement-uuid",
    "type": "COMMISSION_PLATEFORME",
    "statut": "EXECUTED",
    "fromRibId": "rib-plateforme-uuid",
    "toRibId": "rib-plateforme-uuid",
    "montant": 70.00,
    "reference": "TRX-STRIPE-20251025-ABC123",
    "notes": "Commission 20% plateforme",
    "dateCreation": "2025-10-25T14:30:00",
    "dateExecution": "2025-10-25T14:35:00"
  }
]
```

---

## 🔄 Diagramme de séquence

```
Frontend          Backend            Base de données       PSP (Stripe/PayPal)
   |                 |                      |                      |
   |-- POST /api/paiements ----------------->|                      |
   |                 |                      |                      |
   |                 |-- save(paiement) --->|                      |
   |                 |<---------------------|                      |
   |                 |                      |                      |
   |                 |-- generateSplit() -->|                      |
   |                 |-- save(2 instructions: PENDING) ----------->|
   |                 |<---------------------|                      |
   |                 |                      |                      |
   |<-- PaiementDTO (EN_ATTENTE) ----------|                      |
   |                 |                      |                      |
   |-- Rediriger vers PSP -------------------------------->|
   |                                                        |
   |<-- Callback PSP avec transaction ref ------------------|
   |                 |                      |                      |
   |-- PUT /api/paiements/{id}/confirmer -->|                      |
   |                 |                      |                      |
   |                 |-- marquerCommePaye() ->|                     |
   |                 |-- generatePayin() ----->|                    |
   |                 |-- markExecuted(80/20) ->|                    |
   |                 |<---------------------|                      |
   |                 |                      |                      |
   |<-- PaiementDTO (PAYE) ----------------|                      |
   |                 |                      |                      |
```

---

## 🛠️ Exemple d'intégration React/Vue/Angular

### Étape 1 : Créer le paiement

```javascript
// Après confirmation de la réservation
async function creerPaiement(reservation) {
  const response = await fetch('http://localhost:8080/api/paiements', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': `Bearer ${token}` // si JWT activé
    },
    body: JSON.stringify({
      reservationId: reservation.id,
      montant: reservation.montantTotal,
      typePaiement: 'TOTAL',
      modePaiement: 'CARTE_BANCAIRE',
      description: `Paiement ${reservation.titreAnnonce} - ${reservation.nombreNuits} nuits`,
      metadonnees: JSON.stringify({
        source: 'web',
        userAgent: navigator.userAgent
      })
    })
  });

  const paiement = await response.json();
  
  // Sauvegarder l'ID du paiement pour confirmation ultérieure
  sessionStorage.setItem('paiementEnCours', paiement.id);
  
  return paiement;
}
```

### Étape 2 : Rediriger vers le PSP (exemple Stripe)

```javascript
async function redirigerVersStripe(paiement) {
  // Initialiser Stripe
  const stripe = Stripe('pk_test_...');
  
  // Créer une session Stripe (vous devez avoir un endpoint backend pour ça)
  const session = await fetch('/api/stripe/create-checkout-session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      paiementId: paiement.id,
      montant: paiement.montant,
      description: paiement.description
    })
  }).then(res => res.json());
  
  // Rediriger vers Stripe Checkout
  await stripe.redirectToCheckout({ sessionId: session.id });
}
```

### Étape 3 : Confirmer le paiement (callback après PSP)

```javascript
// Page de retour après succès Stripe
async function confirmerPaiement(transactionRef, referenceExterne) {
  const paiementId = sessionStorage.getItem('paiementEnCours');
  
  const response = await fetch(`http://localhost:8080/api/paiements/${paiementId}/confirmer`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      numeroTransaction: transactionRef,
      referenceExterne: referenceExterne,
      metadonnees: JSON.stringify({
        psp: 'stripe',
        confirmedAt: new Date().toISOString()
      })
    })
  });

  const paiementConfirme = await response.json();
  
  // Nettoyer la session
  sessionStorage.removeItem('paiementEnCours');
  
  // Afficher la confirmation
  afficherPageSucces(paiementConfirme);
  
  return paiementConfirme;
}
```

---

## ⚠️ Cas d'erreur à gérer

### Erreur 1 : Réservation non confirmée
```json
{
  "error": "La réservation doit être confirmée pour créer un paiement",
  "status": 400
}
```
**Solution:** Vérifier que `reservation.statut === "CONFIRMEE"` avant de créer le paiement.

---

### Erreur 2 : Paiement déjà en attente
```json
{
  "error": "Un paiement est déjà en attente pour cette réservation",
  "status": 400
}
```
**Solution:** Récupérer le paiement existant via `GET /api/paiements/reservation/{reservationId}` et l'utiliser.

---

### Erreur 3 : Paiement expiré
```json
{
  "error": "Le paiement a expiré",
  "status": 400
}
```
**Solution:** Créer un nouveau paiement. Les paiements expirent après 24h par défaut.

---

### Erreur 4 : RIB manquant
```json
{
  "error": "RIB par défaut du locateur introuvable",
  "status": 404
}
```
**Solution:** Le locateur doit d'abord configurer son RIB via `POST /api/ribs`.

---

## 📦 Types TypeScript (optionnel)

```typescript
interface CreationPaiementDTO {
  reservationId: string;
  montant: number;
  typePaiement: 'TOTAL' | 'ACOMPTE' | 'SOLDE';
  modePaiement: 'CARTE_BANCAIRE' | 'PAYPAL' | 'VIREMENT' | 'PAIEMENT_SUR_PLACE' | 'CHEQUE';
  description: string;
  metadonnees?: string; // JSON stringifié
}

interface ConfirmationPaiementDTO {
  numeroTransaction: string;
  referenceExterne?: string;
  metadonnees?: string; // JSON stringifié
}

interface PaiementDTO {
  id: string;
  reservationId: string;
  montant: number;
  typePaiement: string;
  statut: 'EN_ATTENTE' | 'EN_COURS' | 'PAYE' | 'ECHEC' | 'ANNULE' | 'EXPIRE' | 'REMBOURSE';
  modePaiement: string;
  numeroTransaction?: string;
  referenceExterne?: string;
  description: string;
  dateCreation: string;
  dateModification: string;
  datePaiement?: string;
  dateExpiration: string;
  dateEchec?: string;
  numeroRemboursement?: string;
  dateRemboursement?: string;
  raisonRemboursement?: string;
  metadonnees?: string;
  estExpire: boolean;
  peutEtreAnnule: boolean;
  peutEtreRembourse: boolean;
  heuresRestantes?: number;
  // Infos enrichies
  titreAnnonce?: string;
  adresseAnnonce?: string;
  nomLocataire?: string;
  prenomLocataire?: string;
  emailLocataire?: string;
  nomLocateur?: string;
  prenomLocateur?: string;
  emailLocateur?: string;
  dateArrivee?: string;
  dateDepart?: string;
  nombreNuits?: number;
}

interface TransactionInstructionDTO {
  id: string;
  reservationId?: string;
  paiementId?: string;
  type: 'PAYIN_PLATEFORME' | 'PAYOUT_LOCATEUR' | 'COMMISSION_PLATEFORME' | 
        'REFUND_LOCATAIRE_FROM_LOCATEUR' | 'REFUND_LOCATAIRE_FROM_PLATEFORME';
  statut: 'PENDING' | 'EXECUTED' | 'FAILED' | 'CANCELLED';
  fromRibId?: string;
  toRibId?: string;
  montant: number;
  reference?: string;
  notes?: string;
  dateCreation: string;
  dateModification: string;
  dateExecution?: string;
}
```

---

## 🔍 Endpoints supplémentaires utiles

### Consulter un paiement
```bash
GET /api/paiements/{paiementId}
```

### Lister les paiements d'une réservation
```bash
GET /api/paiements/reservation/{reservationId}
```

### Lister les paiements d'un locataire
```bash
GET /api/paiements/locataire/{locataireId}
```

### Annuler un paiement (avant confirmation)
```bash
PUT /api/paiements/{paiementId}/annuler?raison=Annulation+utilisateur
```

### Consulter le solde d'un locateur
```bash
GET /api/payouts/solde/locateur/{locateurId}
```

**Réponse:**
```json
{
  "entrees": 2800.00,
  "sorties": 0.00,
  "solde": 2800.00
}
```

---

## 📝 Prérequis système

Avant d'utiliser ce flux, assurez-vous que :

1. ✅ Un RIB plateforme existe (créé automatiquement au démarrage par `PlatformRibDataLoader`)
2. ✅ Chaque locateur a configuré au moins un RIB avec `defautCompte: true`
3. ✅ Les réservations sont au statut `CONFIRMEE` avant création de paiement
4. ✅ Vous avez intégré un PSP (Stripe, PayPal, etc.) pour traiter les paiements réels

---

## 🎨 Recommandations UX

### Page de paiement
- Afficher le récapitulatif : montant total, commission plateforme (20%), montant au locateur (80%)
- Afficher le timer d'expiration (24h)
- Proposer plusieurs modes de paiement

### Après paiement
- Afficher une page de succès avec le numéro de transaction
- Envoyer un email de confirmation
- Rediriger vers la page de réservation avec statut mis à jour

### En cas d'échec
- Proposer de réessayer
- Afficher un message clair d'erreur
- Logger l'erreur côté backend pour support

---

## 📞 Support

Pour toute question sur l'intégration, contacter l'équipe backend ou consulter :
- `PAIEMENT_TRANSACTIONS_SOLDES.md` : logique complète des transactions
- `LOGIQUE_PAIEMENT_REMBOURSEMENT.md` : gestion des remboursements
- `API_REFERENCE.md` : référence complète de l'API

---

**Dernière mise à jour :** 25 octobre 2025  
**Version :** 1.0


