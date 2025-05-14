package felosy.authentication;

import java.util.HashMap;
import java.util.Map;

import felosy.authentication.User;
import felosy.utils.FileStorageUtil;

/**
 * Service class for handling user authentication and registration operations.
 * Provides functionality for user creation, authentication, and validation.
 */
public class AuthenticationService {

    // In-memory storage for users - in a real app, this would be a database
    private Map<String, User> usersByUsername = new HashMap<>();
    private Map<String, User> usersByEmail = new HashMap<>();

    // Singleton instance
    private static AuthenticationService instance;

    // Session info
    private User currentUser;

    /**
     * Constructor loads existing users from storage if available
     */
    public AuthenticationService() {
        // Load users from persistent storage if available
        loadUsers();
    }

    /**
     * Get singleton instance
     */
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }


    /**
     * Creates a new user account
     *
     * @param username User name
     * @param email Email address
     * @param password Password
     * @return The newly created User object
     * @throws IllegalArgumentException if validation fails
     */

    public User createUser(String username, String email, String password) {
        // Check for existing username/email
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (isEmailTaken(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        try {
            // Create new user with default wealth of 0.0
            User newUser = new User(username, email, password);
            newUser.setConfirmed(true);

            // Store user in map
            usersByUsername.put(username.toLowerCase(), newUser);
            usersByEmail.put(email.toLowerCase(), newUser);

            // Save updated users to storage
            saveUsers();

            return newUser;
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors from User class
            throw e;
        } catch (Exception e) {
            // Log any other errors
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticates a user with username and password
     *
     * @param username Username
     * @param password Password
     * @return AuthenticationResponse containing the result, message, and user if successful
     */
    public AuthenticationResponse authenticateUser(String username, String password) {
        User user = usersByUsername.get(username);

        if (user == null) {
            return new AuthenticationResponse(
                    AuthenticationResult.INVALID_CREDENTIALS,
                    "Invalid username or password",
                    null
            );
        }

        if (user.authenticate(password)) {
            currentUser = user;
            return new AuthenticationResponse(
                    AuthenticationResult.SUCCESS,
                    "Login successful",
                    user
            );
        }

        return new AuthenticationResponse(
                AuthenticationResult.INVALID_CREDENTIALS,
                "Invalid username or password",
                null
        );
    }


    /**
     * Checks if a username is already taken
     *
     * @param username Username to check
     * @return true if username is taken, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return usersByUsername.containsKey(username.toLowerCase());
    }

    /**
     * Checks if an email is already registered
     *
     * @param email Email to check
     * @return true if email is registered, false otherwise
     */
    public boolean isEmailTaken(String email) {
        return usersByEmail.containsKey(email.toLowerCase());
    }

    /**
     * Gets the currently logged in user
     *
     * @return The current User object or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Loads users from storage
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try {
            // Load user maps from persistent storage
            Map<String, User> loadedUsersByUsername =
                    (Map<String, User>) FileStorageUtil.loadObject("users_by_username.dat");

            Map<String, User> loadedUsersByEmail =
                    (Map<String, User>) FileStorageUtil.loadObject("users_by_email.dat");

            if (loadedUsersByUsername != null && loadedUsersByEmail != null) {
                usersByUsername = loadedUsersByUsername;
                usersByEmail = loadedUsersByEmail;
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();

            // Initialize empty maps if loading fails
            usersByUsername = new HashMap<>();
            usersByEmail = new HashMap<>();
        }
    }

    /**
     * Saves users to storage
     */
    private void saveUsers() {
        try {
            // Save user maps to persistent storage
            FileStorageUtil.saveObject(usersByUsername, "users_by_username.dat");
            FileStorageUtil.saveObject(usersByEmail, "users_by_email.dat");
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}