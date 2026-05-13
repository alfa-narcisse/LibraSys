package com.librasys.dao;

import com.librasys.controller.StudentsController;
import com.librasys.controller.StudentsController.Student;
import com.librasys.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;




public class studentdao {
    private final loandao loanDAO = new loandao();

    // Methode qui retourne la liste des étudiants dans la BDD
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();

        String query = """
            SELECT s.full_name, s.email, s.matricule, s.class_name,
                   s.year_level, s.promotion,
                   COUNT(l.id_loan) as total_loans,
                   SUM(CASE WHEN l.returned = 0 THEN 1 ELSE 0 END) as active_loans,
                   SUM(CASE WHEN l.is_delayed = 1 THEN 1 ELSE 0 END) as delays
            FROM students s
            LEFT JOIN loans l ON s.id = l.id_etudiant
            GROUP BY s.id
            """;
        record RawStudent(
                String fullName,
                String email,
                String matricule,   // ← String, not Object
                String className,
                String yearLevel,
                String promotion,
                int totalLoans,
                int activeLoans,
                int delays
        ) {}

        List<RawStudent> rawData = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement Ps = conn.prepareStatement(query);
             ResultSet rs = Ps.executeQuery(query)) {

            while (rs.next()) {
                rawData.add(new RawStudent(
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("matricule"),  // ← stored as String directly
                        rs.getString("class_name"),
                        rs.getString("year_level"),
                        rs.getString("promotion"),
                        rs.getInt("total_loans"),
                        rs.getInt("active_loans"),
                        rs.getInt("delays")
                ));
            }
            DatabaseConnection.closeConnection();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        for (RawStudent raw : rawData) {
            students.add(new Student(
                    raw.fullName(),
                    raw.email(),
                    raw.matricule(),
                    raw.className(),
                    raw.yearLevel(),
                    raw.promotion(),
                    raw.totalLoans(),
                    raw.activeLoans(),
                    raw.delays(),
                    loanDAO.getRecentLoans(raw.matricule())
            ));
        }

        return students;
    }

    // Methode qui permet d'ajouter un nouvel étudiant à la BDD
    public boolean addStudent(String fullName, String email, String matricule,
                              String className, String yearLevel,
                              LocalDate dateInscription, String promotion) {
        String query = """
            INSERT INTO students
                (full_name, email, matricule, class_name, year_level, date_inscription, promotion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, matricule);
            stmt.setString(4, className);
            stmt.setString(5, yearLevel);
            stmt.setDate(6, Date.valueOf(dateInscription));
            stmt.setString(7, promotion);
            DatabaseConnection.closeConnection();
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Methode qui permet de vérifier l'existence d'un matricule
    public boolean matriculeExists(String matricule) {
        String query = "SELECT id FROM students WHERE matricule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();
            DatabaseConnection.closeConnection();
            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Verification de l'existence de l'email
    public boolean emailExists(String email) {
        String query = "SELECT id FROM students WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    // Methode qui permet d'avoir le nombre d'étudiants
    public int countStudents() {
        String query = "SELECT COUNT(*) as total FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) return rs.getInt("total");
            DatabaseConnection.closeConnection();
            return 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    // Methode qui permet d'avoir le nombre d'étudiants enregistrés dans le mois
    public int countNewStudentsThisMonth() {
        String query = """
            SELECT COUNT(*) as total FROM students
            WHERE MONTH(date_inscription) = MONTH(CURDATE())
              AND YEAR(date_inscription)  = YEAR(CURDATE())
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) return rs.getInt("total");
            DatabaseConnection.closeConnection();
            return 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }




}
