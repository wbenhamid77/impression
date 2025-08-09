# Modifications de la Base de Données - Gestion des Stades CAN 2025

## 📋 **Résumé des Modifications**

Cette documentation décrit les modifications apportées à la base de données pour intégrer la gestion des stades de la CAN 2025 au Maroc et le calcul automatique des distances.

## 🏗️ **Nouvelles Tables Créées**

### 1. Table `stades`
```sql
CREATE TABLE stades (
    id VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    ville VARCHAR(255) NOT NULL,
    adresse_complete VARCHAR(500),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    capacite INT,
    description VARCHAR(1000),
    est_actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL,
    date_modification DATETIME
);
```

### 2. Données des Stades CAN 2025 Chargées Automatiquement

| Nom du Stade | Ville | Latitude | Longitude | Capacité |
|--------------|-------|----------|-----------|----------|
| Stade Mohammed V | Casablanca | 33.5292 | -7.4612 | 45,000 |
| Stade Prince Moulay Abdellah | Rabat | 33.9556 | -6.8341 | 52,000 |
| Stade Adrar | Agadir | 30.3928 | -9.5378 | 45,000 |
| Stade de Fès | Fès | 34.0181 | -5.0078 | 45,000 |
| Stade Ibn Batouta | Tanger | 35.7595 | -5.8134 | 65,000 |
| Stade de Marrakech | Marrakech | 31.6063 | -8.0417 | 45,000 |

## 📊 **Modifications Table `annonces`**

### Colonnes Supprimées
```sql
-- Ces colonnes ne sont plus nécessaires
ALTER TABLE annonces DROP COLUMN stade_plus_proche; -- était VARCHAR(255)
ALTER TABLE annonces DROP COLUMN adresse_stade;     -- était VARCHAR(255)
```

### Colonnes Ajoutées
```sql
-- Nouvelle relation avec la table stades
ALTER TABLE annonces ADD COLUMN stade_id VARCHAR(36);
ALTER TABLE annonces ADD CONSTRAINT FK_annonces_stade 
    FOREIGN KEY (stade_id) REFERENCES stades(id);

-- Note: Les colonnes suivantes existent déjà
-- latitude DECIMAL(10,8)
-- longitude DECIMAL(11,8) 
-- distance_stade DECIMAL(10,2)
```

## 🔄 **Scripts de Migration**

### Script de Nettoyage (si nécessaire)
```sql
-- Supprimer les anciennes colonnes texte des stades
ALTER TABLE annonces DROP COLUMN IF EXISTS stade_plus_proche;
ALTER TABLE annonces DROP COLUMN IF EXISTS adresse_stade;

-- Ajouter la relation avec la table stades
ALTER TABLE annonces ADD COLUMN IF NOT EXISTS stade_id VARCHAR(36);
ALTER TABLE annonces ADD CONSTRAINT FK_annonces_stade 
    FOREIGN KEY (stade_id) REFERENCES stades(id) ON DELETE SET NULL;
```

### Script de Migration des Données (optionnel)
```sql
-- Si vous avez des données existantes avec noms de stades en texte,
-- vous pouvez les migrer vers les nouvelles références :

UPDATE annonces a 
JOIN stades s ON a.stade_plus_proche = s.nom 
SET a.stade_id = s.id 
WHERE a.stade_plus_proche IS NOT NULL;
```

## ⚙️ **Fonctionnalités Automatiques**

### 1. Calcul Automatique du Stade le Plus Proche
- **Quand** : Lors de la création ou modification d'une annonce avec latitude/longitude
- **Comment** : Utilise la formule de Haversine pour calculer les distances
- **Résultat** : Met à jour automatiquement `stade_id` et `distance_stade`

### 2. Formule de Distance (Haversine)
```java
// Formule utilisée dans StadeService.calculerDistance()
double distance = 6371 * acos(
    cos(radians(lat1)) * cos(radians(lat2)) * 
    cos(radians(lon2) - radians(lon1)) + 
    sin(radians(lat1)) * sin(radians(lat2))
);
```

## 🗂️ **Structure des Nouvelles Classes**

### Entité Stade
```java
@Entity
@Table(name = "stades")
public class Stade {
    private UUID id;
    private String nom;
    private String ville;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacite;
    private String description;
    private boolean estActif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
```

