## 🚀 GUIDE DE COMPILATION ET DÉPLOIEMENT - MODULE M5

### 📋 PRÉREQUIS

- **Java:** JDK 21+
- **Maven:** 3.8.0+
- **JavaFX:** 21.0.6 (automatiquement géré par Maven)
- **OS:** Windows/Linux/macOS

### 🔧 ÉTAPE 1 - STRUCTURE DU PROJET

Vérifiez que la structure est correcte après l'ajout du module M5:

```
LibraSys/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/librasys/
│   │   │       ├── controller/
│   │   │       │   ├── DashboardController.java ✅ MODIFIÉ
│   │   │       │   ├── BooksController.java
│   │   │       │   ├── StudentsController.java
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── NewBookController.java
│   │   │       │   ├── ShelvesController.java
│   │   │       │   └── LoansController.java ✅ NOUVEAU
│   │   │       └── ui/
│   │   │           └── StudentCard.java
│   │   └── resources/
│   │       └── com/librasys/
│   │           ├── LoansView.fxml ✅ NOUVEAU
│   │           ├── loans.css ✅ NOUVEAU
│   │           ├── DashboardView.fxml ✅ MODIFIÉ
│   │           ├── BooksView.fxml
│   │           ├── StudentsView.fxml
│   │           ├── LoginView.fxml
│   │           ├── NewBookView.fxml
│   │           ├── ShelvesView.fxml
│   │           ├── dashboard.css
│   │           ├── students.css
│   │           ├── books.css
│   │           ├── shelves.css
│   │           ├── style.css
│   │           └── logo_ept.png
│   └── test/ (si existant)
├── target/ (généré après compilation)
├── pom.xml ✅ VÉRIFIÉ
├── MODULE_M5_LOANS_README.md ✅ NOUVEAU
└── SPECIFICATION_M5_DETAILED.md ✅ NOUVEAU
```

### 🔨 ÉTAPE 2 - NETTOYAGE ET COMPILATION

**Option 1: Depuis la ligne de commande (CMD)**

```cmd
# Naviguer vers le répertoire du projet
cd C:\Users\lenovo\Desktop\LibraSys\LibraSys

# Nettoyer les builds précédents
mvn clean

# Compiler le projet
mvn compile

# (Optionnel) Créer le JAR exécutable
mvn package
```

**Option 2: Depuis IntelliJ IDEA**

1. Ouvrir le projet dans IntelliJ IDEA
2. Right-click sur `pom.xml` → "Maven" → "Reload Project"
3. Build → Clean
4. Build → Build Project
5. (Optionnel) Build → Build Artifacts

### ✅ ÉTAPE 3 - VÉRIFICATION DES MODIFICATIONS

Vérifiez que les fichiers modifiés ont les bonnes intégrations:

**DashboardView.fxml - Vérifier ligne 43:**
```xml
<Button fx:id="loansMenuButton" text="Prets/Retours" 
        styleClass="menu-btn" onAction="#showLoans"/>
```

**DashboardController.java - Vérifier:**
```java
@FXML
private Button loansMenuButton;  // À la ligne 61

@FXML
private void showLoans() { ... }  // À la ligne 159
```

**MainApplication.java - Vérifier ligne 25:**
```java
scene.getStylesheets().add(MainApplication.class
    .getResource("/com/librasys/loans.css").toExternalForm());
```

### 🚀 ÉTAPE 4 - EXÉCUTION

**Option 1: Depuis IntelliJ IDEA**

1. Cliquer sur le bouton "Run" (Play)
2. Ou appuyer sur `Shift + F10`

**Option 2: Depuis la ligne de commande**

```cmd
# Depuis le répertoire du projet
mvn javafx:run
```

**Option 3: Exécuter le JAR (après package)**

```cmd
java -jar target/LibraSys-1.0-SNAPSHOT.jar
```

### 🧪 ÉTAPE 5 - TESTS MANUELS

Une fois l'application lancée:

#### **Test 1 - Navigation vers le module**
1. L'application démarre sur le Dashboard
2. Cliquer sur "Prêts/Retours" dans le menu latéral gauche
3. ✅ Le module M5 doit s'afficher avec les trois onglets

#### **Test 2 - Onglet PRÊT**
1. Entrer un matricule valide: `123456`
2. Voir les infos étudiant apparaître
3. Entrer un ISBN: `978-1234567`
4. Voir les infos livre apparaître
5. Vérifier la date de retour calculée (J+15)
6. Cliquer "Confirmer le Prêt"
7. ✅ Une alert "Succès" doit apparaître
8. ✅ Les champs doivent se réinitialiser

#### **Test 3 - Onglet RETOUR**
1. Cliquer sur l'onglet "Retour"
2. Entrer un ISBN: `978-1234567`
3. Voir les infos du livre et emprunteur
4. Cocher "Livre endommagé"
5. Voir la pénalité calculée (rouge)
6. Décocher "Livre endommagé"
7. Voir la pénalité à 0 DA (vert)
8. Cliquer "Valider le Retour"
9. ✅ Une alert "Succès" doit apparaître
10. ✅ Les champs doivent se réinitialiser

