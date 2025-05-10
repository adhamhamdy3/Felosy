package felosy.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents a connection to a bank account for fetching transactions and balances
 */
public class BankAccount {
    private String accountId;
    private String bankName;
    private float balance;
    private String accessToken;
    private ExternalAccountConnector connector;
    private static final Logger LOGGER = Logger.getLogger(BankAccount.class.getName());

    public BankAccount(String accountId, String bankName) {
        this.accountId = accountId;
        this.bankName = bankName;
        this.balance = 0.0f;
    }
    
    /**
     * Establishes connection to the bank's API
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            // Create appropriate connector based on bank name
            String baseUrl = getBankApiUrl(bankName);
            String apiKey = getStoredApiKey(bankName, accountId);
            
            if (apiKey == null || baseUrl == null) {
                LOGGER.severe("Missing API key or base URL for " + bankName);
                return false;
            }
            
            connector = new ExternalAccountConnector("bank-" + bankName.toLowerCase(), apiKey, baseUrl);
            boolean connected = connector.establishConnection();
            
            if (connected) {
                // Fetch initial balance upon successful connection
                this.balance = getBalance();
                LOGGER.info("Successfully connected to bank account: " + accountId + " at " + bankName);
            }
            
            return connected;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to bank account", e);
            return false;
        }
    }
    
    /**
     * Fetches transactions from the bank account
     * @return List of transactions
     */
    public List<Transaction> fetchTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Connector not initialized or connected");
            return transactions;
        }
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("accountId", accountId);
            payload.put("startDate", getLastMonth());
            payload.put("endDate", getCurrentDate());
            
            Response response = connector.sendRequest("/transactions", "POST", payload);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                JSONArray txArray = responseJson.getJSONArray("transactions");
                
                for (int i = 0; i < txArray.length(); i++) {
                    JSONObject tx = txArray.getJSONObject(i);
                    Transaction transaction = new Transaction(
                        tx.getString("id"),
                        tx.getFloat("amount"),
                        tx.getString("description"),
                        new Date(tx.getLong("timestamp")),
                        tx.getString("category")
                    );
                    transactions.add(transaction);
                }
                
                LOGGER.info("Successfully fetched " + transactions.size() + " transactions");
            } else {
                LOGGER.warning("Failed to fetch transactions: " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching transactions", e);
        }
        
        return transactions;
    }
    
    /**
     * Gets the current balance of the bank account
     * @return the account balance
     */
    public float getBalance() {
        if (connector == null || !connector.isConnected()) {
            LOGGER.warning("Cannot get balance: Connector not initialized or connected");
            return this.balance; // Return last known balance
        }
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("accountId", accountId);
            
            Response response = connector.sendRequest("/balance", "POST", payload);
            
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.getBody());
                this.balance = responseJson.getFloat("balance");
                LOGGER.info("Successfully fetched balance: " + this.balance);
            } else {
                LOGGER.warning("Failed to fetch balance: " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching balance", e);
        }
        
        return this.balance;
    }
    
    /**
     * Disconnects from the bank's API
     * @return true if disconnection was successful
     */
    public boolean disconnect() {
        if (connector != null && connector.isConnected()) {
            boolean result = connector.closeConnection();
            if (result) {
                LOGGER.info("Successfully disconnected from bank account: " + accountId);
            } else {
                LOGGER.warning("Problem while disconnecting from bank account: " + accountId);
            }
            return result;
        }
        return true; // Already disconnected
    }
    
    // Helper methods
    
    private String getBankApiUrl(String bankName) {
        // In a real implementation, this could come from a configuration file or database
        switch (bankName.toLowerCase()) {
            case "chase":
                return "https://api.chase.com/v1";
            case "bank of america":
                return "https://api.bankofamerica.com/v1";
            case "wells fargo":
                return "https://api.wellsfargo.com/v1";
            case "citi":
                return "https://api.citi.com/v1";
            default:
                // Use a generic financial data aggregator like Plaid for unsupported banks
                return "https://api.financialaggregator.com/v1";
        }
    }
    
    private String getStoredApiKey(String bankName, String accountId) {
        // In a real implementation, this would retrieve securely stored API keys
        // This is just a placeholder - never hardcode actual API keys
        return "sk_" + bankName.toLowerCase().replace(" ", "") + "_" + accountId.substring(0, 4);
    }
    
    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
    
    private String getLastMonth() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -1);
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    // Getters and Setters
    
    public String getAccountId() {
        return accountId;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    /**
     * Represents a bank transaction
     */
    public static class Transaction {
        private String id;
        private float amount;
        private String description;
        private Date date;
        private String category;
        
        public Transaction(String id, float amount, String description, Date date, String category) {
            this.id = id;
            this.amount = amount;
            this.description = description;
            this.date = date;
            this.category = category;
        }
        
        public String getId() {
            return id;
        }
        
        public float getAmount() {
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
        
        @Override
        public String toString() {
            return "Transaction{" +
                   "id='" + id + '\'' +
                   ", amount=" + amount +
                   ", description='" + description + '\'' +
                   ", date=" + date +
                   ", category='" + category + '\'' +
                   '}';
        }
    }
}