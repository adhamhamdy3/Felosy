package felosy.controllers;

import felosy.assetmanagement.Gold;
import felosy.services.GoldDataService;
import felosy.services.CryptoDataService;
import felosy.services.RealEstateDataService;
import felosy.services.StockDataService;
import felosy.assetmanagement.Cryptocurrency;
import felosy.assetmanagement.RealEstate;
import felosy.assetmanagement.Stock;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing different types of assets (Gold, Cryptocurrency, Real Estate, Stock)
 * Provides UI functionality for viewing, adding, editing, and deleting assets
 */
public class GoldController {
    // Top section
    @FXML private RadioButton radio_gold;
    @FXML private RadioButton radio_crypto;
    @FXML private RadioButton radio_realEstate;
    @FXML private RadioButton radio_Stock;
    @FXML private Button btn_back;
    @FXML private Text errorText;
    @FXML private ToggleGroup assetTypeGroup;

    // Table
    @FXML private TableView<Gold> goldTable;
    @FXML private TableColumn<Gold, String> idColumn;
    @FXML private TableColumn<Gold, String> nameColumn;
    @FXML private TableColumn<Gold, Date> purchaseDateColumn;
    @FXML private TableColumn<Gold, BigDecimal> gramsColumn;
    @FXML private TableColumn<Gold, BigDecimal> purityColumn;
    @FXML private TableColumn<Gold, BigDecimal> priceColumn;
    @FXML private TableColumn<Gold, BigDecimal> purchasePriceColumn;
    @FXML private TableColumn<Gold, Void> actionsColumn;

    // Bottom form
    @FXML private TextField txt_name;
    @FXML private TextField txt_grams;
    @FXML private TextField txt_purity;
    @FXML private TextField txt_purchasePrice;
    @FXML private DatePicker date_purchase;
    @FXML private Button btn_add;
    @FXML private Label netWorthLabel;
    @FXML private Label totalInvestedLabel;

    // Data model
    private ObservableList<Gold> goldItems = FXCollections.observableArrayList();

    // Current portfolio ID
    private String currentPortfolioId;

    // Default current user ID (would be set from login)
    private String currentUserId = "default-user";

    // Data services for other asset types
    private CryptoDataService cryptoDataService = CryptoDataService.getInstance();
    private RealEstateDataService realEstateDataService = RealEstateDataService.getInstance();
    private StockDataService stockDataService = StockDataService.getInstance();

    @FXML
    public void initialize() {
        // Setup radio button listeners
        radio_gold.setSelected(true);

        // Initialize the table columns
        setupGoldTable();

        // Setup add button
        btn_add.setOnAction(e -> showAddGoldDialog());

        // Load gold data from storage
        loadGoldDataFromStorage();
        updateSummaryLabels();
    }

    /**
     * Set the current portfolio ID and load its assets
     *
     * @param portfolioId The portfolio ID to load
     */
    public void setCurrentPortfolioId(String portfolioId) {
        this.currentPortfolioId = portfolioId;
        loadGoldDataFromStorage();
        updateSummaryLabels();
    }

