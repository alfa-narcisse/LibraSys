package com.librasys.controller;

import com.librasys.dao.bookdao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ShelfBooksController {
    @FXML
    public VBox shelfBookRoot;
    @FXML
    private Label headerLabel;
    @FXML
    private javafx.scene.control.Button backButton;
    @FXML
    private TableView<BooksController.Book> shelfBooksTable;
    @FXML
    private TableColumn<BooksController.Book, String> coverColumn;
    @FXML
    private TableColumn<BooksController.Book, String> titleAuthorColumn;
    @FXML
    private TableColumn<BooksController.Book, String> categoryColumn;
    @FXML
    private TableColumn<BooksController.Book, String> locationColumn;
    @FXML
    private TableColumn<BooksController.Book, String> availabilityColumn;

   private final bookdao bookDAO = new bookdao();

   @FXML
    private void initialize() {
        // configure simple cell factories
        titleAuthorColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        locationColumn.setCellValueFactory(data -> data.getValue().locationProperty());
        availabilityColumn.setCellValueFactory(data -> data.getValue().availabilityProperty());
    }

    private String currentRayonName;

    public void setRayonName(String name) {
        this.currentRayonName = name;
        headerLabel.setText("Livres du Rayon — " + name);
        List<BooksController.Book> rows = bookDAO.getBooksByRayon(name);
        ObservableList<BooksController.Book> list = FXCollections.observableArrayList(rows);
        shelfBooksTable.setItems(list);
        if (!list.isEmpty()) shelfBooksTable.getSelectionModel().selectFirst();

        // decorate back button when the view is initialized via setRayonName
        if (backButton != null) {
            backButton.setTooltip(new javafx.scene.control.Tooltip("Retour à la gestion des rayons"));
            backButton.getStyleClass().add("back-link-btn");
            backButton.setOnMouseEntered(evt -> backButton.setStyle("-fx-underline: true; -fx-text-fill: #283593;"));
            backButton.setOnMouseExited(evt -> backButton.setStyle(""));
        }
    }

    @FXML
    private void onBack() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/ShelvesView.fxml"));
            // Prefer replacing mainContentArea to preserve sidebar
            javafx.scene.Scene scene = shelfBookRoot.getScene();
            if (scene != null) {
                javafx.scene.Parent root = scene.getRoot();
                javafx.scene.Node main = root.lookup("#mainContentArea");
                if (main instanceof javafx.scene.layout.Pane mainPane) {
                    mainPane.getChildren().setAll(view);
                    return;
                }
            }
            if (shelfBookRoot.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().setAll(view);
                return;
            }
            throw new IllegalStateException("Le conteneur principal est introuvable.");
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de revenir à la liste des étudiants.", exception);
        }
    }
}