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
    public TextField login_username_field;
    public Label login_username_error;

    public TextField login_password_field;
    public Label login_password_error;

    // Signup form components
    public TextField signup_username_field;
    public Label signup_username_error;

    public TextField signup_password_field;
    public Label signup_password_error;

    public TextField signup_confirmPass_field;
    public Label signup_confPass_error;

    public TextField signup_email_field;
    public Label signup_email_error;

    // Navigation buttons
    public Button return_btn;
    public Button signup_btn;
    public Button login_btn;
    public Hyperlink login_link;

    private String sessionToken;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the authentication system
        Authentication.initialize();
    }

    /**
     * Handle user signup
     * Validates input fields and registers the user if validation passes
     */
    @FXML
    public void handleSignup(ActionEvent event) {
        // Reset all error messages
        clearErrorMessages();
        
        // Get input values
        String username = signup_username_field.getText();
        String email = signup_email_field.getText();
        String password = signup_password_field.getText();
        String confirmPassword = signup_confirmPass_field.getText();
        
        // Validate inputs
        boolean isValid = true;
        
        // Validate username
        if (!ValidationUtil.isValidUsername(username)) {
            signup_username_error.setText("Username must be 3-50 characters long");
            isValid = false;
        }
        
        // Validate email
        if (!ValidationUtil.isValidEmail(email)) {
            signup_email_error.setText("Please enter a valid email address");
            isValid = false;
        }
        
        // Validate password
        if (!ValidationUtil.isStrongPassword(password)) {
            signup_password_error.setText("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
            isValid = false;
        }
        
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            signup_confPass_error.setText("Passwords do not match");
            isValid = false;
        }
        
        // If validation passes, register the user
        if (isValid) {
            User newUser = Authentication.registerUser(username, email, password);
            
            if (newUser != null) {
                // Registration successful
                AlertUtil.showInformation("Registration Successful: Your account has been created successfully. Please log in.");
                
                // Clear form fields
                clearSignupFields();
                
                // Switch to login view
                try {
                    switchToLogin(event);
                } catch (IOException e) {
                    AlertUtil.showError("Navigation Error: " + e.getMessage());
                }
            } else {
                // Registration failed
                AlertUtil.showError("Registration Failed: This email may already be registered or there was a system error.");
            }
        }
    }
    
    /**
     * Handle user login
     * Validates credentials and logs in the user if validation passes
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Reset error messages
        login_username_error.setText("");
        login_password_error.setText("");
        
        // Get input values
        String usernameOrEmail = login_username_field.getText();
        String password = login_password_field.getText();
        
        // Validate inputs
        boolean isValid = true;
        
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            login_username_error.setText("Please enter your username or email");
            isValid = false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            login_password_error.setText("Please enter your password");
            isValid = false;
        }
        
        // If validation passes, attempt login
        if (isValid) {
            sessionToken = Authentication.login(usernameOrEmail, password);
            
            if (sessionToken != null) {
                // Login successful
                User currentUser = Authentication.getCurrentUser(sessionToken);
                AlertUtil.showInformation("Login Successful: Welcome, " + currentUser.getUserName() + "!");
                
                // Navigate to main application
                try {
                    switchToIndex(event);
                } catch (IOException e) {
                    AlertUtil.showError("Navigation Error: " + e.getMessage());
                }
            } else {
                // Login failed
                AlertUtil.showError("Login Failed: Invalid username/email or password");
            }
        }
    }
    
    /**
     * Switch to the login view
     */
    @FXML
    public void switchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Switch to the index/main view
     */
    @FXML
    public void switchToIndex(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/index.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Clear all error messages
     */
    private void clearErrorMessages() {
        signup_username_error.setText("");
        signup_email_error.setText("");
        signup_password_error.setText("");
        signup_confPass_error.setText("");
        login_username_error.setText("");
        login_password_error.setText("");
    }
    
    /**
     * Clear signup form fields
     */
    private void clearSignupFields() {
        signup_username_field.clear();
        signup_email_field.clear();
        signup_password_field.clear();
        signup_confirmPass_field.clear();
    }
}