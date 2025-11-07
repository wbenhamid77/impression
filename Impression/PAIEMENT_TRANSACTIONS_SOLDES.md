## Paiements → Transactions → Soldes

### 1) Résumé opérationnel
- Lorsqu’un paiement est confirmé, l’application enregistre automatiquement trois transactions financières et les marque payées:
  - PAYIN_PLATEFORME: locataire → plateforme (100%)
  - PAYOUT_LOCATEUR: plateforme → locateur (80%)
  - COMMISSION_PLATEFORME: plateforme → plateforme (20%)
- Les encaissements et soldes (entrées/sorties/net) sont consultables pour: locataire, locateur, plateforme.

### 2) Flux de confirmation
Endpoint: `PUT /api/paiements/{paiementId}/confirmer`
Payload minimal:
```json
{
  "numeroTransaction": "PAY-UNIQUE",
  "referenceExterne": "OPTIONNEL",
  "metadonnees": "{\"gateway\":\"Xyz\"}"
}
```
Étapes exécutées côté serveur:
1. Validation: `numeroTransaction` doit être unique (sinon 400).
2. Le paiement passe au statut PAYE avec ce numéro.
3. Création d’une transaction PAYIN (locataire→plateforme) avec statut EXECUTED et même référence.
4. Génération du split 80/20 (PAYOUT_LOCATEUR et COMMISSION_PLATEFORME).
5. Toutes les instructions liées à ce paiement sont marquées EXECUTED et datées.

Résultat attendu dans `transaction_instructions`: 3 lignes (PAYIN + 2 split), toutes `EXECUTED`.

### 3) Schéma des transactions
- `type`: `PAYIN_PLATEFORME` | `PAYOUT_LOCATEUR` | `COMMISSION_PLATEFORME` | (remboursements: `REFUND_*`)
- `statut`: `PENDING` | `EXECUTED` | `CANCELLED`
- `from_rib_id` / `to_rib_id`: RIB source / destination
- `montant`: décimal (100%, 80%, 20%)
- `reference`: alignée sur `numeroTransaction` du paiement confirmé
- `paiement_id` / `reservation_id`
- `date_execution`: renseignée quand `EXECUTED`

### 4) Pré-requis RIB
- Plateforme: un RIB `PLATEFORME` (par défaut) — créé automatiquement au démarrage si absent.
- Locataire: un RIB par défaut actif.
- Locateur: un RIB par défaut actif.
Endpoints utiles:
- GET `/api/ribs/platform/default` — vérifier le RIB plateforme
- GET `/api/ribs/locataire/{locataireId}` — lister les RIBs locataire
- GET `/api/ribs/locateur/{locateurId}` — lister les RIBs locateur
- POST `/api/ribs` — créer un RIB (type: `LOCATAIRE` ou `LOCATEUR`), puis POST `/api/ribs/{ribId}/defaut` pour le définir par défaut

### 5) APIs d’encaissements et de soldes
Encaissements (instructions `EXECUTED` reçues par les RIBs):
- GET `/api/payouts/encaissements/locataire/{locataireId}`
- GET `/api/payouts/encaissements/locateur/{locateurId}`
- GET `/api/payouts/encaissements/plateforme`

Soldes (entrées, sorties, net):
- GET `/api/payouts/solde/locataire/{locataireId}`
- GET `/api/payouts/solde/locateur/{locateurId}`
- GET `/api/payouts/solde/plateforme`

### 6) Exemples cURL
Confirmer un paiement:
```bash
curl -X PUT \
  "http://localhost:8080/api/paiements/{paiementId}/confirmer" \
  -H "Content-Type: application/json" \
  -d '{
    "numeroTransaction": "PAY-2025-000123",
    "referenceExterne": "PGW-REF-ABC",
    "metadonnees": "{\"gateway\":\"XyzPay\"}"
  }'
```
Lister encaissements locateur:
```bash
curl "http://localhost:8080/api/payouts/encaissements/locateur/{locateurId}"
```
Solde plateforme:
```bash
curl "http://localhost:8080/api/payouts/solde/plateforme"
```

### 7) Dépannage (erreurs fréquentes)
- 400 `numeroTransaction déjà utilisé par un autre paiement`:
  - Envoyer un `numeroTransaction` unique.
- 500 `Erreur interne du serveur` lors de la confirmation:
  - Vérifier la présence des RIBs par défaut:
    - GET `/api/ribs/platform/default` (plateforme)
    - GET `/api/ribs/locataire/{locataireId}` (au moins un RIB, et un en défaut)
    - GET `/api/ribs/locateur/{locateurId}` (au moins un RIB, et un en défaut)
  - Si un RIB manque, créer via POST `/api/ribs` puis définir défaut.
  - Les erreurs lors de la génération d’instructions sont journalisées et la confirmation ne devrait plus échouer.
- 404 `Paiement non trouvé` / 400 `La réservation ...`:
  - Vérifier l’ID du paiement et le statut de la réservation.

### 8) Notes
- Les instructions sont marquées `EXECUTED` immédiatement après confirmation pour modéliser un encaissement instantané. Si vous souhaitez une exécution différée (workflow batch/banque), on peut adapter la logique (`PENDING` puis transition vers `EXECUTED`).



