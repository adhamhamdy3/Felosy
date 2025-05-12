package felosy.utils;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for managing data persistence in the Felosy system.
 * Handles serialization of users, portfolios, assets, and reports.
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

    // --- Directory Utilities ---

    /**
     * Ensures a directory exists, creating it if necessary.
     */
    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.warning("Failed to create directory: " + dirPath);
        }
    }

    /**
     * Returns the path for a new report file.
     */
    public static String getReportPath(String reportType) {
        String timestamp = LocalDateTime.now().format(REPORT_DATE_FORMAT);
        return REPORTS_DIR + File.separator + reportType + "_" + timestamp + ".txt";
    }

    /**
     * Returns the path to the reports directory.
     */
    public static String getReportsDirectory() {
        return REPORTS_DIR;
    }

    // --- Report Methods ---

    /**
     * Saves a report to a file.
     * @param reportType The type of report.
     * @param content The report content.
     * @throws IOException if saving fails.
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

    // --- Serialization Utilities ---

    /**
     * Saves an object to a file using serialization.
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
     * Loads an object from a file using deserialization.
     */
    @SuppressWarnings("unchecked")
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

    // --- User Data Methods ---

    /**
     * Saves the list of users.
     */
    public static void saveUsers(List<felosy.authentication.User> users) throws IOException {
        saveObject(users, USERS_FILE);
    }

    /**
     * Loads the list of users.
     */
    public static List<felosy.authentication.User> loadUsers() throws IOException, ClassNotFoundException {
        return loadObject(USERS_FILE);
    }

    // --- Portfolio Data Methods ---

    /**
     * Saves the list of portfolios.
     */
    public static void savePortfolios(List<felosy.assetmanagement.Portfolio> portfolios) throws IOException {
        saveObject(portfolios, PORTFOLIOS_FILE);
    }

    /**
     * Loads the list of portfolios.
     */
    public static List<felosy.assetmanagement.Portfolio> loadPortfolios() throws IOException, ClassNotFoundException {
        return loadObject(PORTFOLIOS_FILE);
    }

    // --- Asset Data Methods ---

    /**
     * Saves the list of assets.
     */
    public static void saveAssets(List<felosy.assetmanagement.Asset> assets) throws IOException {
        saveObject(assets, ASSETS_FILE);
    }

    /**
     * Loads the list of assets.
     */
    public static List<felosy.assetmanagement.Asset> loadAssets() throws IOException, ClassNotFoundException {
        return loadObject(ASSETS_FILE);
    }

    // --- Backup and Clear Methods ---

    /**
     * Creates a backup of all data files.
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
     * Backs up a single file.
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
     * Clears all serialized data (users, portfolios, assets, and reports).
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
     * Deletes a single file from the data directory.
     */
    private static void deleteFile(String filename) {
        File file = new File(DATA_DIR + File.separator + filename);
        if (file.exists() && file.delete()) {
            LOGGER.info("Deleted file: " + filename);
        }
    }

    /**
     * Clears all files in a directory.
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
} 