package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CryptoDataService {
    private static final CryptoDataService instance = new CryptoDataService();
    private final Map<String, ObservableList<Cryptocurrency>> userCryptoData = new HashMap<>();
    private static final String DATA_FILE = "crypto_data.ser";

    private CryptoDataService() {
        loadData(); // Load data when service is initialized
    }

    public static CryptoDataService getInstance() {
        return instance;
    }

    public ObservableList<Cryptocurrency> getUserCryptoList(String userId) {
        return userCryptoData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserCryptoList(String userId, ObservableList<Cryptocurrency> cryptoList) {
        userCryptoData.put(userId, cryptoList);
        saveData(); // Save data whenever it's updated
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            // Convert ObservableLists to regular Lists before saving
            Map<String, java.util.List<Cryptocurrency>> serializableMap = new HashMap<>();
            userCryptoData.forEach((key, value) ->
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
            Map<String, java.util.List<Cryptocurrency>> loadedData =
                    (Map<String, java.util.List<Cryptocurrency>>) ois.readObject();
            // Convert loaded Lists to ObservableLists
            loadedData.forEach((userId, cryptoList) ->
                    userCryptoData.put(userId, FXCollections.observableArrayList(cryptoList)));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Add this method to save data when the application closes
    public void shutdown() {
        saveData();
    }
}