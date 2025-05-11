package felosy.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.json.JSONObject;

/**
 * Represents a connection to a bank account for fetching transactions and balances
 * Simplified for academic purposes
 */
public class BankAccount extends IslamicFinanceConnector {
    private static final Logger LOGGER = Logger.getLogger(BankAccount.class.getName());
    
    private String accountNumber;
    private String accountType; // SAVINGS, CURRENT, INVESTMENT
    private double balance;
    private Map<String, Double> profitRates;
    private boolean isMudarabahAccount;
    private List<Transaction> transactions;

    public BankAccount(String accountNumber, String accountType) {
        super("bank-" + accountType.toLowerCase(), 
              "demo_key", 
              "http://demo.api.bank.com/v1", 
              "ISLAMIC_BANK");
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = 0.0;
        this.profitRates = new HashMap<>();
        this.isMudarabahAccount = false;
        this.transactions = new ArrayList<>();
        initializeProfitRates();
    }
    
    private void initializeProfitRates() {
        // Simplified profit rates for academic purposes
        profitRates.put("SAVINGS", 0.05); // 5% profit sharing rate
        profitRates.put("INVESTMENT", 0.08); // 8% profit sharing rate
    }
    
    /**
     * Establishes connection to the bank's API
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            boolean connected = super.establishConnection();
            if (connected) {
                LOGGER.info("Successfully connected to bank: " + accountType);
            }
            return connected;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to bank", e);
            return false;
        }
    }
    
    /**
     * Deposits money into the account
     */
    public boolean deposit(double amount) {
        try {
            if (!isShariahCompliant()) {
                LOGGER.warning("Cannot perform transaction: Bank is not Shariah compliant");
                return false;
            }
            
            balance += amount;
            transactions.add(new Transaction(
                "TX" + System.currentTimeMillis(),
                amount,
                "Deposit",
                new Date(),
                "DEPOSIT"
            ));
            
            LOGGER.info("Deposit successful. New balance: " + balance);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing deposit", e);
            return false;
        }
    }
    
    /**
     * Withdraws money from the account
     */
    public boolean withdraw(double amount) {
        try {
            if (!isShariahCompliant()) {
                LOGGER.warning("Cannot perform transaction: Bank is not Shariah compliant");
                return false;
            }
            
            if (amount > balance) {
                LOGGER.warning("Insufficient funds for withdrawal");
                return false;
            }
            
            balance -= amount;
            transactions.add(new Transaction(
                "TX" + System.currentTimeMillis(),
                -amount,
                "Withdrawal",
                new Date(),
                "WITHDRAWAL"
            ));
            
            LOGGER.info("Withdrawal successful. New balance: " + balance);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing withdrawal", e);
            return false;
        }
    }
    
    /**
     * Transfers money to another account
     */
    public boolean transfer(String targetAccount, double amount) {
        try {
            if (!isShariahCompliant()) {
                LOGGER.warning("Cannot perform transaction: Bank is not Shariah compliant");
                return false;
            }
            
            if (amount > balance) {
                LOGGER.warning("Insufficient funds for transfer");
                return false;
            }
            
            balance -= amount;
            transactions.add(new Transaction(
                "TX" + System.currentTimeMillis(),
                -amount,
                "Transfer to " + targetAccount,
                new Date(),
                "TRANSFER"
            ));
            
            LOGGER.info("Transfer successful. New balance: " + balance);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing transfer", e);
            return false;
        }
    }
    
    /**
     * Gets the current balance
     */
    public double getBalance() {
        return balance;
    }
    
    /**
     * Gets the profit sharing rate for the account
     */
    public double getProfitRate() {
        return profitRates.getOrDefault(accountType, 0.0);
    }
    
    /**
     * Gets the transaction history
     */
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
    
    /**
     * Checks if the account is a Mudarabah account
     */
    public boolean isMudarabahAccount() {
        return isMudarabahAccount;
    }
    
    /**
     * Gets the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * Gets the account type
     */
    public String getAccountType() {
        return accountType;
    }
    
    /**
     * Represents a bank transaction
     */
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