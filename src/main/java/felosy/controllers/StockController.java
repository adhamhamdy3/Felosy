package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.Stock;
import felosy.services.StockDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import felosy.assetmanagement.TickerType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    @FXML private TableView<Stock> stockTable;
    @FXML private TableColumn<Stock, String> idColumn;
    @FXML private TableColumn<Stock, String> nameColumn;
    @FXML private TableColumn<Stock, TickerType> tickerColumn;
    @FXML private TableColumn<Stock, String> exchangeColumn;
    @FXML private TableColumn<Stock, Integer> sharesColumn;
    @FXML private TableColumn<Stock, BigDecimal> currentPriceColumn;
    @FXML private TableColumn<Stock, BigDecimal> totalValueColumn;
    @FXML private TableColumn<Stock, BigDecimal> dividendYieldColumn;
    @FXML private TableColumn<Stock, BigDecimal> epsColumn;

    @FXML private TextField txt_name;
    @FXML private ComboBox<TickerType> tickerComboBox;
    @FXML private TextField txt_exchange;
    @FXML private TextField txt_shares;
    @FXML private TextField txt_price;
    @FXML private TextField txt_dividendYield;
    @FXML private TextField txt_eps;
    @FXML private Text errorText;

    @FXML private Button btn_add;
    @FXML private Button btn_buy;
    @FXML private Button btn_sell;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    @FXML private Button btn_gold;
    @FXML private Button btn_crypto;
    @FXML private Button btn_realEstate;

    private StockDataService stockDataService = StockDataService.getInstance();
    private String currentUserId;
    private ObservableList<Stock> stockList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current user's ID from the App class
        currentUserId = App.getCurrentUser().getUserId();

        // Get the user's stock list from the service
        stockList = stockDataService.getUserStockList(currentUserId);

        // Setup UI components
        setupTable();
        setupComboBox();
        setupButtons();

        // Bind the table to the stockList
        stockTable.setItems(stockList);
    }


    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        exchangeColumn.setCellValueFactory(new PropertyValueFactory<>("exchange"));
        sharesColumn.setCellValueFactory(new PropertyValueFactory<>("sharesOwned"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        totalValueColumn.setCellValueFactory(cell -> {
            Stock stock = cell.getValue();
            BigDecimal totalValue = stock.getCurrentValue().multiply(new BigDecimal(stock.getSharesOwned()));
            return new javafx.beans.property.SimpleObjectProperty<>(totalValue);
        });
        dividendYieldColumn.setCellValueFactory(new PropertyValueFactory<>("dividendYield"));
        epsColumn.setCellValueFactory(new PropertyValueFactory<>("eps"));

        stockTable.setItems(stockList);

        stockTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedStock(newSelection);
            }
        });
    }

    private void setupComboBox() {
        tickerComboBox.setItems(FXCollections.observableArrayList(TickerType.values()));
    }

    private void setupButtons() {
        btn_gold.setOnAction(e -> switchToGold());
        btn_crypto.setOnAction(e -> switchToCrypto());
        btn_realEstate.setOnAction(e -> switchToRealEstate());
    }

    private String generateEightDigitId() {
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    @FXML
    private void handleAddStock() {
        try {
            validateInputs();

            String assetId = generateEightDigitId();
            String name = txt_name.getText();
            TickerType ticker = tickerComboBox.getValue();
            String exchange = txt_exchange.getText();
            int shares = Integer.parseInt(txt_shares.getText());
            BigDecimal currentPrice = new BigDecimal(txt_price.getText());
            BigDecimal dividendYield = new BigDecimal(txt_dividendYield.getText());
            BigDecimal eps = new BigDecimal(txt_eps.getText());

            Stock newStock = new Stock(
                    assetId, name, new Date(),
                    currentPrice.multiply(BigDecimal.valueOf(shares)),
                    currentPrice.multiply(BigDecimal.valueOf(shares)),
                    ticker, exchange, shares, dividendYield, eps
            );

            stockList.add(newStock);

            // Save the updated list
            stockDataService.saveUserStockList(currentUserId, stockList);

            clearInputFields();

        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleBuyShares() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            try {
                int shares = Integer.parseInt(txt_shares.getText());
                BigDecimal price = new BigDecimal(txt_price.getText());
                selectedStock.buyShares(shares, price);
                stockTable.refresh();
                clearInputFields();
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numbers for shares and price");
            }
        } else {
            showAlert("Error", "Please select a stock to buy shares");
        }
        stockDataService.saveUserStockList(currentUserId, stockList);
    }

    @FXML
    private void handleSellShares() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            try {
                int shares = Integer.parseInt(txt_shares.getText());
                BigDecimal price = new BigDecimal(txt_price.getText());
                BigDecimal profit = selectedStock.sellShares(shares, price);
                stockTable.refresh();
                clearInputFields();
                showAlert("Success", "Shares sold. Profit/Loss: " + profit);
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numbers for shares and price");
            } catch (IllegalArgumentException e) {
                showAlert("Error", e.getMessage());
            }
        } else {
            showAlert("Error", "Please select a stock to sell shares");
        }
        stockDataService.saveUserStockList(currentUserId, stockList);
    }

    @FXML
    private void handleDeleteStock() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            stockList.remove(selectedStock);
            stockDataService.saveUserStockList(currentUserId, stockList);
            clearInputFields();
        } else {
            showAlert("Error", "Please select a stock to delete");
        }
    }


    private void validateInputs() {
        if (txt_name.getText().isEmpty() || tickerComboBox.getValue() == null ||
                txt_exchange.getText().isEmpty()) {
            throw new IllegalArgumentException("Please fill all required fields");
        }
    }

    private void displaySelectedStock(Stock stock) {
        txt_name.setText(stock.getName());
        tickerComboBox.setValue(stock.getTicker());
        txt_exchange.setText(stock.getExchange());
        txt_shares.setText(String.valueOf(stock.getSharesOwned()));
        txt_price.setText(stock.getCurrentValue().toString());
        txt_dividendYield.setText(stock.getDividendYield().toString());
        txt_eps.setText(stock.getEps().toString());
    }

    private void clearInputFields() {
        txt_name.clear();
        tickerComboBox.setValue(null);
        txt_exchange.clear();
        txt_shares.clear();
        txt_price.clear();
        txt_dividendYield.clear();
        txt_eps.clear();
    }

    @FXML
    private void handleBack() {
        SceneHandler.switchToDashboard();
    }

    private void switchToGold() {
        SceneHandler.switchToAssetsAndInvestments();
    }

    private void switchToCrypto() {
        SceneHandler.switchToCrypto();
    }

    private void switchToRealEstate() {
        SceneHandler.switchToRealEstate();
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