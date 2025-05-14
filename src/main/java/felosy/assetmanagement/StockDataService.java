package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StockDataService {
    private static final StockDataService instance = new StockDataService();
    private final Map<String, ObservableList<Stock>> userStockData = new HashMap<>();
    private static final String DATA_FILE = "stock_data.ser";

    private StockDataService() {
        loadData(); // Load data when service is initialized
    }

    public static StockDataService getInstance() {
        return instance;
    }

    public ObservableList<Stock> getUserStockList(String userId) {
        return userStockData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserStockList(String userId, ObservableList<Stock> stockList) {
        userStockData.put(userId, stockList);
        saveData(); // Save data whenever it's updated
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            // Convert ObservableLists to regular Lists before saving
            Map<String, java.util.List<Stock>> serializableMap = new HashMap<>();
            userStockData.forEach((key, value) ->
                    serializableMap.put(key, new java.util.ArrayList<>(value)));
            oos.writeObject(serializableMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, java.util.List<Stock>> loadedData =
                    (Map<String, java.util.List<Stock>>) ois.readObject();
            // Convert loaded Lists to ObservableLists
            loadedData.forEach((userId, stockList) ->
                    userStockData.put(userId, FXCollections.observableArrayList(stockList)));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Add this method to save data when the application closes
    public void shutdown() {
        saveData();
    }
}