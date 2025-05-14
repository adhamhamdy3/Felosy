package felosy.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import felosy.assetmanagement.Asset;
import felosy.assetmanagement.Portfolio;
import felosy.storage.DataStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import felosy.islamicfinance.ZakatCalculator;
import java.util.List;
import java.util.ArrayList;

public class ZakatAndComplianceController implements Initializable {
    @FXML
    private Button selectAssets_box;
    @FXML
    private Label assetsSelected_label;
    @FXML
    private Button calculate_btn;
    @FXML
    private ComboBox<String> currency_combobox;
    @FXML
    private Label curr_per_gram_label;
    @FXML
    private Label curr_per_silver;
    @FXML
    private TextField gold_field;
    @FXML
    private TextField silver_field;

    private List<Asset> selectedAssets = new ArrayList<>();

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        calculate_btn.setOnAction(this::handleCalculateButton);
        // Populate currency combo box
        currency_combobox.getItems().addAll(
            "EGP", "USD", "EUR", "GBP", "JPY", "SAR", "AED", "CAD", "AUD", "CHF", "INR", "CNY"
        );
        currency_combobox.setValue("EGP");
        currency_combobox.setOnAction(e -> updateGoldSilverLabels());
        updateGoldSilverLabels();
    }

    public void switchToDashboard() {
        SceneHandler.switchToDashboard();
    }

    @FXML
    private void handleSelectAssetsButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/select_assets_dialog.fxml"));
            Parent root = loader.load();

            // Get controller for the dialog
            felosy.controllers.SelectAssetsDialogController dialogController = loader.getController();

            // Load all asset types for the current user
            String userId = App.getCurrentUser() != null ? App.getCurrentUser().getUserId() : null;
            javafx.collections.ObservableList<felosy.assetmanagement.Asset> allAssets = javafx.collections.FXCollections.observableArrayList();
            if (userId != null) {
                // Gold
                for (felosy.assetmanagement.Gold gold : felosy.services.GoldDataService.getInstance().getUserGoldList(userId)) {
                    allAssets.add(gold);
                }
                // Stock
                for (felosy.assetmanagement.Stock stock : felosy.services.StockDataService.getInstance().getUserStockList(userId)) {
                    allAssets.add(stock);
                }
                // Crypto
                for (felosy.assetmanagement.Cryptocurrency crypto : felosy.services.CryptoDataService.getInstance().getUserCryptoList(userId)) {
                    allAssets.add(crypto);
                }
                // Real Estate
                for (felosy.assetmanagement.RealEstate realEstate : felosy.services.RealEstateDataService.getInstance().getUserRealEstateList(userId)) {
                    allAssets.add(realEstate);
                }
            }
            dialogController.loadAssets(allAssets);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Assets");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Block other windows
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            dialogStage.showAndWait(); // Show dialog and wait for it to be closed

            // After dialog closes, update the label with the number of selected assets
            int selectedCount = dialogController.getSelectedAssets().size();
            updateAssetsSelectedLabel(selectedCount);
            // Store selected assets for calculation
            selectedAssets = new ArrayList<>(dialogController.getSelectedAssets());

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., show an error alert)
        }
    }

    @FXML
    private void handleCalculateButton(ActionEvent event) {
        if (selectedAssets == null || selectedAssets.isEmpty()) {
            System.out.println("No assets selected for Zakat calculation.");
            gold_field.setText("0.00");
            silver_field.setText("0.00");
            return;
        }
        ZakatCalculator calculator = new ZakatCalculator("user-portfolio", selectedAssets);
        float totalZakat = calculator.calculateZakat();
        System.out.println("Total Zakat Due: " + totalZakat);
        // Print breakdown
        java.util.Map<String, Float> zakatByAsset = calculator.getZakatByAsset();
        zakatByAsset.forEach((type, amount) -> {
            System.out.println(type + " Zakat: " + amount);
        });
        // Set gold and silver fields to Stock and Cryptocurrency Zakat
        float stockZakat = 0.0f;
        float cryptoZakat = 0.0f;
        for (java.util.Map.Entry<String, Float> entry : zakatByAsset.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Stock")) {
                stockZakat += entry.getValue();
            } else if (entry.getKey().equalsIgnoreCase("Cryptocurrency")) {
                cryptoZakat += entry.getValue();
            }
        }
        gold_field.setText(String.format("%.2f", stockZakat));
        silver_field.setText(String.format("%.2f", cryptoZakat));
        // TODO: Display results in the UI (e.g., in a label or dialog)
    }

    private void updateAssetsSelectedLabel(int count) {
        assetsSelected_label.setText(count + " assets selected");
    }

    private void updateGoldSilverLabels() {
        String currency = currency_combobox.getValue();
        // Mock prices per gram (could be fetched from a service)
        double goldPriceUSD = 65.0; // USD/gram
        double silverPriceUSD = 0.8; // USD/gram
        double rate = 1.0;
        String symbol = "$";
        switch (currency) {
            case "EGP":
                rate = 48.0; symbol = "E£"; break;
            case "EUR":
                rate = 0.92; symbol = "€"; break;
            case "GBP":
                rate = 0.79; symbol = "£"; break;
            case "JPY":
                rate = 157.0; symbol = "¥"; break;
            case "SAR":
                rate = 3.75; symbol = "ر.س"; break;
            case "AED":
                rate = 3.67; symbol = "د.إ"; break;
            case "CAD":
                rate = 1.36; symbol = "C$"; break;
            case "AUD":
                rate = 1.52; symbol = "A$"; break;
            case "CHF":
                rate = 0.90; symbol = "Fr"; break;
            case "INR":
                rate = 83.0; symbol = "₹"; break;
            case "CNY":
                rate = 7.2; symbol = "¥"; break;
            case "USD":
            default:
                rate = 1.0; symbol = "$"; break;
        }
        double goldPrice = goldPriceUSD * rate;
        double silverPrice = silverPriceUSD * rate;
        curr_per_gram_label.setText(String.format("%.2f %s / gram", goldPrice, symbol));
        curr_per_silver.setText(String.format("%.2f %s / gram", silverPrice, symbol));
    }
}
