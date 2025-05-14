package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.RealEstate;
import felosy.services.RealEstateDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;

public class RealEstateController implements Initializable {

    @FXML private TableView<RealEstate> realEstateTable;
    @FXML private TableColumn<RealEstate, String> idColumn;
    @FXML private TableColumn<RealEstate, String> nameColumn;
    @FXML private TableColumn<RealEstate, String> locationColumn;
    @FXML private TableColumn<RealEstate, RealEstate.PropertyType> propertyTypeColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> areaColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> rentalIncomeColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> valueColumn;
    @FXML private TableColumn<RealEstate, Float> occupancyRateColumn;
    @FXML private TableColumn<RealEstate, Date> purchaseDateColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> purchasePriceColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> propertyTaxColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> maintenanceCostColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> insuranceCostColumn;
    @FXML private TableColumn<RealEstate, Void> actionsColumn;

    @FXML private Text errorText;

    @FXML private RadioButton radio_gold;
    @FXML private RadioButton radio_crypto;
    @FXML private RadioButton radio_realEstate;
    @FXML private RadioButton radio_Stock;
    @FXML private Button btn_add;
    @FXML private Button btn_back;
    @FXML
    private ToggleGroup assetTypeGroup;

    private RealEstateDataService realEstateDataService = RealEstateDataService.getInstance();
    private String currentUserId;
    private ObservableList<RealEstate> realEstateList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserId = App.getCurrentUser().getUserId();
        realEstateList = realEstateDataService.getUserRealEstateList(currentUserId);

        setupTable();
        setupButtons();

