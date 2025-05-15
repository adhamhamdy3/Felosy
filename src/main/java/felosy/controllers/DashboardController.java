package felosy.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.io.IOException;
import felosy.App;
import javafx.scene.control.Label;
import javafx.scene.text.Text;


public class DashboardController {
    public Label welcomeMsg_label;
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
    public void handleReportsClick(ActionEvent event) {
        javafx.scene.control.Dialog<String[]> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Export Report");
        dialog.setHeaderText("Choose export format and location:");

        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.RadioButton pdfRadio = new javafx.scene.control.RadioButton("PDF");
        javafx.scene.control.RadioButton excelRadio = new javafx.scene.control.RadioButton("Excel");
        pdfRadio.setSelected(true);
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();
        pdfRadio.setToggleGroup(group);
        excelRadio.setToggleGroup(group);

        javafx.scene.control.TextField pathField = new javafx.scene.control.TextField();
        pathField.setPromptText("Choose save location...");
        pathField.setEditable(false);
        javafx.scene.control.Button browseBtn = new javafx.scene.control.Button("Browse");
        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Report");
            if (pdfRadio.isSelected()) {
                fileChooser.getExtensionFilters().setAll(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            } else {
                fileChooser.getExtensionFilters().setAll(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            }
            java.io.File file = fileChooser.showSaveDialog(browseBtn.getScene().getWindow());
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });

        javafx.scene.layout.HBox locationBox = new javafx.scene.layout.HBox(10, pathField, browseBtn);
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, pdfRadio, excelRadio, locationBox);
        vbox.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String format = pdfRadio.isSelected() ? "PDF" : "Excel";
                String path = pathField.getText();
                if (path == null || path.isEmpty()) return null;
                return new String[]{format, path};
            }
            return null;
        });

        java.util.Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(arr -> {
            String format = arr[0];
            String path = arr[1];
            if ("PDF".equalsIgnoreCase(format)) {
                // Get current user
                felosy.authentication.User user = App.getCurrentUser();
                if (user != null) {
                    // Gather all assets for the user
                    String userId = user.getUserId();
                    java.util.List<felosy.assetmanagement.Gold> goldList = new java.util.ArrayList<>(felosy.services.GoldDataService.getInstance().getUserGoldList(userId));
                    java.util.List<felosy.assetmanagement.Stock> stockList = new java.util.ArrayList<>(felosy.services.StockDataService.getInstance().getUserStockList(userId));
                    java.util.List<felosy.assetmanagement.Cryptocurrency> cryptoList = new java.util.ArrayList<>(felosy.services.CryptoDataService.getInstance().getUserCryptoList(userId));
                    java.util.List<felosy.assetmanagement.RealEstate> realEstateList = new java.util.ArrayList<>(felosy.services.RealEstateDataService.getInstance().getUserRealEstateList(userId));

                    // Aggregate all assets into a single portfolio
                    felosy.assetmanagement.Portfolio reportPortfolio = new felosy.assetmanagement.Portfolio(userId);
                    for (felosy.assetmanagement.Gold gold : goldList) {
                        reportPortfolio.addAsset(gold);
                    }
                    for (felosy.assetmanagement.Stock stock : stockList) {
                        reportPortfolio.addAsset(stock);
                    }
                    for (felosy.assetmanagement.Cryptocurrency crypto : cryptoList) {
                        reportPortfolio.addAsset(crypto);
                    }
                    for (felosy.assetmanagement.RealEstate realEstate : realEstateList) {
                        reportPortfolio.addAsset(realEstate);
                    }

                    // Create and export the report
                    felosy.reporting.Report report = new felosy.reporting.Report(
                        java.util.UUID.randomUUID().toString(),
                        "Full User Report",
                        new java.util.Date(),
                        user
                    );
                    report.setPortfolio(reportPortfolio);
                    report.setFormat(felosy.reporting.Report.ReportFormat.PDF);
                    boolean success = report.exportAsPDF(path);
                    if (success) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("PDF report exported successfully to:\n" + path);
                        alert.showAndWait();
                    } else {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to export PDF report.");
                        alert.showAndWait();
                    }
                } else {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("User not found.");
                    alert.showAndWait();
                }
            } else {
                // TODO: Implement Excel export
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Export");
                alert.setHeaderText(null);
                alert.setContentText("Excel export is not implemented yet.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    public void initialize() {
        //welcomeMsg_label.setText("Welcome, " + AuthController.getUsername() + "!");
        if (App.getCurrentUser() != null) {
            welcomeMsg_label.setText("Welcome, " + App.getCurrentUser().getUserName() + "!");
        } else {
            welcomeMsg_label.setText("Welcome!"); // Fallback if user is somehow null
        }
    }
    @FXML
    public void handleLogOutClick(ActionEvent event) {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            showLogoutError("Error loading Main page");
        }
    }

    public void switchToZakatAndCompliance() {
        SceneHandler.switchToZakat();
    }

    public void switchToAssetsInvestments() {
        SceneHandler.switchToAssetsAndInvestments();
    }
}