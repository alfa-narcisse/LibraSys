package com.librasys.controller;

import com.librasys.util.SessionManager;
import com.librasys.dao.bookdao;
import com.librasys.dao.loandao;
import com.librasys.dao.studentdao;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {
    @FXML
    private BarChart<String, Number> monthlyActivityChart;

    @FXML
    private TableView<LoanRow> recentLoansTable;

    @FXML
    private TableColumn<LoanRow, String> studentColumn;

    @FXML
    private TableColumn<LoanRow, String> bookColumn;

    @FXML
    private TableColumn<LoanRow, String> returnDateColumn;

    @FXML
    private TableColumn<LoanRow, String> statusColumn;

    @FXML
    private ImageView sidebarLogoImageView;

    @FXML
    private VBox mainContentArea;

    @FXML
    private Button dashboardMenuButton;

    @FXML
    private Button studentsMenuButton;

    @FXML
    private Button booksMenuButton;

    @FXML
    private Button loansMenuButton;
    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label totalBooksLabel;
    @FXML
    private Label activeLoansLabel;
    @FXML
    private Label criticalDelaysLabel;
    @FXML
    private Label headerLabel;
    @FXML
    private Label availableBooksLabel;
    @FXML
    private Label newStudentsLabel;
    @FXML
    private Label todayLoansLabel;
    @FXML
    private Label todayReturnsLabel;
    @FXML
    private Label totalDelaysLabel;
    @FXML
    private VBox  popularBooksContainer;

    private List<Node> dashboardHomeNodes;

    private final loandao loanDAO = new loandao();
    private final studentdao studentDAO = new studentdao();
    private final bookdao bookDAO = new bookdao();

    @FXML
    private void initialize() {
        loadLogo();
        loadSummaryCards();
        initChart();
        initTable();
        dashboardHomeNodes = new ArrayList<>(mainContentArea.getChildren());
        setActiveMenu(dashboardMenuButton);

    }

    private void loadLogo() {
        URL logoUrl = getClass().getResource("/com/librasys/logo_ept.png");
        if (logoUrl != null) {
            sidebarLogoImageView.setImage(new Image(logoUrl.toExternalForm()));
        }
    }


    // =====================
    // SUMMARY CARDS FROM DB
    // =====================
    private void loadSummaryCards() {
        // Header with logged-in username and today's date
        String username = SessionManager.getUsername();
        String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        if (headerLabel != null) {
            headerLabel.setText("Bonjour, " + username + " — " + today);
        }

        // Total books and available
        int totalBooks     = bookDAO.countBooks();
        int availableBooks = bookDAO.countAvailableBooks();
        if (totalBooksLabel     != null) totalBooksLabel.setText(String.valueOf(totalBooks));
        if (availableBooksLabel != null) availableBooksLabel.setText(availableBooks + " disponibles");

        // Total students and new this month
        int totalStudents = studentDAO.countStudents();
        int newStudents   = studentDAO.countNewStudentsThisMonth();
        if (totalStudentsLabel != null) totalStudentsLabel.setText(String.valueOf(totalStudents));
        if (newStudentsLabel   != null) newStudentsLabel.setText("+" + newStudents + " ce mois");

        // Loan stats
        int[] stats = loanDAO.getSummaryStats();
        int activeLoans    = stats[0];
        int newToday       = stats[1];
        int expectedToday  = stats[2];
        int criticalDelays = stats[4];

        if (activeLoansLabel    != null) activeLoansLabel.setText(activeLoans + " prets actifs");
        if (criticalDelaysLabel != null) criticalDelaysLabel.setText(criticalDelays + " retards");
        if (todayLoansLabel     != null) todayLoansLabel.setText(String.valueOf(newToday));
        if (todayReturnsLabel   != null) todayReturnsLabel.setText(expectedToday + " retours");
        if (totalDelaysLabel    != null) totalDelaysLabel.setText(String.valueOf(criticalDelays));

        // Color for critical delays
        if (criticalDelaysLabel != null) {
            if (criticalDelays > 5)     criticalDelaysLabel.setStyle("-fx-text-fill: #e74c3c;");
            else if (criticalDelays > 0) criticalDelaysLabel.setStyle("-fx-text-fill: #f39c12;");
            else                         criticalDelaysLabel.setStyle("-fx-text-fill: #2ecc71;");
        }

        // Popular books
        loadPopularBooks();
    }
    private void loadPopularBooks() {
        if (popularBooksContainer == null) return;

        List<String[]> popular = bookDAO.getPopularBooks();
        popularBooksContainer.getChildren().clear();

        for (String[] book : popular) {
            String title    = book[0];
            double progress = Double.parseDouble(book[1]);

            Label nameLabel = new Label(title);
            nameLabel.getStyleClass().add("popular-label");

            ProgressBar bar = new ProgressBar(progress);
            bar.getStyleClass().add("popular-progress");
            bar.setMaxWidth(Double.MAX_VALUE);

            VBox entry = new VBox(4, nameLabel, bar);
            popularBooksContainer.getChildren().add(entry);
        }
    }
    // =====================
    // CHART FROM DB
    // =====================
    private void initChart() {
        List<int[]> monthlyStats = loanDAO.getMonthlyStats();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prêts");

        String[] monthNames = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};

        if (monthlyStats.isEmpty()) {
            // Fallback — show empty chart
            for (String month : monthNames) {
                series.getData().add(new XYChart.Data<>(month, 0));
            }
        } else {
            for (int[] row : monthlyStats) {
                int month = row[0];
                int count = row[1];
                series.getData().add(new XYChart.Data<>(monthNames[month - 1], count));
            }
        }

        monthlyActivityChart.getData().clear();
        monthlyActivityChart.getData().add(series);
        monthlyActivityChart.setLegendVisible(false);
        monthlyActivityChart.setAnimated(false);
    }

    // =====================
    // RECENT LOANS TABLE FROM DB
    // =====================
    private void initTable() {
        studentColumn.setCellValueFactory(data -> data.getValue().studentProperty());
        bookColumn.setCellValueFactory(data -> data.getValue().bookProperty());
        returnDateColumn.setCellValueFactory(data -> data.getValue().returnDateProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        statusColumn.setCellFactory(column -> new TableCell<>() {
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

        // Load recent loans from DB
        loadRecentLoans();
    }

    private void loadRecentLoans() {
        List<LoanRow> recentLoans = loanDAO.getRecentLoansForDashboard();
        recentLoansTable.getItems().clear();
        recentLoansTable.getItems().addAll(recentLoans);
    }





    @FXML
    private void showDashboard() {
        mainContentArea.getChildren().setAll(dashboardHomeNodes);
        setActiveMenu(dashboardMenuButton);
    }

    @FXML
    private void showStudents() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/StudentsView.fxml"));
            mainContentArea.getChildren().setAll(view);
            setActiveMenu(studentsMenuButton);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de charger la vue des etudiants.", exception);
        }
    }

    @FXML
    private void showBooks() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/BooksView.fxml"));
            mainContentArea.getChildren().setAll(view);
            setActiveMenu(booksMenuButton);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de charger la vue des livres.", exception);
        }
    }

    @FXML
    private void showLoans() {
        try {
            Pane view = FXMLLoader.load(getClass().getResource("/com/librasys/LoansView.fxml"));
            mainContentArea.getChildren().setAll(view);
            setActiveMenu(loansMenuButton);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de charger la vue des prêts/retours.", exception);
        }
    }

    private void setActiveMenu(Button activeButton) {
        dashboardMenuButton.getStyleClass().remove("menu-btn-selected");
        studentsMenuButton.getStyleClass().remove("menu-btn-selected");
        booksMenuButton.getStyleClass().remove("menu-btn-selected");
        loansMenuButton.getStyleClass().remove("menu-btn-selected");
        activeButton.getStyleClass().add("menu-btn-selected");
    }

    public static class LoanRow {
        private final StringProperty student;
        private final StringProperty book;
        private final StringProperty returnDate;
        private final StringProperty status;

        public LoanRow(String student, String book, String returnDate, String status) {
            this.student = new SimpleStringProperty(student);
            this.book = new SimpleStringProperty(book);
            this.returnDate = new SimpleStringProperty(returnDate);
            this.status = new SimpleStringProperty(status);
        }

        public StringProperty studentProperty() {
            return student;
        }

        public StringProperty bookProperty() {
            return book;
        }

        public StringProperty returnDateProperty() {
            return returnDate;
        }

        public StringProperty statusProperty() {
            return status;
        }
    }

    @FXML
    private Button logoutButton;

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vous déconnecter ?");

        java.net.URL cssUrl = getClass().getResource("/com/librasys/style.css");
        if (cssUrl != null) alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        alert.getDialogPane().getStyleClass().add("confirm-dialog");

        java.util.Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            SessionManager.logout();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/librasys/LoginView.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                double w = stage.getWidth();
                double h = stage.getHeight();

                Scene scene = new Scene(root, w, h);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

                stage.setScene(scene);
                stage.setTitle("LibraSys - Login");
            } catch (IOException e) {
                throw new IllegalStateException("Impossible de charger la page de login.", e);
            }
        }
    }
}
