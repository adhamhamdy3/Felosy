package felosy.controllers;

import felosy.App;
import felosy.assetmanagement.RealEstate;
import felosy.assetmanagement.RealEstateDataService;
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

public class RealEstateController implements Initializable {

    @FXML private TableView<RealEstate> realEstateTable;
    @FXML private TableColumn<RealEstate, String> idColumn;
    @FXML private TableColumn<RealEstate, String> nameColumn;
    @FXML private TableColumn<RealEstate, String> locationColumn;
    @FXML private TableColumn<RealEstate, RealEstate.PropertyType> propertyTypeColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> areaColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> rentalIncomeColumn;
    @FXML private TableColumn<RealEstate, BigDecimal> valueColumn;

    @FXML private TextField txt_name;
    @FXML private TextField txt_location;
    @FXML private ComboBox<RealEstate.PropertyType> propertyTypeComboBox;
    @FXML private TextField txt_area;
    @FXML private TextField txt_rentalIncome;
    @FXML private TextField txt_occupancyRate;
    @FXML private Text errorText;

    @FXML private Button btn_add;
    @FXML private Button btn_delete;
    @FXML private Button btn_back;
    @FXML private Button btn_gold;
    @FXML private Button btn_crypto;
    @FXML private Button btn_Stock;

    private RealEstateDataService realEstateDataService = RealEstateDataService.getInstance();
    private String currentUserId;
    private ObservableList<RealEstate> realEstateList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserId = App.getCurrentUser().getUserId();
        realEstateList = realEstateDataService.getUserRealEstateList(currentUserId);

        setupTable();
        setupComboBox();
        setupButtons();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        propertyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("areaSquareMeters"));
        rentalIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyRentalIncome"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));

        realEstateTable.setItems(realEstateList);

        realEstateTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedRealEstate(newSelection);
            }
        });
    }

    private void setupComboBox() {
        propertyTypeComboBox.setItems(FXCollections.observableArrayList(RealEstate.PropertyType.values()));
    }

    private void setupButtons() {
        btn_gold.setOnAction(e -> switchToGold());
        btn_crypto.setOnAction(e -> switchToCrypto());
        btn_Stock.setOnAction(e -> switchToStock());
    }

    private String generateEightDigitId() {
        int randomNum = 10000000 + (int)(Math.random() * 90000000);
        return String.valueOf(randomNum);
    }

    @FXML
    private void handleAddRealEstate() {
        try {
            String id = generateEightDigitId();
            String name = txt_name.getText();
            String location = txt_location.getText();
            RealEstate.PropertyType propertyType = propertyTypeComboBox.getValue();
            BigDecimal area = new BigDecimal(txt_area.getText());
            BigDecimal rentalIncome = new BigDecimal(txt_rentalIncome.getText());
            float occupancyRate = Float.parseFloat(txt_occupancyRate.getText());

            if (name.isEmpty() || location.isEmpty() || propertyType == null) {
                showAlert("Error", "Please fill all required fields");
                return;
            }

            if (occupancyRate < 0 || occupancyRate > 1) {
                showAlert("Error", "Occupancy rate must be between 0 and 1");
                return;
            }

            RealEstate property = new RealEstate(id, name, new Date(), BigDecimal.valueOf(200),
                    BigDecimal.valueOf(150), location, area, propertyType, rentalIncome, occupancyRate);

            realEstateList.add(property);
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for area, rental income, and occupancy rate");
        }
    }

    @FXML
    private void handleDeleteRealEstate() {
        RealEstate selectedProperty = realEstateTable.getSelectionModel().getSelectedItem();
        if (selectedProperty != null) {
            realEstateList.remove(selectedProperty);
            clearInputFields();
        } else {
            showAlert("Error", "Please select a property to delete");
        }
    }

    private void displaySelectedRealEstate(RealEstate property) {
        txt_name.setText(property.getName());
        txt_location.setText(property.getLocation());
        propertyTypeComboBox.setValue(property.getPropertyType());
        txt_area.setText(property.getAreaSquareMeters().toString());
        txt_rentalIncome.setText(property.getMonthlyRentalIncome().toString());
        txt_occupancyRate.setText(String.valueOf(property.getOccupancyRate()));
    }

    private void clearInputFields() {
        txt_name.clear();
        txt_location.clear();
        propertyTypeComboBox.setValue(null);
        txt_area.clear();
        txt_rentalIncome.clear();
        txt_occupancyRate.clear();
    }

    @FXML
    private void handleBack() {
        try {
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
            App.setRoot("dashboard");
        } catch (IOException e) {
            showError("Error returning to dashboard");
        }
    }

    private void switchToGold() {
        try {
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
            App.setRoot("AssetsManagement");
        } catch (IOException e) {
            showError("Error switching to Gold management");
        }
    }

    private void switchToCrypto() {
        try {
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
            App.setRoot("CryptoManagement");
        } catch (IOException e) {
            showError("Error switching to Cryptocurrency management");
        }
    }

    private void switchToStock() {
        try {
            realEstateDataService.saveUserRealEstateList(currentUserId, realEstateList);
            App.setRoot("StockManagement");
        } catch (IOException e) {
            showError("Error switching to Cryptocurrency management");
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