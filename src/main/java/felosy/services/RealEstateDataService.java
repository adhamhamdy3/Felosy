package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RealEstateDataService {
    private static final RealEstateDataService instance = new RealEstateDataService();
    private final Map<String, ObservableList<RealEstate>> userRealEstateData = new HashMap<>();
    private static final String DATA_FILE = "realestate_data.ser";

    private RealEstateDataService() {
        loadData(); // Load data when service is initialized
    }

    public static RealEstateDataService getInstance() {
        return instance;
    }

    public ObservableList<RealEstate> getUserRealEstateList(String userId) {
        return userRealEstateData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserRealEstateList(String userId, ObservableList<RealEstate> realEstateList) {
        userRealEstateData.put(userId, realEstateList);
        saveData(); // Save data whenever it's updated
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            // Convert ObservableLists to regular Lists before saving
            Map<String, java.util.List<RealEstate>> serializableMap = new HashMap<>();
            userRealEstateData.forEach((key, value) ->
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
            Map<String, java.util.List<RealEstate>> loadedData =
                    (Map<String, java.util.List<RealEstate>>) ois.readObject();
            // Convert loaded Lists to ObservableLists
            loadedData.forEach((userId, realEstateList) ->
                    userRealEstateData.put(userId, FXCollections.observableArrayList(realEstateList)));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Add this method to save data when the application closes
    public void shutdown() {
        saveData();
    }
}