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

        // Local record — matricule stays a clean String
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

        // Step 1 — Read all rows, close ResultSet
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
    public List<StudentsController.LoanEntry> getRecentLoans(String matricule) {
        return loanDAO.getRecentLoans(matricule);
    }
    public void addStudent(Student student) {
        String query = """
                INSERT INTO students (full_name, email, matricule, class_name, year_level, promotion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement Ps = conn.prepareStatement(query)) {

            Ps.setString(1, student.getFullName());
            Ps.setString(2, student.getEmail());
            Ps.setString(3, student.getMatricule());
            Ps.setString(4, student.getClassName());
            Ps.setString(5, student.getYearLevel());
            Ps.setString(6, student.getPromotion());
            Ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    // Delete a student
    public void deleteStudent(String matricule) {
        String query = "DELETE FROM students WHERE matricule = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement Ps = conn.prepareStatement(query)) {

            Ps.setString(1, matricule);
            Ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // =====================
// ADD STUDENT
// Used by NewStudentController.onSaveStudent()
// =====================
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
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
// CHECK IF MATRICULE EXISTS
// Used before inserting to avoid duplicates
// =====================
    public boolean matriculeExists(String matricule) {
        String query = "SELECT id FROM students WHERE matricule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
// CHECK IF EMAIL EXISTS
// Used before inserting to avoid duplicates
// =====================
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
    public int countStudents() {
        String query = "SELECT COUNT(*) as total FROM students";
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
    // Count new students registered this month
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
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }




}
