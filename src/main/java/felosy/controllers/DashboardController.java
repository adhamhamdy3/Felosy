package felosy.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.io.IOException;
import felosy.App;
import javafx.scene.text.Text;


public class DashboardController {
    @FXML
    private Button assetsInvestments_btn;
    @FXML
    private Button zakatComp_btn;
    @FXML
    private Button reports_btn;
    @FXML
    private Button logout_btn;
    @FXML
    private Text logoutErrorText;

    private void showLogoutError(String errorMessage) {
        logoutErrorText.setText(errorMessage);
        logoutErrorText.setVisible(true);
    }

    @FXML
    public void handleAssetsInvestmentsClick(ActionEvent event) {
        // Implement assets and investments button click handler
    }

    @FXML
    public void initialize() {
        // Initialize any dashboard components here
        // This method is called automatically when the FXML is loaded
    }
    @FXML
    public void handleLogOutClick(ActionEvent event) {
        try {
            App.setRoot("index");
        } catch (IOException e) {
            showLogoutError("Error loading Main page");
        }
    }
}