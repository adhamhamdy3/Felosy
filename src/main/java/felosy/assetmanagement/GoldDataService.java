package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GoldDataService {
    private static final GoldDataService instance = new GoldDataService();
    private final Map<String, ObservableList<Gold>> userGoldData = new HashMap<>();
    private static final String DATA_FILE = "gold_data.ser";

    private GoldDataService() {
        loadData(); // Load data when service is initialized
    }

    public static GoldDataService getInstance() {
        return instance;
    }

    public ObservableList<Gold> getUserGoldList(String userId) {
        return userGoldData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserGoldList(String userId, ObservableList<Gold> goldList) {
        userGoldData.put(userId, goldList);
        saveData(); // Save data whenever it's updated
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            // Convert ObservableLists to regular Lists before saving
            Map<String, java.util.List<Gold>> serializableMap = new HashMap<>();
            userGoldData.forEach((key, value) -> serializableMap.put(key, new java.util.ArrayList<>(value)));
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
            Map<String, java.util.List<Gold>> loadedData = (Map<String, java.util.List<Gold>>) ois.readObject();
            // Convert loaded Lists to ObservableLists
            loadedData.forEach((userId, goldList) ->
                    userGoldData.put(userId, FXCollections.observableArrayList(goldList)));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Add this method to save data when the application closes
    public void shutdown() {
        saveData();
    }
}