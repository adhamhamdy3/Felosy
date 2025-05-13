package felosy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import felosy.utils.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for handling scene transitions in the application
 *
 * @author Adham Hamdy
 */
public class SceneHandler implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic can be added here if needed
    }

    /**
     * Switch to the Sign Up screen
     *
     * @return boolean indicating if the switch was successful
     */
    public static boolean switchToSignUp() {
        try {
            App.setRoot("signup");
            return true;
        } catch (IOException e) {
            handleSceneError("signup", e);
            return false;
        } catch (Exception e) {
            AlertUtil.showError("Unexpected Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Switch to the Login screen
     *
     * @return boolean indicating if the switch was successful
     */
    public static boolean switchToLogin() {
        try {
            App.setRoot("login");
            return true;
        } catch (IOException e) {
            handleSceneError("login", e);
            return false;
        } catch (Exception e) {
            AlertUtil.showError("Unexpected Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Switch to the Index screen
     *
     * @return boolean indicating if the switch was successful
     */
    public static boolean switchToIndex() {
        try {
            App.setRoot("index");
            return true;
        } catch (IOException e) {
            handleSceneError("index", e);
            return false;
        } catch (Exception e) {
            AlertUtil.showError("Unexpected Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Switch to the Dashboard screen
     *
     * @return boolean indicating if the switch was successful
     */
    public static boolean switchToDashboard() {
        try {
            App.setRoot("dashboard");
            return true;
        } catch (IOException e) {
            handleSceneError("dashboard", e);
            return false;
        } catch (Exception e) {
            AlertUtil.showError("Unexpected Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle errors that occur during scene switching
     *
     * @param sceneName the name of the scene that failed to load
     * @param e the exception that was thrown
     */
    private static void handleSceneError(String sceneName, IOException e) {
        System.err.println("Error loading " + sceneName + " view: " + e.getMessage());
        e.printStackTrace();

        // Display user-friendly error message
        AlertUtil.showError("Navigation Error: Unable to load " + sceneName + " screen. Please restart the application.");

        // Try to fall back to index screen as a recovery measure
        if (!sceneName.equals("index")) {
            try {
                App.setRoot("index");
            } catch (IOException fallbackError) {
                // Critical error - can't even show index screen
                System.err.println("Critical error: Failed to load fallback screen.");
                fallbackError.printStackTrace();
                AlertUtil.showError("Critical Error: Application navigation failure. Please restart the application.");
            }
        }
    }
}