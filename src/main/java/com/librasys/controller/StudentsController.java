package com.librasys.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

public class StudentsController {
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> promotionFilter;

    @FXML
    private ComboBox<String> sortFilter;

    @FXML
    private TableView<Student> studentsTable;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> emailColumn;

    @FXML
    private TableColumn<Student, String> matriculeColumn;

    @FXML
    private TableColumn<Student, String> classColumn;

    @FXML
    private TableColumn<Student, String> yearColumn;

    @FXML
    private TableColumn<Student, Number> totalLoansColumn;

    @FXML
    private TableColumn<Student, Number> activeLoansColumn;

    @FXML
    private TableColumn<Student, Number> delaysColumn;

    @FXML
    private VBox studentDetailPane;

    @FXML
    private Label detailNameLabel;

    @FXML
    private Label detailSectionLabel;

    @FXML
    private Label detailMatriculeLabel;

    @FXML
    private Label detailLoanChipLabel;

    @FXML
    private Label cardNameLabel;

    @FXML
    private Label cardMatriculeLabel;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(students, student -> true);

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        seedData();
        refreshTable();
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                showStudentDetails(newValue);
            }
        });

        if (!students.isEmpty()) {
            showStudentDetails(students.getFirst());
            studentsTable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onSearchChanged() {
        applyFilters();
    }

    @FXML
    private void onPromotionFilterChanged() {
        applyFilters();
    }

    @FXML
    private void onSortFilterChanged() {
        refreshTable();
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(data -> data.getValue().fullNameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        matriculeColumn.setCellValueFactory(data -> data.getValue().matriculeProperty());
        classColumn.setCellValueFactory(data -> data.getValue().classNameProperty());
        yearColumn.setCellValueFactory(data -> data.getValue().yearLevelProperty());
        totalLoansColumn.setCellValueFactory(data -> data.getValue().totalLoansProperty());
        activeLoansColumn.setCellValueFactory(data -> data.getValue().activeLoansProperty());
        delaysColumn.setCellValueFactory(data -> data.getValue().delaysProperty());
    }

    private void configureFilters() {
        promotionFilter.setItems(FXCollections.observableArrayList("Toutes", "2024", "2025", "2026"));
        promotionFilter.getSelectionModel().selectFirst();
        sortFilter.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Retards (desc)", "Classe (A-Z)"));
        sortFilter.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String promotion = promotionFilter.getValue();

        filteredStudents.setPredicate(student -> {
            boolean matchesQuery = query.isBlank()
                    || student.getFullName().toLowerCase().contains(query)
                    || student.getMatricule().toLowerCase().contains(query);
            boolean matchesPromotion = promotion == null
                    || "Toutes".equals(promotion)
                    || promotion.equals(student.getPromotion());
            return matchesQuery && matchesPromotion;
        });

        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Student> sorted = FXCollections.observableArrayList(filteredStudents);
        String selectedSort = sortFilter.getValue();
        if ("Retards (desc)".equals(selectedSort)) {
            sorted.sort(Comparator.comparingInt(Student::getDelays).reversed().thenComparing(Student::getFullName));
        } else if ("Classe (A-Z)".equals(selectedSort)) {
            sorted.sort(Comparator.comparing(Student::getClassName).thenComparing(Student::getFullName));
        } else {
            sorted.sort(Comparator.comparing(Student::getFullName));
        }

        studentsTable.setItems(sorted);
    }

    private void showStudentDetails(Student student) {
        detailNameLabel.setText(student.getFullName());
        detailSectionLabel.setText(student.getClassName() + " - " + student.getYearLevel());
        detailMatriculeLabel.setText(student.getMatricule());
        cardNameLabel.setText(student.getFullName());
        cardMatriculeLabel.setText(student.getMatricule());
        detailLoanChipLabel.setText(student.getActiveLoans() + " prêts");
        detailLoanChipLabel.getStyleClass().removeAll("loan-chip-warning", "loan-chip-active");
        detailLoanChipLabel.getStyleClass().add(student.isNearDelay() ? "loan-chip-warning" : "loan-chip-active");
    }

    private void seedData() {
        students.addAll(
                new Student("Ahmed Bensalem", "ahmed.bensalem@ept.tn", "2023-INF-0412", "GINF-A", "3ème", "2026", 14, 2, 1,
                        List.of(new LoanEntry("Algorithmique", "14/04/2026"), new LoanEntry("Droit civil", "10/04/2026"))),
                new Student("Sarra Khalfallah", "sarra.khalfallah@ept.tn", "2024-MAT-0098", "GMATH-B", "2ème", "2025", 11, 1, 3,
                        List.of(new LoanEntry("Analyse avancee", "11/04/2026"), new LoanEntry("Physique", "07/04/2026"))),
                new Student("Youssef Triki", "youssef.triki@ept.tn", "2022-CHI-0220", "GCHIM-A", "4ème", "2024", 18, 3, 0,
                        List.of(new LoanEntry("Chimie org.", "16/04/2026"), new LoanEntry("Thermodynamique", "13/04/2026"))),
                new Student("Meriem Ayed", "meriem.ayed@ept.tn", "2023-INF-0444", "GINF-A", "3ème", "2026", 16, 2, 2,
                        List.of(new LoanEntry("Reseaux", "09/04/2026"), new LoanEntry("Architecture SI", "04/04/2026"))),
                new Student("Hatem Ghezal", "hatem.ghezal@ept.tn", "2024-MEC-0156", "GMEC-C", "2ème", "2025", 7, 0, 0,
                        List.of(new LoanEntry("Mecanique des fluides", "02/04/2026"), new LoanEntry("Calcul numerique", "29/03/2026")))
        );
    }

    public static class Student {
        private final StringProperty fullName;
        private final StringProperty email;
        private final StringProperty matricule;
        private final StringProperty className;
        private final StringProperty yearLevel;
        private final StringProperty promotion;
        private final IntegerProperty totalLoans;
        private final SimpleIntegerProperty activeLoans;
        private final IntegerProperty delays;
        private final List<LoanEntry> recentLoans;

        public Student(String fullName, String email, String matricule, String className, String yearLevel, String promotion,
                       int totalLoans, int activeLoans, int delays,
                       List<LoanEntry> recentLoans) {
            this.fullName = new SimpleStringProperty(fullName);
            this.email = new SimpleStringProperty(email);
            this.matricule = new SimpleStringProperty(matricule);
            this.className = new SimpleStringProperty(className);
            this.yearLevel = new SimpleStringProperty(yearLevel);
            this.promotion = new SimpleStringProperty(promotion);
            this.totalLoans = new SimpleIntegerProperty(totalLoans);
            this.activeLoans = new SimpleIntegerProperty(activeLoans);
            this.delays = new SimpleIntegerProperty(delays);
            this.recentLoans = recentLoans;
        }

        public String getFullName() {
            return fullName.get();
        }

        public StringProperty fullNameProperty() {
            return fullName;
        }

        public String getEmail() {
            return email.get();
        }

        public StringProperty emailProperty() {
            return email;
        }

        public String getMatricule() {
            return matricule.get();
        }

        public StringProperty matriculeProperty() {
            return matricule;
        }

        public String getClassName() {
            return className.get();
        }

        public StringProperty classNameProperty() {
            return className;
        }

        public String getYearLevel() {
            return yearLevel.get();
        }

        public StringProperty yearLevelProperty() {
            return yearLevel;
        }

        public String getPromotion() {
            return promotion.get();
        }

        public int getTotalLoans() {
            return totalLoans.get();
        }

        public IntegerProperty totalLoansProperty() {
            return totalLoans;
        }

        public int getActiveLoans() {
            return activeLoans.get();
        }

        public IntegerProperty activeLoansProperty() {
            return activeLoans;
        }

        public int getDelays() {
            return delays.get();
        }

        public IntegerProperty delaysProperty() {
            return delays;
        }

        public boolean isNearDelay() {
            return getDelays() > 0;
        }

        public List<LoanEntry> getRecentLoans() {
            return recentLoans;
        }
    }

    public record LoanEntry(String title, String date) {
    }
}
