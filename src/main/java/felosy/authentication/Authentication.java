package felosy.authentication;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Duration;

import felosy.storage.DataStorage;

/**
 * Provides comprehensive authentication services for the application
 * including user registration, login, password management, and session handling.
 * Integrates with DataStorage for user persistence.
 */
public class Authentication {
    private static final Logger LOGGER = Logger.getLogger(Authentication.class.getName());
    
    // Session management
    private static final Map<String, UserSession> activeSessions = new HashMap<>();
    private static final int SESSION_EXPIRY_MINUTES = 30;
    
    // Password policy constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final boolean REQUIRE_UPPERCASE = true;
    private static final boolean REQUIRE_LOWERCASE = true;
    private static final boolean REQUIRE_DIGITS = true;
    private static final boolean REQUIRE_SPECIAL_CHARS = true;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    // Random generator for tokens
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Initialize the authentication system
     * Ensures DataStorage is initialized
     */
    public static void initialize() {
        try {
            // Ensure data storage is initialized
            DataStorage.initialize();
            
            // Clean up expired sessions
            cleanupExpiredSessions();
            
            LOGGER.info("Authentication system initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize authentication system", e);
            throw new RuntimeException("Failed to initialize authentication system", e);
        }
    }
    
    /**
     * Register a new user
     * 
     * @param username The username
     * @param email The email address
     * @param password The password
     * @return The newly created user, or null if registration failed
     */
    public static User registerUser(String username, String email, String password) {
        try {
            // Validate inputs
            if (!isValidUsername(username)) {
                LOGGER.warning("Invalid username format during registration: " + username);
                return null;
            }
            
            if (!isValidEmail(email)) {
                LOGGER.warning("Invalid email format during registration: " + email);
                return null;
            }
            
            if (!isValidPassword(password)) {
                LOGGER.warning("Invalid password format during registration");
                return null;
            }
            
            // Check if email is already registered
            if (isEmailRegistered(email)) {
                LOGGER.warning("Email already registered: " + email);
                return null;
            }
            
            // Create new user
            User newUser = new User(username, email, password);
            
            // Save user to storage
            boolean saved = DataStorage.saveUser(newUser);
            if (!saved) {
                LOGGER.severe("Failed to save new user to storage");
                return null;
            }
            
            LOGGER.info("User registered successfully: " + newUser.getUserId());
            return newUser;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during user registration", e);
            return null;
        }
    }
    
    /**
     * Login a user with username/email and password
     * 
     * @param usernameOrEmail The username or email
     * @param password The password
     * @return A session token if login successful, null otherwise
     */
    public static String login(String usernameOrEmail, String password) {
        try {
            if (usernameOrEmail == null || password == null) {
                LOGGER.warning("Null username/email or password during login");
                return null;
            }
            
            // Find user by username or email
            User user = findUserByUsernameOrEmail(usernameOrEmail);
            if (user == null) {
                LOGGER.warning("User not found during login: " + usernameOrEmail);
                return null;
            }
            
            // Authenticate user
            if (!user.authenticate(password)) {
                LOGGER.warning("Invalid password during login for user: " + user.getUserId());
                return null;
            }
            
            // Create session
            String sessionToken = generateSessionToken();
            UserSession session = new UserSession(user.getUserId(), LocalDateTime.now());
            activeSessions.put(sessionToken, session);
            
            LOGGER.info("User logged in successfully: " + user.getUserId());
            return sessionToken;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login", e);
            return null;
        }
    }
    
