package com.librasys.dao;

import com.librasys.controller.BooksController;
import com.librasys.controller.BooksController.Book;
import com.librasys.controller.BooksController.Rayon;
import com.librasys.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class bookdao {


    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = """
                SELECT b.title, b.author, b.isbn, b.category,
                       s.name as rayon_name,
                       CONCAT(s.name, ', Étagère ', e.code_barre) as location,
                       CASE WHEN e.disponible = TRUE THEN 'En rayon' ELSE 'Emprunté' END as availability,
                       b.description,
                       CONCAT('LIB-', LPAD(b.id_book, 5, '0')) as internal_code,
                       COALESCE(
                           SUM(CASE WHEN e.state IN ('Usé','Endommagé') THEN 1 ELSE 0 END) * 1.0
                           / NULLIF(COUNT(e.id_exemplaire), 0),
                       0) as wear_rate
                FROM books b
                LEFT JOIN shelves s    ON b.id_shelf = s.id_shelf
                LEFT JOIN exemplaire e ON b.id_book  = e.id_livre
                GROUP BY b.id_book, b.title, b.author, b.isbn,
                         b.category, s.name, e.code_barre,
                         e.disponible, b.description
                ORDER BY b.title
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement Ps = conn.prepareStatement(query);
             ResultSet rs = Ps.executeQuery(query)) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getString("rayon_name"),
                        rs.getString("location"),
                        rs.getString("availability"),
                        "",                              // cover image handled locally
                        rs.getString("internal_code"),
                        rs.getDouble("wear_rate"),
                        rs.getString("description") != null ? rs.getString("description") : ""
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return books;
    }

    // =====================
    // GET ALL RAYONS (shelves)
    public List<BooksController.Rayon> getRayonsFromShelves() {
        shelfdao shelfDAO = new shelfdao();
        return shelfDAO.getAllShelves()
                .stream()
                .map(shelf -> new BooksController.Rayon(
                        shelf.name(),
                        shelf.booksCount(),
                        shelf.maxCapacity(),
                        resolveRayonIcon(shelf.name()),
                        shelf.location()
                ))
                .toList();
    }

    // =====================
    // GET BOOKS BY RAYON
    // Used when user clicks a rayon pill/card
    // =====================
    public List<Book> getBooksByRayon(String rayonName) {
        List<Book> books = new ArrayList<>();
        String query = """
                SELECT b.title, b.author, b.isbn, b.category,
                       s.name as rayon_name,
                       CONCAT(s.name, ', Étagère ', e.code_barre) as location,
                       CASE WHEN e.disponible = TRUE THEN 'En rayon' ELSE 'Emprunté' END as availability,
                       b.description,
                       CONCAT('LIB-', LPAD(b.id_book, 5, '0')) as internal_code,
                       COALESCE(
                           SUM(CASE WHEN e.state IN ('Usé','Endommagé') THEN 1 ELSE 0 END) * 1.0
                           / NULLIF(COUNT(e.id_exemplaire), 0),
                       0) as wear_rate
                FROM books b
                LEFT JOIN shelves s    ON b.id_shelf = s.id_shelf
                LEFT JOIN exemplaire e ON b.id_book  = e.id_livre
                WHERE s.name = ?
                GROUP BY b.id_book, b.title, b.author, b.isbn,
                         b.category, s.name, e.code_barre,
                         e.disponible, b.description
                ORDER BY b.title
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, rayonName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getString("rayon_name"),
                        rs.getString("location"),
                        rs.getString("availability"),
                        "",
                        rs.getString("internal_code"),
                        rs.getDouble("wear_rate"),
                        rs.getString("description") != null ? rs.getString("description") : ""
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return books;
    }

    // =====================
    // ADD BOOK
    // Used by NewBookController
    // =====================
    public boolean addBook(String title, String author, String isbn, String edition,
                           String category, int yearEdition, String description, int idShelf) {
        String query = """
                INSERT INTO books (title, author, isbn, edition, category, year_edition, description, id_shelf)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            stmt.setString(4, edition);
            stmt.setString(5, category);
            stmt.setInt(6, yearEdition);
            stmt.setString(7, description);
            stmt.setInt(8, idShelf);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
    // DELETE BOOK
    // Used by delete action in table
    // =====================
    public boolean deleteBook(String isbn) {
        String query = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
    // SEARCH BOOKS
    // Used by search field in controller
    // =====================
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String query = """
                SELECT b.title, b.author, b.isbn, b.category,
                       s.name as rayon_name,
                       CONCAT(s.name, ', Étagère ', e.code_barre) as location,
                       CASE WHEN e.disponible = TRUE THEN 'En rayon' ELSE 'Emprunté' END as availability,
                       b.description,
                       CONCAT('LIB-', LPAD(b.id_book, 5, '0')) as internal_code,
                       COALESCE(
                           SUM(CASE WHEN e.state IN ('Usé','Endommagé') THEN 1 ELSE 0 END) * 1.0
                           / NULLIF(COUNT(e.id_exemplaire), 0),
                       0) as wear_rate
                FROM books b
                LEFT JOIN shelves s    ON b.id_shelf = s.id_shelf
                LEFT JOIN exemplaire e ON b.id_book  = e.id_livre
                WHERE b.title   LIKE ?
                   OR b.author  LIKE ?
                   OR b.isbn    LIKE ?
                GROUP BY b.id_book, b.title, b.author, b.isbn,
                         b.category, s.name, e.code_barre,
                         e.disponible, b.description
                ORDER BY b.title
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getString("rayon_name"),
                        rs.getString("location"),
                        rs.getString("availability"),
                        "",
                        rs.getString("internal_code"),
                        rs.getDouble("wear_rate"),
                        rs.getString("description") != null ? rs.getString("description") : ""
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return books;
    }

    // =====================
    // HELPER — resolve icon based on rayon name
    // =====================
    private String resolveRayonIcon(String rayonName) {
        if (rayonName == null) return "📚";
        String lower = rayonName.toLowerCase();
        if (lower.contains("info"))   return "📘";
        if (lower.contains("math"))   return "📗";
        if (lower.contains("phys"))   return "📙";
        if (lower.contains("chim"))   return "🧪";
        if (lower.contains("droit"))  return "⚖️";
        return "📚";
    }
    // =====================
