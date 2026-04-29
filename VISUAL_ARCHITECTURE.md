## 🎨 ARCHITECTURE VISUELLE - MODULE M5

### 📊 DIAGRAMME DE FLUX GLOBAL

```
┌─────────────────────────────────────────────────────────────────┐
│                        LIBRASYS DASHBOARD                       │
├──────────────┬──────────────────────────────────────────────────┤
│              │                                                  │
│   SIDEBAR    │          MODULE M5 - LOANS & RETURNS             │
│              │                                                  │
│  • Dashboard │  ┌──────────────────────────────────────────┐   │
│  • Students  │  │ Gestion des Prêts & Retours  [← Retour] │   │
│  • Books     │  └──────────────────────────────────────────┘   │
│  • LOANS ✓   │                                                  │
│  • Reports   │  ┌────────────┬────────────┬──────────────┐    │
│  • Settings  │  │  Summary 1 │  Summary 2 │  Summary 3   │    │
│              │  │ Prêts      │ Retours    │ Retards      │    │
│              │  │ Actifs: 84 │ Attendus:12│ Critiques: 7 │    │
│              │  └────────────┴────────────┴──────────────┘    │
│              │                                                  │
│              │  ┌─────────────────────────────────────────┐    │
│              │  │ [Prêt] [Retour] [Historique Générale]  │    │
│              │  └─────────────────────────────────────────┘    │
│              │                                                  │
│              │  ┌─────────────────────────────────────────┐    │
│              │  │      CONTENT AREA (StackPane)           │    │
│              │  │                                         │    │
│              │  │  Onglet Prêt / Retour / Historique      │    │
│              │  │  (Affichage selon tab sélectionné)      │    │
│              │  │                                         │    │
│              │  └─────────────────────────────────────────┘    │
│              │                                                  │
└──────────────┴──────────────────────────────────────────────────┘
```

---

### 🔀 FLUX DE NAVIGATION DES ONGLETS

```
╔══════════════════════════════════════════════════════════════════╗
║                    NAVIGATION TABBED INTERFACE                   ║
╠═════════════════╦═════════════════╦════════════════════════════╣
║   [Prêt] ✓      ║   [Retour]      ║  [Historique Générale]      ║
║ tab-btn active  ║  tab-btn       ║   tab-btn                   ║
║ (Border bottom) ║  inactive      ║   inactive                  ║
╚═════════════════╩═════════════════╩════════════════════════════╝
         ↓
┌─────────────────────────────────────────────────────────────────┐
│                      CONTENT CONTAINER (StackPane)              │
│                                                                 │
│  pretContainer VISIBLE:                                        │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │ Étape 1 : Identification de l'Étudiant           │          │
│  │ ┌──────────────────────┐  ┌─────────┐           │          │
│  │ │ [Matricule......]    │  │ Scanner │           │          │
│  │ └──────────────────────┘  └─────────┘           │          │
│  │ Quick Card: [AB] Ahmed Ben Ali                  │          │
│  │            Quota: 3/5 livres                    │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │ Étape 2 : Sélection du Livre                    │          │
│  │ ┌──────────────────────┐  ┌─────────┐           │          │
│  │ │ [ISBN......]         │  │ Scanner │           │          │
│  │ └──────────────────────┘  └─────────┘           │          │
│  │ Quick Card: [📖] Algorithmique Avancée          │          │
│  │             État: Excellent                     │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │ Étape 3 : Validation & Confirmation              │          │
│  │ Date de retour: 13/05/2026                      │          │
│  │ ┌─────────────────────────────────────┐         │          │
│  │ │  ✓ Confirmer le Prêt (Vert)        │         │          │
│  │ └─────────────────────────────────────┘         │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### 🔄 PROCESSUS PRÊT (M5a) EN DÉTAIL

```
┌─────────────────────────────────────────────────────────────────┐
│                        TAB PRÊT (LOAN)                           │
└─────────────────────────────────────────────────────────────────┘

