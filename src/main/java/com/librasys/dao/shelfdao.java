package com.librasys.dao;

import com.librasys.controller.ShelvesController.Shelf;
import com.librasys.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class shelfdao {
    // Methode qui permet d'obtenir la liste des rayons disponibles
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
                        0,
                        rs.getString("description") != null ? rs.getString("description") : ""
                ));
            }
            DatabaseConnection.closeConnection();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return shelves;
    }

    // Methode qui permet d'ajouter un rayon
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
            DatabaseConnection.closeConnection();
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    // Methode qui permet d'obtenir le nom des rayons pour l'affichage dans le combo box
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
            System.out.println(e.getMessage());
                    }
        return names;
   }

    // Methode qui permet de verifier l'existence d'un rayon
    public boolean shelfExists(String name) {
        String query = "SELECT id_shelf FROM shelves WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    // Methode qui permet d'obtenir l'id du rayon à partir du rayon
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
            System.out.println(e.getMessage());
            return -1;
        }
    }
}
