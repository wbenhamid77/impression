## Remboursements — Règles, APIs, exemples et résultats

Ce document explique précisément la logique des remboursements, les endpoints disponibles, les prérequis (RIB), ainsi que des exemples complets de requêtes/réponses.

---

### 1) Règles métier des remboursements

- Référence: date d'arrivée de la réservation (minuit, début de journée).
- Notation: montant total payé = M.

- Cas A — Annulation à moins de 24h avant l'arrivée:
  - **Remboursement**: 0% de M (aucune instruction générée)

- Cas B — Annulation entre 24h et 48h avant l'arrivée:
  - **Remboursement**: 50% de M au locataire
    - 40% de M depuis le RIB du locateur → locataire
    - 10% de M depuis le RIB de la plateforme → locataire

- Cas C — Annulation à 48h ou plus avant l'arrivée:
  - **Remboursement**: 100% de M au locataire
    - 80% de M depuis le RIB du locateur → locataire
    - 20% de M depuis le RIB de la plateforme → locataire

Notes:
- Les remboursements sont matérialisés par des « instructions de transaction » (statut PENDING → EXECUTED/ CANCELLED) à exécuter manuellement par l'opérateur.
- Les pourcentages sont arrondis au centime (`HALF_UP`).

---

### 2) Prérequis et hypothèses

- Un seul RIB plateforme doit exister (type `PLATEFORME`) et sera utilisé dans toutes les opérations impliquant la plateforme.
- Le locateur doit disposer d'un RIB par défaut (pour rembourser 40%/80%).
- Le locataire doit disposer d'un RIB par défaut (pour recevoir le remboursement).
- Un paiement de la réservation doit être au statut `PAYE` pour déclencher un remboursement.

---

### 3) Déclencheurs des remboursements

- Automatique: lors de l'annulation d'une réservation (`ReservationService.annulerReservation`), si un paiement `PAYE` existe, les instructions de remboursement sont générées selon la fenêtre (<24h, 24–48h, ≥48h).
- Manuel: via l'endpoint dédié pour (re)générer les instructions.

---

### 4) Endpoints pertinents

- Générer (ou régénérer) les instructions de remboursement pour une réservation annulée:
```
POST /api/payouts/generate/remboursement/{reservationId}
```

- Lister les instructions en attente d'exécution:
```
GET /api/payouts/pending
```

- Marquer une instruction comme exécutée (après virement réel):
```
POST /api/payouts/{instructionId}/executer
Body: { "reference": "VIR-YYYY-XXXXX" }
```

- Annuler une instruction:
```
POST /api/payouts/{instructionId}/annuler
Body: { "notes": "motif" }
```

---

### 5) Exemples complets

Supposons `M = 1000.00`.

#### 5.1 Annulation à moins de 24h

Requête:
```bash
curl -X POST http://localhost:8080/api/payouts/generate/remboursement/{reservationId}
```

Réponse:
```json
[]
```

Commentaires:
- Aucune instruction créée (0%).

#### 5.2 Annulation entre 24h et 48h

Requête:
```bash
curl -X POST http://localhost:8080/api/payouts/generate/remboursement/{reservationId}
```

Réponse (abrégée):
```json
[
  {
    "type": "REFUND_LOCATAIRE_FROM_LOCATEUR",
    "statut": "PENDING",
    "fromRibId": "<RIB_LOCATEUR>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 400.00
  },
  {
    "type": "REFUND_LOCATAIRE_FROM_PLATEFORME",
    "statut": "PENDING",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 100.00
  }
]
```

Commentaires:
- Total 50%: 400 (locateur) + 100 (plateforme) = 500.

#### 5.3 Annulation à 48h ou plus

Requête:
```bash
curl -X POST http://localhost:8080/api/payouts/generate/remboursement/{reservationId}
```

Réponse (abrégée):
```json
[
  {
    "type": "REFUND_LOCATAIRE_FROM_LOCATEUR",
    "statut": "PENDING",
    "fromRibId": "<RIB_LOCATEUR>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 800.00
  },
  {
    "type": "REFUND_LOCATAIRE_FROM_PLATEFORME",
    "statut": "PENDING",
    "fromRibId": "<RIB_PLATEFORME>",
    "toRibId": "<RIB_LOCATAIRE>",
    "montant": 200.00
  }
]
```

Commentaires:
- Total 100%: 800 (locateur) + 200 (plateforme) = 1000.

#### 5.4 Exécuter une instruction (après virement)

Requête:
```bash
curl -X POST http://localhost:8080/api/payouts/{instructionId}/executer \
  -H "Content-Type: application/json" \
  -d '{ "reference": "VIR-2025-000123" }'
```

Réponse (abrégée):
```json
{
  "id": "...",
  "type": "REFUND_LOCATAIRE_FROM_LOCATEUR",
  "statut": "EXECUTED",
  "reference": "VIR-2025-000123",
  "dateExecution": "2025-10-22T14:10:00"
}
```

#### 5.5 Annuler une instruction

Requête:
```bash
curl -X POST http://localhost:8080/api/payouts/{instructionId}/annuler \
  -H "Content-Type: application/json" \
  -d '{ "notes": "annulé par l\'admin" }'
```

Réponse (abrégée):
```json
{
  "id": "...",
  "type": "REFUND_LOCATAIRE_FROM_PLATEFORME",
  "statut": "CANCELLED",
  "notes": "annulé par l'admin"
}
```

---

### 6) Modèle de donnée — TransactionInstructionDTO (réponse)

