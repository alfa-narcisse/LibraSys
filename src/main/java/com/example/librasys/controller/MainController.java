package com.example.librasys.controller;

import com.example.librasys.model.Book;
import com.example.librasys.service.LibraryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {
    private final LibraryService libraryService = new LibraryService();

    @FXML
    private Label totalTitlesLabel;

    @FXML
    private Label totalCopiesLabel;

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableColumn<Book, String> isbnColumn;

    @FXML
    private TableColumn<Book, String> categoryColumn;

    @FXML
    private TableColumn<Book, Integer> copiesColumn;

    @FXML
    private TextField titleField;

    @FXML
    private TextField authorField;

    @FXML
    private TextField isbnField;

    @FXML
    private ComboBox<String> categoryBox;

    @FXML
    private TextField copiesField;

    @FXML
    private void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(
                "Informatique", "Architecture", "Science", "Litterature", "Histoire"
        ));

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        copiesColumn.setCellValueFactory(new PropertyValueFactory<>("copies"));

        booksTable.setItems(libraryService.getBooks());
        refreshStats();
    }

    @FXML
    private void onAddBook() {
        if (!isFormValid()) {
            showWarning("Tous les champs sont requis et 'Exemplaires' doit etre un nombre positif.");
            return;
        }

        int copies = Integer.parseInt(copiesField.getText().trim());
        Book book = new Book(
                titleField.getText().trim(),
                authorField.getText().trim(),
                isbnField.getText().trim(),
                categoryBox.getValue(),
                copies
        );
        libraryService.addBook(book);
        clearForm();
        refreshStats();
    }

    @FXML
    private void onRemoveSelectedBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showWarning("Selectionnez un livre a supprimer.");
            return;
        }

        libraryService.removeBook(selectedBook);
        refreshStats();
    }

    private boolean isFormValid() {
        if (titleField.getText().isBlank() || authorField.getText().isBlank() || isbnField.getText().isBlank()) {
            return false;
        }
        if (categoryBox.getValue() == null || categoryBox.getValue().isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(copiesField.getText().trim()) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void refreshStats() {
        totalTitlesLabel.setText(String.valueOf(libraryService.getTotalBooks()));
        totalCopiesLabel.setText(String.valueOf(libraryService.getTotalCopies()));
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        categoryBox.getSelectionModel().clearSelection();
        copiesField.clear();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText("Action impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
