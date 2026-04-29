package com.librasys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class NewBookController {
    @FXML
    private VBox newBookRoot;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private ComboBox<String> rayonCombo;
    @FXML
    private Spinner<Integer> stockSpinner;

    @FXML
    private void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Informatique", "Mathématiques", "Physique", "Littérature", "Économie", "Droit"
        ));
        rayonCombo.setItems(FXCollections.observableArrayList(
                "Rayon A - Info", "Rayon B - Math", "Rayon C - Physique", "Rayon D - Littérature"
        ));
        stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 1));
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
        navigateToBooksList();
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
