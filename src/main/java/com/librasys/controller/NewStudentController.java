package com.librasys.controller;

import com.librasys.dao.studentdao;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class NewStudentController {

    @FXML
    public VBox studentDetailPane;

    @FXML private VBox newStudentRoot;

    // les champs à remplir
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> classNameCombo;
    @FXML private ComboBox<String> yearLevelCombo;
    @FXML private ComboBox<String> promotionCombo;
    @FXML private Label feedbackLabel;

    // Preview elements (carte étudiant) mapped to Students detail style
    @FXML private javafx.scene.control.Label detailNameLabel;
    @FXML private javafx.scene.control.Label detailSectionLabel;
    @FXML private javafx.scene.control.Label detailMatriculeLabel;
    @FXML private javafx.scene.control.Label detailLoanChipLabel;
    @FXML private javafx.scene.control.Label cardNameLabel;
    @FXML private javafx.scene.control.Label cardMatriculeLabel;

    // Pour la connexion avec la base de données pour enregistrer les formulaires
    private final studentdao studentDAO = new studentdao();

    @FXML
    private void initialize() {
        feedbackLabel.setText("");  // Un id permettant d'afficher les erreurs occurents

        classNameCombo.setItems(FXCollections.observableArrayList(
                "TC-1", "TC-2",
                "SISY", "SYSCO",
                "EGES"
        ));

        yearLevelCombo.setItems(FXCollections.observableArrayList(
                "1ère", "2ème", "3ème"
        ));

        // Promotions: fixed range 2015..2040
        int currentYear = LocalDate.now().getYear();
        if (promotionCombo != null) {
            javafx.collections.ObservableList<String> promos = FXCollections.observableArrayList();
            for (int y = 2020; y <= 2035; y++) promos.add(String.valueOf(y));
            promotionCombo.setItems(promos);

            // Mettre l'année actuelle comme valeur par défaut
            String cur = String.valueOf(currentYear);
            if (promos.contains(cur)) promotionCombo.getSelectionModel().select(cur);
        }

        // On ajoute des valeurs par défaut et on modifie le contenu dès que le personnel saisie l'information
        if (detailNameLabel != null) detailNameLabel.setText("Nom complet");
        if (detailMatriculeLabel != null) detailMatriculeLabel.setText("2500XY");
        if (detailSectionLabel != null) detailSectionLabel.setText("SISY");
        if (detailLoanChipLabel != null) detailLoanChipLabel.setText("0 prêts");
        if (cardNameLabel != null) cardNameLabel.setText("Nom complet");
        if (cardMatriculeLabel != null) cardMatriculeLabel.setText("2500XY");

        // Génération  à temps réel de la carte de bibliothèque:
        if (fullNameField != null) {
            fullNameField.textProperty().addListener((obs, oldV, newV) -> {
                String v = (newV == null || newV.isBlank()) ? "Nom complet" : newV;
                if (detailNameLabel != null) detailNameLabel.setText(v);
                if (cardNameLabel != null) cardNameLabel.setText(v);
            });
        }
        if (matriculeField != null) {
            matriculeField.textProperty().addListener((obs, oldV, newV) -> {
                String v = (newV == null || newV.isBlank()) ? "2500XY" : newV;
                if (detailMatriculeLabel != null) detailMatriculeLabel.setText(v);
                if (cardMatriculeLabel != null) cardMatriculeLabel.setText(v);
            });
        }
        if (classNameCombo != null) {
            classNameCombo.valueProperty().addListener((obs, oldV, newV) -> {
                String cls = (newV == null) ? "SISY" : newV;
                String promo = (promotionCombo != null && promotionCombo.getValue() != null) ? promotionCombo.getValue() : String.valueOf(currentYear);
                if (detailSectionLabel != null) detailSectionLabel.setText(cls + " - " + promo);
            });
        }
        if (yearLevelCombo != null) {
            yearLevelCombo.valueProperty().addListener((obs, oldV, newV) -> {
                String lvl = (newV == null) ? "1ère" : newV;
                String cls = (classNameCombo != null && classNameCombo.getValue() != null) ? classNameCombo.getValue() : "SISY";
                if (detailSectionLabel != null) detailSectionLabel.setText(cls + " - " + lvl);
            });
        }
        if (promotionCombo != null) {
            promotionCombo.valueProperty().addListener((obs, oldV, newV) -> {
                String promo = (newV == null) ? String.valueOf(currentYear) : newV;
                String cls = (classNameCombo != null && classNameCombo.getValue() != null) ? classNameCombo.getValue() : "SISY";
                if (detailSectionLabel != null) detailSectionLabel.setText(cls + " - " + promo);
            });
        }
    }

    @FXML
    private void onBackToList() {
        navigateToStudentsList();
    }

    @FXML
    private void onCancel() {
        navigateToStudentsList();
    }


    // Si on clique sur le bouton ajouter l'étudiant
    @FXML
    private void onSaveStudent() {

        // 1ere etape : validation des champs de remplissage  ( on utilise la méthode déjà créer):
        if (!validateFields()) return;

        String fullName  = fullNameField.getText().trim();
        String email     = emailField.getText().trim();
        String matricule = matriculeField.getText().trim();
        String className = classNameCombo == null ? null : classNameCombo.getValue();
        String yearLevel = yearLevelCombo == null ? null : yearLevelCombo.getValue();
        String promotion = promotionCombo == null ? null : promotionCombo.getValue();
        LocalDate dateInscription = LocalDate.now();

        // 2 etape : verification de l'existence du matricule
        if (studentDAO.matriculeExists(matricule)) {
            feedbackLabel.setText("Ce matricule existe déjà.");
            return;
        }

        // 3 etape : verification de l'existence de l'email
        if (studentDAO.emailExists(email)) {
            feedbackLabel.setText("Cet email est déjà utilisé.");
            return;
        }

        // 4 etape : sauvegarde dans la BDD
        boolean saved = studentDAO.addStudent(
                fullName, email, matricule,
                className, yearLevel,
                dateInscription, promotion
        );

        if (!saved) {
            feedbackLabel.setText("Erreur lors de l'enregistrement de l'étudiant.");
            return;
        }

        // Ajout validé
        showSuccess("Étudiant \"" + fullName + "\" ajouté avec succès.");
        navigateToStudentsList();
    }

    // ===============================================================================
    // VALIDATION   du cintenu de chaque formulaire
    // ===============================================================================

    private boolean validateFields() {
        if (fullNameField.getText() == null || fullNameField.getText().isBlank()) {
            feedbackLabel.setText("Le nom complet est obligatoire.");
            return false;
        }
        if (emailField.getText() == null || emailField.getText().isBlank()) {
            feedbackLabel.setText("L'email est obligatoire.");
            return false;
        }
        if (!emailField.getText().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            feedbackLabel.setText("L'adresse email n'est pas valide.");
            return false;
        }
        if (matriculeField.getText() == null || matriculeField.getText().isBlank()) {
            feedbackLabel.setText("Le matricule est obligatoire.");
            return false;
        }
        if (classNameCombo == null || classNameCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner une classe.");
            return false;
        }
        if (yearLevelCombo == null || yearLevelCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner un niveau.");
            return false;
        }
        if (promotionCombo == null || promotionCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner une promotion.");
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToStudentsList() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/StudentsView.fxml"));
            Scene scene = newStudentRoot.getScene();
            if (scene != null) {
                javafx.scene.Parent root = scene.getRoot();
                javafx.scene.Node main = root.lookup("#mainContentArea");
                if (main instanceof javafx.scene.layout.Pane mainPane) {
                    mainPane.getChildren().setAll(view);
                    return;
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de revenir à la liste des étudiants.", exception);
        }
    }
}