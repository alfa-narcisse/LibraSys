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

        String remember = rememberMeCheckBox.isSelected() ? " (session memorisee)" : "";
        feedbackLabel.setText("Connexion en cours pour " + role + remember + "...");
    }
}
