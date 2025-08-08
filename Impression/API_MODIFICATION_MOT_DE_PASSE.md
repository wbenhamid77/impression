# API de Modification de Mot de Passe

Cette API permet aux utilisateurs (locataires et locateurs) de modifier leur mot de passe en fournissant leur ancien mot de passe et un nouveau mot de passe.

## Endpoints Disponibles

### 1. Modification via AuthController
- **URL**: `PUT /api/auth/modifier-mot-de-passe`
- **Description**: Endpoint principal pour la modification de mot de passe

### 2. Modification via AuthentificationController
- **URL**: `PUT /api/auth/changer-mot-de-passe`
- **Description**: Endpoint alternatif pour la modification de mot de passe

## Structure de la Requête

### ModificationMotDePasseDTO
```json
{
  "utilisateurId": "uuid-de-l-utilisateur",
  "ancienMotDePasse": "ancien-mot-de-passe-en-clair",
  "nouveauMotDePasse": "nouveau-mot-de-passe-en-clair"
}
```

### Champs Requis
- `utilisateurId` (UUID, obligatoire) : L'identifiant unique de l'utilisateur
- `ancienMotDePasse` (String, obligatoire) : L'ancien mot de passe en clair
- `nouveauMotDePasse` (String, obligatoire) : Le nouveau mot de passe en clair

## Structure de la Réponse

### ReponseModificationMotDePasseDTO
```json
{
  "succes": true,
  "message": "Mot de passe modifié avec succès",
  "utilisateurId": "uuid-de-l-utilisateur",
  "email": "email@example.com"
}
```

### Champs de Réponse
- `succes` (boolean) : Indique si l'opération a réussi
- `message` (String) : Message descriptif du résultat
- `utilisateurId` (String) : ID de l'utilisateur (en cas de succès)
- `email` (String) : Email de l'utilisateur (en cas de succès)

## Codes de Statut HTTP

- **200 OK** : Mot de passe modifié avec succès
- **400 Bad Request** : Erreur de validation ou échec de l'opération

## Messages d'Erreur Possibles

- "Utilisateur non trouvé" : L'ID utilisateur n'existe pas
- "Compte désactivé" : Le compte utilisateur est désactivé
- "Ancien mot de passe incorrect" : L'ancien mot de passe fourni ne correspond pas
- "Erreur lors de la modification du mot de passe" : Erreur technique

## Exemple d'Utilisation

### Requête cURL
```bash
curl -X PUT "http://localhost:8080/api/auth/modifier-mot-de-passe" \
  -H "Content-Type: application/json" \
  -d '{
    "utilisateurId": "123e4567-e89b-12d3-a456-426614174000",
    "ancienMotDePasse": "ancienMotDePasse123",
    "nouveauMotDePasse": "nouveauMotDePasse456"
  }'
```

### Réponse de Succès
```json
{
  "succes": true,
  "message": "Mot de passe modifié avec succès",
  "utilisateurId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "utilisateur@example.com"
}
```

### Réponse d'Erreur
```json
{
  "succes": false,
  "message": "Ancien mot de passe incorrect"
}
```

## Sécurité

- L'ancien mot de passe est vérifié avant toute modification
- Le nouveau mot de passe est automatiquement hashé avant stockage
- Seuls les utilisateurs actifs peuvent modifier leur mot de passe
- La date de modification est automatiquement mise à jour

## Validation

- L'ID utilisateur doit être un UUID valide
- L'ancien et le nouveau mot de passe ne peuvent pas être vides
- Le compte utilisateur doit être actif
- L'ancien mot de passe doit correspondre au hash stocké en base 