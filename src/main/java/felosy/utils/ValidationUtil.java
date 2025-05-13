package felosy.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating various types of user input.
 */
public class ValidationUtil {

    // Regular expression for validating email addresses
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Regular expression for validating strong passwords
    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    /**
     * Validates if an email address has a valid format.
     *
     * @param email The email address to validate
     * @return true if the email has a valid format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        // Check for null or empty
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Match against pattern
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates if a username meets the required criteria.
     * Username must be 3-50 characters, alphanumeric with underscores.
     *
     * @param username The username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        // Check for null or empty
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // Check length
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }

        // Check characters (alphanumeric and underscore only)
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Checks if a password is strong.
     * Strong passwords must:
     * - Be at least 8 characters long
     * - Contain at least one digit
     * - Contain at least one lowercase letter
     * - Contain at least one uppercase letter
     * - Contain at least one special character
     * - No whitespace
     *
     * @param password The password to check
     * @return true if the password is strong, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        // Check for null or empty
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Match against pattern
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates if a password meets minimum requirements.
     * Minimum requirements are at least 8 characters.
     *
     * @param password The password to validate
     * @return true if the password meets minimum requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        // Check for null or empty
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Check minimum length
        return password.length() >= 8;
    }

    /**
     * Validates if a numeric string represents a valid positive double value.
     *
     * @param numericString The string to validate
     * @return true if the string is a valid positive double, false otherwise
     */
    public static boolean isValidPositiveDouble(String numericString) {
        try {
            double value = Double.parseDouble(numericString);
            return value >= 0.0 && !Double.isInfinite(value) && !Double.isNaN(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}