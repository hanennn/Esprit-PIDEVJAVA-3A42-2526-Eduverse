# 🚫 Système de Filtration Avancée des Badwords

## 📋 Vue d'ensemble

Le système de filtration des badwords est une fonctionnalité de modération avancée pour Eduverse Forum qui détecte et filtre automatiquement les contenus sensibles ou offensants.

## ⚙️ Trois modes d'action

### 1. **MASK** (Masquage)
- **Comportement** : Le mot interdit est remplacé par `***`
- **Utilisateur** : Voit une notification "Contenu filtré: mots sensibles masqués"
- **Modérateurs** : Pas d'alerte
- **Cas d'usage** : Mots légers ou spam mineur

### 2. **BLOCK** (Blocage)
- **Comportement** : Le message/sujet est rejeté
- **Utilisateur** : Voit "❌ Contenu refusé: mot interdit détecté"
- **Modérateurs** : Entrée dans les logs
- **Cas d'usage** : Contenu grave (spam, piratage, arnaque)

### 3. **ALERT** (Alerte)
- **Comportement** : Le contenu est filtré ET les modérateurs sont notifiés
- **Utilisateur** : Voit "⚠️ Attention: mot sensible détecté. Modérateurs notifiés"
- **Modérateurs** : Logs détaillés du message
- **Cas d'usage** : Contenu sensible (haine, violence)

## 🚀 Installation

### Mode automatique recommandé
Le système s'initialise maintenant automatiquement au lancement de l'application JavaFX. Si les tables `badword` et `badword_log` n'existent pas, elles sont créées au démarrage sans script externe.

### Vérification manuelle
```sql
mysql -u root ala < setup-badwords.sql
```

Le fichier SQL est fourni comme référence du schéma, mais il n'est plus nécessaire pour l'utilisation normale.
### Étape suivante : Lancer l'application
```bash
mvn javafx:run
```

## 🎯 Utilisation

### Interface Admin
1. Cliquez sur le bouton **"🚫 Badwords"** dans la sidebar (admin seulement)
2. Entrez un mot à ajouter dans le champ "Mot à ajouter..."
3. Sélectionnez l'action : **MASK**, **BLOCK**, ou **ALERT**
4. Cliquez sur **Ajouter**
5. Pour supprimer, cliquez sur le bouton 🗑 dans la table

### Badwords par défaut (exemple)
```
spam      → BLOCK  (bloqué directement)
hack      → BLOCK  (bloqué directement)
fraud     → ALERT  (filtré + notification)
hate      → ALERT  (filtré + notification)
violence  → ALERT  (filtré + notification)
xxx       → BLOCK  (bloqué directement)
malware   → BLOCK  (bloqué directement)
phishing  → BLOCK  (bloqué directement)
```

## 📊 Tables de la base de données

### Table `badword`
```sql
+--------+----------+-------------------------------------------+--------+
| id     | word     | action                                    | active |
+--------+----------+-------------------------------------------+--------+
| 1      | spam     | BLOCK                                     | 1      |
| 2      | fraud    | ALERT                                     | 1      |
+--------+----------+-------------------------------------------+--------+
```

### Table `badword_log`
```sql
+---------+---------+----------------+---------+------+----------+
| id      | user_id | violated_word  | action  | ... | timestamp|
+---------+---------+----------------+---------+------+----------+
| 1       | 2       | spam           | BLOCK   | ...  | 2026-... |
| 2       | 5       | fraud          | ALERT   | ...  | 2026-... |
+---------+---------+----------------+---------+------+----------+
```

## 🔍 Fonctionnement interne

### Filtrage intelligent
- **Case-insensitive** : "Spam", "SPAM", "spam" sont détectés
- **Limite aux mots entiers** : "hammer" ne déclenche pas "ham"
- **Multi-violation** : Si plusieurs badwords sont trouvés, le plus grave est appliqué
  - Priorité: BLOCK > ALERT > MASK

### Points de contrôle
1. **Création de sujet** → Filtrage du titre + contenu
2. **Publication de message** → Filtrage du contenu
3. **Logs** → Traçage de toutes les violations détectées

## 📝 Exemples de scénarios

### Scénario 1 : Message avec MASK
```
Contenu original:  "Bonjour, c'est du spam"
Après filtrage:    "Bonjour, c'est du ***"
Notification:      ✅ "Contenu filtré: mots sensibles masqués."
```

### Scénario 2 : Message avec BLOCK
```
Contenu original:  "Ceci contient hack"
Action:            ❌ Message REJETÉ
Notification:      "❌ Contenu refusé: mot interdit détecté (hack)"
```

### Scénario 3 : Message avec ALERT
```
Contenu original:  "Ce contenu parle de violence"
Après filtrage:    "Ce contenu parle de ***"
Notification user: "⚠️ Attention: mot sensible détecté. Modérateurs notifiés."
Notification mod:  Message enregistré dans les logs
```

## 🛠️ Gestion des badwords

### Ajouter un badword
```
Interface Admin → Champ "Mot à ajouter..." → Sélectionner action → Cliquer "Ajouter"
```

### Modifier un badword
1. Supprimer l'ancien (bouton 🗑)
2. Ajouter le nouveau avec la nouvelle action

### Désactiver temporairement
Actuellement, suppression = désactivation. Une feature "Toggle actif/inactif" peut être ajoutée.

## 📈 Statistiques et monitoring

Les logs des violations sont stockés dans `badword_log` :
- **Quand** : Timestamp exact
- **Qui** : User ID
- **Quel mot** : Le mot détecté
- **Quelle action** : MASK/BLOCK/ALERT
- **Contenu complet** : Pour révision par modérateurs

Pour consulter les logs :
```sql
SELECT user_id, violated_word, action, COUNT(*) as nb_violations
FROM badword_log
WHERE timestamp > DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY user_id, violated_word
ORDER BY nb_violations DESC;
```

## 🔒 Sécurité

- **Seuls les admins** peuvent ajouter/modifier/supprimer les badwords
- **Les logs** sont conservés même après suppression du badword
- **Les utilisateurs réguliers** ne voient que les avertissements/rejets
- **Les modérateurs** ont accès aux logs complets pour analyse

## 🚀 Améliorations futures possibles

- [ ] Expressions régulières pour les patterns complexes
- [ ] Détection de variations (l33t speak: h4ck, h@ck)
- [ ] Pagination des logs de violations
- [ ] Export des logs en CSV
- [ ] Statistiques visuel par badword
- [ ] Règles basées sur le contexte (e.g., "sex" bloqué sauf en contexte éducatif)
- [ ] Machine Learning pour détection automatique de contenu offensant
- [ ] Notifications en temps réel aux modérateurs
- [ ] Sanctions progressives (avertissement → suspension)

---

**Version 1.0** - Système complet et fonctionnel de filtration des badwords avec trois modes d'action ✅
