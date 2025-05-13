package felosy.authentication;
import java.io.Serializable;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User class that maintains basic user information and wealth data
 * with robust defensive programming and error handling
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Basic user information
    private String userId;
    private String userName;
    private String email;
    private String passwordHash; // Store hashed password
    private double currentWealth; // Added current wealth field

    // Constants for validation
    private static final double MIN_WEALTH = 0.0;
    private static final double MAX_WEALTH = Double.MAX_VALUE;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;

    /**
     * Constructor with validation for all fields
     *
     * @param userName User's name (must be between 3-50 characters)
     * @param email User's email address (must be valid format)
     * @param password User's plaintext password (will be hashed)
     * @param currentWealth User's current wealth amount (must be non-negative)
     * @throws IllegalArgumentException if any validation check fails
     */
    public User(String userName, String email, String password, double currentWealth) {
        // Validate all inputs before assignment
        validateUserName(userName);
        validateEmail(email);
        validatePassword(password);
        validateWealth(currentWealth);

        // Once validation passes, assign values
        this.userId = generateUniqueUserId();
        this.userName = userName;
        this.email = email;
        this.passwordHash = hashPassword(password);
        this.currentWealth = currentWealth;
    }

    /**
     * Basic constructor with default wealth value of 0.0
     */
    public User(String userName, String email, String password) {
        this(userName, email, password, 0.0);
    }

    /**
     * Generates a unique user ID
     *
     * @return A unique user ID string
     */
    private String generateUniqueUserId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validates user name
     *
     * @param userName The user name to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserName(String userName) {
        if (userName == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        userName = userName.trim();

        if (userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (userName.length() < MIN_USERNAME_LENGTH || userName.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username must be between " +
                    MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters");
        }
    }

    /**
     * Validates email address format
     *
     * @param email The email address to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        email = email.trim();

        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Basic email validation (contains @ and at least one dot after @)
        if (!email.contains("@") || email.indexOf('@') > email.lastIndexOf('.')) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validates password
     *
     * @param password The password to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    /**
     * Validates wealth amount
     *
     * @param wealth The wealth amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateWealth(double wealth) {
        if (Double.isNaN(wealth)) {
            throw new IllegalArgumentException("Wealth cannot be NaN");
        }

        if (Double.isInfinite(wealth)) {
            throw new IllegalArgumentException("Wealth cannot be infinite");
        }

        if (wealth < MIN_WEALTH) {
            throw new IllegalArgumentException("Wealth cannot be negative");
        }

        if (wealth > MAX_WEALTH) {
            throw new IllegalArgumentException("Wealth exceeds maximum allowed value");
        }
    }

    /**
     * Hash password with SHA-256
     *
     * @param password The plaintext password to hash
     * @return The hashed password as hexadecimal string
     * @throws RuntimeException if the hashing algorithm is not available
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not available", e);
        }
    }

    /**
     * Authenticate a user with password
     *
     * @param password The plaintext password to verify
     * @return true if password matches, false otherwise
     */
    public boolean authenticate(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return passwordHash.equals(hashPassword(password));
    }

    // Getters

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public double getCurrentWealth() {
        return currentWealth;
    }

    // Setters with validation

    public void setUserName(String userName) {
        validateUserName(userName);
        this.userName = userName;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public void setPassword(String password) {
        validatePassword(password);
        this.passwordHash = hashPassword(password);
    }

    public void setCurrentWealth(double wealth) {
        validateWealth(wealth);
        this.currentWealth = wealth;
    }

    /**
     * Add to current wealth (with validation)
     *
     * @param amount Amount to add to current wealth
     * @throws IllegalArgumentException if amount is invalid
     */
    public void addWealth(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Invalid wealth amount");
        }

        double newWealth = currentWealth + amount;

        // Check for overflow
        if (amount > 0 && newWealth < currentWealth) {
            throw new IllegalArgumentException("Wealth addition would cause overflow");
        }

        // Check for minimum bound
        if (newWealth < MIN_WEALTH) {
            throw new IllegalArgumentException("Cannot reduce wealth below " + MIN_WEALTH);
        }

        currentWealth = newWealth;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", currentWealth=" + String.format("$%,.2f", currentWealth) +
                '}';
    }
}