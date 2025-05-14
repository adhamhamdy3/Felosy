package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.Cryptocurrency;
import felosy.services.CryptoDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class CryptoController implements Initializable {

    @FXML private TableView<Cryptocurrency> cryptoTable;
    @FXML private TableColumn<Cryptocurrency, String> idColumn;
    @FXML private TableColumn<Cryptocurrency, String> nameColumn;
    @FXML private TableColumn<Cryptocurrency, Cryptocurrency.CoinType> coinTypeColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> amountColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> priceColumn;

    @FXML private TextField txt_name;
    @FXML private ComboBox<Cryptocurrency.CoinType> coinTypeComboBox;
    @FXML private TextField txt_amount;
    @FXML private Text errorText;

    @FXML private RadioButton radio_gold;
    @FXML private RadioButton radio_crypto;
    @FXML private RadioButton radio_realEstate;
    @FXML private RadioButton radio_Stock;
    @FXML private Button btn_add;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    private ToggleGroup assetTypeGroup;

    private CryptoDataService cryptoDataService = CryptoDataService.getInstance();
    private String currentUserId;
    private ObservableList<Cryptocurrency> cryptoList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current user's ID from the App class
        currentUserId = App.getCurrentUser().getUserId();

        // Get the user's crypto list from the service
        cryptoList = cryptoDataService.getUserCryptoList(currentUserId);

        // Set up the UI components
        setupTable();
        setupComboBox();
        setupButtons();

        // Bind the table to the cryptoList
        cryptoTable.setItems(cryptoList);
    }


    private void setupTable() {
        // Configure the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        coinTypeColumn.setCellValueFactory(new PropertyValueFactory<>("coinType"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
    }


    private void setupComboBox() {
        coinTypeComboBox.setItems(FXCollections.observableArrayList(Cryptocurrency.CoinType.values()));
    }

    private void setupButtons() {
        // Setup radio buttons with toggle group
        assetTypeGroup = new ToggleGroup();
        radio_gold.setToggleGroup(assetTypeGroup);
        radio_crypto.setToggleGroup(assetTypeGroup);
        radio_realEstate.setToggleGroup(assetTypeGroup);
        radio_Stock.setToggleGroup(assetTypeGroup);
        radio_crypto.setSelected(true);
        
        // Add listener to handle navigation when radio selection changes
        assetTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == radio_gold) {
                switchToGold();
            } else if (newVal == radio_realEstate) {
                switchToRealEstate();
            } else if (newVal == radio_Stock) {
                switchToStock();
            }
        });
    }

    private String generateEightDigitId() {
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    @FXML
    private void handleAddCrypto() {
        try {
            // Input validation
            if (txt_name.getText().isEmpty()) {
                showAlert("Error", "Name cannot be empty");
                return;
            }

            if (coinTypeComboBox.getValue() == null) {
                showAlert("Error", "Please select a coin type");
                return;
            }

            // Parse inputs
            String name = txt_name.getText();
            Cryptocurrency.CoinType coinType = coinTypeComboBox.getValue();
            BigDecimal amount = new BigDecimal(txt_amount.getText());

            // Create new Cryptocurrency asset
            String assetId = generateEightDigitId();
            Date purchaseDate = new Date();
            BigDecimal purchasePrice = amount.multiply(new BigDecimal("50000")); // Example price
            BigDecimal currentValue = purchasePrice; // Initially same as purchase price

            Cryptocurrency newCrypto = new Cryptocurrency(
                    assetId,          // assetId
                    name,            // name
                    purchaseDate,    // purchaseDate
                    purchasePrice,   // purchasePrice
                    currentValue,    // currentValue
                    coinType,        // coinType
                    amount          // amount
            );

            // Add to the observable list
            cryptoList.add(newCrypto);

            // Save the updated list to the service
            cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);

            // Clear input fields
            clearInputFields();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCrypto() {
        Cryptocurrency selectedCrypto = cryptoTable.getSelectionModel().getSelectedItem();
        if (selectedCrypto != null) {
            cryptoList.remove(selectedCrypto);
            clearInputFields();
        } else {
            showAlert("Error", "Please select a cryptocurrency to delete");
        }
    }

    private void displaySelectedCrypto(Cryptocurrency crypto) {
        txt_name.setText(crypto.getName());
        coinTypeComboBox.setValue(crypto.getCoin());
        txt_amount.setText(crypto.getAmount().toString());
    }

    private void clearInputFields() {
        txt_name.clear();
        coinTypeComboBox.setValue(null);
        txt_amount.clear();
    }

    @FXML
    private void handleBack() {
        SceneHandler.switchToDashboard();
    }

    private void switchToGold() {
        SceneHandler.switchToAssetsAndInvestments();
    }

    private void switchToRealEstate() {
        SceneHandler.switchToRealEstate();
    }

    private void switchToStock() {
        SceneHandler.switchToStock();
    }

    private void showError(String errorMessage) {
        errorText.setText(errorMessage);
        errorText.setVisible(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}