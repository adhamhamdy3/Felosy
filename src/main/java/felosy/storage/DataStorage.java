package felosy.storage;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import felosy.assetmanagement.*;
import felosy.authentication.User;
import felosy.islamicfinance.*;
import felosy.islamicfinance.model.ComplianceRule;
import felosy.reporting.Report;

/**
 * Handles data persistence for the entire application using serialization
 * Provides thread-safe operations for storing and retrieving application data
 * with transaction-like semantics and automatic backup functionality.
 */
public class DataStorage {
    private static final Logger LOGGER = Logger.getLogger(DataStorage.class.getName());
    
    // Directory paths
    private static final String DATA_DIR = "data";
    private static final String BACKUP_DIR = "data/backups";
    private static final String TEMP_DIR = "data/temp";
    
    // File names for different data types
    private static final String USERS_FILE = "users.dat";
    private static final String PORTFOLIOS_FILE = "portfolios.dat";
    private static final String COMPLIANCE_RULES_FILE = "compliance_rules.dat";
    private static final String REPORTS_FILE = "reports.dat";
    
    // Thread safety locks
    private static final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock portfoliosLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock rulesLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock reportsLock = new ReentrantReadWriteLock();
    
    // In-memory caches
    private static Map<String, User> userCache = new HashMap<>();
    private static Map<String, Portfolio> portfolioCache = new HashMap<>();
    private static boolean cacheInitialized = false;
    
    /**
     * Initialize the data storage system
     * Creates necessary directories if they don't exist
     */
    public static void initialize() {
        try {
            // Create data directories
            createDirectories();
            
            // Initialize caches
            initializeCaches();
            
            LOGGER.info("DataStorage system initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize data storage system", e);
            throw new RuntimeException("Failed to initialize data storage system", e);
        }
    }
    
