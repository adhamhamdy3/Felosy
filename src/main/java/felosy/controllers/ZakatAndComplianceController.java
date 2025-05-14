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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import felosy.App;
import felosy.assetmanagement.Asset;
import felosy.assetmanagement.Portfolio;
import felosy.storage.DataStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ZakatAndComplianceController implements Initializable {
    @FXML
    private Button selectAssets_box;
    @FXML
    private Label assetsSelected_label;

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., show an error alert)
        }
    }

    private void updateAssetsSelectedLabel(int count) {
        assetsSelected_label.setText(count + " assets selected");
    }
}