ENTRÉE UTILISATEUR:
    Matricule: 123456 ──→ [Validation: ^[0-9]{6}$]
                          ├─ Valide ──→ loadStudentInfo()
                          │             ├─ Récupérer données BD
                          │             ├─ Afficher Avatar
                          │             └─ Afficher Quota
                          └─ Invalide ──→ Alert("Matricule invalide")

    ISBN: 978-1234567 ──→ [Validation: length >= 5]
                          ├─ Valide ──→ loadBookInfo()
                          │             ├─ Récupérer données BD
                          │             ├─ Afficher Titre
                          │             └─ Afficher État
                          └─ Invalide ──→ Alert("ISBN invalide")

CALCUL AUTOMATIQUE:
    
    calculateReturnDate():
        LocalDate today = LocalDate.now()     # 28/04/2026
        LocalDate returnDate = today.plus(15) # 13/05/2026
        afficher: "Date de retour: 13/05/2026"

CONFIRMATION:
    
    Clic "Confirmer le Prêt":
        ├─ Validation: matricule ≠ vide ET isbn ≠ vide
        ├─ Valide:
        │   ├─ createLoan(matricule, isbn, today, returnDate)
        │   ├─ saveToDB()
        │   ├─ addToHistory()
        │   ├─ showAlert("Succès", "Prêt confirmé...")
        │   └─ clearAllFields()
        └─ Invalide:
            └─ showAlert("Erreur", "Remplir tous les champs")

RÉSULTAT:
    ✅ Prêt créé et enregistré
    ✅ Historique mis à jour
    ✅ Interface réinitialisée
```

---

### 🔄 PROCESSUS RETOUR (M5b) EN DÉTAIL

```
┌─────────────────────────────────────────────────────────────────┐
│                      TAB RETOUR (RETURN)                         │
└─────────────────────────────────────────────────────────────────┘

ENTRÉE UTILISATEUR:
    
    Scan ISBN: 978-1234567 ──→ [Validation: length >= 5]
                                ├─ Valide ──→ findLoanByISBN()
                                │             ├─ Chercher prêt BD
                                │             └─ loadReturnInfo()
                                │                 ├─ Afficher livre
                                │                 └─ Afficher emprunteur
                                └─ Invalide ──→ Alert("Livre non trouvé")

VÉRIFICATION ÉTAT:
    
    damagedCheckBox.setOnAction():
        ├─ Si COCHÉ:
        │   ├─ calculatePenalty()
        │   ├─ penalty = 500 DA (dommage) + retard
        │   ├─ penaltyLabel.setText("Pénalité: 500 DA + retard")
        │   └─ penaltyLabel.setStyle(RED)
        └─ Si DÉCOCHÉ:
            ├─ penalty = 0 DA
            ├─ penaltyLabel.setText("Pénalité: 0 DA")
            └─ penaltyLabel.setStyle(GREEN)

VALIDATION:
    
    Clic "Valider le Retour":
        ├─ Validation: scanBookField ≠ vide
        ├─ Valide:
        │   ├─ createReturn(isbn, student, returnDate)
        │   ├─ applyPenalty(penalty) si applicable
        │   ├─ updateLoanStatus("Retourné")
        │   ├─ saveToDB()
        │   ├─ showAlert("Succès", "Retour validé...")
        │   └─ clearAllFields()
        └─ Invalide:
            └─ showAlert("Erreur", "Scanner un livre")

RÉSULTAT:
    ✅ Retour enregistré
    ✅ Prêt marqué comme retourné
    ✅ Pénalité appliquée (si nécessaire)
    ✅ Historique mis à jour
```

---

### 📊 PROCESSUS HISTORIQUE (M5c) EN DÉTAIL

```
┌─────────────────────────────────────────────────────────────────┐
│              TAB HISTORIQUE (LOAN HISTORY)                       │
└─────────────────────────────────────────────────────────────────┘

