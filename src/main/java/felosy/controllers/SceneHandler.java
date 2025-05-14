package felosy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Adham Hamdy
 */
public class SceneHandler implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public static void switchToSignUp() {
        try {
            App.setRoot("signup");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToLogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToIndex() {
        try {
            App.setRoot("index");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToZakat() {
        try {
            App.setRoot("zakat_and_compliance");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToAssetsAndInvestments() {
        try {
            App.setRoot("gold");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToRealEstate() {
        try {
            App.setRoot("real_estate");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToStock() {
        try {
            App.setRoot("stocks");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchToCrypto() {
        try {
            App.setRoot("crypto");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}