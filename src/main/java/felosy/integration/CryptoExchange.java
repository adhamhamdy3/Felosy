package felosy.integration;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents a connection to a cryptocurrency exchange
 */
public class CryptoExchange {
    private String exchangeId;
    private String exchangeName;
    private ExternalAccountConnector connector;
    private boolean isConnected;
    private static final Logger LOGGER = Logger.getLogger(CryptoExchange.class.getName());

    public CryptoExchange(String exchangeId, String exchangeName) {
        this.exchangeId = exchangeId;
        this.exchangeName = exchangeName;
        this.isConnected = false;
    }
    
    /**
     * Establishes connection to the crypto exchange's API
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            // Create appropriate connector based on exchange name
            String baseUrl = getExchangeApiUrl(exchangeName);
            String apiKey = getStoredApiKey(exchangeName, exchangeId);
            String apiSecret = getStoredApiSecret(exchangeName, exchangeId);
            
            if (apiKey == null || baseUrl == null) {
                LOGGER.severe("Missing API key or base URL for " + exchangeName);
                return false;
            }
            
            // For crypto exchanges, we use a specialized implementation of the connector
            connector = new CryptoExchangeConnector(
                "crypto-" + exchangeName.toLowerCase(), 
                apiKey, 
                baseUrl,
                apiSecret
            );
            
            isConnected = connector.establishConnection();
            
            if (isConnected) {
                LOGGER.info("Successfully connected to crypto exchange: " + exchangeName);
            }
            
            return isConnected;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to crypto exchange", e);
            return false;
        }
    }
    
    /**
     * Fetches cryptocurrency holdings from the exchange
     * @return List of crypto assets
     */
    public List<CryptoAsset> fetchHoldings() {
        List<CryptoAsset> holdings = new ArrayList<>();
        
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Connector not initialized or connected");
            return holdings;
        }
        
        try {
            Response response = connector.sendRequest("/balances", "GET", null);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                JSONArray balancesArray = responseJson.getJSONArray("balances");
                
                for (int i = 0; i < balancesArray.length(); i++) {
                    JSONObject b = balancesArray.getJSONObject(i);
                    // Only include non-zero balances
                    float amount = b.getFloat("free") + b.getFloat("locked");
                    if (amount > 0) {
                        String symbol = b.getString("asset");
                        // Fetch current price for the asset
                        float price = fetchCryptoPrice(symbol);
                        
                        CryptoAsset asset = new CryptoAsset(
                            symbol,
                            amount,
                            price
                        );
                        holdings.add(asset);
                    }
                }
                
                LOGGER.info("Successfully fetched " + holdings.size() + " crypto assets");
            } else {
                LOGGER.warning("Failed to fetch crypto balances: " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching crypto holdings", e);
        }
        
        return holdings;
    }
    
    /**
     * Fetches current price for a specific cryptocurrency
     * @param symbol Cryptocurrency symbol (e.g., BTC, ETH)
     * @return Current price in USD
     */
    public float fetchCryptoPrice(String symbol) {
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Cannot fetch crypto price: Connector not initialized or connected");
            return 0.0f;
        }
        
        try {
            Response response = connector.sendRequest("/ticker/price?symbol=" + symbol + "USDT", "GET", null);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                float price = responseJson.getFloat("price");
                return price;
            } else {
                LOGGER.warning("Failed to fetch price for " + symbol + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching crypto price", e);
        }
        
        return 0.0f;
    }
    
    /**
     * Disconnects from the crypto exchange's API
     * @return true if disconnection was successful
     */
    public boolean disconnect() {
        if (connector != null && connector.isConnected()) {
            boolean result = connector.closeConnection();
            isConnected = !result;
            
            if (result) {
                LOGGER.info("Successfully disconnected from crypto exchange: " + exchangeName);
            } else {
                LOGGER.warning("Problem while disconnecting from crypto exchange: " + exchangeName);
            }
            
            return result;
        }
        return true; // Already disconnected
    }
    
    // Helper methods
    
    private String getExchangeApiUrl(String exchangeName) {
        // In a real implementation, this could come from a configuration file or database
        switch (exchangeName.toLowerCase()) {
            case "binance":
                return "https://api.binance.com";
            case "coinbase":
                return "https://api.coinbase.com";
            case "kraken":
                return "https://api.kraken.com";
            case "gemini":
                return "https://api.gemini.com";
            default:
                return "https://api.genericexchange.com";
        }
    }
    
    private String getStoredApiKey(String exchangeName, String exchangeId) {
        // In a real implementation, this would retrieve securely stored API keys
        return "api_" + exchangeName.toLowerCase() + "_" + exchangeId.substring(0, 4);
    }
    
    private String getStoredApiSecret(String exchangeName, String exchangeId) {
        // In a real implementation, this would retrieve securely stored API secrets
        return "secret_" + exchangeName.toLowerCase() + "_" + exchangeId.substring(0, 4);
    }
    
    // Inner classes
    
    /**
     * Specialized connector for crypto exchanges that require additional authentication
     */
    private static class CryptoExchangeConnector extends ExternalAccountConnector {
        private String apiSecret;
        
        public CryptoExchangeConnector(String connectorId, String apiKey, String baseUrl, String apiSecret) {
            super(connectorId, apiKey, baseUrl);
            this.apiSecret = apiSecret;
        }
        
        @Override
        public Response sendRequest(String endpoint, String method, JSONObject payload) {
            // For crypto exchanges, we need to sign the request with the API secret
            long timestamp = System.currentTimeMillis();
            String signature = generateSignature(endpoint, timestamp, payload);
            
            // Add signature to request
            if (payload == null) {
                payload = new JSONObject();
            }
            payload.put("timestamp", timestamp);
            payload.put("signature", signature);
            
            return super.sendRequest(endpoint, method, payload);
        }
        
        private String generateSignature(String endpoint, long timestamp, JSONObject payload) {
            // In a real implementation, this would generate a HMAC-SHA256 signature
            // based on the request parameters, timestamp and the API secret
            try {
                StringBuilder queryString = new StringBuilder();
                queryString.append("timestamp=").append(timestamp);
                
                if (payload != null) {
                    for (String key : payload.keySet()) {
                        queryString.append("&").append(key).append("=").append(payload.get(key));
                    }
                }
                
                javax.crypto.Mac hmacSha256 = javax.crypto.Mac.getInstance("HmacSHA256");
                javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    apiSecret.getBytes(), "HmacSHA256");
                hmacSha256.init(secretKeySpec);
                
                byte[] hash = hmacSha256.doFinal(queryString.toString().getBytes());
                return bytesToHex(hash);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating signature", e);
                return "";
            }
        }
        
        private String bytesToHex(byte[] bytes) {
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }
    
    // Getters
    
    public String getExchangeId() {
        return exchangeId;
    }
    
    public String getExchangeName() {
        return exchangeName;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Represents a cryptocurrency asset held in an exchange
     */
    public static class CryptoAsset {
        private String symbol;
        private float amount;
        private float currentPrice;
        
        public CryptoAsset(String symbol, float amount, float currentPrice) {
            this.symbol = symbol;
            this.amount = amount;
            this.currentPrice = currentPrice;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public float getAmount() {
            return amount;
        }
        
        public float getCurrentPrice() {
            return currentPrice;
        }
        
        public float getTotalValue() {
            return amount * currentPrice;
        }
        
        @Override
        public String toString() {
            return "CryptoAsset{" +
                   "symbol='" + symbol + '\'' +
                   ", amount=" + amount +
                   ", currentPrice=" + currentPrice +
                   ", totalValue=" + getTotalValue() +
                   '}';
        }
    }
}