package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;

public class RealEstateDataService {
    private static final RealEstateDataService instance = new RealEstateDataService();
    private final Map<String, ObservableList<RealEstate>> userRealEstateData = new HashMap<>();

    private RealEstateDataService() {}

    public static RealEstateDataService getInstance() {
        return instance;
    }

    public ObservableList<RealEstate> getUserRealEstateList(String userId) {
        return userRealEstateData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserRealEstateList(String userId, ObservableList<RealEstate> realEstateList) {
        userRealEstateData.put(userId, realEstateList);
    }
}