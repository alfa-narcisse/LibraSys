package com.librasys.dao;

import com.librasys.controller.DashboardController;
import com.librasys.controller.LoansController.LoanHistoryRow;
import com.librasys.controller.StudentsController.LoanEntry;
import com.librasys.util.DatabaseConnection;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class loandao {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =====================
    // GET RECENT LOANS FOR ONE STUDENT
    // Used by studentdao.getAllStudents()
    // =====================
    public List<LoanEntry> getRecentLoans(String matricule) {
        List<LoanEntry> loans = new ArrayList<>();
        String query = """
                SELECT b.title, l.return_date
                FROM loans l
                JOIN exemplaire e ON l.id_exemplaire = e.id_exemplaire
                JOIN books b      ON e.id_livre       = b.id_book
                JOIN students s   ON l.id_etudiant    = s.id
                WHERE s.matricule = ?
                  AND l.returned  = false
                ORDER BY l.loan_date DESC
                LIMIT 5
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title      = rs.getString("title");
                String returnDate = rs.getDate("return_date")
                        .toLocalDate()
                        .format(formatter);
                loans.add(new LoanEntry(title, returnDate));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return loans;
    }
    //=====================
    // GET SUMMARY STATS
    // Used by LoansController to fill summary cards
    // =====================
    public int[] getSummaryStats() {
        // Methode qui va retourner les valeurs pour le résumé de l'activité journalière
        String query = """
            SELECT
                SUM(CASE WHEN returned = false THEN 1 ELSE 0 END)
                    as active_loans,
                SUM(CASE WHEN returned = false AND loan_date = CURDATE() THEN 1 ELSE 0 END)
                    as new_today,
                SUM(CASE WHEN returned = false AND return_date = CURDATE() THEN 1 ELSE 0 END)
                    as expected_today,
                SUM(CASE WHEN returned = false AND return_date = CURDATE() AND is_delayed = true THEN 1 ELSE 0 END)
                    as late_returns,
                SUM(CASE WHEN returned = false AND is_delayed = true THEN 1 ELSE 0 END)
                    as critical_delays
            FROM loans
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return new int[]{
                        rs.getInt("active_loans"),
                        rs.getInt("new_today"),
                        rs.getInt("expected_today"),
                        rs.getInt("late_returns"),
                        rs.getInt("critical_delays")
                };
            }
            return new int[]{0, 0, 0, 0, 0};

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new int[]{0, 0, 0, 0, 0};
        }
    }

    // =====================
    // GET ALL LOANS
    // Used by LoansController historique table
    // =====================
    public List<LoanHistoryRow> getAllLoans() {
        List<LoanHistoryRow> loans = new ArrayList<>();
        String query = """
                SELECT s.full_name, b.title,
                       l.loan_date, l.return_date,
                       l.returned, l.is_delayed
                FROM loans l
                JOIN students  s  ON l.id_etudiant    = s.id
                JOIN exemplaire e ON l.id_exemplaire   = e.id_exemplaire
                JOIN books b      ON e.id_livre        = b.id_book
                ORDER BY l.loan_date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String status;
                if (rs.getBoolean("returned")) {
                    status = "Rendu";
                } else if (rs.getBoolean("is_delayed")) {
                    status = "En retard";
                } else {
                    status = "En cours";
                }

                loans.add(new LoanHistoryRow(
                        rs.getString("full_name"),
                        rs.getString("title"),
                        rs.getDate("loan_date").toLocalDate().format(formatter),
                        rs.getDate("return_date").toLocalDate().format(formatter),
                        status
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return loans;
    }

    // =====================
    // CONFIRM LOAN
    // Used by LoansController.confirmLoan()
    // =====================
    public boolean confirmLoan(String matricule, String codeBarre,int idUser) {
        String query = """
                INSERT INTO loans (loan_date, return_date, returned, is_delayed,
                                   is_damaged, penality, id_etudiant, id_exemplaire, id_user)
                VALUES (
                    CURDATE(),
                    DATE_ADD(CURDATE(), INTERVAL 15 DAY),
                    false,
                    false,
                    false,
                    0.00,
                    (SELECT id          FROM students   WHERE matricule  = ?),
                    (SELECT id_exemplaire FROM exemplaire WHERE code_barre = ?),
                    ?
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);
            stmt.setString(2, codeBarre);
            stmt.setInt(3, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
    // VALIDATE RETURN
    // Used by LoansController.validateReturn()
    // =====================
    public boolean validateReturn(String codeBarre, boolean isDamaged) {
        String query = """
            UPDATE loans
            SET returned   = true,
                is_delayed = CASE WHEN return_date < CURDATE() THEN true ELSE false END,
                is_damaged = ?,
                penality   = CASE WHEN ? = true THEN 500.00 ELSE 0.00 END
            WHERE id_exemplaire = (
                SELECT id_exemplaire
                FROM exemplaire
                WHERE code_barre = ?
            )
            AND returned = false
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isDamaged);
            stmt.setBoolean(2, isDamaged);
            stmt.setString(3, codeBarre);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Rows updated: " + rowsUpdated); // ← debug
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
    // UPDATE EXEMPLAIRE AVAILABILITY
    // Called after confirmLoan() and validateReturn()
    // =====================
    public boolean setExemplaireDisponible(String codeBarre, boolean disponible) {
        String query = """
                UPDATE exemplaire
                SET disponible = ?
                WHERE code_barre = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, disponible);
            stmt.setString(2, codeBarre);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // =====================
    // GET LOAN INFO BY CODE BARRE
    // Used by LoansController.loadReturnInfo()
    // =====================
    public String getLoanInfoByCodeBarre(String codeBarre) {
        String query = """
                SELECT b.title, s.full_name
                FROM loans l
                JOIN exemplaire e ON l.id_exemplaire = e.id_exemplaire
                JOIN books b      ON e.id_livre       = b.id_book
                JOIN students s   ON l.id_etudiant    = s.id
                WHERE e.code_barre = ?
                  AND l.returned   = false
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, codeBarre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Livre: " + rs.getString("title")
                        + " - Emprunteur: " + rs.getString("full_name");
            }
            return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // =====================
    // GET STUDENT INFO BY MATRICULE
    // Used by LoansController.loadStudentInfo()
    // =====================
    public String getStudentInfo(String matricule) {
        String query = """
                SELECT s.full_name,
                       COUNT(l.id_loan) as active_loans
                FROM students s
                LEFT JOIN loans l ON s.id = l.id_etudiant AND l.returned = false
                WHERE s.matricule = ?
                GROUP BY s.id, s.full_name
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name") + "|" + rs.getInt("active_loans");
            }
            return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    // =====================
// GET BOOK INFO BY CODE BARRE
// Used by LoansController.loadBookInfo()
// =====================
    public String[] getBookInfoByCodeBarre(String codeBarre) {
        String query = """
            SELECT b.title, e.state, e.disponible
            FROM exemplaire e
            JOIN books b ON e.id_livre = b.id_book
            WHERE e.code_barre = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, codeBarre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new String[]{
                        rs.getString("title"),
                        rs.getString("state"),
                        rs.getBoolean("disponible") ? "disponible" : "indisponible"
                };
            }
            return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    // =====================
// GET MONTHLY STATS FOR CHART
// =====================
    public List<int[]> getMonthlyStats() {
        List<int[]> stats = new ArrayList<>();
        String query = """
            SELECT MONTH(loan_date) as month, COUNT(*) as count
            FROM loans
            WHERE YEAR(loan_date) = YEAR(CURDATE())
            GROUP BY MONTH(loan_date)
            ORDER BY MONTH(loan_date)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                stats.add(new int[]{
                        rs.getInt("month"),
                        rs.getInt("count")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // =====================
// GET RECENT LOANS FOR DASHBOARD TABLE
// =====================
    public List<DashboardController.LoanRow> getRecentLoansForDashboard() {
        List<DashboardController.LoanRow> loans = new ArrayList<>();
        String query = """
            SELECT s.full_name, b.title,
                   l.return_date,
                   l.returned, l.is_delayed
            FROM loans l
            JOIN students  s  ON l.id_etudiant    = s.id
            JOIN exemplaire e ON l.id_exemplaire   = e.id_exemplaire
            JOIN books b      ON e.id_livre        = b.id_book
            ORDER BY l.loan_date DESC
            LIMIT 10
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String status;
                if (rs.getBoolean("returned")) {
                    status = "Rendu";
                } else if (rs.getBoolean("is_delayed")) {
                    status = "En retard";
                } else {
                    status = "En cours";
                }

                loans.add(new DashboardController.LoanRow(
                        rs.getString("full_name"),
                        rs.getString("title"),
                        rs.getDate("return_date").toLocalDate().format(formatter),
                        status
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }
}