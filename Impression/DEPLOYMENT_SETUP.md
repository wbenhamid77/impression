# Configuration du Pipeline de Déploiement Automatique

Ce projet utilise un pipeline CI/CD complet qui déploie automatiquement votre application sur Docker Hub, Kubernetes et AWS EKS à chaque push sur la branche `main` ou `master`.

## 🔧 Configuration des Secrets GitHub

Pour que le pipeline fonctionne correctement, vous devez configurer les secrets suivants dans votre repository GitHub :

### 1. Secrets Docker Hub
- `DOCKER_USERNAME` : Votre nom d'utilisateur Docker Hub
- `DOCKER_PASSWORD` : Votre mot de passe Docker Hub

### 2. Secrets AWS
- `AWS_ACCESS_KEY_ID` : Votre clé d'accès AWS
- `AWS_SECRET_ACCESS_KEY` : Votre clé secrète AWS

## 📋 Étapes de Configuration

### 1. Configuration des Secrets GitHub
1. Allez dans votre repository GitHub
2. Cliquez sur "Settings" → "Secrets and variables" → "Actions"
3. Ajoutez les secrets mentionnés ci-dessus

### 2. Configuration AWS EKS
1. Créez un cluster EKS nommé `impression-cluster` dans la région `us-east-1`
2. Configurez les permissions IAM appropriées pour votre utilisateur AWS
3. Assurez-vous que votre cluster EKS est accessible via kubectl

### 3. Configuration Docker Hub
1. Créez un compte Docker Hub si vous n'en avez pas
2. Créez un repository nommé `impression`
3. Configurez les secrets Docker Hub dans GitHub

## 🚀 Fonctionnement du Pipeline

Le pipeline effectue les étapes suivantes :

1. **Build Maven** : Compile l'application Java avec Maven
2. **Build Docker** : Crée l'image Docker et la pousse sur Docker Hub
3. **Configuration AWS** : Configure les credentials AWS
4. **Déploiement Kubernetes** : Déploie l'application sur EKS
5. **Vérification** : Vérifie que le déploiement s'est bien passé

## 📁 Structure des Fichiers

```
Impression/
├── .github/workflows/
│   └── docker-build.yml          # Pipeline CI/CD
├── k8s/
│   ├── k8s-config.yaml          # Configuration Kubernetes complète
│   ├── deployment.yaml           # Déploiement Kubernetes
│   ├── service.yaml              # Service Kubernetes
│   └── namespace.yaml            # Namespace Kubernetes
└── Dockerfile                    # Configuration Docker
```

## 🔍 Monitoring

Après chaque déploiement, vous pouvez vérifier le statut de votre application :

```bash
# Vérifier les pods
kubectl get pods -n impression

# Vérifier les services
kubectl get services -n impression

# Vérifier les logs
kubectl logs -n impression deployment/impression-deployment
```

## 🎯 Points d'Accès

- **Application** : Accessible via le LoadBalancer AWS
- **Health Check** : `/actuator/health`
- **Port** : 8083 (interne), 80 (externe)

## ⚠️ Notes Importantes

1. Assurez-vous que votre cluster EKS est dans la région `us-east-1`
2. Le nom du cluster doit être `impression-cluster`
3. L'image Docker sera taguée automatiquement avec `latest`
4. Le pipeline se déclenche à chaque push sur `main` ou `master`

## 🆘 Dépannage

Si le déploiement échoue :

1. Vérifiez que tous les secrets GitHub sont configurés
2. Vérifiez que votre cluster EKS est accessible
3. Vérifiez les logs du pipeline dans l'onglet "Actions" de GitHub
4. Vérifiez les logs Kubernetes avec `kubectl logs`

## 📞 Support

Pour toute question ou problème, consultez les logs du pipeline dans GitHub Actions ou contactez l'équipe de développement. 