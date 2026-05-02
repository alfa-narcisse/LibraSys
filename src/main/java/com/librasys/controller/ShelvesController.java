package com.librasys.controller;

import com.librasys.dao.shelfdao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Comparator;

public class  ShelvesController {
    @FXML
    private VBox shelvesRoot;
    @FXML
    private FlowPane shelvesFlowPane;

    private final ObservableList<Shelf> shelves = FXCollections.observableArrayList();
    private final shelfdao shelfDAO = new shelfdao();

    @FXML
    private void initialize() {
        loadFromDatabase();
        renderShelves();
    }

    @FXML
    private void onBackToLibrary() {
        loadIntoMainContent("/com/librasys/BooksView.fxml", "Impossible de charger la bibliothèque.");
    }

    @FXML
    private void showAddShelfDialog() {
        Dialog<ButtonTypeWrapper> dialog = new Dialog<>();
        dialog.setTitle("Configuration du Rayon");
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/librasys/shelves.css").toExternalForm());
        pane.getStyleClass().add("add-shelf-dialog");

        VBox content = new VBox(10);
        content.setPadding(new Insets(14));

        Label title = new Label("Configuration du Rayon");
        title.getStyleClass().add("dialog-main-title");

        TextField shelfNameField = new TextField();
        shelfNameField.setPromptText("Nom du Rayon");
        shelfNameField.getStyleClass().add("dialog-input");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Informatique", "Mathématiques", "Physique", "Littérature", "Droit", "Économie"
        ));
        categoryCombo.setPromptText("Catégorie associée");
        categoryCombo.getStyleClass().add("dialog-input");
        categoryCombo.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> capacitySpinner = new Spinner<>();
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 200, 10));
        capacitySpinner.getStyleClass().add("dialog-input");
        capacitySpinner.setEditable(true);
        capacitySpinner.setMaxWidth(Double.MAX_VALUE);

        TextField locationField = new TextField();
        locationField.setPromptText("Emplacement physique (Allée/Étage)");
        locationField.getStyleClass().add("dialog-input");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("dialog-cancel-btn");
        Button createButton = new Button("Créer le rayon");
        createButton.getStyleClass().add("dialog-create-btn");
        actions.getChildren().addAll(cancelButton, createButton);

        content.getChildren().addAll(
                title,
                labelField("Nom du Rayon", shelfNameField),
                labelField("Catégorie associée", categoryCombo),
                labelField("Capacité maximale", capacitySpinner),
                labelField("Emplacement Physique", locationField),
                actions
        );

        pane.setContent(content);
        pane.getButtonTypes().clear();

        cancelButton.setOnAction(event -> dialog.setResult(ButtonTypeWrapper.CANCEL));
        createButton.setOnAction(event -> {
            String name     = shelfNameField.getText() == null ? "" : shelfNameField.getText().trim();
            String category = categoryCombo.getValue() == null ? "Général" : categoryCombo.getValue();
            String location = locationField.getText() == null ? "" : locationField.getText().trim();
            int capacity    = capacitySpinner.getValue();

            if (name.isBlank() || location.isBlank()) {
                return;
            }

            // Check for duplicates before inserting
            String fullName = name + " - " + category;
            if (shelfDAO.shelfExists(fullName)) {
                // show error — shelf already exists
                return;
            }

            boolean success = shelfDAO.addShelf(fullName, "#3498db", location);
            if (success) {
                shelves.add(new Shelf(fullName, 0, capacity, 0, location));
                shelves.sort(Comparator.comparing(Shelf::name));
                renderShelves();
                dialog.setResult(ButtonTypeWrapper.CREATE);
            }
        });

        dialog.showAndWait();
    }

    private VBox labelField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("dialog-label");
        VBox wrapper = new VBox(4, label, field);
        VBox.setVgrow(field, Priority.NEVER);
        return wrapper;
    }

    private void renderShelves() {
        shelvesFlowPane.getChildren().clear();
        for (Shelf shelf : shelves) {
            shelvesFlowPane.getChildren().add(buildShelfCard(shelf));
        }
    }

    private VBox buildShelfCard(Shelf shelf) {
        VBox card = new VBox(10);
        card.getStyleClass().add("shelf-card");
        card.setPrefWidth(320);

        Label title = new Label(shelf.name());
        title.getStyleClass().add("shelf-card-title");

        ProgressBar occupancyBar = new ProgressBar((double) shelf.booksCount() / shelf.maxCapacity());
        occupancyBar.getStyleClass().add("shelf-progress");
        occupancyBar.setMaxWidth(Double.MAX_VALUE);

        Label stats = new Label(shelf.booksCount() + " / " + shelf.maxCapacity() + " Livres");
        stats.getStyleClass().add("shelf-stat");
        Label shelvesCount = new Label(shelf.shelvesCount() + " étagères");
        shelvesCount.getStyleClass().add("shelf-stat");
        Label location = new Label("Localisation : " + shelf.location());
        location.getStyleClass().add("shelf-stat");

        Button viewBooksBtn = new Button("Voir les livres");
        viewBooksBtn.getStyleClass().add("shelf-view-btn");

        card.getChildren().addAll(title, occupancyBar, stats, shelvesCount, location, viewBooksBtn);
        return card;
    }

    private void loadIntoMainContent(String fxmlPath, String errorMessage) {
        try {
            Pane view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (shelvesRoot.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().setAll(view);
                return;
            }
            throw new IllegalStateException("Le conteneur principal est introuvable.");
        } catch (IOException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    private void loadFromDatabase() {
        shelves.addAll(shelfDAO.getAllShelves());
        shelves.sort(Comparator.comparing(Shelf::name));
    }

    private enum ButtonTypeWrapper {
        CANCEL, CREATE
    }

    public record Shelf(String name, int booksCount, int maxCapacity, int shelvesCount, String location) {
    }
}