    /**
     * Creates the necessary directories for data storage
     */
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(TEMP_DIR));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create data directories", e);
            throw new RuntimeException("Failed to create data directories", e);
        }
    }
    
    /**
     * Initialize the in-memory caches from disk
     */
    private static void initializeCaches() {
        if (cacheInitialized) {
            return;
        }
        
        // Load users
        Map<String, User> loadedUsers = loadUsers();
        if (loadedUsers != null) {
            userCache = loadedUsers;
        }
        
        // Load portfolios
        Map<String, Portfolio> loadedPortfolios = loadPortfolios();
        if (loadedPortfolios != null) {
            portfolioCache = loadedPortfolios;
        }
        
        cacheInitialized = true;
    }
    
    /**
     * Save a user to persistent storage
     * 
     * @param user The user to save
     * @return true if the operation was successful
     */
    public static boolean saveUser(User user) {
        if (user == null) {
            LOGGER.warning("Attempted to save null user");
            return false;
        }
        
        usersLock.writeLock().lock();
        try {
            // Update cache
            userCache.put(user.getUserId(), user);
            
            // Save to disk with transaction-like semantics
            return saveObjectToFile(userCache, USERS_FILE);
        } finally {
            usersLock.writeLock().unlock();
        }
    }
    
    /**
     * Save multiple users to persistent storage in a single transaction
     * 
     * @param users The users to save
     * @return true if the operation was successful
     */
    public static boolean saveUsers(Collection<User> users) {
        if (users == null || users.isEmpty()) {
            LOGGER.warning("Attempted to save null or empty users collection");
            return false;
        }
        
        usersLock.writeLock().lock();
        try {
            // Update cache
            for (User user : users) {
                userCache.put(user.getUserId(), user);
            }
            
            // Save to disk
            return saveObjectToFile(userCache, USERS_FILE);
        } finally {
            usersLock.writeLock().unlock();
        }
    }
    
    /**
     * Load a user from persistent storage
     * 
     * @param userId The ID of the user to load
     * @return The loaded user, or null if not found
     */
    public static User loadUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to load user with null or empty ID");
            return null;
        }
        
        usersLock.readLock().lock();
        try {
            // Ensure cache is initialized
            if (!cacheInitialized) {
                initializeCaches();
            }
            
            return userCache.get(userId);
        } finally {
            usersLock.readLock().unlock();
        }
    }
    
    /**
     * Load all users from persistent storage
     * 
     * @return A map of user IDs to users
     */
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        usersLock.readLock().lock();
        try {
            Object obj = loadObjectFromFile(USERS_FILE);
            if (obj instanceof Map) {
                return (Map<String, User>) obj;
            } else {
                LOGGER.warning("Loaded users object is not a Map");
                return new HashMap<>();
            }
        } finally {
            usersLock.readLock().unlock();
        }
    }
    
    /**
     * Delete a user from persistent storage
     * 
     * @param userId The ID of the user to delete
     * @return true if the operation was successful
     */
    public static boolean deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to delete user with null or empty ID");
            return false;
        }
        
        usersLock.writeLock().lock();
        try {
            // Remove from cache
            userCache.remove(userId);
            
            // Save updated cache to disk
            return saveObjectToFile(userCache, USERS_FILE);
        } finally {
            usersLock.writeLock().unlock();
        }
    }
    
    /**
     * Save a portfolio to persistent storage
     * 
     * @param portfolio The portfolio to save
     * @return true if the operation was successful
     */
    public static boolean savePortfolio(Portfolio portfolio) {
        if (portfolio == null) {
            LOGGER.warning("Attempted to save null portfolio");
            return false;
        }
        
        portfoliosLock.writeLock().lock();
        try {
            // Update cache
            portfolioCache.put(portfolio.getPortfolioId(), portfolio);
            
            // Save to disk
            return saveObjectToFile(portfolioCache, PORTFOLIOS_FILE);
        } finally {
            portfoliosLock.writeLock().unlock();
        }
    }
    
    /**
     * Save multiple portfolios to persistent storage in a single transaction
     * 
     * @param portfolios The portfolios to save
     * @return true if the operation was successful
     */
    public static boolean savePortfolios(Collection<Portfolio> portfolios) {
        if (portfolios == null || portfolios.isEmpty()) {
            LOGGER.warning("Attempted to save null or empty portfolios collection");
            return false;
        }
        
        portfoliosLock.writeLock().lock();
        try {
            // Update cache
            for (Portfolio portfolio : portfolios) {
                portfolioCache.put(portfolio.getPortfolioId(), portfolio);
            }
            
            // Save to disk
            return saveObjectToFile(portfolioCache, PORTFOLIOS_FILE);
        } finally {
            portfoliosLock.writeLock().unlock();
        }
    }
    
    /**
     * Load a portfolio from persistent storage
     * 
     * @param portfolioId The ID of the portfolio to load
     * @return The loaded portfolio, or null if not found
     */
    public static Portfolio loadPortfolio(String portfolioId) {
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            LOGGER.warning("Attempted to load portfolio with null or empty ID");
            return null;
        }
        
        portfoliosLock.readLock().lock();
        try {
            // Ensure cache is initialized
            if (!cacheInitialized) {
                initializeCaches();
            }
            
            return portfolioCache.get(portfolioId);
        } finally {
            portfoliosLock.readLock().unlock();
        }
    }
    
    /**
     * Load all portfolios from persistent storage
     * 
     * @return A map of portfolio IDs to portfolios
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Portfolio> loadPortfolios() {
        portfoliosLock.readLock().lock();
        try {
            Object obj = loadObjectFromFile(PORTFOLIOS_FILE);
            if (obj instanceof Map) {
                return (Map<String, Portfolio>) obj;
            } else {
                LOGGER.warning("Loaded portfolios object is not a Map");
                return new HashMap<>();
            }
        } finally {
            portfoliosLock.readLock().unlock();
        }
    }
    
    /**
     * Load all portfolios for a specific user
     * 
     * @param userId The ID of the user
     * @return A list of portfolios belonging to the user
     */
    public static List<Portfolio> loadUserPortfolios(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("Attempted to load portfolios for null or empty user ID");
            return new ArrayList<>();
        }
        
        portfoliosLock.readLock().lock();
        try {
            // Ensure cache is initialized
            if (!cacheInitialized) {
                initializeCaches();
            }
            
            List<Portfolio> userPortfolios = new ArrayList<>();
            for (Portfolio portfolio : portfolioCache.values()) {
                if (userId.equals(portfolio.getUserId())) {
                    userPortfolios.add(portfolio);
                }
            }
            
            return userPortfolios;
        } finally {
            portfoliosLock.readLock().unlock();
        }
    }
    
    /**
     * Delete a portfolio from persistent storage
     * 
     * @param portfolioId The ID of the portfolio to delete
     * @return true if the operation was successful
     */
    public static boolean deletePortfolio(String portfolioId) {
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            LOGGER.warning("Attempted to delete portfolio with null or empty ID");
            return false;
        }
        
        portfoliosLock.writeLock().lock();
        try {
            // Remove from cache
            portfolioCache.remove(portfolioId);
            
            // Save updated cache to disk
            return saveObjectToFile(portfolioCache, PORTFOLIOS_FILE);
        } finally {
            portfoliosLock.writeLock().unlock();
        }
    }
    
    /**
     * Create a backup of all data files
     * 
     * @return true if the backup was successful
     */
    public static boolean createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = BACKUP_DIR + "/backup_" + timestamp;
            Files.createDirectories(Paths.get(backupPath));
            
            // Backup users
            if (Files.exists(Paths.get(DATA_DIR, USERS_FILE))) {
                Files.copy(Paths.get(DATA_DIR, USERS_FILE), Paths.get(backupPath, USERS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Backup portfolios
            if (Files.exists(Paths.get(DATA_DIR, PORTFOLIOS_FILE))) {
                Files.copy(Paths.get(DATA_DIR, PORTFOLIOS_FILE), Paths.get(backupPath, PORTFOLIOS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Backup compliance rules
            if (Files.exists(Paths.get(DATA_DIR, COMPLIANCE_RULES_FILE))) {
                Files.copy(Paths.get(DATA_DIR, COMPLIANCE_RULES_FILE), Paths.get(backupPath, COMPLIANCE_RULES_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Backup reports
            if (Files.exists(Paths.get(DATA_DIR, REPORTS_FILE))) {
                Files.copy(Paths.get(DATA_DIR, REPORTS_FILE), Paths.get(backupPath, REPORTS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            LOGGER.info("Created backup at " + backupPath);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create backup", e);
            return false;
        }
    }
    
    /**
     * Restore from a backup
     * 
     * @param backupDir The backup directory to restore from
     * @return true if the restore was successful
     */
    public static boolean restoreFromBackup(String backupDir) {
        if (backupDir == null || backupDir.trim().isEmpty()) {
            LOGGER.warning("Attempted to restore from null or empty backup directory");
            return false;
        }
        
        try {
            // Create a backup of current data before restoring
            createBackup();
            
            // Restore users
            if (Files.exists(Paths.get(backupDir, USERS_FILE))) {
                Files.copy(Paths.get(backupDir, USERS_FILE), Paths.get(DATA_DIR, USERS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Restore portfolios
            if (Files.exists(Paths.get(backupDir, PORTFOLIOS_FILE))) {
                Files.copy(Paths.get(backupDir, PORTFOLIOS_FILE), Paths.get(DATA_DIR, PORTFOLIOS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Restore compliance rules
            if (Files.exists(Paths.get(backupDir, COMPLIANCE_RULES_FILE))) {
                Files.copy(Paths.get(backupDir, COMPLIANCE_RULES_FILE), Paths.get(DATA_DIR, COMPLIANCE_RULES_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Restore reports
            if (Files.exists(Paths.get(backupDir, REPORTS_FILE))) {
                Files.copy(Paths.get(backupDir, REPORTS_FILE), Paths.get(DATA_DIR, REPORTS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Reset caches
            cacheInitialized = false;
            initializeCaches();
            
            LOGGER.info("Restored from backup at " + backupDir);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to restore from backup", e);
            return false;
        }
    }
    
    /**
     * List all available backups
     * 
     * @return A list of backup directory names
     */
    public static List<String> listBackups() {
        try {
            List<String> backups = new ArrayList<>();
            Files.list(Paths.get(BACKUP_DIR))
                .filter(Files::isDirectory)
                .forEach(path -> backups.add(path.getFileName().toString()));
            return backups;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to list backups", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Save an object to a file with transaction-like semantics
     * Uses a temporary file to ensure atomicity
     * 
     * @param object The object to save
     * @param filename The filename to save to
     * @return true if the operation was successful
     */
    private static boolean saveObjectToFile(Object object, String filename) {
        // Create a temporary file
        String tempFilename = TEMP_DIR + "/" + UUID.randomUUID().toString() + ".tmp";
        File tempFile = new File(tempFilename);
        File targetFile = new File(DATA_DIR, filename);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            // Write object to temporary file
            oos.writeObject(object);
            oos.flush();
            
            // Rename temporary file to target file (atomic operation)
            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            
            LOGGER.fine("Successfully saved object to " + filename);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save object to " + filename, e);
            // Clean up temporary file if it exists
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return false;
        }
    }
    
    /**
     * Load an object from a file
     * 
     * @param filename The filename to load from
     * @return The loaded object, or null if an error occurs
     */
    private static Object loadObjectFromFile(String filename) {
        File file = new File(DATA_DIR, filename);
        
        // Check if file exists
        if (!file.exists()) {
            LOGGER.fine("File does not exist: " + filename);
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            LOGGER.fine("Successfully loaded object from " + filename);
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load object from " + filename, e);
            return null;
        }
    }
    
    /**
     * Clear all data (for testing purposes)
     */
    public static void clearAllData() {
        usersLock.writeLock().lock();
        portfoliosLock.writeLock().lock();
        rulesLock.writeLock().lock();
        reportsLock.writeLock().lock();
        
        try {
            // Clear caches
            userCache.clear();
            portfolioCache.clear();
            
            // Delete files
            new File(DATA_DIR, USERS_FILE).delete();
            new File(DATA_DIR, PORTFOLIOS_FILE).delete();
            new File(DATA_DIR, COMPLIANCE_RULES_FILE).delete();
            new File(DATA_DIR, REPORTS_FILE).delete();
            
            LOGGER.warning("All data has been cleared");
        } finally {
            reportsLock.writeLock().unlock();
            rulesLock.writeLock().unlock();
            portfoliosLock.writeLock().unlock();
            usersLock.writeLock().unlock();
        }
    }
}