CHARGEMENT INITIAL:
    
    loadSampleData():
        └─ allLoans.addAll(
            ├─ Ahmed Ben Ali, Algorithmique, 15/04, 30/04, En cours
            ├─ Mariam Trabelsi, Physique, 10/04, 25/04, En retard
            ├─ Rached Gharbi, Droit, 08/04, 23/04, Rendu
            ├─ Samira Kooli, Chimie, 12/04, 27/04, En cours
            ├─ Layla Mansouri, BDD, 20/04, 05/05, En cours
            ├─ Karim Hadj, Distribués, 05/04, 20/04, Rendu
            ├─ Fatima Zahra, ML, 18/04, 03/05, En cours
            └─ Ali Amri, Génie, 07/04, 22/04, En retard
           )

AFFICHAGE TABLE:
    
    ┌──────────────┬─────────────────┬──────────┬──────────┬──────────┐
    │ Étudiant     │ Titre           │ Prêt     │ Retour   │ Statut   │
    ├──────────────┼─────────────────┼──────────┼──────────┼──────────┤
    │ Ahmed B.     │ Algorithmique   │ 15/04    │ 30/04    │ [En c.] │
    │ Mariam T.    │ Physique        │ 10/04    │ 25/04    │ [Retard]│
    │ Rached G.    │ Droit           │ 08/04    │ 23/04    │ [Rendu] │
    │ ...          │ ...             │ ...      │ ...      │ ...      │
    └──────────────┴─────────────────┴──────────┴──────────┴──────────┘

FILTRAGE & RECHERCHE:
    
    searchField.textProperty().addListener():
        └─ Utilisateur tape "Ahmed"
           └─ filterHistorique()
              └─ Afficher: [Ahmed Ben Ali, Algorithmique, ...]

    filterComboBox.setOnAction():
        └─ Utilisateur sélect "En retard"
           └─ filterHistorique()
              └─ Afficher: [Mariam T., Ali A.]

    Combinaison:
        └─ Recherche "Physique" + Filtre "En retard"
           └─ filterHistorique()
              └─ Afficher: [Mariam T., Physique, ..., En retard]

RENDU STATUTS (TableCell Custom):
    
    ┌──────────────────────────────────────┐
    │ Statut              CSS Class         │
    ├──────────────────────────────────────┤
    │ [Rendu]       (.status-rendu)        │
    │ Fond: #dff7e7, Texte: #1e7a43 (Vert)│
    │                                      │
    │ [En cours]    (.status-en-cours)     │
    │ Fond: #d9e6ff, Texte: #1c3e91 (Bleu)│
    │                                      │
    │ [En retard]   (.status-retard)       │
    │ Fond: #ffe4e4, Texte: #b3261e (Rouge)│
    └──────────────────────────────────────┘

RÉSULTAT:
    ✅ Tableau complet et filtrable
    ✅ Recherche en temps réel
    ✅ Statuts visuels clairs
```

---

### 🎨 STRUCTURE CSS HIÉRARCHIQUE

```
loans.css
│
├─ .loans-root
│   ├─ .loans-header
│   │   └─ .back-btn
│   │
│   ├─ .loans-summary
│   │   ├─ .summary-card
│   │   │   ├─ .summary-title
│   │   │   ├─ .summary-value
│   │   │   └─ .summary-sub
│   │   │
│   │   └─ .summary-card-alert
│   │       ├─ .summary-title
│   │       ├─ .summary-value-alert
│   │       └─ .summary-sub
│   │
│   ├─ .loans-tabs
│   │   ├─ .tab-btn
│   │   └─ .tab-btn-inactive
│   │
│   └─ .tab-content
│       ├─ .process-section
│       │   └─ .section-title
│       │
│       ├─ .form-input
│       ├─ .scan-btn
│       │
│       ├─ .quick-card
│       │   ├─ .avatar-placeholder
│       │   ├─ .book-placeholder
│       │   ├─ .student-quick-name
│       │   ├─ .student-quick-quota
│       │   ├─ .book-quick-title
│       │   └─ .book-quick-state
│       │
│       ├─ .validation-card
│       │   └─ .return-date-info
│       │
│       ├─ .confirm-btn
│       ├─ .return-btn
│       │
│       ├─ .damage-checkbox
│       ├─ .penalty-info
│       │
│       ├─ .search-input
│       ├─ .filter-combo
│       │
│       └─ .table-container
│           ├─ .table-view
│           ├─ .status-chip
│           ├─ .status-rendu
│           ├─ .status-en-cours
│           └─ .status-retard
│
└─ Animations & Pseudo-classes
    ├─ :hover
    ├─ :focused
    ├─ :selected
    └─ :filled
