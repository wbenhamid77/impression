# API de Modification de Profil Utilisateur

Cette API permet aux utilisateurs (locataires, locateurs et admins) de modifier leur profil en fournissant leur ID utilisateur et les nouvelles informations.

## Endpoints Disponibles

### 1. Contrôleur Profil (Recommandé)
- **URL**: `PUT /api/profil/{id}`
- **Description**: Endpoint principal pour la modification de profil

### 2. Contrôleur Utilisateur (Alternatif)
- **URL**: `PUT /api/utilisateurs/{id}/profil`
- **Description**: Endpoint alternatif pour la modification de profil

### 3. Récupération de Profil
- **URL**: `GET /api/profil/{id}`
- **Description**: Récupérer le profil d'un utilisateur par ID

- **URL**: `GET /api/profil/email/{email}`
- **Description**: Récupérer le profil d'un utilisateur par email

## Structure de la Requête

### ModificationProfilDTO
```json
{
  "nom": "Nouveau Nom",
  "prenom": "Nouveau Prénom",
  "email": "nouveau@email.com",
  "telephone": "+33123456789",
  "photoProfil": "https://example.com/photo.jpg",
  
  // Champs spécifiques pour Admin
  "matricule": "ADM123",
  "departement": "Informatique",
  
  // Champs spécifiques pour Locateur
  "description": "Description du locateur",
  "numeroSiret": "12345678901234",
  "raisonSociale": "Ma Société SARL",
  "adresseProfessionnelle": "123 Rue de la Paix, 75001 Paris",
  
  // Champs spécifiques pour Locataire
  "profession": "Développeur",
  "revenuAnnuel": 45000.0,
  "employeur": "Ma Société",
  "dateEmbauche": "2023-01-15"
}
```

### Champs Disponibles
- **Champs de base** (tous les utilisateurs) :
  - `nom` (String, 2-100 caractères) : Nom de l'utilisateur
  - `prenom` (String, 2-100 caractères) : Prénom de l'utilisateur
  - `email` (String, format email valide) : Adresse email
  - `telephone` (String, max 20 caractères) : Numéro de téléphone
  - `photoProfil` (String, max 500 caractères) : URL de la photo de profil

- **Champs spécifiques Admin** :
  - `matricule` (String) : Matricule de l'administrateur
  - `departement` (String) : Département de l'administrateur

- **Champs spécifiques Locateur** :
  - `description` (String, max 1000 caractères) : Description du locateur
  - `numeroSiret` (String, max 14 caractères) : Numéro SIRET
  - `raisonSociale` (String, max 100 caractères) : Raison sociale
  - `adresseProfessionnelle` (String, max 500 caractères) : Adresse professionnelle

- **Champs spécifiques Locataire** :
  - `profession` (String, max 100 caractères) : Profession
  - `revenuAnnuel` (Double) : Revenu annuel
  - `employeur` (String, max 100 caractères) : Nom de l'employeur
  - `dateEmbauche` (Date, format YYYY-MM-DD) : Date d'embauche

## Structure de la Réponse

### ReponseModificationProfilDTO
```json
{
  "succes": true,
  "message": "Profil modifié avec succès",
  "utilisateurId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "utilisateur@example.com",
  "nom": "Nouveau Nom",
  "prenom": "Nouveau Prénom",
  "telephone": "+33123456789",
  "photoProfil": "https://example.com/photo.jpg",
  "dateModification": "2024-01-15T10:30:00"
}
```

### Champs de Réponse
- `succes` (boolean) : Indique si l'opération a réussi
- `message` (String) : Message descriptif du résultat
- `utilisateurId` (UUID) : ID de l'utilisateur
- `email` (String) : Email de l'utilisateur
- `nom` (String) : Nom mis à jour
- `prenom` (String) : Prénom mis à jour
- `telephone` (String) : Téléphone mis à jour
- `photoProfil` (String) : Photo de profil mise à jour
- `dateModification` (DateTime) : Date et heure de la modification

## Codes de Statut HTTP

- **200 OK** : Profil modifié avec succès
- **400 Bad Request** : Erreur de validation ou échec de l'opération
- **404 Not Found** : Utilisateur non trouvé

## Messages d'Erreur Possibles

- "Utilisateur non trouvé" : L'ID utilisateur n'existe pas
- "Compte désactivé" : Le compte utilisateur est désactivé
- "Cet email est déjà utilisé par un autre utilisateur" : L'email fourni est déjà pris
- "Erreur lors de la modification du profil" : Erreur technique

## Exemples d'Utilisation

### 1. Modification d'un Profil Locataire
```bash
curl -X PUT "http://localhost:8080/api/profil/123e4567-e89b-12d3-a456-426614174000" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "telephone": "+33123456789",
    "profession": "Développeur",
    "revenuAnnuel": 45000.0,
    "employeur": "TechCorp"
  }'
```

### 2. Modification d'un Profil Locateur
```bash
curl -X PUT "http://localhost:8080/api/profil/123e4567-e89b-12d3-a456-426614174000" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Martin",
    "prenom": "Marie",
    "description": "Propriétaire de plusieurs appartements",
    "numeroSiret": "12345678901234",
    "raisonSociale": "Immobilier Martin SARL",
    "adresseProfessionnelle": "456 Avenue des Champs, 75008 Paris"
  }'
```

### 3. Modification d'un Profil Admin
```bash
curl -X PUT "http://localhost:8080/api/profil/123e4567-e89b-12d3-a456-426614174000" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Admin",
    "prenom": "Super",
    "matricule": "ADM001",
    "departement": "Sécurité"
  }'
```

### Réponse de Succès
```json
{
  "succes": true,
  "message": "Profil modifié avec succès",
  "utilisateurId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "utilisateur@example.com",
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+33123456789",
  "photoProfil": null,
  "dateModification": "2024-01-15T10:30:00"
}
```

### Réponse d'Erreur
```json
{
  "succes": false,
  "message": "Cet email est déjà utilisé par un autre utilisateur"
}
```

## Fonctionnalités

### ✅ **Validation des Données**
- Validation des formats (email, téléphone)
- Vérification des longueurs de champs
- Validation des types de données

### ✅ **Sécurité**
- Vérification de l'existence de l'utilisateur
- Vérification que le compte est actif
- Validation de l'unicité de l'email
- Mise à jour automatique de la date de modification

### ✅ **Flexibilité**
- Modification partielle (seuls les champs fournis sont modifiés)
- Support de tous les types d'utilisateurs (Locataire, Locateur, Admin)
- Champs spécifiques selon le type d'utilisateur

### ✅ **Gestion d'Erreurs**
- Messages d'erreur clairs et explicites
- Codes de statut HTTP appropriés
- Validation des données d'entrée

## Notes Importantes

1. **Modification Partielle** : Seuls les champs fournis dans la requête seront modifiés
2. **Validation d'Email** : L'email doit être unique dans le système
3. **Comptes Actifs** : Seuls les comptes actifs peuvent être modifiés
4. **Types d'Utilisateurs** : Les champs spécifiques ne sont appliqués que pour le bon type d'utilisateur
5. **Date de Modification** : Mise à jour automatique lors de toute modification 