package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.Gold;
import felosy.assetmanagement.GoldDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class AssetsController implements Initializable {

    @FXML private TableView<Gold> goldTable;
    @FXML private TableColumn<Gold, String> idColumn;
    @FXML private TableColumn<Gold, String> nameColumn;
    @FXML private TableColumn<Gold, BigDecimal> gramsColumn;
    @FXML private TableColumn<Gold, BigDecimal> purityColumn;
    @FXML private TableColumn<Gold, BigDecimal> priceColumn;

    @FXML private Button btn_Stock;
    @FXML private Button btn_add;
    @FXML private Button btn_back;
    @FXML private Button btn_crypto;
    @FXML private Button btn_delete;
    @FXML private Button btn_realEstate;
    @FXML private TextField txt_grams;
    @FXML private TextField txt_name;
    @FXML private TextField txt_purity;
    @FXML private Text loginErrorText;

    private GoldDataService goldDataService = GoldDataService.getInstance();
    private String currentUserId; // Will store the current user's ID


    private ObservableList<Gold> goldList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current user ID from authentication service
        currentUserId = App.getCurrentUser().getUserId(); // Assuming you have this method

        setupTable();
        setupButtons();
        // Load user's gold data
        goldList = goldDataService.getUserGoldList(currentUserId);

    }


    private void setupTable() {
        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gramsColumn.setCellValueFactory(new PropertyValueFactory<>("weightGrams"));
        purityColumn.setCellValueFactory(new PropertyValueFactory<>("purity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));

        // Set the items
        goldTable.setItems(goldList);

        // Add selection listener
        goldTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedGold(newSelection);
            }
        });
    }

    private void setupButtons() {
        btn_add.setOnAction(e -> handleAddGold());
        btn_delete.setOnAction(e -> handleDeleteGold());
        btn_back.setOnAction(e -> handleBack());
        btn_crypto.setOnAction(e -> handleCrypto());
        btn_realEstate.setOnAction(e -> handleRealState());
        btn_Stock.setOnAction(e -> handleStock());
    }

    private String generateEightDigitId() {
        // Generate random number between 10000000 and 99999999
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    private void handleCrypto() {
        try {
            // Save data before navigating back
            goldDataService.saveUserGoldList(currentUserId, goldList);
            App.setRoot("CryptoManagement");
        } catch (IOException e) {
            showBackError("Error switching to Crypto management");
        }
    }

    private void handleRealState() {
        try {
            // Save data before navigating back
            goldDataService.saveUserGoldList(currentUserId, goldList);
            App.setRoot("RealEstateManagement");
        } catch (IOException e) {
            showBackError("Error switching to Crypto management");
        }
    }

    private void handleStock() {
        try {
            // Save data before navigating back
            goldDataService.saveUserGoldList(currentUserId, goldList);
            App.setRoot("StockManagement");
        } catch (IOException e) {
            showBackError("Error switching to Crypto management");
        }
    }

    private void handleAddGold() {
        try {
            // Input validation
            if (txt_name.getText().isEmpty()) {
                showAlert("Error", "Name cannot be empty");
                return;
            }

            // Parse inputs
            String name = txt_name.getText();
            BigDecimal grams = new BigDecimal(txt_grams.getText());
            BigDecimal purity = new BigDecimal(txt_purity.getText());

            // Validate purity range
            if (purity.compareTo(BigDecimal.ZERO) < 0 || purity.compareTo(BigDecimal.ONE) > 0) {
                showAlert("Error", "Purity must be between 0 and 1");
                return;
            }

            // Create new Gold asset
            // Note: Using placeholder values for purchase price and current value
            // You should implement proper price calculation based on your requirements
            String assetId = generateEightDigitId();
            Date purchaseDate = new Date();
            BigDecimal purchasePrice = grams.multiply(new BigDecimal("50")); // Example price calculation
            BigDecimal currentValue = purchasePrice; // Initially same as purchase price

            Gold newGold = new Gold(assetId, name, purchaseDate, purchasePrice, currentValue, grams, purity);

            // Add to table
            goldList.add(newGold);

            // Clear input fields
            clearInputFields();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for grams and purity");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void handleDeleteGold() {
        Gold selectedGold = goldTable.getSelectionModel().getSelectedItem();
        if (selectedGold != null) {
            goldList.remove(selectedGold);
            clearInputFields();
        } else {
            showAlert("Error", "Please select an item to delete");
        }
    }

    private void displaySelectedGold(Gold gold) {
        txt_name.setText(gold.getName());
        txt_grams.setText(gold.getWeightGrams().toString());
        txt_purity.setText(gold.getPurity().toString());
    }

    private void clearInputFields() {
        txt_name.clear();
        txt_grams.clear();
        txt_purity.clear();
    }

    private void showBackError(String errorMessage) {
        loginErrorText.setText(errorMessage);
        loginErrorText.setVisible(true);
    }

    @FXML
    private void handleBack() {
        try {
            // Save data before navigating back
            goldDataService.saveUserGoldList(currentUserId, goldList);
            App.setRoot("dashboard");
        } catch (IOException e) {
            showBackError("Error returning to dashboard");
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}