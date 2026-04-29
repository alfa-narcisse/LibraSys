package com.librasys.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

public class BooksController {
    @FXML
    private VBox booksRoot;
    @FXML
    private TextField searchField;
    @FXML
    private HBox pillsContainer;
    @FXML
    private HBox rayonCardsContainer;
    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, String> coverColumn;
    @FXML
    private TableColumn<Book, String> titleAuthorColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, String> locationColumn;
    @FXML
    private TableColumn<Book, String> availabilityColumn;
    @FXML
    private TableColumn<Book, String> actionsColumn;
    @FXML
    private VBox detailsPane;
    @FXML
    private ImageView detailCoverImageView;
    @FXML
    private Label detailBookTitleLabel;
    @FXML
    private Label detailMetaLabel;
    @FXML
    private TextArea detailSummaryArea;
    @FXML
    private Label detailCodeLabel;
    @FXML
    private ProgressBar detailWearProgress;
    @FXML
    private Label detailWearLabel;

    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<Rayon> rayons = FXCollections.observableArrayList();
    private final FilteredList<Book> filteredBooks = new FilteredList<>(books, book -> true);
    private String selectedRayon = "Tous";

    @FXML
    private void initialize() {
        seedData();
        buildRayonPills();
        buildRayonCards();
        configureTable();
        applyFilters();
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> showBookDetails(selected));
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);
    }

    @FXML
    private void onSearchChanged() {
        applyFilters();
    }

    @FXML
    private void onAddBook() {
        loadIntoMainContent("/com/librasys/NewBookView.fxml", "Impossible de charger le formulaire d'ajout de livre.");
    }

    @FXML
    private void onManageRayons() {
        loadIntoMainContent("/com/librasys/ShelvesView.fxml", "Impossible de charger la vue de gestion des rayons.");
    }

    private void configureTable() {
        coverColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        coverColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Book book = (Book) getTableRow().getItem();
                Node cover = createSmallCover(book);
                setGraphic(new HBox(cover));
            }
        });

        titleAuthorColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        titleAuthorColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Book book = (Book) getTableRow().getItem();
                Label title = new Label(book.getTitle());
                title.getStyleClass().add("book-title-cell");
                Label author = new Label(book.getAuthor());
                author.getStyleClass().add("book-author-cell");
                VBox box = new VBox(3, title, author);
                setGraphic(box);
            }
        });

        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("category-badge");
                badge.getStyleClass().add(resolveCategoryClass(item));
                setGraphic(badge);
            }
        });

        locationColumn.setCellValueFactory(data -> data.getValue().locationProperty());

        availabilityColumn.setCellValueFactory(data -> data.getValue().availabilityProperty());
        availabilityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(item);
                chip.getStyleClass().add("availability-chip");
                chip.getStyleClass().add("En rayon".equals(item) ? "availability-in" : "availability-out");
                setGraphic(chip);
            }
        });

        actionsColumn.setCellValueFactory(data -> data.getValue().isbnProperty());
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Label edit = new Label("✎");
                Label delete = new Label("🗑");
                edit.getStyleClass().add("action-icon");
                delete.getStyleClass().add("action-icon");
                HBox box = new HBox(10, edit, delete);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
    }

    private String resolveCategoryClass(String category) {
        String lower = category.toLowerCase(Locale.ROOT);
        if (lower.contains("info")) {
            return "cat-info";
        }
        if (lower.contains("math")) {
            return "cat-math";
        }
        if (lower.contains("phys")) {
            return "cat-phys";
        }
        return "cat-generic";
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        filteredBooks.setPredicate(book -> {
            boolean matchesRayon = "Tous".equals(selectedRayon) || selectedRayon.equals(book.getRayonName());
            boolean matchesQuery = query.isBlank()
                    || book.getTitle().toLowerCase(Locale.ROOT).contains(query)
                    || book.getAuthor().toLowerCase(Locale.ROOT).contains(query)
                    || book.getIsbn().toLowerCase(Locale.ROOT).contains(query);
            return matchesRayon && matchesQuery;
        });

        ObservableList<Book> sorted = FXCollections.observableArrayList(filteredBooks);
        sorted.sort(Comparator.comparing(Book::getTitle));
        booksTable.setItems(sorted);
        if (!sorted.isEmpty()) {
            booksTable.getSelectionModel().selectFirst();
        } else {
            showBookDetails(null);
        }
    }

    private void showBookDetails(Book book) {
        if (book == null) {
            detailsPane.setVisible(false);
            detailsPane.setManaged(false);
            return;
        }
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
        detailBookTitleLabel.setText(book.getTitle());
        detailMetaLabel.setText(book.getAuthor() + " • " + book.getLocation());
        detailSummaryArea.setText(book.getSummary());
        detailCodeLabel.setText(book.getInternalCode());
        detailWearProgress.setProgress(book.getWearRate());
        detailWearLabel.setText(String.format("Usure: %d%%", Math.round(book.getWearRate() * 100)));

        URL imageUrl = getClass().getResource("/com/librasys/images/" + book.getCoverImageFile());
        if (imageUrl != null) {
            detailCoverImageView.setImage(new Image(imageUrl.toExternalForm()));
            detailCoverImageView.setVisible(true);
        } else {
            detailCoverImageView.setImage(null);
            detailCoverImageView.setVisible(false);
        }
    }

    private Node createSmallCover(Book book) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(34);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("book-cover-thumb");
        URL imageUrl = getClass().getResource("/com/librasys/images/" + book.getCoverImageFile());
        if (imageUrl != null) {
            imageView.setImage(new Image(imageUrl.toExternalForm()));
            return imageView;
        }

        Rectangle placeholder = new Rectangle(34, 48);
        placeholder.setArcWidth(8);
        placeholder.setArcHeight(8);
        placeholder.setFill(Color.web("#e6ebff"));
        placeholder.setStroke(Color.web("#b7c2ef"));
        Label icon = new Label("📕");
        StackPane stack = new StackPane(placeholder, icon);
        stack.setAlignment(Pos.CENTER);
        return stack;
    }

    private void buildRayonPills() {
        pillsContainer.getChildren().clear();
        pillsContainer.getChildren().add(createPill("Tous"));
        for (Rayon rayon : rayons) {
            pillsContainer.getChildren().add(createPill(rayon.name()));
        }
    }

    private Button createPill(String rayonName) {
        Button pill = new Button(rayonName);
        pill.getStyleClass().add("rayon-pill");
        if (rayonName.equals(selectedRayon)) {
            pill.getStyleClass().add("rayon-pill-active");
        }
        pill.setOnAction(event -> {
            selectedRayon = rayonName;
            buildRayonPills();
            highlightSelectedRayonCard();
            applyFilters();
        });
        return pill;
    }

    private void buildRayonCards() {
        rayonCardsContainer.getChildren().clear();
        for (Rayon rayon : rayons) {
            VBox card = new VBox(8);
            card.getStyleClass().add("rayon-card");

            HBox header = new HBox(8);
            Label icon = new Label(rayon.icon());
            icon.getStyleClass().add("rayon-icon");
            VBox info = new VBox(2);
            Label name = new Label(rayon.name());
            name.getStyleClass().add("rayon-name");
            Label stats = new Label(rayon.currentCount() + "/" + rayon.capacity() + " livres");
            stats.getStyleClass().add("rayon-stats");
            info.getChildren().addAll(name, stats);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(icon, info, spacer);

            ProgressBar progress = new ProgressBar((double) rayon.currentCount() / rayon.capacity());
            progress.getStyleClass().add("rayon-progress");
            progress.setMaxWidth(Double.MAX_VALUE);

            Label location = new Label(rayon.location());
            location.getStyleClass().add("rayon-location");
            card.getChildren().addAll(header, location, progress);

            card.setOnMouseClicked(event -> {
                selectedRayon = rayon.name();
                buildRayonPills();
                highlightSelectedRayonCard();
                applyFilters();
            });
            rayonCardsContainer.getChildren().add(card);
        }
        highlightSelectedRayonCard();
    }

    private void highlightSelectedRayonCard() {
        for (Node node : rayonCardsContainer.getChildren()) {
            if (!(node instanceof VBox card) || card.getChildren().isEmpty()) {
                continue;
            }
            card.getStyleClass().remove("rayon-card-selected");
            card.setScaleX(1.0);
            card.setScaleY(1.0);

            HBox header = (HBox) card.getChildren().getFirst();
            VBox info = (VBox) header.getChildren().get(1);
            Label name = (Label) info.getChildren().getFirst();
            if (name.getText().equals(selectedRayon)) {
                card.getStyleClass().add("rayon-card-selected");
                card.setScaleX(1.03);
                card.setScaleY(1.03);
            }
        }
    }

    private void sortRayons() {
        FXCollections.sort(rayons, Comparator.comparing(Rayon::name));
    }

    private void loadIntoMainContent(String fxmlPath, String errorMessage) {
        try {
            Pane view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (booksRoot.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().setAll(view);
                return;
            }
            throw new IllegalStateException("Le conteneur principal est introuvable.");
        } catch (IOException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    private void seedData() {
        rayons.addAll(
                new Rayon("Rayon A - Info", 124, 200, "📘", "Allée 1, Étagère B"),
                new Rayon("Rayon B - Math", 91, 180, "📗", "Allée 2, Étagère C"),
                new Rayon("Rayon C - Physique", 73, 160, "📙", "Allée 3, Étagère A"),
                new Rayon("Rayon D - Littérature", 67, 140, "📕", "Allée 4, Étagère D")
        );

        books.addAll(List.of(
                new Book("Clean Code", "Robert C. Martin", "9780132350884", "Informatique",
                        "Rayon A - Info", "Rayon A, Étagère 3", "En rayon", "book_clean_code.jpg",
                        "LIB-45219", 0.22, "Guide de bonnes pratiques pour écrire du code lisible et maintenable."),
                new Book("Algèbre Linéaire", "Gilbert Strang", "9780980232776", "Mathématiques",
                        "Rayon B - Math", "Rayon B, Étagère 1", "En rayon", "book_algebra.jpg",
                        "LIB-38217", 0.36, "Référence pour la modélisation matricielle et les espaces vectoriels."),
                new Book("Mécanique Quantique", "Cohen-Tannoudji", "9782253105158", "Physique",
                        "Rayon C - Physique", "Rayon C, Étagère 4", "Emprunté", "book_quantum.jpg",
                        "LIB-77431", 0.61, "Présentation structurée des principes de la mécanique quantique."),
                new Book("Structures de Données", "Mark Allen Weiss", "9780132847377", "Informatique",
                        "Rayon A - Info", "Rayon A, Étagère 2", "En rayon", "book_data_structures.jpg",
                        "LIB-19872", 0.17, "Cours orienté implémentation sur listes, arbres et tables de hachage."),
                new Book("Analyse Numérique", "Kincaid & Cheney", "9780821847886", "Mathématiques",
                        "Rayon B - Math", "Rayon B, Étagère 5", "Emprunté", "book_analysis.jpg",
                        "LIB-22104", 0.41, "Méthodes numériques pour résoudre systèmes, équations et interpolation."),
                new Book("Optique Moderne", "Eugene Hecht", "9781292096933", "Physique",
                        "Rayon C - Physique", "Rayon C, Étagère 2", "En rayon", "book_optics.jpg",
                        "LIB-91820", 0.29, "Ouvrage de référence pour l’optique géométrique et ondulatoire.")
        ));
    }

    public record Rayon(String name, int currentCount, int capacity, String icon, String location) {
    }

    public static class Book {
        private final StringProperty title;
        private final StringProperty author;
        private final StringProperty isbn;
        private final StringProperty category;
        private final StringProperty rayonName;
        private final StringProperty location;
        private final StringProperty availability;
        private final StringProperty coverImageFile;
        private final StringProperty internalCode;
        private final DoubleProperty wearRate;
        private final StringProperty summary;

        public Book(String title, String author, String isbn, String category, String rayonName, String location,
                    String availability, String coverImageFile, String internalCode, double wearRate, String summary) {
            this.title = new SimpleStringProperty(title);
            this.author = new SimpleStringProperty(author);
            this.isbn = new SimpleStringProperty(isbn);
            this.category = new SimpleStringProperty(category);
            this.rayonName = new SimpleStringProperty(rayonName);
            this.location = new SimpleStringProperty(location);
            this.availability = new SimpleStringProperty(availability);
            this.coverImageFile = new SimpleStringProperty(coverImageFile);
            this.internalCode = new SimpleStringProperty(internalCode);
            this.wearRate = new SimpleDoubleProperty(wearRate);
            this.summary = new SimpleStringProperty(summary);
        }

        public String getTitle() {
            return title.get();
        }

        public StringProperty titleProperty() {
            return title;
        }

        public String getAuthor() {
            return author.get();
        }

        public String getIsbn() {
            return isbn.get();
        }

        public StringProperty isbnProperty() {
            return isbn;
        }

        public String getCategory() {
            return category.get();
        }

        public StringProperty categoryProperty() {
            return category;
        }

        public String getRayonName() {
            return rayonName.get();
        }

        public String getLocation() {
            return location.get();
        }

        public StringProperty locationProperty() {
            return location;
        }

        public String getAvailability() {
            return availability.get();
        }

        public StringProperty availabilityProperty() {
            return availability;
        }

        public String getCoverImageFile() {
            return coverImageFile.get();
        }

        public String getInternalCode() {
            return internalCode.get();
        }

        public double getWearRate() {
            return wearRate.get();
        }

        public String getSummary() {
            return summary.get();
        }
    }
}
