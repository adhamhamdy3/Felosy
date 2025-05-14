package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.Cryptocurrency;
import felosy.assetmanagement.CryptoDataService;
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

    @FXML private Button btn_add;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    @FXML private Button btn_gold;
    @FXML private Button btn_realEstate;
    @FXML private Button btn_Stock;

    private CryptoDataService cryptoDataService = CryptoDataService.getInstance();
    private String currentUserId;
    private ObservableList<Cryptocurrency> cryptoList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserId = App.getCurrentUser().getUserId();
        cryptoList = cryptoDataService.getUserCryptoList(currentUserId);

        setupTable();
        setupComboBox();
        setupButtons();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        coinTypeColumn.setCellValueFactory(new PropertyValueFactory<>("coin"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));

        cryptoTable.setItems(cryptoList);

        cryptoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedCrypto(newSelection);
            }
        });
    }

    private void setupComboBox() {
        coinTypeComboBox.setItems(FXCollections.observableArrayList(Cryptocurrency.CoinType.values()));
    }

    private void setupButtons() {
        btn_gold.setOnAction(e -> switchToGold());
        btn_realEstate.setOnAction(e -> switchToRealEstate());
        btn_Stock.setOnAction(e -> switchToStock());
    }

    private String generateEightDigitId() {
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    @FXML
    private void handleAddCrypto() {
        try {
            String id = generateEightDigitId();
            String name = txt_name.getText();
            Cryptocurrency.CoinType coinType = coinTypeComboBox.getValue();
            BigDecimal amount = new BigDecimal(txt_amount.getText());

            if (name.isEmpty() || coinType == null) {
                showAlert("Error", "Please fill all fields");
                return;
            }

            Cryptocurrency crypto = new Cryptocurrency(id, name, new Date(), BigDecimal.valueOf(250),
                    BigDecimal.valueOf(250), coinType, amount);
            cryptoList.add(crypto);
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount");
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
        try {
            cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
            App.setRoot("dashboard");
        } catch (IOException e) {
            showError("Error returning to dashboard");
        }
    }

    private void switchToGold() {
        try {
            cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
            App.setRoot("AssetsManagement");
        } catch (IOException e) {
            showError("Error switching to Gold management");
        }
    }

    private void switchToRealEstate() {
        try {
            cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
            App.setRoot("RealEstateManagement");
        } catch (IOException e) {
            showError("Error switching to Gold management");
        }
    }

    private void switchToStock() {
        try {
            cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
            App.setRoot("StockManagement");
        } catch (IOException e) {
            showError("Error switching to Gold management");
        }
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