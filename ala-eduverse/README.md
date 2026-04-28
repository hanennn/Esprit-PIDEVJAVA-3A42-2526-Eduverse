# 📖 Guide Complet - Eduverse Forum JavaFX

## 🎯 Démarrage rapide

### 1. Installation des dépendances
```bash
cd /home/iyed/Documents/ala-eduverse
mvn clean install
```

### 2. Badwords
L'application initialise automatiquement les tables `badword` et `badword_log` au démarrage avec `mvn javafx:run`.

### 3. Lancer l'application
```bash
mvn javafx:run
```

L'application démarre et affiche :
- ✅ Connexion à la DB MySQL (localhost:3306, DB: ala)
- ✅ Chargement des utilisateurs actifs
- ✅ Interface sidebar avec navigation
- ✅ Page "Tous les Sujets" par défaut

---

## 🏗️ Architecture du projet

```
/ala-eduverse/
├── pom.xml                              # Configuration Maven
├── src/main/
│   ├── java/com/eduverse/forum/
│   │   ├── EduverseForumApp.java        # Point d'entrée JavaFX
│   │   ├── models/                      # Modèles de données
│   │   │   ├── User.java
│   │   │   ├── Sujet.java
│   │   │   ├── Message.java
│   │   │   └── Badword.java
│   │   ├── services/                    # Couche métier
│   │   │   ├── MyDB.java                # Singleton DB
│   │   │   ├── UserService.java
│   │   │   ├── SujetService.java
│   │   │   ├── MessageService.java
│   │   │   ├── BadwordService.java
│   │   │   └── BadwordLogService.java
│   │   ├── utils/                       # Utilitaires
│   │   │   ├── AppContext.java          # Contexte global
│   │   │   ├── ReputationUtil.java      # Calcul réputation
│   │   │   ├── TranslateUtil.java       # Traduction API
│   │   │   └── BadwordFilterUtil.java   # Filtration badwords
│   │   └── controllers/                 # Contrôleurs FXML
│   │       ├── MainController.java
│   │       ├── SujetController.java
│   │       ├── MessageController.java
│   │       ├── AdminController.java
│   │       ├── StatsController.java
│   │       └── BadwordController.java
│   └── resources/
│       ├── main.fxml                    # Interface principale
│       ├── sujet-view.fxml              # Vue sujets
│       ├── message-view.fxml            # Vue messages
│       ├── admin-view.fxml              # Vue admin
│       ├── stats-view.fxml              # Vue stats
│       ├── badword-view.fxml            # Vue badwords
│       └── styles.css                   # Thème Eduverse
├── setup-badwords.sql                   # Schéma de référence
├── BADWORDS_SYSTEM.md                   # Doc badwords
└── README.md                            # Ce fichier
```

---

## 🎨 Interface utilisateur

### Sidebar (Gauche)
- **Logo Eduverse** : Branding
- **Sélecteur d'utilisateur** : Choisir session (admin seulement)
- **Boutons de navigation** :
  - 📋 Tous les Sujets
  - ➕ Nouveau Sujet
  - 👑 Administration (admin)
  - 📊 Statistiques Forum (admin)
  - 🚫 Badwords (admin)
- **Badge réputation** : Affiche la réputation de l'utilisateur courant

### Zone de contenu (Centre)
Affiche différentes vues selon la navigation.

---

## 📝 Fonctionnalités principales

### 1. Gestion des Sujets
- **Voir tous** : Affichage en cartes scrollables
- **Filtrer** : Recherche en temps réel par titre/contenu
- **Trier** : Par date, popularité (nombre de messages)
- **Créer** : Validation titre/contenu obligatoires
- **Modifier** : Seulement par auteur ou admin
- **Supprimer** : Avec confirmation
- **Traduire** : API MyMemory (FR→EN)

### 2. Gestion des Messages
- **Lister** : Tous les messages d'un sujet
- **Créer** : Répondre à un sujet
- **Modifier** : Seulement par auteur ou admin
- **Supprimer** : Avec confirmation
- **Traduire** : Sujet complet en anglais

### 3. Réputation utilisateur
- 🌱 **Nouveau** : 0 contribution
- 🌱 **Débutant** : 1-2 contributions
- ⭐ **Actif** : 3-9 contributions
- 🏆 **Expert** : 10+ contributions

### 4. Administration
- **TableView Sujets** : Liste complète avec ID, titre, auteur, date, nb messages
- **TableView Messages** : Liste complète avec actions
- **Actions** : Modifier/Supprimer (admin)

