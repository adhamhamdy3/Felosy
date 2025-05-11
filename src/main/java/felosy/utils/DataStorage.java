package felosy.utils;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for managing data persistence in the Felosy system
 * Handles serialization of users, portfolios, assets, and reports
 */
public class DataStorage {
    private static final Logger LOGGER = Logger.getLogger(DataStorage.class.getName());
    
    private static final String DATA_DIR = "data";
    private static final String REPORTS_DIR = "reports";
    private static final String BACKUP_DIR = "backups";
    
    private static final String USERS_FILE = "users.ser";
    private static final String PORTFOLIOS_FILE = "portfolios.ser";
    private static final String ASSETS_FILE = "assets.ser";
    
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        // Create necessary directories
        createDirectoryIfNotExists(DATA_DIR);
        createDirectoryIfNotExists(REPORTS_DIR);
        createDirectoryIfNotExists(BACKUP_DIR);
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                LOGGER.info("Created directory: " + dirPath);
            } else {
                LOGGER.warning("Failed to create directory: " + dirPath);
            }
        }
    }

    /**
     * Get the path for a new report file
     */
    public static String getReportPath(String reportType) {
        String timestamp = LocalDateTime.now().format(REPORT_DATE_FORMAT);
        return REPORTS_DIR + File.separator + reportType + "_" + timestamp + ".txt";
    }

    /**
     * Save a report to a file
     */
    public static void saveReport(String reportType, String content) throws IOException {
        String reportPath = getReportPath(reportType);
        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(content);
            LOGGER.info("Report saved to: " + reportPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save report: " + reportPath, e);
            throw e;
        }
    }

    /**
     * Save an object to a file using serialization
     */
    private static <T> void saveObject(T object, String filename) throws IOException {
        String filePath = DATA_DIR + File.separator + filename;
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            LOGGER.info("Object saved to: " + filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save object: " + filePath, e);
            throw e;
        }
    }

    /**
     * Load an object from a file using deserialization
     */
    private static <T> T loadObject(String filename) throws IOException, ClassNotFoundException {
        String filePath = DATA_DIR + File.separator + filename;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            T object = (T) ois.readObject();
            LOGGER.info("Object loaded from: " + filePath);
            return object;
        } catch (FileNotFoundException e) {
            LOGGER.info("File not found: " + filePath + ", returning empty list");
            return (T) new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load object: " + filePath, e);
            throw e;
        }
    }

    /**
     * Create a backup of all data files
     */
    public static void createBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
        String backupPath = BACKUP_DIR + File.separator + "backup_" + timestamp;
        createDirectoryIfNotExists(backupPath);
        
        // Backup all data files
        backupFile(USERS_FILE, backupPath);
        backupFile(PORTFOLIOS_FILE, backupPath);
        backupFile(ASSETS_FILE, backupPath);
        
        LOGGER.info("Backup created at: " + backupPath);
    }

    /**
     * Backup a single file
     */
    private static void backupFile(String filename, String backupPath) throws IOException {
        File sourceFile = new File(DATA_DIR + File.separator + filename);
        if (sourceFile.exists()) {
            File destFile = new File(backupPath + File.separator + filename);
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
    }

    /**
     * Clear all serialized data
     */
    public static void clearAllData() throws IOException {
        // Create backup before clearing
        createBackup();
        
        // Delete all data files
        deleteFile(USERS_FILE);
        deleteFile(PORTFOLIOS_FILE);
        deleteFile(ASSETS_FILE);
        
        // Clear reports directory
        clearDirectory(REPORTS_DIR);
        
        LOGGER.info("All data cleared successfully");
    }

    /**
     * Delete a single file
     */
    private static void deleteFile(String filename) {
        File file = new File(DATA_DIR + File.separator + filename);
        if (file.exists() && file.delete()) {
            LOGGER.info("Deleted file: " + filename);
        }
    }

    /**
     * Clear all files in a directory
     */
    private static void clearDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        LOGGER.info("Deleted file: " + file.getName());
                    }
                }
            }
        }
    }

    // User data methods
    public static void saveUsers(List<felosy.authentication.User> users) throws IOException {
        saveObject(users, USERS_FILE);
    }

    public static List<felosy.authentication.User> loadUsers() throws IOException, ClassNotFoundException {
        return loadObject(USERS_FILE);
    }

    // Portfolio data methods
    public static void savePortfolios(List<felosy.assetmanagement.Portfolio> portfolios) throws IOException {
        saveObject(portfolios, PORTFOLIOS_FILE);
    }

    public static List<felosy.assetmanagement.Portfolio> loadPortfolios() throws IOException, ClassNotFoundException {
        return loadObject(PORTFOLIOS_FILE);
    }

    // Asset data methods
    public static void saveAssets(List<felosy.assetmanagement.Asset> assets) throws IOException {
        saveObject(assets, ASSETS_FILE);
    }

    public static List<felosy.assetmanagement.Asset> loadAssets() throws IOException, ClassNotFoundException {
        return loadObject(ASSETS_FILE);
    }
} 