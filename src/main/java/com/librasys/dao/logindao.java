package com.librasys.dao;

import com.librasys.util.DatabaseConnection;

import java.sql.*;

public class logindao {

    public int getUserId(String username) {
        String query = "SELECT id_user FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_user");
            }
            DatabaseConnection.closeConnection();
            return -1;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

// Methode qui permet d'obtenir le nom d'utilisateur et qui permet de vérifier si l'utilisateur est déja enregistré
    public String getusername(String usernameField){
        String query = """
                SELECT username FROM users WHERE username = ? and active =TRUE
        """;
        try (Connection Con = DatabaseConnection.getConnection();
            PreparedStatement Ps = Con.prepareStatement(query);
            ){
            Ps.setString(1, usernameField);
            ResultSet rs = Ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");

            }

            else {
                DatabaseConnection.closeConnection();
                return null;}


        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    // Methode qui permet d'obtenir le mot de passe à partir du nom d'utilisateur
    public String getpassword(String usernameField){
        String query = """
                SELECT password FROM users WHERE username = ? and active =TRUE
        """;
        try(Connection Con = DatabaseConnection.getConnection();
            PreparedStatement Ps = Con.prepareStatement(query);){
            Ps.setString(1, usernameField);
            ResultSet rs = Ps.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
            else {
                DatabaseConnection.closeConnection();
                return null;}

        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return null;
        }

    }
    public String getRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                return rs.getString("role");
            }
            else {
                DatabaseConnection.closeConnection();
                return null;}

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
