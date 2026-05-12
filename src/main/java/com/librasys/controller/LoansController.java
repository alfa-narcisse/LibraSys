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

import com.librasys.dao.loandao;
import com.librasys.util.SessionManager;

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

    // Summary States Components
    @FXML
    private Label activeLoansLabel;
    @FXML
    private Label newTodayLabel;
    @FXML
    private Label expectedReturnsLabel;
    @FXML
    private Label lateReturnsLabel;
    @FXML
    private Label criticalDelaysLabel;


    private List<LoanHistoryRow> allLoans = new ArrayList<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final loandao loanDAO = new loandao();
    @FXML
    private void initialize() {
        initializeTabButtons();
        initializeTabPret();
        initializeTabRetour();
        initializeTabHistorique();
        showTabPret();
        loadSampleData();
        loadSummary();
    }

    private void loadSummary() {
        int[] stats = loanDAO.getSummaryStats();

        int activeLoans    = stats[0];
        int newToday       = stats[1];
        int expectedToday  = stats[2];
        int lateReturns    = stats[3];
        int criticalDelays = stats[4];

        activeLoansLabel.setText(String.valueOf(activeLoans));
        newTodayLabel.setText("+" + newToday + " aujourd'hui");

        expectedReturnsLabel.setText(String.valueOf(expectedToday));
        lateReturnsLabel.setText(lateReturns + " en retard");

        criticalDelaysLabel.setText(String.valueOf(criticalDelays));

        // Change color if critical delays are high
        if (criticalDelays > 5) {
            criticalDelaysLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else if (criticalDelays > 0) {
            criticalDelaysLabel.setStyle("-fx-text-fill: #f39c12;");
        } else {
            criticalDelaysLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    private void initializeTabButtons() {
        tabPretBtn.setOnAction(e -> showTabPret());
        tabRetourBtn.setOnAction(e -> showTabRetour());
        tabHistoriqueBtn.setOnAction(e -> showTabHistorique());
    }

    private void initializeTabPret() {
        studentMatriculeField.focusedProperty().addListener((observable, wasfocused, isnowfocused) -> {
            if (!isnowfocused) {
                String matricule = studentMatriculeField.getText().trim();
                if (!matricule.isEmpty()) {
                    loadStudentInfo(matricule);
                }
            }
        });
        studentMatriculeField.setOnAction(e -> {
            String matricule = studentMatriculeField.getText().trim();
            if (!matricule.isEmpty()) {
                loadStudentInfo(matricule);
            }
        });

        bookIsbnField.focusedProperty().addListener((observable, wasfocused, isnowfocused) -> {
            if (!isnowfocused) {
                String codeBarre = bookIsbnField.getText().trim();
                if (!codeBarre.isEmpty()) {
                    loadBookInfo(codeBarre);

                }
            }
        });

        bookIsbnField.setOnAction(e -> {
            String isbn = bookIsbnField.getText().trim();
            if (!isbn.isEmpty()) {
                loadBookInfo(isbn);
            }
        });

        confirmLoanBtn.setOnAction(e -> confirmLoan());


        studentMatriculeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !bookIsbnField.getText().isEmpty()) {
                calculateReturnDate();
            }
        });
    }

    private void initializeTabRetour() {

        scanBookField.focusedProperty().addListener((observable, wasfocused, isnowfocused) -> {
            if (!isnowfocused) {
                String codeBarre = scanBookField.getText().trim();
                if (!codeBarre.isEmpty()) {
                    loadReturnInfo(codeBarre);
                }
            }
        });
        scanBookField.setOnAction(e -> {
            String isbn = scanBookField.getText().trim();
            if (!isbn.isEmpty()) {
                loadReturnInfo(isbn);
            }
        });
        damagedCheckBox.setOnAction(e -> calculatePenalty());

        validateReturnBtn.setOnAction(e -> validateReturn());
    }

    private void initializeTabHistorique() {
        studentColHistorique.setCellValueFactory(new PropertyValueFactory<>("student"));
        bookColHistorique.setCellValueFactory(new PropertyValueFactory<>("book"));
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColHistorique.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadSampleData();

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
        tabPretBtn.getStyleClass().add("tab-btn-active");
        tabRetourBtn.getStyleClass().remove("tab-btn-active");
        tabHistoriqueBtn.getStyleClass().remove("tab-btn-active");
        contentContainer.getChildren().setAll(pretContainer);
    }

    private void showTabRetour() {
        tabPretBtn.getStyleClass().remove("tab-btn-active");
        tabRetourBtn.getStyleClass().add("tab-btn-active");
        tabHistoriqueBtn.getStyleClass().remove("tab-btn-active");
        contentContainer.getChildren().setAll(retourContainer);
    }

    private void showTabHistorique() {
        tabPretBtn.getStyleClass().remove("tab-btn-active");
        tabRetourBtn.getStyleClass().remove("tab-btn-active");
        tabHistoriqueBtn.getStyleClass().add("tab-btn-active");
        contentContainer.getChildren().setAll(historiqueContainer);
    }

    private void loadStudentInfo(String matricule) {
        String info = loanDAO.getStudentInfo(matricule);
        if (info != null) {
            String[] parts = info.split("\\|");
            studentNameLabel.setText(parts[0]);
            studentQuotaLabel.setText("Prêts actifs: " + parts[1] + "/5");
            studentQuotaLabel.setStyle("-fx-text-fill: #2ecc71");
            calculateReturnDate();
        }
        else {
            showAlert("Erreur", "Matricule n'existe pas");
            studentNameLabel.setText("Erreur");
            studentQuotaLabel.setText("Quota : 0/5");
        }
    }

    private void loadBookInfo(String codeBarre) {
        String[] bookInfo = loanDAO.getBookInfoByCodeBarre(codeBarre);

        if (bookInfo == null) {
            showAlert("Erreur", "Aucun exemplaire trouvé pour ce code-barres.");
            bookTitleLabel.setText("Non trouvé");
            bookStateLabel.setText("État: Inconnu");
            bookStateLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String title      = bookInfo[0];
        String state      = bookInfo[1];
        String disponible = bookInfo[2];

        // Check if book is available for loan
        if ("indisponible".equals(disponible)) {
            showAlert("Erreur", "Cet exemplaire est déjà emprunté.");
            bookTitleLabel.setText(title);
            bookStateLabel.setText("État: " + state + " — Indisponible");
            bookStateLabel.setStyle("-fx-text-fill: #e74c3c;");
            confirmLoanBtn.setDisable(true);
            return;
        }

        // Book found and available
        bookTitleLabel.setText(title);
        bookStateLabel.setText("État: " + state);
        confirmLoanBtn.setDisable(false);

        // Color state label based on condition
        switch (state) {
            case "Excellent" -> bookStateLabel.setStyle("-fx-text-fill: #2ecc71;");
            case "Bon"       -> bookStateLabel.setStyle("-fx-text-fill: #f39c12;");
            case "Usé"       -> bookStateLabel.setStyle("-fx-text-fill: #e67e22;");
            case "Endommagé" -> bookStateLabel.setStyle("-fx-text-fill: #e74c3c;");
            default          -> bookStateLabel.setStyle("-fx-text-fill: #7f8c8d;");
        }

        // Auto calculate return date once book is scanned
        calculateReturnDate();
    }

    private void calculateReturnDate() {
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = loanDate.plusDays(15);
        returnDateLabel.setText("Date de retour: " + dateFormatter.format(returnDate));
        returnDateLabel.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold;");
    }

    private void confirmLoan() {
        String matricule = studentMatriculeField.getText().trim();
        String codeBarre = bookIsbnField.getText().trim();

        if (matricule.isEmpty() || codeBarre.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        int idUser = SessionManager.getUserId();
        if (idUser == -1) {
            showAlert("Erreur", "Session expirée.");
            return;
        }

        boolean success = loanDAO.confirmLoan(matricule, codeBarre,idUser);
        if (success) {
            loanDAO.setExemplaireDisponible(codeBarre,false);
            showAlert("Succès", "Prêt confirmé !");
            studentMatriculeField.clear();
            bookIsbnField.clear();
            studentNameLabel.setText("");
            studentQuotaLabel.setText("");
            bookTitleLabel.setText("");
            bookStateLabel.setText("");
            returnDateLabel.setText("");
            loadSampleData();
            loadSummary();
        } else {
            showAlert("Erreur", "Impossible de confirmer le prêt.");
        }
    }

    private void loadReturnInfo(String codeBarre) {
        String info = loanDAO.getLoanInfoByCodeBarre(codeBarre);
        if (info == null) {
            returnStudentLabel.setText("Aucun prêt actif trouvé.");
            returnStudentLabel.setStyle("-fx-text-fill: #e74c3c;");
            validateReturnBtn.setDisable(true);
            damagedCheckBox.setSelected(false);
            penaltyLabel.setText("Aucune pénalité");
            return;
        }
        returnStudentLabel.setText(info);
        returnStudentLabel.setStyle("-fx-text-fill: #e74c3c;");
        validateReturnBtn.setDisable(false);
        calculatePenalty();

    }

    private void calculatePenalty() {
        if (damagedCheckBox.isSelected()) {
            penaltyLabel.setText("Pénalité: 50 DT (dommage) ");
            penaltyLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            penaltyLabel.setText("Pénalité: 0 DT");
            penaltyLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    private void validateReturn() {
        String codeBarre = scanBookField.getText().trim();
        if (codeBarre.isEmpty()) {
            showAlert("Erreur", "Veuillez scanner ou saisir le code barre de l'exemplaire.");
            return;
        }
        boolean isDamaged = damagedCheckBox.isSelected();
        boolean success = loanDAO.validateReturn(codeBarre, isDamaged);
        if (success) {
            loanDAO.setExemplaireDisponible(codeBarre,true);
            showAlert("Succès", "Retour validé avec succès");
            scanBookField.clear();
            returnStudentLabel.setText("");
            damagedCheckBox.setSelected(false);
            penaltyLabel.setText("");
            loadSampleData();
            loadSummary();
        }
        else {
            showAlert("Erreur", "Impossible de valider le retour ou livre déja retourné.");
        }
    }

    private void filterHistorique() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = filterComboBox.getValue();

        allLoans = loanDAO.getAllLoans();

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
            allLoans.clear();
            allLoans = loanDAO.getAllLoans(); // ← from database now
            historiqueTable.getItems().addAll(allLoans);
            historiqueTable.getItems().clear();
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
