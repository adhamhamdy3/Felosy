package felosy.assetmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;

public class CryptoDataService {
    private static final CryptoDataService instance = new CryptoDataService();
    private final Map<String, ObservableList<Cryptocurrency>> userCryptoData = new HashMap<>();

    private CryptoDataService() {}

    public static CryptoDataService getInstance() {
        return instance;
    }

    public ObservableList<Cryptocurrency> getUserCryptoList(String userId) {
        return userCryptoData.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    public void saveUserCryptoList(String userId, ObservableList<Cryptocurrency> cryptoList) {
        userCryptoData.put(userId, cryptoList);
    }
}