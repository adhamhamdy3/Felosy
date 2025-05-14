package felosy.services;

import felosy.assetmanagement.Gold;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GoldDataService {
    private static final Logger LOGGER = Logger.getLogger(GoldDataService.class.getName());
    private static final GoldDataService instance = new GoldDataService();
    private final Map<String, ObservableList<Gold>> userGoldData = new HashMap<>();
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/gold_data.ser";

    private GoldDataService() {
        createDataDirectory();
        loadData(); // Load data when service is initialized
    }

    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LOGGER.severe("Failed to create data directory: " + DATA_DIR);
            }
        }
    }

    public static GoldDataService getInstance() {
        return instance;
    }

    public ObservableList<Gold> getUserGoldList(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to get gold list for null or empty user ID");
            return FXCollections.observableArrayList();
        }
        return userGoldData.computeIfAbsent(userId, k -> {
            LOGGER.info("Creating new gold list for user: " + userId);
            return FXCollections.observableArrayList();
        });
    }

    public void saveUserGoldList(String userId, ObservableList<Gold> goldList) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to save gold list for null or empty user ID");
            return;
        }
        if (goldList == null) {
            LOGGER.warning("Attempted to save null gold list for user: " + userId);
            return;
        }
        
        userGoldData.put(userId, goldList);
        saveData(); // Save data whenever it's updated
    }

    private void saveData() {
        File file = new File(DATA_FILE);
        File tempFile = new File(DATA_FILE + ".tmp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            // Convert ObservableLists to regular Lists before saving
            Map<String, java.util.List<Gold>> serializableMap = new HashMap<>();
            userGoldData.forEach((key, value) -> serializableMap.put(key, new java.util.ArrayList<>(value)));
            oos.writeObject(serializableMap);
            oos.flush();

            // Atomic file replacement
            if (file.exists() && !file.delete()) {
                throw new IOException("Could not delete existing data file");
            }
            if (!tempFile.renameTo(file)) {
                throw new IOException("Could not rename temporary file to data file");
            }

            LOGGER.info("Successfully saved gold data to: " + DATA_FILE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save gold data", e);
            // Clean up temporary file if it exists
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            LOGGER.info("No existing gold data file found at: " + DATA_FILE);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, java.util.List<Gold>> loadedData = (Map<String, java.util.List<Gold>>) ois.readObject();
            // Convert loaded Lists to ObservableLists
            loadedData.forEach((userId, goldList) -> {
                userGoldData.put(userId, FXCollections.observableArrayList(goldList));
                LOGGER.info("Loaded " + goldList.size() + " gold items for user: " + userId);
            });
            LOGGER.info("Successfully loaded gold data from: " + DATA_FILE);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load gold data", e);
        }
    }

    public void addGold(String userId, Gold gold) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to add gold for null or empty user ID");
            return;
        }
        if (gold == null) {
            LOGGER.warning("Attempted to add null gold for user: " + userId);
            return;
        }

        ObservableList<Gold> userList = getUserGoldList(userId);
        userList.add(gold);
        saveData();
        LOGGER.info("Added gold asset " + gold.getAssetId() + " for user: " + userId);
    }

    public void removeGold(String userId, Gold gold) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to remove gold for null or empty user ID");
            return;
        }
        if (gold == null) {
            LOGGER.warning("Attempted to remove null gold for user: " + userId);
            return;
        }

        ObservableList<Gold> userList = getUserGoldList(userId);
        if (userList.remove(gold)) {
            saveData();
            LOGGER.info("Removed gold asset " + gold.getAssetId() + " for user: " + userId);
        }
    }

    public void updateGold(String userId, Gold oldGold, Gold newGold) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to update gold for null or empty user ID");
            return;
        }
        if (oldGold == null || newGold == null) {
            LOGGER.warning("Attempted to update with null gold for user: " + userId);
            return;
        }

        ObservableList<Gold> userList = getUserGoldList(userId);
        int index = userList.indexOf(oldGold);
        if (index != -1) {
            userList.set(index, newGold);
            saveData();
            LOGGER.info("Updated gold asset " + newGold.getAssetId() + " for user: " + userId);
        }
    }

    // Add this method to save data when the application closes
    public void shutdown() {
        LOGGER.info("Shutting down GoldDataService and saving data");
        saveData();
    }
}