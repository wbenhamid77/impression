## Transactions Paiement & Remboursement — Guide API (Plateforme / Locateur / Locataire)

Ce document décrit la logique fonctionnelle et les APIs pour:
- le split automatique 80% (locateur) / 20% (plateforme) lors d’un paiement confirmé,
- la génération d’instructions de remboursement selon la fenêtre d’annulation (<24h, 24–48h, >48h),
- la gestion des RIB (plateforme, locateur, locataire), et
- l’exécution manuelle des virements via des « instructions de transaction ».

Toutes les IDs sont des UUID. Les montants sont au format décimal (scale=2).

---

### Définitions

- RIB (entité `Rib`):
  - type: `PLATEFORME` | `LOCATEUR` | `LOCATAIRE`
  - titulaire: selon le type (locateur/locataire) ou aucun pour plateforme
  - champs principaux: `iban`, `bic`, `titulaireNom`, `banque`, `actif`, `defautCompte`

- Instruction de transaction (entité `TransactionInstruction`):
  - type (`TransactionType`):
    - `PAYOUT_LOCATEUR`: virement 80% vers le locateur
    - `COMMISSION_PLATEFORME`: 20% conservé par la plateforme (enregistrement comptable)
    - `REFUND_LOCATAIRE_FROM_LOCATEUR`: remboursement vers le locataire depuis le locateur
    - `REFUND_LOCATAIRE_FROM_PLATEFORME`: remboursement vers le locataire depuis la plateforme
  - statut (`TransactionStatus`): `PENDING` | `EXECUTED` | `CANCELLED`
  - `fromRib` → `toRib` et `montant`

---

### Pré-requis

1) Un RIB plateforme par défaut doit exister.
2) Chaque locateur doit avoir au moins un RIB (un RIB par défaut conseillé).
3) Chaque locataire doit avoir un RIB par défaut si des remboursements doivent lui être versés.

Exemple de création du RIB plateforme par défaut:
```json
POST /api/ribs
{
  "type": "PLATEFORME",
  "iban": "FR7630006000011234567890189",
  "bic": "AGRIFRPPXXX",
  "titulaireNom": "IMPRESSION PLATFORM",
  "banque": "Credit Agricole",
  "defautCompte": true
}
```

---

### Flux fonctionnels

#### A) Paiement confirmé → Split 80/20

- Au moment de la confirmation d’un paiement (statut `PAYE`), deux instructions sont générées:
  - 80%: `PAYOUT_LOCATEUR` (from: RIB plateforme → to: RIB par défaut du locateur)
  - 20%: `COMMISSION_PLATEFORME` (from: RIB plateforme → to: RIB plateforme)

Cela est déclenché automatiquement dans `PaiementService.confirmerPaiement`, et peut aussi être (re)généré manuellement via:
```
POST /api/payouts/generate/split/{paiementId}
```

Réponse (exemple abrégé):
```json
[
  {
    "id": "...",
    "paiementId": "...",
    "reservationId": "...",
    "type": "PAYOUT_LOCATEUR",
    "statut": "PENDING",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_LOCATEUR>",
    "montant": 800.00
  },
  {
    "id": "...",
    "paiementId": "...",
    "reservationId": "...",
    "type": "COMMISSION_PLATEFORME",
    "statut": "PENDING",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_PLATEFORME>",
    "montant": 200.00
  }
]
```
(Ex.: total = 1000 → 800/200)

#### B) Annulation → Remboursements selon la fenêtre

Soit `hoursBefore = heures` avant la date d’arrivée.

- `< 24h`: aucun remboursement
- `24h ≤ hoursBefore < 48h`: remboursement 50% du total au locataire
  - 40% depuis le RIB du locateur → locataire (`REFUND_LOCATAIRE_FROM_LOCATEUR`)
  - 10% depuis le RIB de la plateforme → locataire (`REFUND_LOCATAIRE_FROM_PLATEFORME`)
- `≥ 48h`: remboursement 100% du total au locataire
  - 80% depuis le RIB du locateur → locataire
  - 20% depuis le RIB de la plateforme → locataire

Déclenché automatiquement dans `ReservationService.annulerReservation` s’il existe un paiement `PAYE`.
API pour (re)générer manuellement:
```
POST /api/payouts/generate/remboursement/{reservationId}
```

Exemples de réponses:
- Cas 24–48h (total 1000):
```json
[
  {
    "type": "REFUND_LOCATAIRE_FROM_LOCATEUR",
    "fromRibId": "<RIB_LOCATEUR>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 400.00,
    "statut": "PENDING"
  },
  {
    "type": "REFUND_LOCATAIRE_FROM_PLATEFORME",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 100.00,
    "statut": "PENDING"
  }
]
```