### Modification Entité Annonce
```java
// Ancienne structure (supprimée)
// private String stadePlusProche;
// private String adresseStade;

// Nouvelle structure
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "stade_id")
private Stade stadePlusProche;

private BigDecimal distanceStade; // reste inchangé
private BigDecimal latitude;      // reste inchangé  
private BigDecimal longitude;     // reste inchangé
```

## 📱 **Impact sur l'API**

### Changements dans les DTOs

#### AnnonceDTO (Modifié)
```java
// Ancien
// private String stadePlusProche;
// private String adresseStade;

// Nouveau  
private StadeDTO stadePlusProche;
// adresseStade supprimé
```

#### Nouveau StadeDTO
```java
public class StadeDTO {
    private UUID id;
    private String nom;
    private String ville;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacite;
    private String description;
    private boolean estActif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
```

### Exemples de Réponses API

#### Avant (Ancien Format)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "titre": "Appartement proche stade",
  "stadePlusProche": "Stade Mohammed V",
  "distanceStade": 2.5,
  "adresseStade": "Boulevard Mohamed Zerktouni, Casablanca",
  "latitude": 33.5400,
  "longitude": -7.4500
}
```

#### Après (Nouveau Format)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "titre": "Appartement proche stade",
  "stadePlusProche": {
    "id": "stade-uuid-123",
    "nom": "Stade Mohammed V",
    "ville": "Casablanca",
    "adresseComplete": "Boulevard Mohamed Zerktouni, Casablanca",
    "latitude": 33.5292,
    "longitude": -7.4612,
    "capacite": 45000,
    "description": "Stade principal de Casablanca, rénové pour la CAN 2025"
  },
  "distanceStade": 2.5,
  "latitude": 33.5400,
  "longitude": -7.4500
}
```

## 🚀 **Avantages des Modifications**

### ✅ **Avantages Techniques**
1. **Calcul Automatique** : Plus besoin de saisir manuellement le stade le plus proche
2. **Données Structurées** : Informations complètes sur chaque stade
3. **Précision** : Calcul exact des distances avec coordonnées GPS
4. **Cohérence** : Données standardisées pour tous les stades

### ✅ **Avantages Fonctionnels**
1. **Recherche Améliorée** : Filtrage par proximité de stade
2. **Expérience Utilisateur** : Informations riches sur les stades
3. **CAN 2025** : Données officielles des stades de la compétition
4. **Évolutivité** : Facile d'ajouter de nouveaux stades

## 🔧 **Maintenance et Mise à Jour**

### Ajouter un Nouveau Stade
```java
Stade nouveauStade = new Stade(
    "Nom du Stade",
    "Ville",
    "Adresse complète",
    new BigDecimal("latitude"),
    new BigDecimal("longitude"),
    capacite
);
stadeRepository.save(nouveauStade);
```

### Recalculer les Distances pour Toutes les Annonces
```java
// Service method to recalculate all distances
public void recalculerToutesLesDistances() {
    List<Annonce> annonces = annonceRepository.findAll();
    for (Annonce annonce : annonces) {
        if (annonce.getLatitude() != null && annonce.getLongitude() != null) {
            StadeService.StadeAvecDistance stadeProche = 
                stadeService.trouverStadeLePlusProche(
                    annonce.getLatitude(), 
                    annonce.getLongitude()
                );
            if (stadeProche != null) {
                annonce.setStadePlusProche(stadeProche.getStade());
                annonce.setDistanceStade(stadeProche.getDistance());
                annonceRepository.save(annonce);
            }
        }
    }
}
```

## ⚠️ **Points d'Attention**

1. **Migration des Données** : Vérifiez les données existantes avant migration
2. **Performance** : Le calcul de distance peut être intensif pour de gros volumes
3. **Coordonnées** : Assurez-vous que latitude/longitude sont correctes
4. **Cache** : Considérez la mise en cache des calculs de distance
5. **Index** : Ajoutez des index sur les colonnes de géolocalisation si nécessaire

```sql
-- Index recommandés pour les performances
CREATE INDEX idx_annonces_coordinates ON annonces(latitude, longitude);
CREATE INDEX idx_stades_coordinates ON stades(latitude, longitude);
CREATE INDEX idx_annonces_stade_id ON annonces(stade_id);
``` 