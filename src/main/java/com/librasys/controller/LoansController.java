package com.librasys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoansController {
    @FXML
    private VBox mainContainer;

    @FXML
    private Button tabPretBtn;

    @FXML
    private Button tabRetourBtn;

    @FXML
    private Button tabHistoriqueBtn;

    @FXML
    private StackPane contentContainer;

    @FXML
    private VBox pretContainer;

    @FXML
    private VBox retourContainer;

    @FXML
    private VBox historiqueContainer;

    // Tab Prêt Components
    @FXML
    private TextField studentMatriculeField;

    @FXML
    private TextField bookIsbnField;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentQuotaLabel;

    @FXML
    private Label returnDateLabel;

    @FXML
    private Button confirmLoanBtn;

    @FXML
    private Label bookTitleLabel;

    @FXML
    private Label bookStateLabel;

    // Tab Retour Components
    @FXML
    private TextField scanBookField;

    @FXML
    private CheckBox damagedCheckBox;

    @FXML
    private Label penaltyLabel;

    @FXML
    private Button validateReturnBtn;

    @FXML
    private Label returnStudentLabel;

    // Tab Historique Components
    @FXML
    private TableView<LoanHistoryRow> historiqueTable;

    @FXML
    private TableColumn<LoanHistoryRow, String> studentColHistorique;

    @FXML
    private TableColumn<LoanHistoryRow, String> bookColHistorique;

    @FXML
    private TableColumn<LoanHistoryRow, String> loanDateCol;

    @FXML
    private TableColumn<LoanHistoryRow, String> returnDateCol;

    @FXML
    private TableColumn<LoanHistoryRow, String> statusColHistorique;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    private List<LoanHistoryRow> allLoans = new ArrayList<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        loadStyles();
        initializeTabButtons();
        initializeTabPret();
        initializeTabRetour();
        initializeTabHistorique();
        showTabPret();
        loadSampleData();
    }

    private void loadStyles() {
        // Styles will be loaded by MainApplication
    }

    private void initializeTabButtons() {
        tabPretBtn.setOnAction(e -> showTabPret());
        tabRetourBtn.setOnAction(e -> showTabRetour());
        tabHistoriqueBtn.setOnAction(e -> showTabHistorique());
    }

    private void initializeTabPret() {
        studentMatriculeField.setPromptText("Scannez le matricule étudiant");
        bookIsbnField.setPromptText("Scannez l'ISBN ou le code-barres du livre");

        studentMatriculeField.setOnAction(e -> {
            String matricule = studentMatriculeField.getText().trim();
            if (!matricule.isEmpty()) {
                loadStudentInfo(matricule);
            }
        });

        bookIsbnField.setOnAction(e -> {
            String isbn = bookIsbnField.getText().trim();
            if (!isbn.isEmpty()) {
                loadBookInfo(isbn);
            }
        });

        confirmLoanBtn.setOnAction(e -> confirmLoan());

        // Initialize return date calculation
        studentMatriculeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !bookIsbnField.getText().isEmpty()) {
                calculateReturnDate();
            }
        });
    }

    private void initializeTabRetour() {
        scanBookField.setPromptText("Scannez le livre à retourner");
        damagedCheckBox.setOnAction(e -> calculatePenalty());
        scanBookField.setOnAction(e -> {
            String isbn = scanBookField.getText().trim();
            if (!isbn.isEmpty()) {
                loadReturnInfo(isbn);
            }
        });

        validateReturnBtn.setOnAction(e -> validateReturn());
    }

    private void initializeTabHistorique() {
        studentColHistorique.setCellValueFactory(new PropertyValueFactory<>("student"));
        bookColHistorique.setCellValueFactory(new PropertyValueFactory<>("book"));
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColHistorique.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColHistorique.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }

                Label chip = new Label(status);
                chip.getStyleClass().add("status-chip");
                if ("En retard".equals(status)) {
                    chip.getStyleClass().add("status-retard");
                } else if ("Rendu".equals(status)) {
                    chip.getStyleClass().add("status-rendu");
                } else {
                    chip.getStyleClass().add("status-en-cours");
                }

                StackPane wrapper = new StackPane(chip);
                wrapper.setMaxWidth(Double.MAX_VALUE);
                setGraphic(wrapper);
            }
        });

        filterComboBox.getItems().addAll("Tous", "Rendu", "En retard", "En cours");
        filterComboBox.setValue("Tous");
        filterComboBox.setOnAction(e -> filterHistorique());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterHistorique());
    }

    private void showTabPret() {
        tabPretBtn.getStyleClass().remove("tab-btn-inactive");
        tabRetourBtn.getStyleClass().add("tab-btn-inactive");
        tabHistoriqueBtn.getStyleClass().add("tab-btn-inactive");
        contentContainer.getChildren().setAll(pretContainer);
    }

    private void showTabRetour() {
        tabPretBtn.getStyleClass().add("tab-btn-inactive");
        tabRetourBtn.getStyleClass().remove("tab-btn-inactive");
        tabHistoriqueBtn.getStyleClass().add("tab-btn-inactive");
        contentContainer.getChildren().setAll(retourContainer);
    }

    private void showTabHistorique() {
        tabPretBtn.getStyleClass().add("tab-btn-inactive");
        tabRetourBtn.getStyleClass().add("tab-btn-inactive");
        tabHistoriqueBtn.getStyleClass().remove("tab-btn-inactive");
        contentContainer.getChildren().setAll(historiqueContainer);
    }

    private void loadStudentInfo(String matricule) {
        // Simulation - dans une vraie app, chercher dans une base de données
        if (matricule.matches("^[0-9]{6}$")) {
            studentNameLabel.setText("Ahmed Ben Ali");
            studentQuotaLabel.setText("Quota: 3/5 livres");
            studentQuotaLabel.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            showAlert("Erreur", "Matricule invalide. Utilisez 6 chiffres.");
            studentNameLabel.setText("Non trouvé");
            studentQuotaLabel.setText("Quota: 0/5");
        }
    }

    private void loadBookInfo(String isbn) {
        // Simulation - dans une vraie app, chercher dans une base de données
        if (isbn.length() >= 5) {
            bookTitleLabel.setText("Algorithmique Avancée");
            bookStateLabel.setText("État: Excellent");
            bookStateLabel.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            showAlert("Erreur", "ISBN invalide.");
            bookTitleLabel.setText("Non trouvé");
            bookStateLabel.setText("État: Inconnu");
        }
    }

    private void calculateReturnDate() {
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = loanDate.plusDays(15);
        returnDateLabel.setText("Date de retour: " + dateFormatter.format(returnDate));
        returnDateLabel.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold;");
    }

    private void confirmLoan() {
        String matricule = studentMatriculeField.getText().trim();
        String isbn = bookIsbnField.getText().trim();

        if (matricule.isEmpty() || isbn.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        showAlert("Succès", "Prêt confirmé pour Ahmed Ben Ali - Algorithmique Avancée");
        studentMatriculeField.clear();
        bookIsbnField.clear();
        studentNameLabel.setText("");
        studentQuotaLabel.setText("");
        bookTitleLabel.setText("");
        bookStateLabel.setText("");
        returnDateLabel.setText("");
    }

    private void loadReturnInfo(String isbn) {
        // Simulation
        if (isbn.length() >= 5) {
            returnStudentLabel.setText("Livre: Algorithmique Avancée - Emprunteur: Ahmed Ben Ali");
            damagedCheckBox.setSelected(false);
            penaltyLabel.setText("Pénalité: 0 DA");
            penaltyLabel.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            showAlert("Erreur", "Livre non trouvé.");
        }
    }

    private void calculatePenalty() {
        if (damagedCheckBox.isSelected()) {
            penaltyLabel.setText("Pénalité: 500 DA (dommage) + retard");
            penaltyLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            penaltyLabel.setText("Pénalité: 0 DA");
            penaltyLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    private void validateReturn() {
        String scanText = scanBookField.getText().trim();
        if (scanText.isEmpty()) {
            showAlert("Erreur", "Veuillez scanner un livre.");
            return;
        }

        showAlert("Succès", "Retour validé pour Algorithmique Avancée");
        scanBookField.clear();
        returnStudentLabel.setText("");
        damagedCheckBox.setSelected(false);
        penaltyLabel.setText("");
    }

    private void filterHistorique() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = filterComboBox.getValue();

        historiqueTable.getItems().clear();
        historiqueTable.getItems().addAll(
            allLoans.stream()
                .filter(loan -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        loan.getStudent().toLowerCase().contains(searchText) ||
                        loan.getBook().toLowerCase().contains(searchText);
                    boolean matchesStatus = "Tous".equals(statusFilter) ||
                        loan.getStatus().equals(statusFilter);
                    return matchesSearch && matchesStatus;
                })
                .toList()
        );
    }

    private void loadSampleData() {
/*
*         allLoans.addAll(
            new LoanHistoryRow("Ahmed Ben Ali", "Algorithmique Avancée", "15/04/2026", "30/04/2026", "En cours"),
            new LoanHistoryRow("Mariam Trabelsi", "Physique Quantique", "10/04/2026", "25/04/2026", "En retard"),
            new LoanHistoryRow("Rached Gharbi", "Droit Civil", "08/04/2026", "23/04/2026", "Rendu"),
            new LoanHistoryRow("Samira Kooli", "Chimie Organique", "12/04/2026", "27/04/2026", "En cours"),
            new LoanHistoryRow("Layla Mansouri", "Base de Données", "20/04/2026", "05/05/2026", "En cours"),
            new LoanHistoryRow("Karim Hadj", "Systèmes Distribués", "05/04/2026", "20/04/2026", "Rendu"),
            new LoanHistoryRow("Fatima Zahra", "Machine Learning", "18/04/2026", "03/05/2026", "En cours"),
            new LoanHistoryRow("Ali Amri", "Génie Logiciel", "07/04/2026", "22/04/2026", "En retard")
        );
*
* */

        historiqueTable.getItems().addAll(allLoans);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for Loan History
    public static class LoanHistoryRow {
        private final StringProperty student;
        private final StringProperty book;
        private final StringProperty loanDate;
        private final StringProperty returnDate;
        private final StringProperty status;

        public LoanHistoryRow(String student, String book, String loanDate, String returnDate, String status) {
            this.student = new SimpleStringProperty(student);
            this.book = new SimpleStringProperty(book);
            this.loanDate = new SimpleStringProperty(loanDate);
            this.returnDate = new SimpleStringProperty(returnDate);
            this.status = new SimpleStringProperty(status);
        }

        public String getStudent() { return student.get(); }
        public StringProperty studentProperty() { return student; }

        public String getBook() { return book.get(); }
        public StringProperty bookProperty() { return book; }

        public String getLoanDate() { return loanDate.get(); }
        public StringProperty loanDateProperty() { return loanDate; }

        public String getReturnDate() { return returnDate.get(); }
        public StringProperty returnDateProperty() { return returnDate; }

        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
    }
}
