package felosy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.authentication.User;
import felosy.authentication.Authentication;
import felosy.utils.AlertUtil;
import felosy.utils.ValidationUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * A controller for authentication views. Coordinates between authentication views and authentication service.
 *
 * @author Adham Hamdy
 */
public class AuthController implements Initializable {
    // Login form components
    @FXML
    public TextField login_username_field;
    @FXML
    public Label login_username_error;

    @FXML
    public TextField login_password_field;
    @FXML
    public Label login_password_error;

    // Signup form components
    @FXML
    public TextField signup_username_field;
    @FXML
    public Label signup_username_error;

    @FXML
    public TextField signup_password_field;
    @FXML
    public Label signup_password_error;

    @FXML
    public TextField signup_confirmPass_field;
    @FXML
    public Label signup_confPass_error;

    @FXML
    public TextField signup_email_field;
    @FXML
    public Label signup_email_error;

    // Navigation buttons
    @FXML
    public Button return_btn;
    @FXML
    public Button signup_btn;
    @FXML
    public Button login_btn;
    @FXML
    public Hyperlink login_link;

    private String sessionToken;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize the authentication system
            Authentication.initialize();
        } catch (Exception e) {
            AlertUtil.showError("Initialization Error: " + e.getMessage());
        }
    }

    /**
     * Handle user signup
     * Validates input fields and registers the user if validation passes
     */
    @FXML
    public void handleSignup(ActionEvent event) {
        try {
            // Reset all error messages
            clearErrorMessages();

            // Get input values
            String username = signup_username_field != null ? signup_username_field.getText() : "";
            String email = signup_email_field != null ? signup_email_field.getText() : "";
            String password = signup_password_field != null ? signup_password_field.getText() : "";
            String confirmPassword = signup_confirmPass_field != null ? signup_confirmPass_field.getText() : "";

            // Validate inputs
            boolean isValid = true;

            // Validate username
            if (!ValidationUtil.isValidUsername(username)) {
                if (signup_username_error != null) {
                    signup_username_error.setText("Username must be 3-50 characters long");
                }
                isValid = false;
            }

            // Validate email
            if (!ValidationUtil.isValidEmail(email)) {
                if (signup_email_error != null) {
                    signup_email_error.setText("Please enter a valid email address");
                }
                isValid = false;
            }

            // Validate password
            if (!ValidationUtil.isStrongPassword(password)) {
                if (signup_password_error != null) {
                    signup_password_error.setText("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
                }
                isValid = false;
            }

            // Validate password confirmation
            if (!password.equals(confirmPassword)) {
                if (signup_confPass_error != null) {
                    signup_confPass_error.setText("Passwords do not match");
                }
                isValid = false;
            }

            // If validation passes, register the user
            if (isValid) {
                try {
                    // First check if the email is already registered before attempting registration
                    if (Authentication.isEmailRegistered(email)) {
                        AlertUtil.showError("Registration Failed: This email is already registered. Please use a different email or try logging in.");
                        return;
                    }

                    User newUser = Authentication.registerUser(username, email, password);

                    // Check if registration was successful - newUser should not be null
                    if (newUser != null) {
                        // Registration successful - Show success message
                        AlertUtil.showInformation("Registration Successful: Your account has been created successfully. You will now be redirected to the dashboard.");

                        // Clear form fields
                        clearSignupFields();

                        // Automatically login the user with the newly created credentials
                        try {
                            sessionToken = Authentication.login(username, password);

                            if (sessionToken != null) {
                                // Switch to Dashboard view
                                if (!SceneHandler.switchToDashboard()) {
                                    // If dashboard switch fails, try to go back to login at least
                                    SceneHandler.switchToLogin();
                                }
                            } else {
                                // Auto-login failed, but account was created
                                AlertUtil.showInformation("Account created successfully. Please log in with your new credentials.");
                                SceneHandler.switchToLogin();
                            }
                        } catch (Exception e) {
                            // Auto-login failed with exception
                            AlertUtil.showWarning("Account created but automatic login failed: " + e.getMessage() + ". Please log in manually.");
                            SceneHandler.switchToLogin();
                        }
                    } else {
                        // General registration failure
                        AlertUtil.showError("Registration Failed: Unable to create account. Please try again later.");
                    }
                } catch (Exception e) {
                    AlertUtil.showError("Registration Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle user login
     * Validates credentials and logs in the user if validation passes
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            // Reset error messages
            if (login_username_error != null) login_username_error.setText("");
            if (login_password_error != null) login_password_error.setText("");

            // Get input values
            String usernameOrEmail = login_username_field != null ? login_username_field.getText() : "";
            String password = login_password_field != null ? login_password_field.getText() : "";

            // Validate inputs
            boolean isValid = true;

            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                if (login_username_error != null) {
                    login_username_error.setText("Please enter your username or email");
                }
                isValid = false;
            }

            if (password == null || password.trim().isEmpty()) {
                if (login_password_error != null) {
                    login_password_error.setText("Please enter your password");
                }
                isValid = false;
            }

            // If validation passes, attempt login
            if (isValid) {
                try {
                    sessionToken = Authentication.login(usernameOrEmail, password);

                    if (sessionToken != null) {
                        // Login successful
                        User currentUser = Authentication.getCurrentUser(sessionToken);
                        if (currentUser != null) {
                            AlertUtil.showInformation("Login Successful: Welcome, " + currentUser.getUserName() + "!");
                        } else {
                            AlertUtil.showInformation("Login Successful: Welcome!");
                        }

                        // Navigate to main application
                        if (!SceneHandler.switchToDashboard()) {
                            // If dashboard switch fails, stay on login screen
                            AlertUtil.showError("Failed to navigate to dashboard. Please try again.");
                        }
                    } else {
                        // Login failed
                        AlertUtil.showError("Login Failed: Invalid username/email or password");
                    }
                } catch (Exception e) {
                    AlertUtil.showError("Login Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Switch to the login view
     */
    @FXML
    public void switchToLogin(ActionEvent event) {
        try {
            SceneHandler.switchToLogin();
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Switch to the index/main view
     */
    @FXML
    public void switchToIndex(ActionEvent event) {
        try {
            SceneHandler.switchToIndex();
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Switch to the Signup view
     */
    @FXML
    public void switchToSignUp(ActionEvent event) {
        try {
            SceneHandler.switchToSignUp();
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Switch to the Dashboard view
     */
    @FXML
    public void switchToDashboard(ActionEvent event) {
        try {
            SceneHandler.switchToDashboard();
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error: " + e.getMessage());
        }
    }

    /**
     * Clear all error messages
     */
    private void clearErrorMessages() {
        // Check if UI elements exist before trying to access them
        if (signup_username_error != null) signup_username_error.setText("");
        if (signup_email_error != null) signup_email_error.setText("");
        if (signup_password_error != null) signup_password_error.setText("");
        if (signup_confPass_error != null) signup_confPass_error.setText("");
        if (login_username_error != null) login_username_error.setText("");
        if (login_password_error != null) login_password_error.setText("");
    }

    /**
     * Clear signup form fields
     */
    private void clearSignupFields() {
        if (signup_username_field != null) signup_username_field.clear();
        if (signup_email_field != null) signup_email_field.clear();
        if (signup_password_field != null) signup_password_field.clear();
        if (signup_confirmPass_field != null) signup_confirmPass_field.clear();
    }
}