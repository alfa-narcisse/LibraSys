package com.librasys.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import static java.util.logging.Logger.*;


public class DatabaseConnection {
    private static String URL = "jdbc:mysql://localhost:3306/librasys";
    private static String username = "root";
    private static String password = "";
    private static Connection Con;
    private static Logger logger = getLogger(GLOBAL_LOGGER_NAME);

    public static Connection getConnection() throws SQLException {
        try {
            if (Con == null || Con.isClosed()) {
                Con = DriverManager.getConnection(URL, username, password);
                logger.info("Connected to database");

            }
        } catch (SQLException e) {
            logger.info(e.toString());


        }
        return Con;


    }
    public static void closeConnection() throws SQLException {
        if (Con != null || !Con.isClosed()) {
            Con.close();
        }
    }
}