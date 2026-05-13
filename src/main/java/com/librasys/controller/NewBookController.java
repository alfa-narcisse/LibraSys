package com.librasys.controller;

import com.librasys.dao.bookdao;
import com.librasys.dao.shelfdao;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class NewBookController {

    @FXML private VBox newBookRoot;

    // ── Fields from your FXML ──
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField editionField;
    @FXML private TextField yearEditionField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> rayonCombo;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private Label feedbackLabel;

    private final bookdao  bookDAO  = new bookdao();
    private final shelfdao shelfDAO = new shelfdao();

    @FXML
    private void initialize() {
        // Categories are fixed
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Informatique", "Mathématiques", "Physique",
                "Littérature", "Économie"
        ));

        // Rayons come from the database
        List<String> shelfNames = shelfDAO.getAllShelfNames();
        if (shelfNames.isEmpty()) {
            feedbackLabel.setText("Aucun rayon trouvé. Veuillez créer un rayon d'abord.");
        } else {
            rayonCombo.setItems(FXCollections.observableArrayList(shelfNames));
        }

        stockSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 1)
        );


    }

    @FXML
    private void onBackToList() {
        navigateToBooksList();
    }

    @FXML
    private void onCancel() {
        navigateToBooksList();
    }

    @FXML
    private void onSaveBook() {
        // Step 1 — Validate all fields
        if (!validateFields()) return;

        String title       = titleField.getText().trim();
        String author      = authorField.getText().trim();
        String isbn        = isbnField.getText().trim();
        String edition     = editionField.getText().trim();
        String category    = categoryCombo.getValue();
        String rayonName   = rayonCombo.getValue();
        String description = descriptionField.getText().trim();
        int stock          = stockSpinner.getValue();
        int yearEdition ;

        // Step 2 — Parse year
        try {
            yearEdition = Integer.parseInt(yearEditionField.getText().trim());
        } catch (NumberFormatException e) {
            feedbackLabel.setText("L'année d'édition doit être un nombre valide.");
            return;
        }

        // Step 3 — Get shelf id from shelf name
        int idShelf = shelfDAO.getShelfIdByName(rayonName);
        if (idShelf == -1) {
            feedbackLabel.setText("Rayon introuvable. Veuillez réessayer.");
            return;
        }

        // Step 4 — Check if ISBN already exists
        if (bookDAO.isbnExists(isbn)) {
            feedbackLabel.setText("Un livre avec cet ISBN existe déjà.");
            return;
        }

        // Step 5 — Save book to database
        boolean bookSaved = bookDAO.addBook(
                title, author, isbn, edition,
                category, yearEdition, description, idShelf
        );

        if (!bookSaved) {
            feedbackLabel.setText("Erreur lors de l'enregistrement du livre.");
            return;
        }

        // Step 6 — Add exemplaires (physical copies) based on stock
        boolean exemplairesSaved = bookDAO.addExemplaires(isbn, stock);
        if (!exemplairesSaved) {
            feedbackLabel.setText("Livre enregistré mais erreur lors de l'ajout des exemplaires.");
            return;
        }

        // Step 7 — Success
        showSuccess("Livre \"" + title + "\" ajouté avec " + stock + " exemplaire(s).");
        navigateToBooksList();
    }

    // =====================
    // VALIDATION
    // =====================
    private boolean validateFields() {
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            feedbackLabel.setText("Le titre est obligatoire.");
            return false;
        }
        if (authorField.getText() == null || authorField.getText().isBlank()) {
            feedbackLabel.setText("L'auteur est obligatoire.");
            return false;
        }
        if (isbnField.getText() == null || isbnField.getText().isBlank()) {
            feedbackLabel.setText("L'ISBN est obligatoire.");
            return false;
        }
        if (categoryCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner une catégorie.");
            return false;
        }
        if (rayonCombo.getValue() == null) {
            feedbackLabel.setText("Veuillez sélectionner un rayon.");
            return false;
        }
        if (yearEditionField.getText() == null || yearEditionField.getText().isBlank()) {
            feedbackLabel.setText("L'année d'édition est obligatoire.");
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

    private void navigateToBooksList() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/BooksView.fxml"));
            if (newBookRoot.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().setAll(view);
                return;
            }
            throw new IllegalStateException("Le conteneur principal est introuvable.");
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de revenir à la liste des livres.", exception);
        }
    }
}