        realEstateTable.setItems(realEstateList);
    }


    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        propertyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("areaSquareMeters"));
        rentalIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyRentalIncome"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        occupancyRateColumn.setCellValueFactory(new PropertyValueFactory<>("occupancyRate"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        propertyTaxColumn.setCellValueFactory(new PropertyValueFactory<>("annualPropertyTax"));
        maintenanceCostColumn.setCellValueFactory(new PropertyValueFactory<>("annualMaintenanceCost"));
        insuranceCostColumn.setCellValueFactory(new PropertyValueFactory<>("annualInsuranceCost"));

        occupancyRateColumn.setCellFactory(col -> new TableCell<RealEstate, Float>() {
            @Override
            protected void updateItem(Float value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", value * 100));
                }
            }
        });

        purchaseDateColumn.setCellFactory(col -> new TableCell<RealEstate, Date>() {
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

        List<TableColumn<RealEstate, BigDecimal>> currencyColumns = Arrays.asList(
            rentalIncomeColumn, valueColumn, purchasePriceColumn,
            propertyTaxColumn, maintenanceCostColumn, insuranceCostColumn
        );

        for (TableColumn<RealEstate, BigDecimal> column : currencyColumns) {
            column.setCellFactory(col -> new TableCell<RealEstate, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%,.2f", value));
                    }
                }
            });
        }
        
        actionsColumn.setCellFactory(param -> new TableCell<RealEstate, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(editButton, deleteButton);

            {
                pane.setSpacing(5);
                editButton.setOnAction(event -> {
                    RealEstate property = getTableView().getItems().get(getIndex());
                    handleEditRealEstate(property);
                });
                deleteButton.setOnAction(event -> {
                    RealEstate property = getTableView().getItems().get(getIndex());
                    deleteRealEstateFromRow(property);
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

        realEstateTable.setItems(realEstateList);
    }

    private void setupButtons() {
        assetTypeGroup = new ToggleGroup();
        radio_gold.setToggleGroup(assetTypeGroup);
        radio_crypto.setToggleGroup(assetTypeGroup);
        radio_realEstate.setToggleGroup(assetTypeGroup);
        radio_Stock.setToggleGroup(assetTypeGroup);
        radio_realEstate.setSelected(true);

        assetTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == radio_gold) {
                switchToGold();
            } else if (newVal == radio_crypto) {
                switchToCrypto();
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
    private void handleAddRealEstate() {
        Dialog<RealEstate> dialog = new Dialog<>();
        dialog.setTitle("Add New Real Estate Asset");
        dialog.setHeaderText("Enter the details for the new real estate property.");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        ComboBox<RealEstate.PropertyType> propertyTypeCombo = new ComboBox<>();
        propertyTypeCombo.setItems(FXCollections.observableArrayList(RealEstate.PropertyType.values()));
        propertyTypeCombo.setPromptText("Property Type");
        TextField areaField = new TextField();
        areaField.setPromptText("Area (m²)");
        TextField rentalIncomeField = new TextField();
        rentalIncomeField.setPromptText("Monthly Rental Income");
        TextField occupancyRateField = new TextField();
        occupancyRateField.setPromptText("Occupancy Rate (0.0-1.0)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Property Type:"), 0, 2);
        grid.add(propertyTypeCombo, 1, 2);
        grid.add(new Label("Area (m²):"), 0, 3);
        grid.add(areaField, 1, 3);
        grid.add(new Label("Monthly Rental:"), 0, 4);
        grid.add(rentalIncomeField, 1, 4);
        grid.add(new Label("Occupancy Rate:"), 0, 5);
        grid.add(occupancyRateField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText();
                    String location = locationField.getText();
                    RealEstate.PropertyType propertyType = propertyTypeCombo.getValue();
                    BigDecimal area = new BigDecimal(areaField.getText());
                    BigDecimal rentalIncome = new BigDecimal(rentalIncomeField.getText());
                    float occupancyRate = Float.parseFloat(occupancyRateField.getText());

                    if (name.isEmpty() || location.isEmpty()) {
                        showAlert("Input Error", "Name and location cannot be empty.");
                        return null;
                    }
                    if (propertyType == null) {
                        showAlert("Input Error", "Please select a property type.");
                        return null;
                    }
                     if (occupancyRate < 0.0f || occupancyRate > 1.0f) {
                         showAlert("Input Error", "Occupancy rate must be between 0.0 and 1.0.");
                         return null;
                    }

                    String assetId = generateEightDigitId();
                    Date purchaseDate = new Date();
                    BigDecimal purchasePrice = area.multiply(new BigDecimal("2000")); 
                    BigDecimal currentValue = purchasePrice; 

                    return new RealEstate(
                            assetId, name, purchaseDate, purchasePrice, currentValue,
                            location, area, propertyType, rentalIncome, occupancyRate
                    );
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Please enter valid numbers for area, rental income, and occupancy rate.");
                    return null;
                } catch (IllegalArgumentException e) {
                    showAlert("Input Error", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<RealEstate> result = dialog.showAndWait();
        result.ifPresent(newProperty -> {
            realEstateList.add(newProperty);
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
        });
    }

    private void handleEditRealEstate(RealEstate propertyToEdit) {
        Dialog<RealEstate.PropertyType> dialog = new Dialog<>();
        dialog.setTitle("Edit Real Estate Asset");
        dialog.setHeaderText("Edit the details for: " + propertyToEdit.getName());

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(propertyToEdit.getName());
        TextField locationField = new TextField(propertyToEdit.getLocation());
        ComboBox<RealEstate.PropertyType> propertyTypeCombo = new ComboBox<>();
        propertyTypeCombo.setItems(FXCollections.observableArrayList(RealEstate.PropertyType.values()));
        propertyTypeCombo.setValue(propertyToEdit.getPropertyType());
        TextField areaField = new TextField(propertyToEdit.getAreaSquareMeters().toPlainString());
        TextField rentalIncomeField = new TextField(propertyToEdit.getMonthlyRentalIncome().toPlainString());
        TextField occupancyRateField = new TextField(String.valueOf(propertyToEdit.getOccupancyRate()));
        
        TextField annualPropertyTaxField = new TextField(propertyToEdit.getAnnualPropertyTax().toPlainString());
        TextField annualMaintenanceCostField = new TextField(propertyToEdit.getAnnualMaintenanceCost().toPlainString());
        TextField annualInsuranceCostField = new TextField(propertyToEdit.getAnnualInsuranceCost().toPlainString());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Property Type:"), 0, 2);
        grid.add(propertyTypeCombo, 1, 2);
        grid.add(new Label("Area (m²):"), 0, 3);
        grid.add(areaField, 1, 3);
        grid.add(new Label("Monthly Rental:"), 0, 4);
        grid.add(rentalIncomeField, 1, 4);
        grid.add(new Label("Occupancy Rate (0.0-1.0):"), 0, 5);
        grid.add(occupancyRateField, 1, 5);
        
        grid.add(new Label("Annual Property Tax:"), 0, 6);
        grid.add(annualPropertyTaxField, 1, 6);
        grid.add(new Label("Annual Maintenance Cost:"), 0, 7);
        grid.add(annualMaintenanceCostField, 1, 7);
        grid.add(new Label("Annual Insurance Cost:"), 0, 8);
        grid.add(annualInsuranceCostField, 1, 8);

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    String location = locationField.getText();
                    RealEstate.PropertyType propertyType = propertyTypeCombo.getValue();
                    BigDecimal area = new BigDecimal(areaField.getText());
                    BigDecimal rentalIncome = new BigDecimal(rentalIncomeField.getText());
                    float occupancyRate = Float.parseFloat(occupancyRateField.getText());
                    BigDecimal annualPropertyTax = new BigDecimal(annualPropertyTaxField.getText());
                    BigDecimal annualMaintenanceCost = new BigDecimal(annualMaintenanceCostField.getText());
                    BigDecimal annualInsuranceCost = new BigDecimal(annualInsuranceCostField.getText());

                    if (name.isEmpty() || location.isEmpty()) {
                        showAlert("Input Error", "Name and location cannot be empty.");
                        return null; 
                    }
                    if (propertyType == null) {
                        showAlert("Input Error", "Please select a property type.");
                        return null; 
                    }
                    if (occupancyRate < 0.0f || occupancyRate > 1.0f) {
                         showAlert("Input Error", "Occupancy rate must be between 0.0 and 1.0.");
                         return null;
                    }

                    propertyToEdit.setName(name);
                    propertyToEdit.setLocation(location);
                    propertyToEdit.setPropertyType(propertyType);
                    propertyToEdit.setAreaSquareMeters(area);
                    propertyToEdit.setMonthlyRentalIncome(rentalIncome);
                    propertyToEdit.setOccupancyRate(occupancyRate);
                    propertyToEdit.setAnnualPropertyTax(annualPropertyTax);
                    propertyToEdit.setAnnualMaintenanceCost(annualMaintenanceCost);
                    propertyToEdit.setAnnualInsuranceCost(annualInsuranceCost);
                    
                    return propertyToEdit.getPropertyType(); 
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Please enter valid numbers for numeric fields.");
                    return null; 
                } catch (IllegalArgumentException e) {
                    showAlert("Input Error", e.getMessage());
                    return null; 
                }
            }
            return null;
        });

        Optional<RealEstate.PropertyType> result = dialog.showAndWait();
        result.ifPresent(updatedPropertyType -> {
            realEstateTable.refresh();
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
        });
    }

    private void deleteRealEstateFromRow(RealEstate propertyToDelete) {
        if (propertyToDelete == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Real Estate Asset");
        confirmDialog.setContentText("Are you sure you want to delete the property: " + propertyToDelete.getName() + "?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            realEstateList.remove(propertyToDelete);
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
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

    private void switchToStock() {
        SceneHandler.switchToStock();
    }

    private void showError(String errorMessage) {
        errorText.setText(errorMessage);
        errorText.setVisible(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}