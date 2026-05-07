package com.librasys.util;

public class AdminAuthenticator {
    // Default admin password - in production, this should be hashed and stored in database
    private static final String ADMIN_PASSWORD = "admin123";

    /**
     * Verify if the provided password matches the admin password
     * @param password The password to verify
     * @return true if the password is correct
     */
    public static boolean verifyAdminPassword(String password) {
        if (password == null) {
            return false;
        }
        return ADMIN_PASSWORD.equals(password);
    }

    /**
     * Set a new admin password (in production, would hash and store)
     * @param newPassword The new password to set
     */
    public static void setAdminPassword(String newPassword) {
        // In production: hash and store in database
        // For now, just a placeholder
    }
}