    /**
     * Set the current user ID
     *
     * @param userId The current user ID
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        // Reload data for the new user
        loadGoldDataFromStorage();
        updateSummaryLabels();
    }

    /**
     * Setup the gold table columns and formatters
     */
    private void setupGoldTable() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        gramsColumn.setCellValueFactory(new PropertyValueFactory<>("weightGrams"));
        purityColumn.setCellValueFactory(cellData -> {
            BigDecimal purity = cellData.getValue().getPurity();
            return new SimpleObjectProperty<>(purity);
        });
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));

        // Setup custom cell formats
        purchaseDateColumn.setCellFactory(column -> new TableCell<Gold, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toString());
                }
            }
        });

        purityColumn.setCellFactory(column -> new TableCell<Gold, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal purity, boolean empty) {
                super.updateItem(purity, empty);
                if (empty || purity == null) {
                    setText(null);
                } else {
                    // Display as decimal and karat
                    BigDecimal karatValue = purity.multiply(new BigDecimal("24"));
                    setText(String.format("%.2f (%.1fK)", purity.doubleValue(), karatValue.doubleValue()));
                }
            }
        });

        priceColumn.setCellFactory(column -> new TableCell<Gold, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price.doubleValue()));
                }
            }
        });

        purchasePriceColumn.setCellFactory(column -> new TableCell<Gold, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price.doubleValue()));
                }
            }
        });

        // Setup action column with Edit and Delete buttons
        setupActionsColumn();

        // Bind the table to the data model
        goldTable.setItems(goldItems);
    }

    /**
     * Setup the actions column with Edit and Delete buttons
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                // Edit button action
                editButton.setOnAction(event -> {
                    Gold gold = getTableView().getItems().get(getIndex());
                    showEditDialog(gold);
                });

                // Delete button action
                deleteButton.setOnAction(event -> {
                    Gold gold = getTableView().getItems().get(getIndex());
                    handleDeleteGold(gold);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Show dialog for adding a new gold asset
     */
    private void showAddGoldDialog() {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Gold Asset");
            dialog.setHeaderText("Enter gold asset details");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form fields
            TextField nameField = new TextField();
            nameField.setPromptText("Name");
            
            TextField gramsField = new TextField();
            gramsField.setPromptText("Grams");
            
            TextField purityField = new TextField();
            purityField.setPromptText("Purity (0->1)");
            
            TextField purchasePriceField = new TextField();
            purchasePriceField.setPromptText("Purchase Price ($)");
            
            DatePicker purchaseDatePicker = new DatePicker();
            purchaseDatePicker.setPromptText("Purchase Date");

            // Layout the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Weight (grams):"), 0, 1);
            grid.add(gramsField, 1, 1);
            grid.add(new Label("Purity (0->1):"), 0, 2);
            grid.add(purityField, 1, 2);
            grid.add(new Label("Purchase Price ($):"), 0, 3);
            grid.add(purchasePriceField, 1, 3);
            grid.add(new Label("Purchase Date:"), 0, 4);
            grid.add(purchaseDatePicker, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the name field by default
            nameField.requestFocus();

            // Process the result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                // Validate input fields
                if (nameField.getText().isEmpty() || gramsField.getText().isEmpty() || 
                    purityField.getText().isEmpty() || purchasePriceField.getText().isEmpty() ||
                    purchaseDatePicker.getValue() == null) {
                    showError("All fields are required");
                    return;
                }

                String name = nameField.getText();
                BigDecimal grams = new BigDecimal(gramsField.getText());
                BigDecimal purity = new BigDecimal(purityField.getText());
                BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText());
                Date purchaseDate = Date.from(purchaseDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

                // Validate values
                if (grams.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Weight must be greater than 0");
                    return;
                }

                if (purity.compareTo(BigDecimal.ZERO) < 0 || purity.compareTo(BigDecimal.ONE) > 0) {
                    showError("Purity must be between 0 and 1");
                    return;
                }

                if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Purchase price must be greater than 0");
                    return;
                }

                // Generate unique ID for the new gold asset
                String assetId = UUID.randomUUID().toString().substring(0, 8);

                // Create a new Gold asset
                Gold newGold = new Gold(assetId, name, purchaseDate, purchasePrice, purchasePrice, grams, purity);

                // Add to table and save
                goldItems.add(newGold);
                saveGoldDataToStorage();
                hideError();
                updateSummaryLabels();
            }
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle deleting a gold asset
     *
     * @param gold The gold asset to delete
     */
    private void handleDeleteGold(Gold gold) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Gold Asset");
        alert.setContentText("Are you sure you want to delete " + gold.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            goldItems.remove(gold);
            saveGoldDataToStorage();
            updateSummaryLabels();
        }
    }

    /**
     * Show dialog for editing a gold asset
     *
     * @param gold The gold asset to edit
     */
    private void showEditDialog(Gold gold) {
        // Create a dialog for editing
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Gold Asset");
        dialog.setHeaderText("Update gold asset details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField nameField = new TextField(gold.getName());
        TextField gramsField = new TextField(gold.getWeightGrams().toString());
        TextField purityField = new TextField(gold.getPurity().toString());

        // Additional fields for advanced editing
        DatePicker purchaseDatePicker = new DatePicker();
        // Convert Date to LocalDate
        if (gold.getPurchaseDate() != null) {
            LocalDate purchaseLocalDate = gold.getPurchaseDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            purchaseDatePicker.setValue(purchaseLocalDate);
        }

        TextField purchasePriceField = new TextField(gold.getPurchasePrice().toString());
        TextField currentValueField = new TextField(gold.getCurrentValue().toString());

        // Layout the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Weight (grams):"), 0, 1);
        grid.add(gramsField, 1, 1);
        grid.add(new Label("Purity (0->1):"), 0, 2);
        grid.add(purityField, 1, 2);
        grid.add(new Label("Purchase Date:"), 0, 3);
        grid.add(purchaseDatePicker, 1, 3);
        grid.add(new Label("Purchase Price ($):"), 0, 4);
        grid.add(purchasePriceField, 1, 4);
        grid.add(new Label("Current Value ($):"), 0, 5);
        grid.add(currentValueField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        nameField.requestFocus();

        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                // Update the gold asset with new values
                gold.setName(nameField.getText());
                gold.setWeightGrams(new BigDecimal(gramsField.getText()));
                gold.setPurity(new BigDecimal(purityField.getText()));

                // Update additional fields
                if (purchaseDatePicker.getValue() != null) {
                    Date purchaseDate = Date.from(purchaseDatePicker.getValue().atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
                    gold.setPurchaseDate(purchaseDate);
                }

                gold.setPurchasePrice(new BigDecimal(purchasePriceField.getText()));
                gold.setCurrentValue(new BigDecimal(currentValueField.getText()));

                // Refresh the table
                goldTable.refresh();

                // Save changes to storage
                saveGoldDataToStorage();
                updateSummaryLabels();

            } catch (NumberFormatException e) {
                showError("Invalid number format");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        }
    }

    /**
     * Show an error message
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisible(true);
    }

    /**
     * Hide the error message
     */
    private void hideError() {
        errorText.setVisible(false);
    }

    /**
     * Show an error for unimplemented features
     *
     * @param message The error message to display
     */
    private void showNotImplementedError(String message) {
        showError(message);
        // Reset selection to gold
        radio_gold.setSelected(true);
    }

    /**
     * Load gold data from the data storage system
     */
    private void loadGoldDataFromStorage() {
        // Clear current items
        goldItems.clear();

        try {
            // Get the user's gold list from the service
            ObservableList<Gold> userGoldList = GoldDataService.getInstance().getUserGoldList(currentUserId);
            goldItems.addAll(userGoldList);
            updateSummaryLabels();
        } catch (Exception e) {
            showError("Failed to load gold data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save gold data to the data storage system
     */
    private void saveGoldDataToStorage() {
        try {
            // Save the current gold items to the service
            GoldDataService.getInstance().saveUserGoldList(currentUserId, goldItems);
        } catch (Exception e) {
            showError("Failed to save gold data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle switching to the dashboard view
     */
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Save any pending changes before leaving
            saveGoldDataToStorage();
            // Switch to dashboard
            SceneHandler.switchToDashboard();
        } catch (Exception e) {
            showError("Failed to return to dashboard: " + e.getMessage());
        }
    }

    /**
     * Switch to Gold assets view
     */
    @FXML
    public void switchToGold(ActionEvent event) {
        hideError();
        // Gold view is already loaded
    }

    /**
     * Switch to Cryptocurrency assets view
     */
    @FXML
    public void switchToCrypto(ActionEvent event) {
        try {
            // Save any pending changes before switching
            saveGoldDataToStorage();
            // Switch to crypto view
            SceneHandler.switchToCrypto();
        } catch (Exception e) {
            showError("Failed to switch to cryptocurrency view: " + e.getMessage());
        }
    }

    /**
     * Switch to Real Estate assets view
     */
    @FXML
    public void switchToRealEstate(ActionEvent event) {
        try {
            // Save any pending changes before switching
            saveGoldDataToStorage();
            // Switch to real estate view
            SceneHandler.switchToRealEstate();
        } catch (Exception e) {
            showError("Failed to switch to real estate view: " + e.getMessage());
        }
    }

    /**
     * Switch to Stock assets view
     */
    @FXML
    public void switchToStock(ActionEvent event) {
        try {
            // Save any pending changes before switching
            saveGoldDataToStorage();
            // Switch to stock view
            SceneHandler.switchToStock();
        } catch (Exception e) {
            showError("Failed to switch to stock view: " + e.getMessage());
        }
    }

    /**
     * Calculates and updates the net worth and total invested labels.
     */
    private void updateSummaryLabels() {
        BigDecimal totalNetWorth = BigDecimal.ZERO;
        BigDecimal totalInvestedInGold = BigDecimal.ZERO;

        // Calculate Gold net worth and total invested in Gold
        for (Gold gold : goldItems) {
            if (gold.getCurrentValue() != null) {
                totalNetWorth = totalNetWorth.add(gold.getCurrentValue());
            }
            if (gold.getPurchasePrice() != null) {
                totalInvestedInGold = totalInvestedInGold.add(gold.getPurchasePrice());
            }
        }

        // Add Crypto net worth
        ObservableList<Cryptocurrency> cryptoList = cryptoDataService.getUserCryptoList(currentUserId);
        if (cryptoList != null) {
            for (Cryptocurrency crypto : cryptoList) {
                if (crypto.getCurrentValue() != null) {
                    totalNetWorth = totalNetWorth.add(crypto.getCurrentValue());
                }
            }
        }

        // Add Real Estate net worth
        ObservableList<RealEstate> realEstateList = realEstateDataService.getUserRealEstateList(currentUserId);
        if (realEstateList != null) {
            for (RealEstate property : realEstateList) {
                if (property.getCurrentValue() != null) {
                    totalNetWorth = totalNetWorth.add(property.getCurrentValue());
                }
            }
        }

        // Add Stock net worth
        ObservableList<Stock> stockList = stockDataService.getUserStockList(currentUserId);
        if (stockList != null) {
            for (Stock stock : stockList) {
                if (stock.getCurrentValue() != null) {
                    totalNetWorth = totalNetWorth.add(stock.getCurrentValue());
                }
            }
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        if (netWorthLabel != null) {
            netWorthLabel.setText(currencyFormatter.format(totalNetWorth));
        }
        if (totalInvestedLabel != null) {
            totalInvestedLabel.setText(currencyFormatter.format(totalInvestedInGold));
        }
    }
}