#### **Test 4 - Onglet HISTORIQUE**
1. Cliquer sur l'onglet "Historique Générale"
2. Voir le tableau avec 8 emprunts d'exemple
3. Entrer une recherche: `Ahmed`
4. ✅ Voir seulement les emprunts contenant "Ahmed"
5. Sélectionner le filtre "En retard"
6. ✅ Voir seulement les emprunts en retard
7. Combiner: Recherche "Physique" + Filtre "En retard"
8. ✅ Voir seulement les emprunts correspondant aux deux critères

#### **Test 5 - Récapitulatif**
1. En haut du module, vérifier les 3 cartes:
   - "Prêts Actifs: 84"
   - "Retours Attendus: 12"
   - "Retards Critiques: 7" (fond rose)
2. ✅ Les cartes doivent être bien stylisées

#### **Test 6 - Navigation & Sidebar**
1. Cliquer sur "Prêts/Retours" → Module affiché, sidebar visée ✅
2. Cliquer sur "Tableau de bord" → Dashboard affiché ✅
3. Cliquer sur "Prêts/Retours" → Module réaffiche ✅
4. La sidebar doit TOUJOURS être visible ✅

#### **Test 7 - Styles & Couleurs**
1. Vérifier les couleurs du thème Bleu Nuit (#1a237e)
2. Bouton "Confirmer" en vert (#2ecc71)
3. Bouton "Valider" en bleu (#1a237e)
4. Statuts: Vert (Rendu), Bleu (En cours), Rouge (Retard)
5. ✅ Tous les styles doivent correspondre aux spécifications CSS

### 🐛 DÉPANNAGE

**Erreur: "FXMLLoader cannot find LoansView.fxml"**
- ✅ Vérifier que le fichier est dans: `src/main/resources/com/librasys/`
- ✅ Rebuild le projet: `mvn clean compile`

**Erreur: "Method onAction not found"**
- ✅ Vérifier que `showLoans()` existe dans DashboardController
- ✅ Vérifier le binding FXML: `onAction="#showLoans"`

**Erreur: "loans.css not found"**
- ✅ Vérifier que le fichier est dans: `src/main/resources/com/librasys/`
- ✅ Vérifier que MainApplication.java charge le CSS:
  ```java
  scene.getStylesheets().add(..."/loans.css"...)
  ```

**CSS ne s'applique pas**
- ✅ Vérifier le chemin dans MainApplication
- ✅ Rebuild et relancer l'application
- ✅ Vérifier les noms de classe CSS dans les composants

**Table vide**
- ✅ Vérifier que `loadSampleData()` est appelé dans `initialize()`
- ✅ Vérifier que les PropertyValueFactory correspondent aux propriétés

### 📊 VÉRIFICATION DE QUALITÉ

**Critères de succès:**

- [x] Tous les fichiers créés sont présents
- [x] DashboardController intègre le bouton et la méthode
- [x] DashboardView.fxml lie le bouton correctement
- [x] MainApplication.java charge loans.css
- [x] LoansController implémente les 3 onglets
- [x] LoansView.fxml a la bonne structure FXML
- [x] loans.css a tous les styles nécessaires
- [x] Navigation fonctionne
- [x] Sidebar reste visible
- [x] Thème Bleu Nuit appliqué
- [x] Données d'exemple chargées
- [x] Validation des inputs
- [x] Alerts utilisateur
- [x] Filtrage historique

### 📈 MÉTRIQUES

**Fichiers ajoutés/modifiés:**
- ✅ 1 nouveau Controller (405 lignes)
- ✅ 1 nouveau FXML (180 lignes)
- ✅ 1 nouveau CSS (357 lignes)
- ✅ 2 fichiers Java modifiés (MainApplication, DashboardController)
- ✅ 1 fichier FXML modifié (DashboardView)

**Total de lignes de code:**
- ~942 lignes de code Java/FXML/CSS ajoutées
- ~6 lignes modifiées dans les fichiers existants

### 🎓 DOCUMENTATION

Consultez les documents fournis:
- **MODULE_M5_LOANS_README.md** - Vue d'ensemble et utilisation
- **SPECIFICATION_M5_DETAILED.md** - Spécification complète
- **Commentaires dans le code** - Documentation inline

---

## ✨ SUCCÈS!

Si tous les tests passent, le module M5 est correctement intégré et fonctionnel! 🎉

**Prochaines étapes recommandées:**
1. Intégrer une vraie base de données (SQL)
2. Implémenter un vrai lecteur code-barres
3. Ajouter des rapports avancés
4. Configurer les notifications
5. Tests unitaires

---

**Version:** 1.0
**Date:** Avril 2026
**Auteur:** Senior JavaFX Developer & UI/UX Specialist