    /**
     * Logout a user by invalidating their session
     * 
     * @param sessionToken The session token
     * @return true if logout successful, false otherwise
     */
    public static boolean logout(String sessionToken) {
        if (sessionToken == null) {
            return false;
        }
        
        UserSession session = activeSessions.remove(sessionToken);
        if (session != null) {
            LOGGER.info("User logged out successfully: " + session.getUserId());
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the current user from a session token
     * 
     * @param sessionToken The session token
     * @return The user, or null if session is invalid or expired
     */
    public static User getCurrentUser(String sessionToken) {
        if (sessionToken == null) {
            return null;
        }
        
        UserSession session = activeSessions.get(sessionToken);
        if (session == null) {
            return null;
        }
        
        // Check if session is expired
        if (isSessionExpired(session)) {
            activeSessions.remove(sessionToken);
            LOGGER.info("Session expired for user: " + session.getUserId());
            return null;
        }
        
        // Update session timestamp
        session.setLastActivity(LocalDateTime.now());
        
        // Load user from storage
        return DataStorage.loadUser(session.getUserId());
    }
    
    /**
     * Change a user's password
     * 
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return true if password change successful, false otherwise
     */
    public static boolean changePassword(String userId, String currentPassword, String newPassword) {
        try {
            // Validate inputs
            if (userId == null || currentPassword == null || newPassword == null) {
                LOGGER.warning("Null input during password change");
                return false;
            }
            
            // Validate new password
            if (!isValidPassword(newPassword)) {
                LOGGER.warning("Invalid new password format during password change");
                return false;
            }
            
            // Load user
            User user = DataStorage.loadUser(userId);
            if (user == null) {
                LOGGER.warning("User not found during password change: " + userId);
                return false;
            }
            
            // Verify current password
            if (!user.authenticate(currentPassword)) {
                LOGGER.warning("Invalid current password during password change for user: " + userId);
                return false;
            }
            
            // Change password
            user.setPassword(newPassword);
            
            // Save user
            boolean saved = DataStorage.saveUser(user);
            if (!saved) {
                LOGGER.severe("Failed to save user after password change");
                return false;
            }
            
            LOGGER.info("Password changed successfully for user: " + userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during password change", e);
            return false;
        }
    }
    
    /**
     * Reset a user's password (for forgotten passwords)
     * In a real application, this would send an email with a reset link
     * For simplicity, this just resets the password directly
     * 
     * @param email The user's email
     * @param newPassword The new password
     * @return true if password reset successful, false otherwise
     */
    public static boolean resetPassword(String email, String newPassword) {
        try {
            // Validate inputs
            if (email == null || newPassword == null) {
                LOGGER.warning("Null input during password reset");
                return false;
            }
            
            // Validate new password
            if (!isValidPassword(newPassword)) {
                LOGGER.warning("Invalid new password format during password reset");
                return false;
            }
            
            // Find user by email
            User user = findUserByEmail(email);
            if (user == null) {
                LOGGER.warning("User not found during password reset: " + email);
                return false;
            }
            
            // Change password
            user.setPassword(newPassword);
            
            // Save user
            boolean saved = DataStorage.saveUser(user);
            if (!saved) {
                LOGGER.severe("Failed to save user after password reset");
                return false;
            }
            
            LOGGER.info("Password reset successfully for user: " + user.getUserId());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during password reset", e);
            return false;
        }
    }
    
    /**
     * Update a user's profile information
     * 
     * @param userId The user ID
     * @param username The new username (or null to keep current)
     * @param email The new email (or null to keep current)
     * @return true if update successful, false otherwise
     */
    public static boolean updateUserProfile(String userId, String username, String email) {
        try {
            // Load user
            User user = DataStorage.loadUser(userId);
            if (user == null) {
                LOGGER.warning("User not found during profile update: " + userId);
                return false;
            }
            
            // Update username if provided
            if (username != null && !username.trim().isEmpty()) {
                if (!isValidUsername(username)) {
                    LOGGER.warning("Invalid username format during profile update: " + username);
                    return false;
                }
                user.setUserName(username);
            }
            
            // Update email if provided
            if (email != null && !email.trim().isEmpty()) {
                if (!isValidEmail(email)) {
                    LOGGER.warning("Invalid email format during profile update: " + email);
                    return false;
                }
                
                // Check if email is already registered to another user
                User existingUser = findUserByEmail(email);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    LOGGER.warning("Email already registered to another user during profile update: " + email);
                    return false;
                }
                
                user.setEmail(email);
            }
            
            // Save user
            boolean saved = DataStorage.saveUser(user);
            if (!saved) {
                LOGGER.severe("Failed to save user after profile update");
                return false;
            }
            
            LOGGER.info("Profile updated successfully for user: " + userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during profile update", e);
            return false;
        }
    }
    
    /**
     * Delete a user account
     * 
     * @param userId The user ID
     * @param password The user's password (for verification)
     * @return true if deletion successful, false otherwise
     */
    public static boolean deleteUserAccount(String userId, String password) {
        try {
            // Validate inputs
            if (userId == null || password == null) {
                LOGGER.warning("Null input during account deletion");
                return false;
            }
            
            // Load user
            User user = DataStorage.loadUser(userId);
            if (user == null) {
                LOGGER.warning("User not found during account deletion: " + userId);
                return false;
            }
            
            // Verify password
            if (!user.authenticate(password)) {
                LOGGER.warning("Invalid password during account deletion for user: " + userId);
                return false;
            }
            
            // Delete user
            boolean deleted = DataStorage.deleteUser(userId);
            if (!deleted) {
                LOGGER.severe("Failed to delete user account");
                return false;
            }
            
            // Invalidate all sessions for this user
            invalidateUserSessions(userId);
            
            LOGGER.info("User account deleted successfully: " + userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during account deletion", e);
            return false;
        }
    }
    
    /**
     * Check if a username is valid
     * 
     * @param username The username to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        
        username = username.trim();
        
        // Check length
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        
        // Check for invalid characters
        return username.matches("^[a-zA-Z0-9._-]+$");
    }
    
    /**
     * Check if an email is valid
     * 
     * @param email The email to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        
        email = email.trim();
        
        // Check basic format
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Check if a password is valid according to password policy
     * 
     * @param password The password to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        
        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        // Check for uppercase letters if required
        if (REQUIRE_UPPERCASE && !password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Check for lowercase letters if required
        if (REQUIRE_LOWERCASE && !password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Check for digits if required
        if (REQUIRE_DIGITS && !password.matches(".*\\d.*")) {
            return false;
        }
        
        // Check for special characters if required
        if (REQUIRE_SPECIAL_CHARS && !password.matches(".*[^a-zA-Z0-9].*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if an email is already registered
     * 
     * @param email The email to check
     * @return true if already registered, false otherwise
     */
    public static boolean isEmailRegistered(String email) {
        return findUserByEmail(email) != null;
    }
    
    /**
     * Find a user by email
     * 
     * @param email The email to search for
     * @return The user, or null if not found
     */
    public static User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        
        email = email.trim().toLowerCase();
        
        // Load all users
        Map<String, User> users = DataStorage.loadUsers();
        
        // Find user with matching email
        for (User user : users.values()) {
            if (email.equals(user.getEmail().toLowerCase())) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Find a user by username or email
     * 
     * @param usernameOrEmail The username or email to search for
     * @return The user, or null if not found
     */
    public static User findUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null) {
            return null;
        }
        
        usernameOrEmail = usernameOrEmail.trim().toLowerCase();
        
        // Load all users
        Map<String, User> users = DataStorage.loadUsers();
        
        // Find user with matching username or email
        for (User user : users.values()) {
            if (usernameOrEmail.equals(user.getUserName().toLowerCase()) || 
                usernameOrEmail.equals(user.getEmail().toLowerCase())) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Generate a random session token
     * 
     * @return A random session token
     */
    private static String generateSessionToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        
        StringBuilder token = new StringBuilder();
        for (byte b : randomBytes) {
            token.append(String.format("%02x", b));
        }
        
        return token.toString();
    }
    
    /**
     * Check if a session is expired
     * 
     * @param session The session to check
     * @return true if expired, false otherwise
     */
    private static boolean isSessionExpired(UserSession session) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(session.getLastActivity(), now);
        return duration.toMinutes() > SESSION_EXPIRY_MINUTES;
    }
    
    /**
     * Clean up expired sessions
     */
    private static void cleanupExpiredSessions() {
        Iterator<Map.Entry<String, UserSession>> iterator = activeSessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, UserSession> entry = iterator.next();
            if (isSessionExpired(entry.getValue())) {
                iterator.remove();
                LOGGER.fine("Removed expired session for user: " + entry.getValue().getUserId());
            }
        }
    }
    
    /**
     * Invalidate all sessions for a specific user
     * 
     * @param userId The user ID
     */
    private static void invalidateUserSessions(String userId) {
        if (userId == null) {
            return;
        }
        
        Iterator<Map.Entry<String, UserSession>> iterator = activeSessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, UserSession> entry = iterator.next();
            if (userId.equals(entry.getValue().getUserId())) {
                iterator.remove();
                LOGGER.fine("Invalidated session for user: " + userId);
            }
        }
    }
    
    /**
     * Inner class to represent a user session
     */
    private static class UserSession {
        private final String userId;
        private LocalDateTime lastActivity;
        
        public UserSession(String userId, LocalDateTime lastActivity) {
            this.userId = userId;
            this.lastActivity = lastActivity;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public LocalDateTime getLastActivity() {
            return lastActivity;
        }
        
        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
}