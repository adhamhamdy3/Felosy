package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.Stock;
import felosy.services.StockDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import felosy.assetmanagement.TickerType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class StockController implements Initializable {

    // Helper record to hold data from the add/edit stock dialog
    private record StockDialogData(String name, TickerType ticker, String exchange, int shares, BigDecimal pricePerShare, BigDecimal dividendYield, BigDecimal eps) {}
    // Helper record for Buy/Sell dialogs
    private record TransactionDialogData(int shares, BigDecimal pricePerShare) {}

    @FXML private TableView<Stock> stockTable;
    @FXML private TableColumn<Stock, String> idColumn;
    @FXML private TableColumn<Stock, String> nameColumn;
    @FXML private TableColumn<Stock, TickerType> tickerColumn;
    @FXML private TableColumn<Stock, String> exchangeColumn;
    @FXML private TableColumn<Stock, Integer> sharesColumn;
    @FXML private TableColumn<Stock, BigDecimal> totalValueColumn;
    @FXML private TableColumn<Stock, BigDecimal> dividendYieldColumn;
    @FXML private TableColumn<Stock, BigDecimal> epsColumn;

    @FXML private Text errorText;

    @FXML private RadioButton radio_gold;
    @FXML private RadioButton radio_crypto;
    @FXML private RadioButton radio_realEstate;
    @FXML private RadioButton radio_Stock;
    @FXML private Button btn_add;
    @FXML private Button btn_buy;
    @FXML private Button btn_sell;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    @FXML
    private ToggleGroup assetTypeGroup;

    private StockDataService stockDataService = StockDataService.getInstance();
    private String currentUserId;
    private ObservableList<Stock> stockList;

    @FXML private TableColumn<Stock, java.util.Date> purchaseDateColumn;
    @FXML private TableColumn<Stock, BigDecimal> purchasePriceColumn;
    @FXML private TableColumn<Stock, BigDecimal> pricePerShareColumn;
    @FXML private TableColumn<Stock, BigDecimal> peRatioColumn;
    @FXML private TableColumn<Stock, BigDecimal> annualDividendColumn;
    @FXML private TableColumn<Stock, BigDecimal> returnPercentageColumn;
    @FXML private TableColumn<Stock, Void> actionsColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserId = App.getCurrentUser().getUserId();
        stockList = stockDataService.getUserStockList(currentUserId);

        setupTable();
        setupButtons();

        stockTable.setItems(stockList);
        setupActionsColumn();
    }


    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        exchangeColumn.setCellValueFactory(new PropertyValueFactory<>("exchange"));
        sharesColumn.setCellValueFactory(new PropertyValueFactory<>("sharesOwned"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        dividendYieldColumn.setCellValueFactory(new PropertyValueFactory<>("dividendYield"));
        epsColumn.setCellValueFactory(new PropertyValueFactory<>("eps"));

        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        pricePerShareColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().fetchPrice()));
        peRatioColumn.setCellValueFactory(cell -> {
            Stock stock = cell.getValue();
            if (stock != null && stock.getEps() != null && stock.getEps().compareTo(BigDecimal.ZERO) != 0) {
                return new javafx.beans.property.SimpleObjectProperty<>(stock.calculatePERatio());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        annualDividendColumn.setCellValueFactory(cell -> {
            Stock stock = cell.getValue();
            if (stock != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(stock.calculateDividend());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        returnPercentageColumn.setCellValueFactory(cell -> {
            Stock stock = cell.getValue();
            if (stock != null && stock.getPurchasePrice() != null && stock.getPurchasePrice().compareTo(BigDecimal.ZERO) != 0) {
                return new javafx.beans.property.SimpleObjectProperty<>(stock.calculateReturn().multiply(new BigDecimal("100")));
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });

        stockTable.setItems(stockList);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(editButton, deleteButton);

            {
                pane.setSpacing(5);
                editButton.setOnAction(event -> {
                    Stock stock = getTableView().getItems().get(getIndex());
                    handleEditStock(stock);
                });
                deleteButton.setOnAction(event -> {
                    Stock stock = getTableView().getItems().get(getIndex());
                    handleDeleteStockForRow(stock);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupButtons() {
        btn_add.setOnAction(e -> handleAddStock());
        btn_buy.setOnAction(e -> handleBuyShares());
        btn_sell.setOnAction(e -> handleSellShares());
        btn_delete.setOnAction(e -> handleDeleteStock());
        btn_back.setOnAction(e -> handleBack());
        
        assetTypeGroup = new ToggleGroup();
        radio_gold.setToggleGroup(assetTypeGroup);
        radio_crypto.setToggleGroup(assetTypeGroup);
        radio_realEstate.setToggleGroup(assetTypeGroup);
        radio_Stock.setToggleGroup(assetTypeGroup);
        radio_Stock.setSelected(true);
        
        assetTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == radio_gold) {
                switchToGold();
            } else if (newVal == radio_crypto) {
                switchToCrypto();
            } else if (newVal == radio_realEstate) {
                switchToRealEstate();
            }
        });
    }

    private String generateEightDigitId() {
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    private Optional<StockDialogData> showAddStockDialog(Stock existingStock) {
        Dialog<StockDialogData> dialog = new Dialog<>();
        dialog.setTitle(existingStock == null ? "Add New Stock" : "Edit Stock");
        dialog.setHeaderText(existingStock == null ? "Enter the details for the new stock." : "Edit stock details.");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        ComboBox<TickerType> tickerComboBoxDialog = new ComboBox<>(FXCollections.observableArrayList(TickerType.values()));
        tickerComboBoxDialog.setPromptText("Select Ticker");
        TextField exchangeField = new TextField();
        exchangeField.setPromptText("Exchange");
        TextField sharesField = new TextField();
        sharesField.setPromptText("Shares");
        TextField pricePerShareField = new TextField();
        pricePerShareField.setPromptText("Price per Share");
        TextField dividendYieldField = new TextField();
        dividendYieldField.setPromptText("Dividend Yield (e.g., 0.02)");
        TextField epsField = new TextField();
        epsField.setPromptText("EPS");

        if (existingStock != null) {
            nameField.setText(existingStock.getName());
            tickerComboBoxDialog.setValue(existingStock.getTicker());
            exchangeField.setText(existingStock.getExchange());
            sharesField.setText(String.valueOf(existingStock.getSharesOwned()));
            pricePerShareField.setText(existingStock.fetchPrice().toString());
            dividendYieldField.setText(existingStock.getDividendYield().toPlainString());
            epsField.setText(existingStock.getEps().toPlainString());

            if (existingStock != null) {
                sharesField.setEditable(false);
                pricePerShareField.setEditable(false);
            }
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ticker:"), 0, 1);
        grid.add(tickerComboBoxDialog, 1, 1);
        grid.add(new Label("Exchange:"), 0, 2);
        grid.add(exchangeField, 1, 2);
        grid.add(new Label("Shares:"), 0, 3);
        grid.add(sharesField, 1, 3);
        grid.add(new Label("Price per Share:"), 0, 4);
        grid.add(pricePerShareField, 1, 4);
        grid.add(new Label("Dividend Yield:"), 0, 5);
        grid.add(dividendYieldField, 1, 5);
        grid.add(new Label("EPS:"), 0, 6);
        grid.add(epsField, 1, 6);

        dialogPane.setContent(grid);
        javafx.application.Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    TickerType ticker = tickerComboBoxDialog.getValue();
                    String exchange = exchangeField.getText();
                    int shares = existingStock != null ? existingStock.getSharesOwned() : Integer.parseInt(sharesField.getText());
                    BigDecimal pricePerShare = existingStock != null ? existingStock.fetchPrice() : new BigDecimal(pricePerShareField.getText());
                    BigDecimal dividendYield = new BigDecimal(dividendYieldField.getText());
                    BigDecimal eps = new BigDecimal(epsField.getText());

                    if (name.isEmpty() || ticker == null || exchange.isEmpty()) {
                        showAlert("Validation Error", "Name, Ticker, and Exchange cannot be empty.");
                        return null;
                    }
                    if (existingStock == null && (shares <= 0 || pricePerShare.compareTo(BigDecimal.ZERO) <= 0)) {
                        showAlert("Validation Error", "Shares and Price must be positive for new stock.");
                        return null;
                    }
                    if (dividendYield.compareTo(BigDecimal.ZERO) < 0) {
                        showAlert("Validation Error", "Dividend yield cannot be negative.");
                        return null;
                    }

                    return new StockDialogData(name, ticker, exchange, shares, pricePerShare, dividendYield, eps);
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Please enter valid numbers for numeric fields.");
                    return null;
                }
            }
            return null;
        });
        return dialog.showAndWait();
    }

    @FXML
    private void handleAddStock() {
        Optional<StockDialogData> result = showAddStockDialog(null);

        result.ifPresent(data -> {
            try {
                String assetId = generateEightDigitId();
                BigDecimal totalInitialValue = data.pricePerShare().multiply(BigDecimal.valueOf(data.shares()));

                Stock newStock = new Stock(
                        assetId, data.name(), new Date(),
                        totalInitialValue,
                        totalInitialValue,
                        data.ticker(), data.exchange(), data.shares(),
                        data.dividendYield(), data.eps()
                );
                stockList.add(newStock);
                stockDataService.saveUserStockList(currentUserId, stockList);
                stockTable.refresh();
            } catch (IllegalArgumentException e) {
                showAlert("Error Adding Stock", e.getMessage());
            }
        });
    }

    private Optional<TransactionDialogData> showBuySellDialog(String title, String headerText) {
        Dialog<TransactionDialogData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField sharesFieldDialog = new TextField();
        sharesFieldDialog.setPromptText("Quantity");
        TextField priceFieldDialog = new TextField();
        priceFieldDialog.setPromptText("Price per Share");

        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(sharesFieldDialog, 1, 0);
        grid.add(new Label("Price per Share:"), 0, 1);
        grid.add(priceFieldDialog, 1, 1);

        dialogPane.setContent(grid);
        javafx.application.Platform.runLater(sharesFieldDialog::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int shares = Integer.parseInt(sharesFieldDialog.getText());
                    BigDecimal price = new BigDecimal(priceFieldDialog.getText());

                    if (shares <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert("Validation Error", "Shares and Price must be positive.");
                        return null;
                    }
                    return new TransactionDialogData(shares, price);
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Please enter valid numbers for shares and price.");
                    return null;
                }
            }
            return null;
        });
        return dialog.showAndWait();
    }

    @FXML
    private void handleBuyShares() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock to buy shares for.");
            return;
        }

        Optional<TransactionDialogData> result = showBuySellDialog("Buy Shares", "Enter quantity and price for buying shares of " + selectedStock.getName());

        result.ifPresent(data -> {
            try {
                selectedStock.buyShares(data.shares(), data.pricePerShare());
                stockTable.refresh();
                stockDataService.saveUserStockList(currentUserId, stockList);
            } catch (IllegalArgumentException e) {
                showAlert("Error Buying Shares", e.getMessage());
            }
        });
    }

    @FXML
    private void handleSellShares() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock to sell shares from.");
            return;
        }

        Optional<TransactionDialogData> result = showBuySellDialog("Sell Shares", "Enter quantity and price for selling shares of " + selectedStock.getName());

        result.ifPresent(data -> {
            try {
                BigDecimal profit = selectedStock.sellShares(data.shares(), data.pricePerShare());
                stockTable.refresh();
                stockDataService.saveUserStockList(currentUserId, stockList);
                showAlert("Success", "Shares sold. Profit/Loss: " + profit.setScale(2, BigDecimal.ROUND_HALF_UP));
            } catch (IllegalArgumentException e) {
                showAlert("Error Selling Shares", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteStock() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            handleDeleteStockForRow(selectedStock);
        } else {
            showAlert("Error", "Please select a stock to delete");
        }
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
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.toLowerCase().contains("error") || title.toLowerCase().contains("validation")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        } else if (title.toLowerCase().contains("success")) {
            alert.setAlertType(Alert.AlertType.INFORMATION);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleEditStock(Stock stockToEdit) {
        Optional<StockDialogData> result = showAddStockDialog(stockToEdit);

        result.ifPresent(data -> {
            try {
                stockToEdit.setName(data.name());
                stockToEdit.setTicker(data.ticker());
                stockToEdit.setExchange(data.exchange());
                stockToEdit.setDividendYield(data.dividendYield());
                stockToEdit.setEps(data.eps());

                stockTable.refresh();
                stockDataService.saveUserStockList(currentUserId, stockList);
            } catch (IllegalArgumentException e) {
                showAlert("Error Editing Stock", e.getMessage());
            }
        });
    }

    private void handleDeleteStockForRow(Stock stockToDelete) {
        if (stockToDelete != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Deletion");
            confirmDialog.setHeaderText("Delete Stock: " + stockToDelete.getName());
            confirmDialog.setContentText("Are you sure you want to delete this stock? This action cannot be undone.");

            Optional<ButtonType> confirmationResult = confirmDialog.showAndWait();
            if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                stockList.remove(stockToDelete);
                stockDataService.saveUserStockList(currentUserId, stockList);
                stockTable.refresh();
            }
        } else {
            showAlert("Error", "No stock selected for deletion.");
        }
    }
}