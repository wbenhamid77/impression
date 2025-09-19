# 🔄 Nouvelle Logique Automatique des Statuts de Réservation

## 📋 Vue d'ensemble

Le système a été modifié pour gérer automatiquement les transitions de statuts des réservations selon des règles temporelles strictes.

## 🎯 Règles Implémentées

### 1. **EN_ATTENTE** - Confirmation limitée dans le temps
- **Utilisation** : Statut initial lors de la création d'une réservation
- **Transition vers CONFIRMEE** : Uniquement si la date d'arrivée n'est pas encore dépassée
- **Transition automatique vers ANNULEE** : Si la date d'arrivée est dépassée sans confirmation

### 2. **CONFIRMEE** - Réservation validée
- **Utilisation** : Réservation acceptée par le locateur avant la date d'arrivée
- **Transition automatique vers EN_COURS** : À la date d'arrivée
- **Transition manuelle vers ANNULEE** : Possible à tout moment

### 3. **EN_COURS** - Séjour en cours
- **Utilisation** : Séjour actuellement en cours (date d'arrivée atteinte)
- **Transition automatique vers TERMINEE** : À la date de départ

### 4. **TERMINEE** - Séjour terminé
- **Utilisation** : Séjour terminé avec succès
- **Transition** : Aucune (statut final)

### 5. **ANNULEE** - Réservation annulée
- **Utilisation** : Réservation annulée (manuelle ou automatique)
- **Transition** : Aucune (statut final)

## ⚙️ Mécanismes Automatiques

### Scheduler Automatique
- **Fréquence** : Toutes les heures
- **Tâches** :
  1. Annule les réservations `EN_ATTENTE` après la date d'arrivée
  2. Passe à `EN_COURS` les réservations `CONFIRMEE` à la date d'arrivée
  3. Passe à `TERMINEE` les réservations `EN_COURS` après la date de départ

### Validation de Confirmation
- **Règle** : Impossible de confirmer une réservation après sa date d'arrivée
- **Erreur** : `"Impossible de confirmer une réservation après la date de début"`

## 🔧 APIs Disponibles

### APIs de Gestion Manuelle
```http
# Confirmer (avec validation temporelle)
PUT /api/reservations/{id}/confirmer

# Annuler
PUT /api/reservations/{id}/annuler?raison=...

# Changer statut manuellement
PUT /api/reservations/{id}/statut?statut=...

# Marquer en cours
PUT /api/reservations/{id}/mettre-en-cours

# Marquer terminée
PUT /api/reservations/{id}/terminer
```

### APIs de Test du Scheduler
```http
# Déclencher manuellement la gestion automatique
POST /api/reservations/scheduler/declencher

# Vérifier l'état du scheduler
GET /api/reservations/scheduler/status
```

## 📊 Exemples de Scénarios

### Scénario 1 : Réservation confirmée à temps
```
1. Création → EN_ATTENTE
2. Confirmation avant date d'arrivée → CONFIRMEE
3. Date d'arrivée atteinte → EN_COURS (automatique)
4. Date de départ atteinte → TERMINEE (automatique)
```

### Scénario 2 : Réservation non confirmée à temps
```
1. Création → EN_ATTENTE
2. Date d'arrivée dépassée → ANNULEE (automatique)
```

### Scénario 3 : Tentative de confirmation tardive
```
1. Création → EN_ATTENTE
2. Tentative de confirmation après date d'arrivée → ERREUR 400
```

### Scénario 4 : Annulation manuelle
```
1. Création → EN_ATTENTE
2. Annulation manuelle → ANNULEE
```

## 🚀 Déploiement

### 1. Redémarrage requis
```bash
# Redémarrer l'application pour activer le scheduler
mvn spring-boot:run
```

### 2. Vérification
```bash
# Tester le déclenchement manuel
curl -X POST http://localhost:8083/api/reservations/scheduler/declencher

# Vérifier le statut
curl http://localhost:8083/api/reservations/scheduler/status
```

## 📝 Logs et Monitoring

### Logs du Scheduler
```
INFO  - Début de la gestion automatique des statuts de réservation
INFO  - Vérification des réservations en attente expirées
INFO  - Annulation automatique de la réservation {id} - Date d'arrivée dépassée: {date}
INFO  - Vérification des réservations à passer en cours
INFO  - Passage en cours de la réservation {id} - Date d'arrivée atteinte: {date}
INFO  - Vérification des réservations à terminer
INFO  - Terminaison de la réservation {id} - Date de départ atteinte: {date}
INFO  - Fin de la gestion automatique des statuts de réservation
```

### Métriques
- Nombre de réservations annulées automatiquement
- Nombre de réservations passées en cours
- Nombre de réservations terminées

## ⚠️ Points d'Attention

### 1. Fuseau Horaire
- Le scheduler utilise le fuseau horaire du serveur
- Vérifiez que les dates sont cohérentes avec le fuseau local

### 2. Performance
- Le scheduler s'exécute toutes les heures
- Pour de gros volumes, considérez une fréquence plus élevée

### 3. Récupération d'Erreurs
- Les erreurs dans le scheduler sont loggées mais n'interrompent pas l'exécution
- Utilisez l'API de déclenchement manuel pour les tests

## 🔍 Tests Recommandés

### 1. Test de Confirmation Tardive
```bash
# Créer une réservation avec date d'arrivée dans le passé
# Tenter de la confirmer → Doit échouer avec erreur 400
```

### 2. Test d'Annulation Automatique
```bash
# Créer une réservation EN_ATTENTE avec date d'arrivée dépassée
# Attendre l'exécution du scheduler ou déclencher manuellement
# Vérifier que le statut est passé à ANNULEE
```

### 3. Test de Transition Automatique
```bash
# Créer une réservation CONFIRMEE avec date d'arrivée aujourd'hui
# Attendre l'exécution du scheduler ou déclencher manuellement
# Vérifier que le statut est passé à EN_COURS
```

---

*Cette nouvelle logique garantit une gestion cohérente et automatique du cycle de vie des réservations.*
