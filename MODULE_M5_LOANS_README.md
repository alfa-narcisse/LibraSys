# Module Gestion des Prêts & Retours (M5) - LibraSys

## 📋 Vue d'ensemble

Le module M5 "Gestion des Prêts & Retours" est un composant crucial de LibraSys permettant de gérer le cycle de vie complet des prêts de livres.

## 🏗️ Architecture

### Fichiers créés:
- **LoansController.java** - Contrôleur principal avec logique métier
- **LoansView.fxml** - Interface utilisateur (FXML)
- **loans.css** - Styles CSS avec thème Bleu Nuit

### Intégration:
- Intégré dans DashboardController
- Accessible via le bouton "Prêts/Retours" dans le menu latéral
- Fragment injecté dans mainContentArea du Dashboard

## 🎯 Fonctionnalités

### 1. Onglet PRÊT (M5a)
**Processus en 3 étapes :**

**Étape 1 - Identification de l'Étudiant:**
- Champ de saisie pour matricule étudiant (6 chiffres)
- Icône scanner (📱) pour lecteur code-barres
- Affichage rapide: Nom, Avatar, Quota disponible
- Validation automatique

**Étape 2 - Sélection du Livre:**
- Champ de saisie pour ISBN/Code-barres
- Icône scanner (📱) pour lecteur code-barres
- Affichage rapide: Titre, Miniature, État du livre
- Validation automatique

**Étape 3 - Validation & Confirmation:**
- Calcul automatique de la date de retour (J+15 jours par défaut)
- Bouton "✓ Confirmer le Prêt" (Vert #2ecc71)
- Confirmation avec feedback utilisateur

### 2. Onglet RETOUR (M5b)
**Processus simplifié :**

**Scan Direct:**
- Un seul champ "Scanner le livre"
- Recherche automatique de l'emprunteur et du prêt associé
- Affichage: Livre, Emprunteur

**Vérification de l'État:**
- Checkbox "Livre endommagé"
- Calcul automatique de la pénalité
- Affichage du montant en temps réel

**Validation:**
- Bouton "✓ Valider le Retour" (Bleu #1a237e)
- Historique mis à jour automatiquement

### 3. Onglet HISTORIQUE (M5c)
**Vue complète et filtrable :**

**Tableau des emprunts:**
- Colonnes: Étudiant, Titre, Date Prêt, Date Retour, Statut
- Statuts: "Rendu" (vert), "En cours" (bleu), "En retard" (rouge)

**Filtrage & Recherche:**
- Recherche par nom d'étudiant ou titre de livre
- Filtre par statut (Tous, Rendu, En cours, En retard)
- Recherche en temps réel

## 🎨 Design & Couleurs

### Thème Bleu Nuit (#1a237e)
- **Couleur primaire:** #1a237e (Bleu Nuit)
- **Couleur succès:** #2ecc71 (Vert)
- **Couleur erreur:** #d32f2f (Rouge)
- **Couleur arrière-plan:** #f4f6fc (Gris clair)
- **Texte principal:** #273253 (Gris foncé)

### Composants stylisés:
- **Cartes de résumé:** Avec ombre discrète
- **Boutons tabs:** Avec bordure active en bas
- **Entrées:** Fond gris avec bordure bleue au focus
- **Puces de statut:** Arrière-plan coloré avec texte contrasté
- **Table:** Lignes alternées avec hover effect

## 🔄 Flux de données

### Prêt:
```
Matricule Étudiant → Validation → Données Étudiant
ISBN Livre → Validation → Données Livre
Calcul automatique Date Retour
Confirmation → Enregistrement
```

### Retour:
```
Scan Livre → Recherche automatique emprunteur et prêt
Vérification état (endommagé?)
Calcul pénalité si retard/dommage
Validation → Mise à jour historique
```

### Historique:
```
Tous les emprunts (prêts + retours)
Filtrage dynamique par statut/date/nom
Affichage en tableau avec statuts visuels
```

## 📊 Données d'exemple

Le module est prérempli avec 8 emprunts d'exemple pour démonstration:
- Ahmed Ben Ali - Algorithmique Avancée (En cours)
- Mariam Trabelsi - Physique Quantique (En retard)
- Rached Gharbi - Droit Civil (Rendu)
- Et 5 autres...

## 🔧 Configuration

### Intégration avec DashboardController:
```java
@FXML
private Button loansMenuButton;

@FXML
private void showLoans() {
    Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/LoansView.fxml"));
    mainContentArea.getChildren().setAll(view);
    setActiveMenu(loansMenuButton);
}
```

### Styles CSS chargés:
- dashboard.css (styles de base)
- students.css (composants réutilisables)
- books.css (composants réutilisables)
- shelves.css (composants réutilisables)
- **loans.css** (styles spécifiques au module M5)

## 🚀 Utilisation

### Pour accéder au module:
1. Depuis le Dashboard principal
2. Cliquer sur "Prêts/Retours" dans le menu latéral
3. Les trois onglets sont disponibles: Prêt, Retour, Historique

### Onglet Prêt:
1. Entrer ou scanner le matricule étudiant
2. Entrer ou scanner l'ISBN du livre
3. Vérifier la date de retour proposée (J+15)
4. Cliquer "Confirmer le Prêt"

### Onglet Retour:
1. Scanner le livre à retourner
2. Cocher si le livre est endommagé (optionnel)
3. Vérifier la pénalité calculée
4. Cliquer "Valider le Retour"

### Onglet Historique:
1. Chercher par nom ou titre
2. Filtrer par statut
3. Consulter les détails des emprunts

## 📝 Notes de développement

- **Validation:** Les champs acceptent les entrées de lecteurs code-barres
- **Feedback:** Alerts pour les opérations (succès/erreur)
- **Responsive:** S'adapte à différentes tailles d'écran
- **Accessibilité:** Labels clairs et structure logique

## 🔮 Améliorations futures

- [ ] Intégration avec une vraie base de données
- [ ] Lecteur code-barres réel (API)
- [ ] Notification push pour retards
- [ ] Export en PDF des historiques
- [ ] Rapports d'emprunt par étudiant/livre
- [ ] Calcul automatique des pénalités avancées
- [ ] Intégration email pour rappels

## ⚙️ Configuration technique

- **JavaFX:** 21.0.6
- **Java:** 21
- **Framework:** FXML + CSS styling
- **Architecture:** MVC avec FXMLLoader

---

**Auteur:** Senior JavaFX Developer & UI/UX Specialist
**Date:** Avril 2026
**Version:** 1.0
