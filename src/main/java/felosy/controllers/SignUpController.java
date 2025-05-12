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
public class SignUpController implements Initializable {

    public TextField username_field;
    public TextField password_field;
    public TextField confirmPass_field;
    public TextField email_field;
    public Button signup_btn;
    public Hyperlink login_btn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void switchToLogin(ActionEvent actionEvent) {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}