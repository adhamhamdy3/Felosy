package felosy.integration;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Represents a connection to a stock trading platform
 */
public class StockMarketAccount extends IslamicFinanceConnector {
    private String accountId;
    private String platformName;
    private ExternalAccountConnector connector;
    private boolean isConnected;
    private static final Logger LOGGER = Logger.getLogger(StockMarketAccount.class.getName());
    
    private Map<String, Double> portfolio;
    private Map<String, Boolean> shariahCompliance;
    private List<Transaction> transactionHistory;
    private double totalValue;
    
    public StockMarketAccount(String accountId, String platformName) {
        super("stock-" + platformName.toLowerCase(), 
              "sk_" + platformName.toLowerCase().replace(" ", ""), 
              getPlatformApiUrl(platformName), 
              "ISLAMIC_STOCK_MARKET");
        this.accountId = accountId;
        this.platformName = platformName;
        this.isConnected = false;
        this.portfolio = new HashMap<>();
        this.shariahCompliance = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
        this.totalValue = 0.0;
    }
    
    /**
     * Establishes connection to the stock platform's API
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            isConnected = super.establishConnection();
            
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
    
    /**
     * Buys stocks that are Shariah compliant
     * @param symbol Stock symbol
     * @param quantity Number of shares to buy
     * @return true if purchase is successful
     */
    public boolean buyStock(String symbol, int quantity) {
        try {
            if (!isShariahCompliant()) {
                LOGGER.warning("Cannot perform transaction: Market is not Shariah compliant");
                return false;
            }
            
            // Check if the stock is Shariah compliant
            if (!isStockShariahCompliant(symbol)) {
                LOGGER.warning("Cannot buy stock: " + symbol + " is not Shariah compliant");
                return false;
            }
            
            JSONObject payload = new JSONObject();
            payload.put("accountNumber", accountId);
            payload.put("symbol", symbol);
            payload.put("quantity", quantity);
            payload.put("transactionType", "BUY");
            
            Response response = sendRequest("/stocks/buy", "POST", payload);
            if (response.isSuccessful()) {
                JSONObject result = new JSONObject(response.getBody());
                double price = result.optDouble("price", 0.0);
                double totalCost = price * quantity;
                
                // Update portfolio
                portfolio.put(symbol, portfolio.getOrDefault(symbol, 0.0) + quantity);
                totalValue += totalCost;
                
                // Add to transaction history
                transactionHistory.add(new Transaction(
                    result.optString("transactionId"),
                    totalCost,
                    "Buy " + quantity + " shares of " + symbol,
                    new Date(),
                    "STOCK_PURCHASE"
                ));
                
                LOGGER.info("Successfully bought " + quantity + " shares of " + symbol);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buying stock", e);
            return false;
        }
    }
    
    /**
     * Sells stocks
     * @param symbol Stock symbol
     * @param quantity Number of shares to sell
     * @return true if sale is successful
     */
    public boolean sellStock(String symbol, int quantity) {
        try {
            if (!isShariahCompliant()) {
                LOGGER.warning("Cannot perform transaction: Market is not Shariah compliant");
                return false;
            }
            
            // Check if we have enough shares
            if (!portfolio.containsKey(symbol) || portfolio.get(symbol) < quantity) {
                LOGGER.warning("Insufficient shares of " + symbol + " to sell");
                return false;
            }
            
            JSONObject payload = new JSONObject();
            payload.put("accountNumber", accountId);
            payload.put("symbol", symbol);
            payload.put("quantity", quantity);
            payload.put("transactionType", "SELL");
            
            Response response = sendRequest("/stocks/sell", "POST", payload);
            if (response.isSuccessful()) {
                JSONObject result = new JSONObject(response.getBody());
                double price = result.optDouble("price", 0.0);
                double totalValue = price * quantity;
                
                // Update portfolio
                portfolio.put(symbol, portfolio.get(symbol) - quantity);
                if (portfolio.get(symbol) == 0) {
                    portfolio.remove(symbol);
                }
                this.totalValue -= totalValue;
                
                // Add to transaction history
                transactionHistory.add(new Transaction(
                    result.optString("transactionId"),
                    totalValue,
                    "Sell " + quantity + " shares of " + symbol,
                    new Date(),
                    "STOCK_SALE"
                ));
                
                LOGGER.info("Successfully sold " + quantity + " shares of " + symbol);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error selling stock", e);
            return false;
        }
    }
    
    /**
     * Gets the current portfolio value
     * @return Total portfolio value
     */
    public double getPortfolioValue() {
        try {
            Response response = sendRequest("/portfolio/value", "GET", null);
            if (response.isSuccessful()) {
                JSONObject result = new JSONObject(response.getBody());
                totalValue = result.optDouble("totalValue", totalValue);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching portfolio value", e);
        }
        return totalValue;
    }
    
    /**
     * Gets the current portfolio holdings
     * @return Map of stock symbols to quantities
     */
    public Map<String, Double> getPortfolio() {
        return new HashMap<>(portfolio);
    }
    
    /**
     * Gets the transaction history
     * @return List of transactions
     */
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
    
    /**
     * Checks if a stock is Shariah compliant
     * @param symbol Stock symbol
     * @return true if the stock is Shariah compliant
     */
    public boolean isStockShariahCompliant(String symbol) {
        try {
            Response response = sendRequest("/stocks/" + symbol + "/compliance", "GET", null);
            if (response.isSuccessful()) {
                JSONObject result = new JSONObject(response.getBody());
                boolean compliant = result.optBoolean("shariahCompliant", false);
                shariahCompliance.put(symbol, compliant);
                return compliant;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking stock compliance", e);
            return false;
        }
    }
    
    /**
     * Gets the Shariah compliance status of all stocks in the portfolio
     * @return Map of stock symbols to compliance status
     */
    public Map<String, Boolean> getPortfolioCompliance() {
        return new HashMap<>(shariahCompliance);
    }
    
    // Helper methods
    
    private static String getPlatformApiUrl(String platformName) {
        // In a real implementation, this could come from a configuration file or database
        switch (platformName.toLowerCase()) {
            case "robinhood":
                return "https://api.robinhood.com/v1";
            case "etrade":
                return "https://api.etrade.com/v1";
            case "fidelity":
                return "https://api.fidelity.com/v1";
            case "charles schwab":
                return "https://api.schwab.com/v1";
            default:
                // Use a generic financial data aggregator like Alpaca for unsupported platforms
                return "https://api.alpaca.markets/v2";
        }
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
    
    public static class Transaction {
        private String id;
        private double amount;
        private String description;
        private Date date;
        private String category;
        
        public Transaction(String id, double amount, String description, Date date, String category) {
            this.id = id;
            this.amount = amount;
            this.description = description;
            this.date = date;
            this.category = category;
        }
        
        public String getId() {
            return id;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Date getDate() {
            return date;
        }
        
        public String getCategory() {
            return category;
        }
    }
}