```

---

### 🔌 INTÉGRATION AVEC LE DASHBOARD

```
┌──────────────────────────────────────────────────────────────┐
│              DashboardController                             │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  showDashboard()  showStudents()  showBooks()               │
│      ↓                 ↓              ↓                     │
│   Dashboard        Students        Books                   │
│                                                              │
│  showLoans() ←────────────────────────────────────────────  │
│      ↓                                                       │
│   LoansView.fxml                                            │
│   └─→ LoansController                                       │
│       ├─→ 3 Onglets                                        │
│       ├─→ Processus Prêt (M5a)                             │
│       ├─→ Processus Retour (M5b)                           │
│       └─→ Historique (M5c)                                 │
│                                                              │
└──────────────────────────────────────────────────────────────┘

MainApplication.java:
    Scene {
        Stylesheets:
        - dashboard.css
        - students.css
        - books.css
        - shelves.css
        - loans.css ←── NOUVEAU
    }
```

---

### 💾 STRUCTURE DE DONNÉES

```
LoanHistoryRow (Inner Class)
├─ student: StringProperty
├─ book: StringProperty
├─ loanDate: StringProperty
├─ returnDate: StringProperty
└─ status: StringProperty

Getters:
├─ getStudent() / studentProperty()
├─ getBook() / bookProperty()
├─ getLoanDate() / loanDateProperty()
├─ getReturnDate() / returnDateProperty()
└─ getStatus() / statusProperty()
```

---

### 🎯 FLUX DE DONNEES GLOBAL

```
┌─────────────────────────────────────────────────────────────┐
│                   LIBRASYS APPLICATION                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     DashboardController.initialize()  │
        │     - loadLogo()                      │
        │     - initChart()                     │
        │     - initTable()                     │
        │     - setActiveMenu()                 │
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     Utilisateur clique "Prêts/Retours"│
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     DashboardController.showLoans()   │
        │     - FXMLLoader.load(LoansView.fxml) │
        │     - mainContentArea.setAll(view)    │
        │     - setActiveMenu(loansMenuButton)  │
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     LoansController.initialize()      │
        │     - initializeTabPret()             │
        │     - initializeTabRetour()           │
        │     - initializeTabHistorique()       │
        │     - loadSampleData()                │
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     Utilisateur interagit avec UI:    │
        │     - Remplit champs                  │
        │     - Clique boutons                  │
        │     - Navigue onglets                 │
        │     - Filtre historique               │
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     Callbacks & Event Handlers:       │
        │     - loadStudentInfo()               │
        │     - loadBookInfo()                  │
        │     - calculateReturnDate()           │
        │     - confirmLoan()                   │
        │     - loadReturnInfo()                │
        │     - calculatePenalty()              │
        │     - validateReturn()                │
        │     - filterHistorique()              │
        └───────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────────┐
        │     Feedback Utilisateur:             │
        │     - Alerts (Succès/Erreur)          │
        │     - Mise à jour UI                  │
        │     - Réinitialisation formulaires    │
        │     - Historique mis à jour           │
        └───────────────────────────────────────┘
```

---

## 📐 DIMENSIONS & ESPACEMENTS

```
┌────────────────────────────────────────────────┐
│ Largeur totale écran: 1320px                  │
├─────────────────┬──────────────────────────────┤
│ Sidebar: 250px  │ Content: 1070px             │
│                 │ (Padding: 22px chaque côté) │
│                 │                             │
│                 │ Main Container width: 1026px│
└─────────────────┴──────────────────────────────┘

Content Area Padding: 18px top/bottom, 22px left/right
Section Spacing: 16px vertical
Card Spacing: 12px (summary cards row)
Input Height: 40px (padding inclus)
Button Height: 40-44px
Card Padding: 14-16px
Border-radius: 8-12px
```

---

**Fin de l'architecture visuelle** 📐✨
