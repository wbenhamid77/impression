# API Documentation - Gestion des Utilisateurs

## 📋 Vue d'ensemble

Cette API permet de gérer les utilisateurs avec des opérations CRUD complètes, incluant la création, la lecture, la mise à jour et la suppression des utilisateurs.

## 🔗 Base URL

```
http://localhost:8083/api/users
```

## 📚 Endpoints

### 1. Créer un utilisateur

**POST** `/api/users`

**Corps de la requête :**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+33123456789",
  "role": "USER"
}
```

**Réponse (201 Created) :**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+33123456789",
  "role": "USER",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 2. Récupérer tous les utilisateurs

**GET** `/api/users`

**Réponse (200 OK) :**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+33123456789",
    "role": "USER",
    "enabled": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 3. Récupérer un utilisateur par ID

**GET** `/api/users/{id}`

**Réponse (200 OK) :**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+33123456789",
  "role": "USER",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 4. Récupérer un utilisateur par nom d'utilisateur

**GET** `/api/users/username/{username}`

### 5. Récupérer un utilisateur par email

**GET** `/api/users/email/{email}`

### 6. Mettre à jour un utilisateur

**PUT** `/api/users/{id}`

**Corps de la requête :**
```json
{
  "username": "john_doe_updated",
  "email": "john.doe.updated@example.com",
  "password": "newpassword123",
  "firstName": "John",
  "lastName": "Doe Updated",
  "phone": "+33987654321",
  "role": "ADMIN"
}
```

### 7. Supprimer un utilisateur

**DELETE** `/api/users/{id}`

**Réponse (204 No Content)**

### 8. Changer le statut d'un utilisateur

**PATCH** `/api/users/{id}/toggle-status`

**Réponse (200 OK) :**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+33123456789",
  "role": "USER",
  "enabled": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:45:00"
}
```

### 9. Récupérer les utilisateurs par rôle

**GET** `/api/users/role/{role}`

**Exemples :**
- `/api/users/role/ADMIN`
- `/api/users/role/USER`
- `/api/users/role/MODERATOR`

### 10. Récupérer les utilisateurs actifs

**GET** `/api/users/active`

### 11. Rechercher des utilisateurs

**GET** `/api/users/search?keyword=john`

### 12. Rechercher des utilisateurs actifs

**GET** `/api/users/search/active?keyword=john`

### 13. Health check

**GET** `/api/users/health`

**Réponse (200 OK) :**
```
API Utilisateurs opérationnelle
```

## 🔧 Validation

### Règles de validation pour la création d'utilisateur :

- **username** : 3-50 caractères, obligatoire, unique
- **email** : format email valide, obligatoire, unique
- **password** : minimum 6 caractères, obligatoire
- **firstName** : maximum 100 caractères, obligatoire
- **lastName** : maximum 100 caractères, obligatoire
- **phone** : maximum 20 caractères, optionnel
- **role** : USER, ADMIN, ou MODERATOR (défaut: USER)

## 🚨 Codes d'erreur

### 400 Bad Request
- Erreurs de validation
- Nom d'utilisateur ou email déjà existant
- Données manquantes ou invalides

### 404 Not Found
- Utilisateur non trouvé

### 500 Internal Server Error
- Erreur interne du serveur

## 📝 Exemples d'utilisation

### Créer un administrateur
```bash
curl -X POST http://localhost:8083/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

### Rechercher des utilisateurs
```bash
curl -X GET "http://localhost:8083/api/users/search?keyword=john"
```

### Changer le statut d'un utilisateur
```bash
curl -X PATCH http://localhost:8083/api/users/1/toggle-status
```

## 🔐 Rôles disponibles

- **USER** : Utilisateur standard
- **ADMIN** : Administrateur avec tous les droits
- **MODERATOR** : Modérateur avec droits limités

## 📊 Structure de la base de données

### Table `users`
- `id` : Clé primaire auto-incrémentée
- `username` : Nom d'utilisateur unique
- `email` : Email unique
- `password` : Mot de passe hashé
- `first_name` : Prénom
- `last_name` : Nom de famille
- `phone` : Numéro de téléphone (optionnel)
- `role` : Rôle de l'utilisateur
- `enabled` : Statut actif/inactif
- `created_at` : Date de création
- `updated_at` : Date de dernière modification

## 🛠️ Technologies utilisées

- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring Security**
- **MySQL/H2 Database**
- **Lombok**
- **Validation API** 