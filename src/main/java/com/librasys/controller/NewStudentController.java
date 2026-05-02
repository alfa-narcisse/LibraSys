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
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class NewStudentController {

    @FXML private VBox newStudentRoot;

    // ── Form fields ──
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> classNameCombo;
    @FXML private ComboBox<String> yearLevelCombo;
    @FXML private ComboBox<String> promotionCombo;
    @FXML private Label feedbackLabel;

    private final studentdao studentDAO = new studentdao();

    @FXML
    private void initialize() {
        feedbackLabel.setText("");

        classNameCombo.setItems(FXCollections.observableArrayList(
                "GINF-A", "GINF-B",
                "GMATH-A", "GMATH-B",
                "GCHIM-A", "GCHIM-B",
                "GMEC-A",  "GMEC-B",  "GMEC-C"
        ));

        yearLevelCombo.setItems(FXCollections.observableArrayList(
                "1ère", "2ème", "3ème", "4ème", "5ème"
        ));

        // Promotions: current year + next 4 years
        int currentYear = LocalDate.now().getYear();
        promotionCombo.setItems(FXCollections.observableArrayList(
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1),
                String.valueOf(currentYear + 2),
                String.valueOf(currentYear + 3),
                String.valueOf(currentYear + 4)
        ));
    }


    @FXML
    private void onBackToList() {
        navigateToStudentsList();
    }

    @FXML
    private void onCancel() {
        navigateToStudentsList();
    }

    @FXML
    private void onSaveStudent() {

        // 1ere etape : validation des champs de remplissage
        if (!validateFields()) return;

        String fullName  = fullNameField.getText().trim();
        String email     = emailField.getText().trim();
        String matricule = matriculeField.getText().trim();
        String className = classNameCombo.getValue();
        String yearLevel = yearLevelCombo.getValue();
        String promotion = promotionCombo.getValue();
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

    // =====================
    // VALIDATION
    // =====================
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
        if (classNameCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner une classe.");
            return false;
        }
        if (yearLevelCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner un niveau.");
            return false;
        }
        if (promotionCombo.getValue() == null) {
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
            if (newStudentRoot.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().setAll(view);
                return;
            }
            throw new IllegalStateException("Le conteneur principal est introuvable.");
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de revenir à la liste des étudiants.", exception);
        }
    }
}