package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;

public class StockDataService {
    private static final StockDataService instance = new StockDataService();
    private final Map<String, ObservableList<Stock>> userStockData = new HashMap<>();

    private StockDataService() {}

    public static StockDataService getInstance() {
        return instance;
    }

    public ObservableList<Stock> getUserStockList(String userId) {
        return userStockData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserStockList(String userId, ObservableList<Stock> stockList) {
        userStockData.put(userId, stockList);
    }
}