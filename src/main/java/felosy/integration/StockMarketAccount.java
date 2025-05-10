package felosy.integration;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents a connection to a stock trading platform
 */
public class StockMarketAccount {
    private String accountId;
    private String platformName;
    private ExternalAccountConnector connector;
    private boolean isConnected;
    private static final Logger LOGGER = Logger.getLogger(StockMarketAccount.class.getName());

    public StockMarketAccount(String accountId, String platformName) {
        this.accountId = accountId;
        this.platformName = platformName;
        this.isConnected = false;
    }
    
    /**
     * Establishes connection to the stock platform's API
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            // Create appropriate connector based on platform name
            String baseUrl = getPlatformApiUrl(platformName);
            String apiKey = getStoredApiKey(platformName, accountId);
            
            if (apiKey == null || baseUrl == null) {
                LOGGER.severe("Missing API key or base URL for " + platformName);
                return false;
            }
            
            connector = new ExternalAccountConnector("stock-" + platformName.toLowerCase(), apiKey, baseUrl);
            isConnected = connector.establishConnection();
            
            if (isConnected) {
                LOGGER.info("Successfully connected to stock platform: " + platformName);
            }
            
            return isConnected;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to stock platform", e);
            return false;
        }
    }
    
    /**
     * Fetches stock holdings from the trading platform
     * @return List of stock holdings
     */
    public List<StockHolding> fetchHoldings() {
        List<StockHolding> holdings = new ArrayList<>();
        
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Connector not initialized or connected");
            return holdings;
        }
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("accountId", accountId);
            
            Response response = connector.sendRequest("/holdings", "GET", null);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                JSONArray holdingsArray = responseJson.getJSONArray("holdings");
                
                for (int i = 0; i < holdingsArray.length(); i++) {
                    JSONObject h = holdingsArray.getJSONObject(i);
                    StockHolding holding = new StockHolding(
                        h.getString("symbol"),
                        h.getInt("quantity"),
                        h.getFloat("purchasePrice"),
                        h.getFloat("currentPrice"),
                        h.getString("sector")
                    );
                    holdings.add(holding);
                }
                
                LOGGER.info("Successfully fetched " + holdings.size() + " stock holdings");
            } else {
                LOGGER.warning("Failed to fetch holdings: " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching stock holdings", e);
        }
        
        return holdings;
    }
    
    /**
     * Fetches current price for a specific ticker symbol
     * @param symbol Stock ticker symbol
     * @return Current price of the stock
     */
    public float fetchStockPrice(String symbol) {
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Cannot fetch stock price: Connector not initialized or connected");
            return 0.0f;
        }
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("symbol", symbol);
            
            Response response = connector.sendRequest("/quote/" + symbol, "GET", null);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                float price = responseJson.getFloat("price");
                LOGGER.info("Successfully fetched price for " + symbol + ": " + price);
                return price;
            } else {
                LOGGER.warning("Failed to fetch price for " + symbol + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching stock price", e);
        }
        
        return 0.0f;
    }
    
    /**
     * Disconnects from the stock platform's API
     * @return true if disconnection was successful
     */
    public boolean disconnect() {
        if (connector != null && connector.isConnected()) {
            boolean result = connector.closeConnection();
            isConnected = !result;
            
            if (result) {
                LOGGER.info("Successfully disconnected from stock platform: " + platformName);
            } else {
                LOGGER.warning("Problem while disconnecting from stock platform: " + platformName);
            }
            
            return result;
        }
        return true; // Already disconnected
    }
    
    // Helper methods
    
    private String getPlatformApiUrl(String platformName) {
        // In a real implementation, this could come from a configuration file or database
        switch (platformName.toLowerCase()) {
            case "robinhood":
                return "https://api.robinhood.com/v1";
            case "etrade":
                return "https://api.etrade.com/v1";
            case "fidelity":
                return "https://api.fidelity.com/v1";
            case "td ameritrade":
                return "https://api.tdameritrade.com/v1";
            default:
                return "https://api.genericbroker.com/v1";
        }
    }
    
    private String getStoredApiKey(String platformName, String accountId) {
        // In a real implementation, this would retrieve securely stored API keys
        // Never hardcode actual API keys in production code
        return "sk_" + platformName.toLowerCase().replace(" ", "") + "_" + accountId.substring(0, 4);
    }
    
    // Getters
    
    public String getAccountId() {
        return accountId;
    }
    
    public String getPlatformName() {
        return platformName;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Represents a stock holding in a brokerage account
     */
    public static class StockHolding {
        private String symbol;
        private int quantity;
        private float purchasePrice;
        private float currentPrice;
        private String sector;
        
        public StockHolding(String symbol, int quantity, float purchasePrice, float currentPrice, String sector) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
            this.currentPrice = currentPrice;
            this.sector = sector;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public float getPurchasePrice() {
            return purchasePrice;
        }
        
        public float getCurrentPrice() {
            return currentPrice;
        }
        
        public String getSector() {
            return sector;
        }
        
        public float getTotalValue() {
            return quantity * currentPrice;
        }
        
        public float getProfitLoss() {
            return (currentPrice - purchasePrice) * quantity;
        }
        
        public float getProfitLossPercentage() {
            if (purchasePrice == 0) return 0;
            return ((currentPrice - purchasePrice) / purchasePrice) * 100;
        }
        
        @Override
        public String toString() {
            return "StockHolding{" +
                   "symbol='" + symbol + '\'' +
                   ", quantity=" + quantity +
                   ", purchasePrice=" + purchasePrice +
                   ", currentPrice=" + currentPrice +
                   ", sector='" + sector + '\'' +
                   '}';
        }
    }
}