- Cas ≥48h (total 1000):
```json
[
  {
    "type": "REFUND_LOCATAIRE_FROM_LOCATEUR",
    "fromRibId": "<RIB_LOCATEUR>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 800.00,
    "statut": "PENDING"
  },
  {
    "type": "REFUND_LOCATAIRE_FROM_PLATEFORME",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 200.00,
    "statut": "PENDING"
  }
]
```

- Cas <24h: `[]` (aucune instruction)

---

### APIs — Détail et exemples

#### 1) RIB

- Créer un RIB
```
POST /api/ribs
Content-Type: application/json
```
Request:
```json
{
  "type": "LOCATEUR",          // PLATEFORME | LOCATEUR | LOCATAIRE
  "locateurId": "<UUID>",      // requis si type LOCATEUR
  "locataireId": "<UUID>",     // requis si type LOCATAIRE
  "iban": "FR76...",
  "bic": "AGRIFRPPXXX",
  "titulaireNom": "DUPONT PIERRE",
  "banque": "Credit Agricole",
  "defautCompte": true
}
```
Response (200):
```json
{
  "id": "<UUID>",
  "type": "LOCATEUR",
  "locateurId": "<UUID>",
  "locataireId": null,
  "iban": "FR76...",
  "bic": "AGRIFRPPXXX",
  "titulaireNom": "DUPONT PIERRE",
  "banque": "Credit Agricole",
  "actif": true,
  "defautCompte": true,
  "dateCreation": "2025-10-22T12:34:56",
  "dateModification": "2025-10-22T12:34:56"
}
```

- Définir un RIB comme défaut
```
POST /api/ribs/{ribId}/defaut
```
Response: `RibDTO` (même structure que ci-dessus)

- Récupérer le RIB plateforme par défaut
```
GET /api/ribs/platform/default
```

- Lister les RIB actifs d’un locateur
```
GET /api/ribs/locateur/{locateurId}
```

- Lister les RIB actifs d’un locataire
```
GET /api/ribs/locataire/{locataireId}
```

#### 2) Instructions de paiement / remboursement

- Générer split 80/20 pour un paiement
```
POST /api/payouts/generate/split/{paiementId}
```
Response: `TransactionInstructionDTO[]`

- Générer instructions de remboursement pour une réservation annulée
```
POST /api/payouts/generate/remboursement/{reservationId}
```
Response: `TransactionInstructionDTO[]` (selon les cas <24h/24–48h/≥48h)

- Lister les instructions en attente
```
GET /api/payouts/pending
```
Response: `TransactionInstructionDTO[]`

- Marquer une instruction comme exécutée
```
POST /api/payouts/{instructionId}/executer
Content-Type: application/json
```
Request:
```json
{ "reference": "VIR-2025-000123" }
```
Response: `TransactionInstructionDTO` (statut: `EXECUTED`, `dateExecution` renseignée)

- Annuler une instruction
```
POST /api/payouts/{instructionId}/annuler
Content-Type: application/json
```
Request:
```json
{ "notes": "annulé par l'admin" }
```
Response: `TransactionInstructionDTO` (statut: `CANCELLED`)

---

### Modèles de données (DTO)

`TransactionInstructionDTO` (réponse API):
```json
{
  "id": "<UUID>",
  "reservationId": "<UUID>",
  "paiementId": "<UUID>",
  "type": "PAYOUT_LOCATEUR | COMMISSION_PLATEFORME | REFUND_LOCATAIRE_FROM_LOCATEUR | REFUND_LOCATAIRE_FROM_PLATEFORME",
  "statut": "PENDING | EXECUTED | CANCELLED",
  "fromRibId": "<UUID>",
  "toRibId": "<UUID>",
  "montant": 123.45,
  "reference": "optionnel",
  "notes": "optionnel",
  "dateCreation": "2025-10-22T12:34:56",
  "dateModification": "2025-10-22T12:34:56",
  "dateExecution": "2025-10-22T12:45:00"
}
```

`RibDTO` (réponse API): cf. section RIB.

---

### Codes d’erreur & validations (exemples)

- 404 ResourceNotFound
```json
{
  "timestamp": "2025-10-22T12:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "RIB plateforme par défaut introuvable",
  "path": "/api/payouts/generate/split/..."
}
```

- 400 BadRequest (ex: règles métier violées)
```json
{
  "timestamp": "2025-10-22T12:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La réservation ne peut pas être annulée",
  "path": "/api/reservations/.../annuler"
}
```

---

### Bonnes pratiques & remarques

- Assurez-vous de définir le RIB plateforme avec de vrais IBAN/BIC.
- Définissez un RIB par défaut pour chaque locateur (payout) et locataire (remboursement).
- L’exécution des virements est manuelle: utilisez les endpoints `executer`/`annuler` pour tracer l’état.
- Les montants sont arrondis au centime (`HALF_UP`).
- Si vous rejouez des générations, dédupliquez côté back-office en filtrant les instructions existantes selon vos règles.


