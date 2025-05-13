package felosy.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author Adham Hamdy
 */
public class AuthController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void switchToSignUp(ActionEvent actionEvent) {
        SceneHandler.switchToSignUp();
    }

    @FXML
    public void switchToLogin(ActionEvent actionEvent) {
        SceneHandler.switchToLogin();
    }

    @FXML
    public void switchToIndex(ActionEvent actionEvent) {
        SceneHandler.switchToIndex();
    }
}