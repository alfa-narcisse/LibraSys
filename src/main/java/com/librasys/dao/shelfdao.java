package com.librasys.dao;

import com.librasys.controller.ShelvesController.Shelf;
import com.librasys.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class shelfdao {
    // =====================
    // GET ALL SHELVES
    // Used by ShelvesController.seedShelves() replacement
    // =====================
    public List<Shelf> getAllShelves() {
        List<Shelf> shelves = new ArrayList<>();
        String query = """
                SELECT s.id_shelf, s.name, s.description,
                       COUNT(DISTINCT b.id_book) as books_count,
                       200 as max_capacity
                FROM shelves s
                LEFT JOIN books b ON s.id_shelf = b.id_shelf
                GROUP BY s.id_shelf, s.name, s.description
                ORDER BY s.name
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                shelves.add(new Shelf(
                        rs.getString("name"),
                        rs.getInt("books_count"),
                        rs.getInt("max_capacity"),
                        0,                            // shelvesCount not in DB yet
                        rs.getString("description") != null ? rs.getString("description") : ""
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shelves;
    }

    // =====================
    // ADD SHELF
    // Used by ShelvesController.showAddShelfDialog()
    // =====================
    public boolean addShelf(String name, String colorCode, String description) {
        String query = """
                INSERT INTO shelves (name, color_code, description)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, colorCode);
            stmt.setString(3, description);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================
    // DELETE SHELF
    // Used by delete action on shelf card
    // =====================
    public boolean deleteShelf(String name) {
        String query = "DELETE FROM shelves WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================
    // UPDATE SHELF
    // Used if user edits a shelf
    // =====================
    public boolean updateShelf(String oldName, String newName, String description) {
        String query = """
                UPDATE shelves
                SET name = ?, description = ?
                WHERE name = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newName);
            stmt.setString(2, description);
            stmt.setString(3, oldName);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================
    // GET SHELF NAMES (for ComboBox)
    // Used by BooksController and NewBookController dropdowns
    // =====================
    public List<String> getAllShelfNames() {
        List<String> names = new ArrayList<>();
        String query = "SELECT name FROM shelves ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                names.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    // =====================
    // CHECK IF SHELF EXISTS
    // Used before adding a new shelf to avoid duplicates
    // =====================
    public boolean shelfExists(String name) {
        String query = "SELECT id_shelf FROM shelves WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // =====================
// GET SHELF ID BY NAME
// Used by NewBookController to get id_shelf from shelf name
// =====================
    public int getShelfIdByName(String name) {
        String query = "SELECT id_shelf FROM shelves WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_shelf");
            }
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