```json
{
  "id": "<UUID>",
  "reservationId": "<UUID>",
  "paiementId": "<UUID>",
  "type": "REFUND_LOCATAIRE_FROM_LOCATEUR | REFUND_LOCATAIRE_FROM_PLATEFORME | ...",
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

---

### 7) Erreurs fréquentes et résolutions

- 404 « RIB plateforme introuvable »
  - Créez/seed un RIB `PLATEFORME` unique et actif.

- 404 « RIB par défaut du locateur/locataire introuvable »
  - Créez un RIB par défaut pour le titulaire concerné.

- 400 « La réservation ne peut pas être annulée »
  - Vérifiez les règles d'annulation (p.ex. date d'arrivée dépassée, statut incompatible).

- 400 « Aucun paiement payé pour cette réservation »
  - Les remboursements ne s'appliquent qu'aux paiements au statut `PAYE`.

---

### 8) Bonnes pratiques

- Évitez les doublons d'instructions: si vous rejouez la génération, utilisez votre back-office pour filtrer les existantes.
- Tenez à jour les `reference` lors du passage en `EXECUTED` pour tracer les virements réels.
- Vérifiez systématiquement la présence des RIB par défaut avant mise en production.





### 9) Rembourser un paiement — Statut et raison (dans Paiement)

- Un paiement ne peut être remboursé que s'il est au statut `PAYE`.
- Lors de l'appel de l'endpoint de remboursement, le modèle `Paiement` est mis à jour automatiquement:
  - **statut** passe à `REMBOURSE`
  - **numeroRemboursement** est enregistré
  - **raisonRemboursement** est enregistrée
  - **dateRemboursement** est renseignée

#### Endpoint

```
PUT /api/paiements/{paiementId}/rembourser
Content-Type: application/json
```

#### Corps de requête

```json
{
  "numeroRemboursement": "RB-2025-000123",
  "raisonRemboursement": "Annulation par le client",
  "metadonnees": "{\"ref\":\"xyz\"}"
}
```

#### Réponse (extrait `PaiementDTO`)

```json
{
  "id": "<UUID>",
  "statut": "REMBOURSE",
  "numeroRemboursement": "RB-2025-000123",
  "raisonRemboursement": "Annulation par le client",
  "dateRemboursement": "2025-10-24T12:34:56",
  "metadonnees": "{\"ref\":\"xyz\"}"
}
```

#### Références de code

1) Méthode métier `Paiement.rembourser(...)` — met le statut à `REMBOURSE` et enregistre la raison/numéro/date:
```135:141:Impression/src/main/java/com/example/Impression/entities/Paiement.java
public void rembourser(String numeroRemboursement, String raison) {
    this.statut = StatutPaiement.REMBOURSE;
    this.numeroRemboursement = numeroRemboursement;
    this.raisonRemboursement = raison;
    this.dateRemboursement = LocalDateTime.now();
    this.dateModification = LocalDateTime.now();
}
```

2) Service `PaiementService.rembourserPaiement(...)` — contrôle l'éligibilité puis appelle `Paiement.rembourser(...)`:
```178:196:Impression/src/main/java/com/example/Impression/services/PaiementService.java
public PaiementDTO rembourserPaiement(UUID paiementId, RemboursementPaiementDTO remboursementDTO) {
    log.info("Remboursement du paiement: {}", paiementId);

    Paiement paiement = paiementRepository.findById(paiementId)
            .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

    if (!paiement.peutEtreRembourse()) {
        throw new ReservationException("Le paiement ne peut pas être remboursé");
    }

    paiement.rembourser(remboursementDTO.getNumeroRemboursement(), remboursementDTO.getRaisonRemboursement());

    if (remboursementDTO.getMetadonnees() != null) {
        paiement.setMetadonnees(remboursementDTO.getMetadonnees());
    }

    paiement = paiementRepository.save(paiement);

    return convertirEnDTO(paiement);
}
```

3) Contrôleur — endpoint HTTP de remboursement:
```98:104:Impression/src/main/java/com/example/Impression/controller/PaiementController.java
@PutMapping("/{id}/rembourser")
public ResponseEntity<PaiementDTO> rembourserPaiement(@PathVariable UUID id,
        @RequestBody RemboursementPaiementDTO remboursementDTO) {
    log.info("Demande de remboursement du paiement: {}", id);

    PaiementDTO paiement = paiementService.rembourserPaiement(id, remboursementDTO);
    return ResponseEntity.ok(paiement);
}
```

4) Enum des statuts — valeur `REMBOURSE`:
```9:10:Impression/src/main/java/com/example/Impression/enums/StatutPaiement.java
REMBOURSE("Remboursé"),
EXPIRE("Expiré");
```

5) DTO d'entrée — champs attendus pour la requête:
```6:10:Impression/src/main/java/com/example/Impression/dto/RemboursementPaiementDTO.java
@Data
public class RemboursementPaiementDTO {
    private String numeroRemboursement;
    private String raisonRemboursement;
    private String metadonnees;
}
```

#### Exemple cURL

```bash
curl -X PUT "http://localhost:8080/api/paiements/<PAIEMENT_ID>/rembourser" \
  -H "Content-Type: application/json" \
  -d '{
        "numeroRemboursement": "RB-2025-000123",
        "raisonRemboursement": "Annulation par le client",
        "metadonnees": "{""ref"":""xyz""}"
      }'
```

Notes:
- L'éligibilité au remboursement est vérifiée côté service (`peutEtreRembourse()` → statut `PAYE`).
- La raison est stockée dans `Paiement.raisonRemboursement` et exposée via `PaiementDTO`.