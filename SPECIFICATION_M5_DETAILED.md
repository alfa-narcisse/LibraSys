## SPÉCIFICATION DÉTAILLÉE - MODULE M5 GESTION DES PRÊTS & RETOURS

### 📋 TABLE DES MATIÈRES
1. [Vue d'ensemble](#vue-densemble)
2. [Architecture générale](#architecture-générale)
3. [Onglet PRÊT (M5a)](#onglet-prêt-m5a)
4. [Onglet RETOUR (M5b)](#onglet-retour-m5b)
5. [Onglet HISTORIQUE (M5c)](#onglet-historique-m5c)
6. [Récapitulatif (Mini-Dashboard)](#récapitulatif-mini-dashboard)
7. [Navigation & Intégration](#navigation--intégration)
8. [Design & CSS](#design--css)

---

## 🎯 VUE D'ENSEMBLE

**Objectif:** Créer un module complet de gestion des prêts et retours de livres pour la bibliothèque LibraSys.

**Élément Principal:** Fragment injecté dans le center du Dashboard (sidebar reste visible)

**Naviguation:** Via le bouton "Prêts/Retours" du menu latéral

---

## 🏗️ ARCHITECTURE GÉNÉRALE

### Fichiers implémentés:
```
LibraSys/
├── src/main/java/com/librasys/controller/
│   └── LoansController.java (405 lignes)
├── src/main/resources/com/librasys/
│   ├── LoansView.fxml (180 lignes)
│   └── loans.css (357 lignes)
└── MODULE_M5_LOANS_README.md
```

### Structure hiérarchique:
```
LoansView.fxml (VBox)
├── loans-header
│   ├── Label "Gestion des Prêts & Retours"
│   └── Button "← Retour"
├── loans-summary
│   ├── VBox "Prêts Actifs" (84)
│   ├── VBox "Retours Attendus" (12)
│   └── VBox "Retards Critiques" (7)
├── loans-tabs
│   ├── Button "Prêt"
│   ├── Button "Retour"
│   └── Button "Historique Générale"
└── contentContainer (StackPane)
    ├── pretContainer (Prêt)
    ├── retourContainer (Retour)
    └── historiqueContainer (Historique)
```

---

## 🔗 ONGLET PRÊT (M5a)

### Processus en 3 étapes séquentielles:

#### **ÉTAPE 1 - Identification de l'Étudiant**

**Composants:**
- TextField: "Matricule Étudiant" (6 chiffres)
- Button: "📱 Scanner" (icône scan)
- Quick Card: Affichage des infos étudiant

**Comportement:**
```
Utilisateur saisit matricule
    ↓
validateMatricule(regex: ^[0-9]{6}$)
    ↓
Si valide:
  - loadStudentInfo(matricule)
  - Afficher: Nom, Avatar (initiales), Quota
  - Color: Label quota en vert (#2ecc71) si quota > 0
Si invalide:
  - Alert: "Matricule invalide"
  - Clear fields
```

**Validation:**
- Format: 6 chiffres exactement
- Feedback: Alert en cas d'erreur
- Avatar placeholder: Initiales sur fond bleu #1a237e

#### **ÉTAPE 2 - Sélection du Livre**

**Composants:**
- TextField: "ISBN / Code-barres Livre"
- Button: "📱 Scanner" (icône scan)
- Quick Card: Affichage des infos livre

**Comportement:**
```
Utilisateur saisit ISBN
    ↓
validateISBN(length >= 5)
    ↓
Si valide:
  - loadBookInfo(isbn)
  - Afficher: Titre, Miniature (📖), État
  - Color: État en vert si "Excellent"
Si invalide:
  - Alert: "ISBN invalide"
```

**Validation:**
- Format: Minimum 5 caractères
- Feedback: Alert en cas d'erreur
- Livre placeholder: Emoji 📖 sur fond bleu clair

#### **ÉTAPE 3 - Validation & Confirmation**

**Composants:**
- Label: "Date de retour: JJ/MM/YYYY" (auto-calculée)
- Button: "✓ Confirmer le Prêt" (Vert #2ecc71)

**Comportement:**
```
calculateReturnDate():
  LocalDate returnDate = LocalDate.now().plusDays(15)
  afficher: "Date de retour: " + dateFormatter(returnDate)

Clic "Confirmer":
  - Validation: matricule + ISBN non vides
  - createLoan(student, book, loanDate, returnDate)
  - showAlert("Succès", "Prêt confirmé pour Ahmed Ben Ali...")
  - clearAllFields()
  - Ajouter à historique
```

**Validation:**
- Tous les champs doivent être remplis
- Feedback: Alert de confirmation
- Reset automatique après confirmation

### **Données affichées (Quick Cards):**

**Student Quick Card:**
```
┌─────────────────────────────┐
│ [AB] Ahmed Ben Ali          │
│      Quota: 3/5 livres      │
└─────────────────────────────┘
```

**Book Quick Card:**
```
┌─────────────────────────────┐
│ [📖] Algorithmique Avancée │
│      État: Excellent        │
└─────────────────────────────┘
```

---

## 🔗 ONGLET RETOUR (M5b)

### Processus simplifié (2 étapes):

#### **ÉTAPE 1 - Scan Direct**

**Composants:**
- TextField: "Scanner le livre à retourner"
- Button: "📱 Scanner"

**Comportement:**
```
Utilisateur scanne livre
    ↓
findLoanByISBN(isbn)
    ↓
Si trouvé:
  - loadReturnInfo(isbn)
  - Afficher: Livre, Emprunteur
  - returnStudentLabel.setText("Livre: ... - Emprunteur: ...")
Si non trouvé:
  - Alert: "Livre non trouvé"
```

#### **ÉTAPE 2 - Vérification État & Pénalité**

**Composants:**
- CheckBox: "Livre endommagé"
- Label: "Pénalité: X DA"

**Calcul de pénalité:**
```
calculatePenalty():
  if (damagedCheckBox.isSelected()):
    pénalité = 500 DA (dommage) + retard
    penaltyLabel.setStyle(RED)
    penaltyLabel.setText("Pénalité: 500 DA (dommage) + retard")
  else:
    pénalité = 0 DA
    penaltyLabel.setStyle(GREEN)
    penaltyLabel.setText("Pénalité: 0 DA")
```

**Validation du Retour:**

**Composants:**
- Button: "✓ Valider le Retour" (Bleu #1a237e)

**Comportement:**
```
Clic "Valider le Retour":
  - Validation: scanBookField non vide
  - createReturn(book, student, returnDate, damaged, penalty)
  - updateLoanHistory()
  - showAlert("Succès", "Retour validé pour ...")
  - clearAllFields()
```

**Validation:**
- Au moins un scan requis
- Feedback: Alert de confirmation
- Historique mis à jour automatiquement

---

## 🔗 ONGLET HISTORIQUE (M5c)

### Vue tableau complète et filtrable:

#### **Tableau des emprunts**

**Colonnes:**
| Colonne | Largeur | Type | Description |
|---------|---------|------|-------------|
| Étudiant | 200px | String | Nom complet de l'étudiant |
| Titre | 250px | String | Titre du livre emprunté |
| Date Prêt | 120px | String | Date du prêt (JJ/MM/YYYY) |
| Date Retour | 120px | String | Date de retour prévue (JJ/MM/YYYY) |
| Statut | 110px | Chip | Rendu / En cours / En retard |

#### **Filtrage & Recherche**

**Composants:**
- TextField: "Chercher par nom, titre..."
- ComboBox: "Statut" (Tous, Rendu, En cours, En retard)

**Comportement:**
```
Recherche:
  input.textProperty().addListener() 
  → filterHistorique()
  → afficher items contenant recherche (case-insensitive)

Filtre:
  comboBox.setOnAction()
  → filterHistorique()
  → afficher items avec statut sélectionné

Combinaison:
  filtrer par (statut) ET (recherche)
```

#### **Données d'exemple (8 lignes)**

| Étudiant | Titre | Date Prêt | Date Retour | Statut |
|----------|-------|-----------|-------------|--------|
| Ahmed Ben Ali | Algorithmique Avancée | 15/04/2026 | 30/04/2026 | En cours |
| Mariam Trabelsi | Physique Quantique | 10/04/2026 | 25/04/2026 | En retard |
| Rached Gharbi | Droit Civil | 08/04/2026 | 23/04/2026 | Rendu |
| Samira Kooli | Chimie Organique | 12/04/2026 | 27/04/2026 | En cours |
| Layla Mansouri | Base de Données | 20/04/2026 | 05/05/2026 | En cours |
| Karim Hadj | Systèmes Distribués | 05/04/2026 | 20/04/2026 | Rendu |
| Fatima Zahra | Machine Learning | 18/04/2026 | 03/05/2026 | En cours |
| Ali Amri | Génie Logiciel | 07/04/2026 | 22/04/2026 | En retard |

#### **Statuts visuels (CSS Chips)**

**Rendu (Vert):**
```css
.status-rendu {
    -fx-background-color: #dff7e7;
    -fx-text-fill: #1e7a43;
}
```

**En cours (Bleu):**
```css
.status-en-cours {
    -fx-background-color: #d9e6ff;
    -fx-text-fill: #1c3e91;
}
```

**En retard (Rouge):**
```css
.status-retard {
    -fx-background-color: #ffe4e4;
    -fx-text-fill: #b3261e;
}
```

---

## 📊 RÉCAPITULATIF (Mini-Dashboard)

### 3 cartes de scores en haut du module:

#### **Carte 1 - Prêts Actifs**
```
┌──────────────────┐
│ Prêts Actifs     │
│ 84               │
│ +3 aujourd'hui   │
└──────────────────┘
```
- Valeur: 84 (grand, #1a237e)
- Sous-texte: "+3 aujourd'hui" (gris #7e849f)

#### **Carte 2 - Retours Attendus Aujourd'hui**
```
┌──────────────────────────┐
│ Retours Attendus         │
│ Aujourd'hui              │
│ 12                       │
│ 2 en retard              │
└──────────────────────────┘
```
- Valeur: 12 (grand, #1a237e)
- Sous-texte: "2 en retard" (gris #7e849f)

#### **Carte 3 - Retards Critiques**
```
┌──────────────────┐
│ Retards          │
│ Critiques        │
│ 7                │
│ À traiter        │
└──────────────────┘
```
- Fond: Rose clair (#fff5f5)
- Bordure: Rose (#ffe4e4)
- Valeur: 7 (grand, rouge #d32f2f)
- Sous-texte: "À traiter aujourd'hui" (gris)

**CSS:** `.summary-card` (12px border-radius, white bg, shadow)

---

## 🔗 NAVIGATION & INTÉGRATION

### Intégration Dashboard:

**Bouton Menu Latéral:**
```xml
<Button fx:id="loansMenuButton" 
        text="Prets/Retours" 
        styleClass="menu-btn" 
        onAction="#showLoans"/>
```

**Handler DashboardController:**
```java
@FXML
private Button loansMenuButton;

@FXML
private void showLoans() {
    Pane view = FXMLLoader.load(getClass()
        .getResource("/com/librasys/LoansView.fxml"));
    mainContentArea.getChildren().setAll(view);
    setActiveMenu(loansMenuButton);
}
```

### Bouton Retour:
- Placeholder (sera traitée par framework)
- Position: Haut droit du module
- Style: Fond gris clair, texte bleu nuit

### Sidebar:
- Reste visible lors du chargement du module
- Bouton "Prêts/Retours" surligné (classe `.menu-btn-selected`)

---

## 🎨 DESIGN & CSS

### Thème Bleu Nuit:

**Couleurs principales:**
| Nom | Hex | Utilisation |
|-----|-----|-------------|
| Bleu Nuit | #1a237e | Texte principal, tabs actif, avatars |
| Vert Succès | #2ecc71 | Bouton confirmer, statuts positifs |
| Bleu Retour | #1a237e | Bouton retour, tabs |
| Rouge Alerte | #d32f2f | Retards, erreurs |
| Gris Clair | #f4f6fc | Arrière-plan |
| Blanc | #ffffff | Cartes, input |

**Typographie:**
- Font: "Segoe UI", "Inter", sans-serif
- Titres: 14-22px, weight 700-800
- Corps: 12-13px, weight 400-600
- Labels: 11-12px, weight 700

**Espacements:**
- Padding cartes: 14-16px
- Margin sections: 16px
- Border-radius: 8-12px
- Box shadow: Discrète, offset 2-4px

### Classes CSS principales:

| Classe | Utilisation |
|--------|-------------|
| `.loans-root` | Conteneur principal |
| `.loans-header` | Barre d'en-tête |
| `.loans-summary` | Cartes de résumé |
| `.loans-tabs` | Barre de tabs |
| `.process-section` | Sections de processus |
| `.quick-card` | Cartes d'info rapide |
| `.confirm-btn` | Bouton confirmer (vert) |
| `.return-btn` | Bouton retour (bleu) |
| `.status-chip` | Badges statut |
| `.form-input` | Champs texte |

---

## 📋 CHECKLIST D'IMPLÉMENTATION

✅ Controller Java (LoansController.java)
✅ Interface FXML (LoansView.fxml)
✅ Styles CSS (loans.css)
✅ Intégration Dashboard
✅ Navigation tabs
✅ Prêt - 3 étapes
✅ Retour - 2 étapes
✅ Historique - tableau filtrable
✅ Récapitulatif - 3 cartes
✅ Données d'exemple
✅ Validation des inputs
✅ Alerts utilisateur
✅ Thème Bleu Nuit

---

## 🚀 PROCHAINES ÉTAPES

1. **Intégration BD:** Connecter à une vraie source de données
2. **Lecteur code-barres:** Implémenter API de scanner matériel
3. **Export PDF:** Générer rapports d'emprunt
4. **Notifications:** Push/Email pour retards
5. **Rapports:** Statistiques avancées
6. **Améliorations UX:** Animations, transitions

---

**Version:** 1.0
**Date:** Avril 2026
**Statut:** ✅ Complet
