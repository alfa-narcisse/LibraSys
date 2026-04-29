package com.librasys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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

    private List<Node> dashboardHomeNodes;

    @FXML
    private void initialize() {
        loadLogo();
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

    private void initChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prets");
        series.getData().add(new XYChart.Data<>("Nov", 43));
        series.getData().add(new XYChart.Data<>("Dec", 51));
        series.getData().add(new XYChart.Data<>("Jan", 58));
        series.getData().add(new XYChart.Data<>("Fev", 65));
        series.getData().add(new XYChart.Data<>("Mar", 74));
        series.getData().add(new XYChart.Data<>("Avr", 68));

        monthlyActivityChart.getData().clear();
        monthlyActivityChart.getData().add(series);
        monthlyActivityChart.setLegendVisible(false);
        monthlyActivityChart.setAnimated(false);
    }

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
                chip.getStyleClass().add("Retard".equals(status) ? "status-retard" : "status-en-cours");

                StackPane wrapper = new StackPane(chip);
                wrapper.setMaxWidth(Double.MAX_VALUE);
                setGraphic(wrapper);
            }
        });

        recentLoansTable.getItems().addAll(
                new LoanRow("A. Ben Ali", "Algorithmique avancee", "18/04/2026", "En cours"),
                new LoanRow("M. Trabelsi", "Physique quantique", "17/04/2026", "Retard"),
                new LoanRow("R. Gharbi", "Droit civil", "19/04/2026", "En cours"),
                new LoanRow("S. Kooli", "Chimie organique", "16/04/2026", "Retard"),
                new LoanRow("L. Mansouri", "Base de donnees", "20/04/2026", "En cours")
        );
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
}
