package com.librasys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private ToggleGroup roleToggleGroup;

    @FXML
    private ToggleButton librarianToggle;

    @FXML
    private ToggleButton adminToggle;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label feedbackLabel;

    @FXML
    private ImageView logoImageView;

    @FXML
    private void initialize() {
        librarianToggle.setSelected(true);
        feedbackLabel.setText("");

        URL logoUrl = getClass().getResource("/com/librasys/logo_ept.png");
        if (logoUrl != null) {
            logoImageView.setImage(new Image(logoUrl.toExternalForm()));
        }
    }

    @FXML
    private void onLoginClick() {
        Toggle selectedToggle = roleToggleGroup.getSelectedToggle();
        String role = selectedToggle == adminToggle ? "Administrateur" : "Bibliothecaire";
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (selectedToggle == null || username.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Veuillez renseigner le role, l'identifiant et le mot de passe.");
            return;
        }

        // Simulation: fictitious user 'librarian' with password 'password'
        if ("librarian".equals(username) && "password".equals(password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/librasys/DashboardView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1320, 820);
                // apply dashboard styles
                scene.getStylesheets().add(com.librasys.MainApplication.class.getResource("/com/librasys/dashboard.css").toExternalForm());
                scene.getStylesheets().add(com.librasys.MainApplication.class.getResource("/com/librasys/students.css").toExternalForm());
                scene.getStylesheets().add(com.librasys.MainApplication.class.getResource("/com/librasys/books.css").toExternalForm());
                scene.getStylesheets().add(com.librasys.MainApplication.class.getResource("/com/librasys/shelves.css").toExternalForm());
                scene.getStylesheets().add(com.librasys.MainApplication.class.getResource("/com/librasys/loans.css").toExternalForm());

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("LibraSys - Dashboard");
            } catch (IOException exception) {
                feedbackLabel.setText("Erreur lors du chargement du tableau de bord.");
            }
        } else {
            feedbackLabel.setText("Identifiants invalides. Utilisez 'librarian' / 'password' pour la demonstration.");
        }
    }
}