// CHECK IF ISBN EXISTS
// Used by NewBookController before inserting
// =====================
    public boolean isbnExists(String isbn) {
        String query = "SELECT id_book FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
// ADD EXEMPLAIRES (physical copies)
// Called after addBook() with the stock quantity
// =====================
    public boolean addExemplaires(String isbn, int quantity) {
        // First get the book id from isbn
        String getIdQuery = "SELECT id_book FROM books WHERE isbn = ?";
        int idBook = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getIdQuery)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idBook = rs.getInt("id_book");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        if (idBook == -1) return false;

        // Insert one exemplaire per unit of stock
        String insertQuery = """
            INSERT INTO exemplaire (code_barre, state, disponible, id_livre)
            VALUES (?, 'Excellent', TRUE, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            for (int i = 1; i <= quantity; i++) {
                // Generate unique barcode: ISBN + sequence number
                String codeBarre = isbn + "-" + String.format("%03d", i);
                stmt.setString(1, codeBarre);
                stmt.setInt(2, idBook);
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public int countBooks() {
        String query = "SELECT COUNT(*) as total FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) return rs.getInt("total");
            return 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    // Count available exemplaires
    public int countAvailableBooks() {
        String query = "SELECT COUNT(*) as total FROM exemplaire WHERE disponible = true";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) return rs.getInt("total");
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Most borrowed books
    public List<String[]> getPopularBooks() {
        List<String[]> books = new ArrayList<>();
        String query = """
            SELECT b.title, COUNT(l.id_loan) as loan_count
            FROM loans l
            JOIN exemplaire e ON l.id_exemplaire = e.id_exemplaire
            JOIN books b      ON e.id_livre      = b.id_book
            GROUP BY b.id_book, b.title
            ORDER BY loan_count DESC
            LIMIT 4
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Get max count for progress bar ratio
            List<int[]> raw = new ArrayList<>();
            int maxCount    = 1;

            while (rs.next()) {
                int count = rs.getInt("loan_count");
                raw.add(new int[]{rs.getRow()});
                if (count > maxCount) maxCount = count;
                books.add(new String[]{rs.getString("title"), String.valueOf(count)});
            }

            // Convert count to progress ratio
            int finalMax = maxCount;
            books.replaceAll(b -> new String[]{
                    b[0],
                    String.valueOf(Double.parseDouble(b[1]) / finalMax)
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
}