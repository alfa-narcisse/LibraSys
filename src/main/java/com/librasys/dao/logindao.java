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
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


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
            else return null;

        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
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
            else return null;

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
            else return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    public String getFullName(String username) {
        String query = "SELECT name, secondname FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name") + " " + rs.getString("secondname");
            }
            return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }




}
