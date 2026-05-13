package com.librasys.controller;

import com.librasys.MainApplication;
import com.librasys.dao.bookdao;
import com.librasys.dao.loandao;
import com.librasys.dao.studentdao;
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
import com.librasys.dao.logindao;
import com.librasys.util.SessionManager;




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
    private Label feedbackLabel;

    @FXML
    private ImageView logoImageView;
    @FXML
    private Label totalBooksLabel;
    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label criticalDelaysLabel;
    @FXML
    private Label activeLoansLabel;

    // Création des instances d'intéraction avec la base de données
    private final logindao loginDAO = new logindao();
    private final bookdao bookDAO = new bookdao();
    private final loandao loanDAO = new loandao();
    private final studentdao studentDAO = new studentdao();


    @FXML
    private void initialize() {
        librarianToggle.setSelected(true);
        feedbackLabel.setText("");

        URL logoUrl = getClass().getResource("/com/librasys/Image/logo_ept.png");
        if (logoUrl != null) {
            logoImageView.setImage(new Image(logoUrl.toExternalForm()));
        }
        loadLibraryInfo();
    }

    @FXML
    private void onLoginClick() {
        Toggle selectedToggle = roleToggleGroup.getSelectedToggle();
        String  role = selectedToggle == adminToggle ? "Administrateur" : "Bibliothecaire";
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (selectedToggle == null) {
            feedbackLabel.setText("Veuillez sélectionner un rôle.");
            return;
        }
        if (username.isEmpty()) {
            feedbackLabel.setText("Veuillez entrer votre identifiant.");
            return;
        }
        if (password.isEmpty()) {
            feedbackLabel.setText("Veuillez entrer votre mot de passe.");
            return;
        }
        // Verification du username
        String dbUsername = loginDAO.getusername(username);
        if (dbUsername == null) {
            feedbackLabel.setText("Cet identifiant n'existe pas.");
            return;
        }

        // Verification du mot de passe: en fonction du username saisie - on compare avec le mdp enregistré
        // dans la base de données
        String dbPassword = loginDAO.getpassword(username);


        if (!password.equals(dbPassword)) {
            feedbackLabel.setText("Mot de passe incorrect.");
            return;
        }
        // Verification du role selectionne
        String dbRole = loginDAO.getRole(username);
        String selectedRole = selectedToggle == adminToggle ? "admin" : "librarian";
        if (!dbRole.equals(selectedRole)) {
            feedbackLabel.setText("Ce compte n'a pas le rôle sélectionné.");
            return;
        }
        int userId = loginDAO.getUserId(username);
        SessionManager.login(userId, dbUsername, dbRole);

        try {
            // Recherche du fichier fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/librasys/DashboardView.fxml"));
            Parent root = loader.load();

            // prepare css urls
            java.net.URL dashCss = MainApplication.class.getResource("/com/librasys/styleSheet/dashboard.css");
            java.net.URL studentsCss = MainApplication.class.getResource("/com/librasys/styleSheet/students.css");
            java.net.URL booksCss = MainApplication.class.getResource("/com/librasys/styleSheet/books.css");
            java.net.URL shelvesCss = MainApplication.class.getResource("/com/librasys/styleSheet/shelves.css");
            java.net.URL loansCss = MainApplication.class.getResource("/com/librasys/styleSheet/loans.css");

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene != null) {
                // reuse existing scene to preserve window size
                scene.setRoot(root);
                scene.getStylesheets().clear();
                if (dashCss != null) scene.getStylesheets().add(dashCss.toExternalForm());
                if (studentsCss != null) scene.getStylesheets().add(studentsCss.toExternalForm());
                if (booksCss != null) scene.getStylesheets().add(booksCss.toExternalForm());
                if (shelvesCss != null) scene.getStylesheets().add(shelvesCss.toExternalForm());
                if (loansCss != null) scene.getStylesheets().add(loansCss.toExternalForm());
            } else {
                Scene newScene = new Scene(root, 1320, 820);
                if (dashCss != null) newScene.getStylesheets().add(dashCss.toExternalForm());
                if (studentsCss != null) newScene.getStylesheets().add(studentsCss.toExternalForm());
                if (booksCss != null) newScene.getStylesheets().add(booksCss.toExternalForm());
                if (shelvesCss != null) newScene.getStylesheets().add(shelvesCss.toExternalForm());
                if (loansCss != null) newScene.getStylesheets().add(loansCss.toExternalForm());
                stage.setScene(newScene);
            }

            stage.setTitle("LibraSys - Dashboard");
        } catch (IOException exception) {
            feedbackLabel.setText("Erreur lors du chargement du tableau de bord.");
        }


    }
    private void loadLibraryInfo() {
        /*
         * Cette méthode est utilisée pour la mise à jour des informations affichées dans la page d'accueil du log in
         * */

        // Le nombre total des livres disponibles dans la bibliothèque
        int totalBooks = bookDAO.countBooks();
        if (totalBooksLabel != null) totalBooksLabel.setText(String.valueOf(totalBooks));

        // Le nombre total des étudiants inscrits dans la base de données
        int totalStudents = studentDAO.countStudents();
        if (totalStudentsLabel != null) totalStudentsLabel.setText(String.valueOf(totalStudents));

        //Etats d'emprunt et de remise - Pour informer s'il y a retard ou non.
        int[] stats = loanDAO.getSummaryStats();
        int activeLoans = stats[0];
        int criticalDelays = stats[4];
        if (activeLoansLabel != null) activeLoansLabel.setText(String.valueOf(activeLoans));
        if (criticalDelaysLabel != null) criticalDelaysLabel.setText(String.valueOf(criticalDelays));
    }
}
