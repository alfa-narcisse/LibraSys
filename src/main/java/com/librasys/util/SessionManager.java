package com.librasys.util;

public class SessionManager {
    private static int loggedInUserId   = -1;
    private static String loggedInUsername = "";
    private static String loggedInRole    = "";

    public static void login(int userId, String username, String role) {
        loggedInUserId   = userId;
        loggedInUsername = username;
        loggedInRole     = role;
    }

    public static int getUserId() {
        return loggedInUserId;
    }

    public static String getUsername() {
        return loggedInUsername;
    }

    public static String getRole() {
        return loggedInRole;
    }

    public static void logout() {
        loggedInUserId   = -1;
        loggedInUsername = "";
        loggedInRole     = "";
    }
}
