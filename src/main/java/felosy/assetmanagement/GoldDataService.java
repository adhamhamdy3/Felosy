package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;

public class GoldDataService {
    private static final GoldDataService instance = new GoldDataService();
    private final Map<String, ObservableList<Gold>> userGoldData = new HashMap<>();

    private GoldDataService() {}

    public static GoldDataService getInstance() {
        return instance;
    }

    public ObservableList<Gold> getUserGoldList(String userId) {
        return userGoldData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserGoldList(String userId, ObservableList<Gold> goldList) {
        userGoldData.put(userId, goldList);
    }
}