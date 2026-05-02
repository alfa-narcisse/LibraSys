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

    private final logindao loginDAO = new logindao();
    private final bookdao bookDAO = new bookdao();
    private final loandao loanDAO = new loandao();
    private final studentdao studentDAO = new studentdao();


    @FXML
    private void initialize() {
        librarianToggle.setSelected(true);
        feedbackLabel.setText("");

        URL logoUrl = getClass().getResource("/com/librasys/logo_ept.png");
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

        // Verification du mot de passe
        String dbPassword = loginDAO.getpassword(username);
        System.out.println("Username entered: " + username);
        System.out.println("Password entered: " + password);
        System.out.println("Password from DB: " + dbPassword);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/librasys/DashboardView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1320, 820);
            // apply dashboard styles
            scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/dashboard.css").toExternalForm());
            scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/students.css").toExternalForm());
            scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/books.css").toExternalForm());
            scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/shelves.css").toExternalForm());
            scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/loans.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LibraSys - Dashboard");
        } catch (IOException exception) {
            feedbackLabel.setText("Erreur lors du chargement du tableau de bord.");
        }


    }
    private void loadLibraryInfo() {
        int totalBooks = bookDAO.countBooks();
        if (totalBooksLabel != null) totalBooksLabel.setText(String.valueOf(totalBooks));

        int totalStudents = studentDAO.countStudents();
        if (totalStudentsLabel != null) totalStudentsLabel.setText(String.valueOf(totalStudents));
        int[] stats = loanDAO.getSummaryStats();
        int activeLoans = stats[0];
        int criticalDelays = stats[4];
        if (activeLoansLabel != null) activeLoansLabel.setText(String.valueOf(activeLoans));
        if (criticalDelaysLabel != null) criticalDelaysLabel.setText(String.valueOf(criticalDelays));
    }
}