### 5. Statistiques
- **Total sujets** : Compte dans la DB
- **Total messages** : Compte dans la DB
- **Top 3 auteurs** : Classement par activité
- **Sujet le plus discuté** : Avec le plus de réponses
- **Dernier sujet créé** : Temps réel

### 6. Modération Avancée (Badwords)
- **Trois modes** : MASK, BLOCK, ALERT
- **Filtrage intelligent** : Case-insensitive, mots entiers
- **Priorités** : BLOCK > ALERT > MASK
- **Logging** : Tous les essais enregistrés

---

## 👥 Rôles et permissions

### ROLE_ADMIN
- ✅ Voir tous les sujets/messages
- ✅ Modifier/supprimer n'importe quel contenu
- ✅ Accès Admin + Stats + Badwords
- ✅ Gérer les badwords

### ROLE_TEACHER
- ✅ Créer sujets/messages
- ✅ Modifier/supprimer les siens uniquement
- ❌ Pas d'accès Admin/Stats/Badwords

### ROLE_STUDENT
- ✅ Créer sujets/messages
- ✅ Modifier/supprimer les siens uniquement
- ❌ Pas d'accès Admin/Stats/Badwords

---

## 🔧 Configuration

### Base de données
```
Host: localhost
Port: 3306
Database: ala
User: root
Password: (vide)
```

### Tables principales
```sql
user, sujet, message, badword, badword_log
```

### Dépendances Maven
- `org.openjfx:javafx-*:21.0.4` : GUI JavaFX
- `mysql:mysql-connector-java:8.0.33` : Connexion DB
- `org.json:json:20240303` : Parsing JSON (API traduction)

---

## 🌐 Traduction (API)

### Service
- **URL** : https://api.mymemory.translated.net
- **Sens** : Français → Anglais
- **Gratuit** : Aucune clé API requise
- **Async** : En arrière-plan (ne bloque pas UI)

### Utilisation
1. Cliquer "Traduire le contenu 🌐" lors de la création d'un sujet
2. Attendre l'affichage du résultat en anglais
3. Le contenu original reste inchangé

---

## 📋 Schéma base de données

### Table user
```
id (PK), username, roles (JSON), nom, prenom, email, is_active
```

### Table sujet
```
id (PK), titre, contenu, date_creation, auteur_id (FK→user)
```

### Table message
```
id (PK), contenu, date_publication, auteur_id (FK→user), sujet_id (FK→sujet)
```

### Table badword
```
id (PK), word (UNIQUE), action (ENUM), active, created_at
```

### Table badword_log
```
id (PK), user_id (FK→user), violated_word, action, content, timestamp
```

---

## 🐛 Dépannage

### Problème : App ne démarre pas
```bash
mvn clean compile
mvn javafx:run -e
```

### Problème : Connexion DB échouée
```bash
# Vérifier MySQL
mysql -h localhost -u root ala
# Vérifier les tables
SHOW TABLES;
```

### Problème : Badwords non appliqués
```bash
# Vérifier la table
SELECT * FROM badword WHERE active = 1;
```

---

## 🎯 Cas d'utilisation communs

### Admin : Ajouter un badword
1. Connexion en tant qu'admin
2. Cliquer 🚫 Badwords
3. Entrer le mot + sélectionner action
4. Cliquer Ajouter

### Utilisateur : Créer un sujet
1. Cliquer ➕ Nouveau Sujet
2. Remplir Titre + Contenu
3. (Optionnel) Cliquer "Traduire le contenu 🌐"
4. Cliquer 💾 Enregistrer

### Utilisateur : Répondre à un sujet
1. Cliquer sur un sujet (vue Tous les Sujets)
2. Voir les réponses existantes
3. Écrire sa réponse en bas
4. Cliquer 📤 Publier

### Admin : Voir les stats
1. Cliquer 📊 Statistiques Forum
2. Voir les cartes avec:
   - Total sujets/messages
   - Top 3 auteurs
   - Sujet le plus discuté
   - Dernier sujet créé

---

## 🚀 Compilation et déploiement

### Build JAR
```bash
mvn clean package
java -jar target/ala-eduverse-forum-1.0.0.jar
```

### Build avec plugins
```bash
mvn install
```

---

## 📞 Support

En cas de problème :
1. Vérifier les logs en console
2. Consulter la doc spécifique (BADWORDS_SYSTEM.md)
3. Vérifier la DB avec MySQL Workbench
4. Regarder les erreurs affichées dans les Alert

---

**Version 1.0 - 2026** ✅
