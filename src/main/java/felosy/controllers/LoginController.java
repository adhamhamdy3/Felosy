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
public class LoginController implements Initializable {

    public Hyperlink signup_btn;
    public TextField username_field;
    public TextField password_field;
    public Button login_btn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void switchToSignUp(ActionEvent actionEvent) {
        try {
            App.setRoot("signup");
        } catch (IOException e) {
            System.err.println("Error loading signup view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}