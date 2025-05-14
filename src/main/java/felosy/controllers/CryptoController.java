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
import javafx.scene.layout.GridPane;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class CryptoController implements Initializable {

    @FXML private TableView<Cryptocurrency> cryptoTable;
    @FXML private TableColumn<Cryptocurrency, String> idColumn;
    @FXML private TableColumn<Cryptocurrency, String> nameColumn;
    @FXML private TableColumn<Cryptocurrency, Cryptocurrency.CoinType> coinTypeColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> amountColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> priceColumn;
    @FXML private TableColumn<Cryptocurrency, Date> purchaseDateColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> purchasePriceColumn;
    @FXML private TableColumn<Cryptocurrency, BigDecimal> returnColumn;
    @FXML private TableColumn<Cryptocurrency, Void> actionsColumn;

    @FXML private Text errorText;

    @FXML private RadioButton radio_gold;
    @FXML private RadioButton radio_crypto;
    @FXML private RadioButton radio_realEstate;
    @FXML private RadioButton radio_Stock;
    @FXML private Button btn_add;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    @FXML
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
        setupButtons();

        // Bind the table to the cryptoList
        cryptoTable.setItems(cryptoList);
    }

    private void setupTable() {
        // Configure the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        coinTypeColumn.setCellValueFactory(new PropertyValueFactory<>("coin"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        returnColumn.setCellValueFactory(new PropertyValueFactory<>("return"));

        // Setup actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    Cryptocurrency crypto = getTableView().getItems().get(getIndex());
                    showEditDialog(crypto);
                });

                deleteButton.setOnAction(event -> {
                    Cryptocurrency crypto = getTableView().getItems().get(getIndex());
                    cryptoList.remove(crypto);
                    cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        // Format the date column
        purchaseDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
                }
            }
        });

        // Format the return column as percentage
        returnColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", value.multiply(new BigDecimal("100"))));
                }
            }
        });

        // Format the coin type column
        coinTypeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Cryptocurrency.CoinType coinType, boolean empty) {
                super.updateItem(coinType, empty);
                if (empty || coinType == null) {
                    setText(null);
                } else {
                    setText(coinType.toString());
                }
            }
        });
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
            // Create dialog for cryptocurrency input
            Dialog<Cryptocurrency> dialog = new Dialog<>();
            dialog.setTitle("Add Cryptocurrency");
            dialog.setHeaderText("Enter Cryptocurrency Details");

            // Set the button types
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the custom dialog content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            // Create input fields
            TextField nameField = new TextField();
            nameField.setPromptText("Name");
            
            ComboBox<Cryptocurrency.CoinType> coinTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(Cryptocurrency.CoinType.values()));
            coinTypeComboBox.setPromptText("Select Coin Type");
            
            TextField amountField = new TextField();
            amountField.setPromptText("Amount");
            
            TextField currentPriceField = new TextField();
            currentPriceField.setPromptText("Current Price");
            
            DatePicker purchaseDatePicker = new DatePicker();
            purchaseDatePicker.setPromptText("Purchase Date");
            
            TextField purchasePriceField = new TextField();
            purchasePriceField.setPromptText("Purchase Price");

            // Add fields to grid
            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            
            grid.add(new Label("Coin Type:"), 0, 1);
            grid.add(coinTypeComboBox, 1, 1);
            
            grid.add(new Label("Amount:"), 0, 2);
            grid.add(amountField, 1, 2);
            
            grid.add(new Label("Current Price:"), 0, 3);
            grid.add(currentPriceField, 1, 3);
            
            grid.add(new Label("Purchase Date:"), 0, 4);
            grid.add(purchaseDatePicker, 1, 4);
            
            grid.add(new Label("Purchase Price:"), 0, 5);
            grid.add(purchasePriceField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the name field by default
            nameField.requestFocus();

            // Convert the result to a Cryptocurrency when the add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        // Validate required fields
                        if (nameField.getText().isEmpty()) {
                            showAlert("Error", "Name cannot be empty");
                            return null;
                        }
                        if (coinTypeComboBox.getValue() == null) {
                            showAlert("Error", "Please select a coin type");
                            return null;
                        }
                        if (amountField.getText().isEmpty()) {
                            showAlert("Error", "Amount cannot be empty");
                            return null;
                        }
                        if (currentPriceField.getText().isEmpty()) {
                            showAlert("Error", "Current price cannot be empty");
                            return null;
                        }
                        if (purchaseDatePicker.getValue() == null) {
                            showAlert("Error", "Purchase date cannot be empty");
                            return null;
                        }
                        if (purchasePriceField.getText().isEmpty()) {
                            showAlert("Error", "Purchase price cannot be empty");
                            return null;
                        }

                        // Parse inputs
                        String name = nameField.getText();
                        Cryptocurrency.CoinType coinType = coinTypeComboBox.getValue();
                        BigDecimal amount = new BigDecimal(amountField.getText());
                        BigDecimal currentPrice = new BigDecimal(currentPriceField.getText());
                        Date purchaseDate = java.sql.Date.valueOf(purchaseDatePicker.getValue());
                        BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText());

                        // Validate numeric values
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Amount must be greater than 0");
                            return null;
                        }
                        if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Current price must be greater than 0");
                            return null;
                        }
                        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Purchase price must be greater than 0");
                            return null;
                        }

                        String assetId = generateEightDigitId();
                        BigDecimal currentValue = currentPrice.multiply(amount);

                        return new Cryptocurrency(
                            assetId,
                            name,
                            purchaseDate,
                            purchasePrice,
                            currentValue,
                            coinType,
                            amount
                        );
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Please enter valid numeric values for amount and prices");
                        return null;
                    } catch (IllegalArgumentException e) {
                        showAlert("Error", e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Cryptocurrency> result = dialog.showAndWait();
            result.ifPresent(crypto -> {
                cryptoList.add(crypto);
                cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
            });

        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCrypto() {
        Cryptocurrency selectedCrypto = cryptoTable.getSelectionModel().getSelectedItem();
        if (selectedCrypto != null) {
            cryptoList.remove(selectedCrypto);
        } else {
            showAlert("Error", "Please select a cryptocurrency to delete");
        }
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

    private void showEditDialog(Cryptocurrency crypto) {
        try {
            Dialog<Cryptocurrency> dialog = new Dialog<>();
            dialog.setTitle("Edit Cryptocurrency");
            dialog.setHeaderText("Edit Cryptocurrency Details");
            dialog.getDialogPane().getStyleClass().add("dialog-pane");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the custom dialog content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            grid.getStyleClass().add("dialog-grid");

            // Create input fields with labels
            Label nameLabel = new Label("Name:");
            nameLabel.getStyleClass().add("dialog-label");
            TextField nameField = new TextField(crypto.getName());
            nameField.setPromptText("Enter name");
            nameField.getStyleClass().add("dialog-field");
            
            Label coinTypeLabel = new Label("Coin Type:");
            coinTypeLabel.getStyleClass().add("dialog-label");
            ComboBox<Cryptocurrency.CoinType> coinTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(Cryptocurrency.CoinType.values()));
            coinTypeComboBox.setValue(crypto.getCoin());
            coinTypeComboBox.setPromptText("Select coin type");
            coinTypeComboBox.getStyleClass().add("dialog-field");
            
            Label amountLabel = new Label("Amount:");
            amountLabel.getStyleClass().add("dialog-label");
            TextField amountField = new TextField(crypto.getAmount().toString());
            amountField.setPromptText("Enter amount");
            amountField.getStyleClass().add("dialog-field");
            
            Label currentPriceLabel = new Label("Current Price:");
            currentPriceLabel.getStyleClass().add("dialog-label");
            TextField currentPriceField = new TextField(crypto.getCurrentValue().divide(crypto.getAmount()).toString());
            currentPriceField.setPromptText("Enter current price");
            currentPriceField.getStyleClass().add("dialog-field");
            
            Label purchaseDateLabel = new Label("Purchase Date:");
            purchaseDateLabel.getStyleClass().add("dialog-label");
            DatePicker purchaseDatePicker = new DatePicker();
            try {
                // Convert Date to LocalDate safely
                java.time.Instant instant = crypto.getPurchaseDate().toInstant();
                java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
                java.time.LocalDate localDate = instant.atZone(zoneId).toLocalDate();
                purchaseDatePicker.setValue(localDate);
            } catch (Exception e) {
                // If conversion fails, set to current date
                purchaseDatePicker.setValue(java.time.LocalDate.now());
            }
            purchaseDatePicker.setPromptText("Select purchase date");
            purchaseDatePicker.getStyleClass().add("dialog-field");
            
            Label purchasePriceLabel = new Label("Purchase Price:");
            purchasePriceLabel.getStyleClass().add("dialog-label");
            TextField purchasePriceField = new TextField(crypto.getPurchasePrice().toString());
            purchasePriceField.setPromptText("Enter purchase price");
            purchasePriceField.getStyleClass().add("dialog-field");

            // Add fields to grid with proper spacing
            grid.add(nameLabel, 0, 0);
            grid.add(nameField, 1, 0);
            
            grid.add(coinTypeLabel, 0, 1);
            grid.add(coinTypeComboBox, 1, 1);
            
            grid.add(amountLabel, 0, 2);
            grid.add(amountField, 1, 2);
            
            grid.add(currentPriceLabel, 0, 3);
            grid.add(currentPriceField, 1, 3);
            
            grid.add(purchaseDateLabel, 0, 4);
            grid.add(purchaseDatePicker, 1, 4);
            
            grid.add(purchasePriceLabel, 0, 5);
            grid.add(purchasePriceField, 1, 5);

            // Add some spacing between the grid and the buttons
            grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

            dialog.getDialogPane().setContent(grid);

            // Request focus on the name field by default
            nameField.requestFocus();

            // Convert the result to a Cryptocurrency when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Validate required fields
                        if (nameField.getText().isEmpty()) {
                            showAlert("Error", "Name cannot be empty");
                            return null;
                        }
                        if (coinTypeComboBox.getValue() == null) {
                            showAlert("Error", "Please select a coin type");
                            return null;
                        }
                        if (amountField.getText().isEmpty()) {
                            showAlert("Error", "Amount cannot be empty");
                            return null;
                        }
                        if (currentPriceField.getText().isEmpty()) {
                            showAlert("Error", "Current price cannot be empty");
                            return null;
                        }
                        if (purchaseDatePicker.getValue() == null) {
                            showAlert("Error", "Purchase date cannot be empty");
                            return null;
                        }
                        if (purchasePriceField.getText().isEmpty()) {
                            showAlert("Error", "Purchase price cannot be empty");
                            return null;
                        }

                        // Parse inputs
                        String name = nameField.getText();
                        Cryptocurrency.CoinType coinType = coinTypeComboBox.getValue();
                        BigDecimal amount = new BigDecimal(amountField.getText());
                        BigDecimal currentPrice = new BigDecimal(currentPriceField.getText());
                        Date purchaseDate = java.sql.Date.valueOf(purchaseDatePicker.getValue());
                        BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText());

                        // Validate numeric values
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Amount must be greater than 0");
                            return null;
                        }
                        if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Current price must be greater than 0");
                            return null;
                        }
                        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
                            showAlert("Error", "Purchase price must be greater than 0");
                            return null;
                        }

                        BigDecimal currentValue = currentPrice.multiply(amount);

                        return new Cryptocurrency(
                            crypto.getAssetId(),
                            name,
                            purchaseDate,
                            purchasePrice,
                            currentValue,
                            coinType,
                            amount
                        );
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Please enter valid numeric values for amount and prices");
                        return null;
                    } catch (IllegalArgumentException e) {
                        showAlert("Error", e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Cryptocurrency> result = dialog.showAndWait();
            result.ifPresent(updatedCrypto -> {
                int index = cryptoList.indexOf(crypto);
                if (index != -1) {
                    cryptoList.set(index, updatedCrypto);
                    cryptoDataService.saveUserCryptoList(currentUserId, cryptoList);
                }
            });
        } catch (Exception e) {
            showAlert("Error", "An error occurred while opening the edit dialog: " + e.getMessage());
        }
    }
}