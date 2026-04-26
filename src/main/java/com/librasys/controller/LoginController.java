package com.librasys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class LoginController {
    @FXML
    private ComboBox<String> roleSelector;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private ImageView logoImageView;

    @FXML
    private void initialize() {
        roleSelector.getItems().addAll("Bibliothecaire", "Administrateur");
        roleSelector.getSelectionModel().selectFirst();
        feedbackLabel.setText("");

        URL logoUrl = getClass().getResource("/com/librasys/logo_ept.png");
        if (logoUrl != null) {
            logoImageView.setImage(new Image(logoUrl.toExternalForm()));
        }
    }

    @FXML
    private void onLoginClick() {
        String role = roleSelector.getValue();
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (role == null || username.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Veuillez renseigner le role, l'identifiant et le mot de passe.");
            return;
        }

        feedbackLabel.setText("Connexion en cours pour " + role + "...");
    }
}
