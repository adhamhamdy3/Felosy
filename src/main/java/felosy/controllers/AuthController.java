package felosy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import felosy.authentication.User;
import felosy.authentication.AuthenticationService;
import felosy.utils.AlertUtil;
import felosy.utils.ValidationUtil;
import felosy.authentication.AuthenticationResponse;
import felosy.authentication.AuthenticationResult;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Controller for handling authentication operations including login and signup.
 *
 * @author Adham Hamdy
 */
public class AuthController implements Initializable {

    // Login form components
    @FXML private TextField username_field;
    @FXML private PasswordField password_field;
    @FXML private Button login_btn;
    @FXML private Text loginErrorText;
    @FXML private Hyperlink signupLink;

    // Signup form components
    @FXML private TextField signupUsernameField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private Button signupButton;
    @FXML private Text signupErrorText;
    @FXML private Hyperlink loginLink;

    // Service for authentication operations
    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Initializes the controller class.
     * @param url The location used to resolve relative paths for the root object
     * @param rb The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Clear any error messages on startup
        if (loginErrorText != null) {
            loginErrorText.setText("");
            loginErrorText.setVisible(false);
        }

        if (signupErrorText != null) {
            signupErrorText.setText("");
            signupErrorText.setVisible(false);
        }
    }

    /**
     * Handles the login button click event.
     * Validates user input and attempts to authenticate the user.
     *
     * @param event The button click event
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Clear previous error messages
        loginErrorText.setText("");
        loginErrorText.setVisible(false);

        try {
            // Validate inputs
            String username = username_field.getText().trim();
            String password = password_field.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showLoginError("Username and password are required");
                return;
            }

            // Attempt to authenticate user
            AuthenticationResponse response = authService.authenticateUser(username, password);

            switch (response.getResult()) {
                case SUCCESS:
                    try {
                        App.setRoot("dashboard");
                    } catch (IOException e) {
                        showLoginError("Error loading dashboard page");
                    }
                    break;

                case INVALID_CREDENTIALS:
                    showLoginError("Invalid username or password");
                    break;

                case ERROR:
                    showLoginError("An error occurred. Please try again later");
                    break;
            }

        } catch (Exception e) {
            // Handle unexpected errors
            showLoginError("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the signup button click event.
     * Validates user input and attempts to create a new user account.
     *
     * @param event The button click event
     */
    @FXML
    public void handleSignup(ActionEvent event) {
        // Clear previous error messages
        signupErrorText.setText("");
        signupErrorText.setVisible(false);

        try {
            // Get and validate input fields
            String username = signupUsernameField.getText().trim();
            String password = signupPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String email = emailField.getText().trim();

            // Validate all fields
            if (!validateSignupFields(username, password, confirmPassword, email)) {
                return; // Validation failed, error already shown
            }

            // Create new user
            User newUser = authService.createUser(username, email, password);

            if (newUser != null) {
                // Show success message
                AlertUtil.showAlert(
                        Alert.AlertType.INFORMATION,
                        "Account Created",
                        "Your account has been created successfully.",
                        "Please log in with your new credentials."
                );

                // Switch to login page
                SceneHandler.switchToLogin();
            } else {
                showSignupError("Failed to create account. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            // Handle validation errors from User class
            showSignupError(e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            showSignupError("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates all signup form fields.
     *
     * @param username The username to validate
     * @param password The password to validate
     * @param confirmPassword The confirmation password to check
     * @param email The email to validate
     * @return true if all validations pass, false otherwise
     */
    private boolean validateSignupFields(String username, String password, String confirmPassword, String email) {
        // Check for empty fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            showSignupError("All fields are required");
            return false;
        }

        // Validate username length
        if (username.length() < 3 || username.length() > 50) {
            showSignupError("Username must be between 3 and 50 characters");
            return false;
        }

        // Check if username already exists
        if (authService.isUsernameTaken(username)) {
            showSignupError("Username already taken");
            return false;
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            showSignupError("Invalid email format");
            return false;
        }

        // Check if email already exists
        if (authService.isEmailTaken(email)) {
            showSignupError("Email already registered");
            return false;
        }

        // Validate password strength
        if (password.length() < 8) {
            showSignupError("Password must be at least 8 characters long");
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showSignupError("Passwords do not match");
            return false;
        }

        return true;
    }

    /**
     * Displays an error message in the login form.
     *
     * @param errorMessage The error message to display
     */
    private void showLoginError(String errorMessage) {
        loginErrorText.setText(errorMessage);
        loginErrorText.setVisible(true);
    }

    /**
     * Displays an error message in the signup form.
     *
     * @param errorMessage The error message to display
     */
    private void showSignupError(String errorMessage) {
        signupErrorText.setText(errorMessage);
        signupErrorText.setVisible(true);
    }

    /**
     * Switches the view to the signup page.
     *
     * @param actionEvent The event that triggered this action
     */
    @FXML
    public void switchToSignUp(ActionEvent actionEvent) {
        SceneHandler.switchToSignUp();
    }

    /**
     * Switches the view to the login page.
     *
     * @param actionEvent The event that triggered this action
     */
    @FXML
    public void switchToLogin(ActionEvent actionEvent) {
        SceneHandler.switchToLogin();
    }

    /**
     * Switches the view to the index/main page.
     * Only accessible after successful authentication.
     *
     * @param actionEvent The event that triggered this action
     */
    @FXML
    public void switchToIndex(ActionEvent actionEvent) {
        SceneHandler.switchToIndex();
    }
}