package felosy.utils;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataStorage {
    private static final String DATA_DIR = "data";
    private static final String REPORTS_DIR = "reports";
    private static final String USERS_FILE = "users.ser";
    private static final String PORTFOLIOS_FILE = "portfolios.ser";
    private static final String ASSETS_FILE = "assets.ser";
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        // Create data and reports directories if they don't exist
        createDirectoryIfNotExists(DATA_DIR);
        createDirectoryIfNotExists(REPORTS_DIR);
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String getReportPath(String reportType) {
        String timestamp = LocalDateTime.now().format(REPORT_DATE_FORMAT);
        return REPORTS_DIR + File.separator + reportType + "_" + timestamp + ".txt";
    }

    public static void saveReport(String reportType, String content) throws IOException {
        String reportPath = getReportPath(reportType);
        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(content);
        }
    }

    public static <T> void saveObject(T object, String filename) throws IOException {
        String filePath = DATA_DIR + File.separator + filename;
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
        }
    }

    public static <T> T loadObject(String filename) throws IOException, ClassNotFoundException {
        String filePath = DATA_DIR + File.separator + filename;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (T) ois.readObject();
        }
    }

    public static void saveUsers(List<felosy.authentication.User> users) throws IOException {
        saveObject(users, USERS_FILE);
    }

    public static List<felosy.authentication.User> loadUsers() throws IOException, ClassNotFoundException {
        try {
            return loadObject(USERS_FILE);
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public static void savePortfolios(List<felosy.assetmanagement.Portfolio> portfolios) throws IOException {
        saveObject(portfolios, PORTFOLIOS_FILE);
    }

    public static List<felosy.assetmanagement.Portfolio> loadPortfolios() throws IOException, ClassNotFoundException {
        try {
            return loadObject(PORTFOLIOS_FILE);
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public static void saveAssets(List<felosy.assetmanagement.Asset> assets) throws IOException {
        saveObject(assets, ASSETS_FILE);
    }

    public static List<felosy.assetmanagement.Asset> loadAssets() throws IOException, ClassNotFoundException {
        try {
            return loadObject(ASSETS_FILE);
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }
} 