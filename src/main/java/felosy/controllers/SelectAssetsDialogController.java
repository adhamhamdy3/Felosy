package felosy.controllers;

import felosy.assetmanagement.Asset;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SelectAssetsDialogController implements Initializable {

    @FXML
    private TableView<Asset> assetsTableView;
    @FXML
    private TableColumn<Asset, Boolean> selectColumn;
    @FXML
    private TableColumn<Asset, String> idColumn;
    @FXML
    private TableColumn<Asset, String> nameColumn;
    @FXML
    private TableColumn<Asset, String> typeColumn;
    @FXML
    private TableColumn<Asset, BigDecimal> valueColumn;
    @FXML
    private TableColumn<Asset, Date> hawlDateColumn;
    @FXML
    private TableColumn<Asset, Boolean> hawlDatePassedColumn;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private ObservableList<Asset> assetList = FXCollections.observableArrayList();
    private Map<Asset, BooleanProperty> selectionMap = new HashMap<>();
    private CheckBox selectAllCheckBox = new CheckBox();
    private boolean updatingSelectAll = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure selectAllCheckBox is unchecked by default
        selectAllCheckBox.setSelected(false);
        // Make table and select column editable
        assetsTableView.setEditable(true);
        selectColumn.setEditable(true);
        // Checkbox column
        selectColumn.setCellValueFactory(cellData -> {
            Asset asset = cellData.getValue();
            BooleanProperty property = selectionMap.computeIfAbsent(asset, a -> new SimpleBooleanProperty(false));
            // Listen for changes to update select-all checkbox
            property.addListener((obs, wasSelected, isNowSelected) -> {
                if (!updatingSelectAll) {
                    updateSelectAllCheckBox();
                }
            });
            return property;
        });
        selectColumn.setCellFactory(col -> {
            CheckBoxTableCell<Asset, Boolean> cell = new CheckBoxTableCell<>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null); // Hide checkbox for empty rows
                    } else {
                        setGraphic(getGraphic()); // Show checkbox for data rows
                    }
                }
            };
            cell.setSelectedStateCallback(index -> {
                Asset asset = assetsTableView.getItems().get(index);
                return selectionMap.computeIfAbsent(asset, a -> new SimpleBooleanProperty(false));
            });
            return cell;
        });
        selectColumn.setGraphic(selectAllCheckBox);
        selectAllCheckBox.setOnAction(e -> handleSelectAll());

        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAssetId()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        hawlDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPurchaseDate()));
        hawlDatePassedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(isHawlDatePassed(cellData.getValue())));

        // Load sample data
        loadSampleAssets();
        assetsTableView.setItems(assetList);
        assetsTableView.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void handleSelectAll() {
        updatingSelectAll = true;
        boolean select = selectAllCheckBox.isSelected();
        for (Asset asset : assetList) {
            BooleanProperty prop = selectionMap.computeIfAbsent(asset, a -> new SimpleBooleanProperty(false));
            prop.set(select);
        }
        updatingSelectAll = false;
    }

    private void updateSelectAllCheckBox() {
        boolean allSelected = true;
        boolean noneSelected = true;
        for (Asset asset : assetList) {
            BooleanProperty prop = selectionMap.get(asset);
            if (prop != null) {
                if (prop.get()) {
                    noneSelected = false;
                } else {
                    allSelected = false;
                }
            }
        }
        if (allSelected) {
            selectAllCheckBox.setSelected(true);
            selectAllCheckBox.setIndeterminate(false);
        } else if (noneSelected) {
            selectAllCheckBox.setSelected(false);
            selectAllCheckBox.setIndeterminate(false);
        } else {
            selectAllCheckBox.setIndeterminate(true);
        }
    }

    private boolean isHawlDatePassed(Asset asset) {
        Date purchaseDate = asset.getPurchaseDate();
        if (purchaseDate == null) return false;
        LocalDate hawlDate;
        if (purchaseDate instanceof java.sql.Date) {
            hawlDate = ((java.sql.Date) purchaseDate).toLocalDate();
        } else {
            hawlDate = purchaseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return hawlDate.isBefore(LocalDate.now().minusDays(354));
    }

    private void loadSampleAssets() {
        // Sample data - replace with actual data loading logic
        assetList.clear();
        // You can add real assets from Portfolio here
    }

    @FXML
    private void handleSaveSelection(ActionEvent event) {
        // You can process selected assets here if needed
        // ObservableList<Asset> selected = getSelectedAssets();
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // Method to be called by ZakatAndComplianceController to pass actual assets if needed
    public void loadAssets(ObservableList<Asset> assets) {
        this.assetList.setAll(assets);
        assetsTableView.setItems(this.assetList);
        // Reset selection map for new assets
        selectionMap.clear();
        // Ensure all checkboxes are unchecked
        for (Asset asset : assetList) {
            selectionMap.put(asset, new SimpleBooleanProperty(false));
        }
        selectAllCheckBox.setSelected(false);
        selectAllCheckBox.setIndeterminate(false);
        updateSelectAllCheckBox();
    }

    // Optionally, add a method to get selected assets
    public ObservableList<Asset> getSelectedAssets() {
        ObservableList<Asset> selected = FXCollections.observableArrayList();
        for (Asset asset : assetList) {
            BooleanProperty prop = selectionMap.get(asset);
            if (prop != null && prop.get()) {
                selected.add(asset);
            }
        }
        return selected;
    }
} 