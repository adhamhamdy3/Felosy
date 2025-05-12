package felosy.storage;

import java.io.*;
import java.util.*;
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
 */
public class DataStorage {
    private static final Logger LOGGER = Logger.getLogger(DataStorage.class.getName());
    private static final String DATA_DIR = "data";
    private static final String BACKUP_DIR = "data/backups";
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private static DataStorage instance;
    private final Map<String, Object> cache;
    private final Set<String> modifiedEntities;
    
    private DataStorage() {
        this.cache = new HashMap<>();
        this.modifiedEntities = new HashSet<>();
        initializeDirectories();
    }
    
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }
    
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create data directories", e);
        }
    }
    
    // Portfolio Operations
    public void savePortfolio(Portfolio portfolio) {
        if (portfolio == null) return;
        String key = "portfolio_" + portfolio.getPortfolioId();
        cache.put(key, portfolio);
        modifiedEntities.add(key);
        LOGGER.info("Portfolio cached for saving: " + portfolio.getPortfolioId());
    }
    
    public Portfolio loadPortfolio(String portfolioId) {
        String key = "portfolio_" + portfolioId;
        if (cache.containsKey(key)) {
            return (Portfolio) cache.get(key);
        }
        
        try {
            Portfolio portfolio = (Portfolio) deserialize(key);
            if (portfolio != null) {
                cache.put(key, portfolio);
            }
            return portfolio;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load portfolio: " + portfolioId, e);
            return null;
        }
    }
    
    // User Operations
    public void saveUser(User user) {
        if (user == null) return;
        String key = "user_" + user.getUserId();
        cache.put(key, user);
        modifiedEntities.add(key);
        LOGGER.info("User cached for saving: " + user.getUserId());
    }
    
    public User loadUser(String userId) {
        String key = "user_" + userId;
        if (cache.containsKey(key)) {
            return (User) cache.get(key);
        }
        
        try {
            User user = (User) deserialize(key);
            if (user != null) {
                cache.put(key, user);
            }
            return user;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load user: " + userId, e);
            return null;
        }
    }
    
    // Report Operations
    public void saveReport(Report report) {
        if (report == null) return;
        String key = "report_" + report.getReportId();
        cache.put(key, report);
        modifiedEntities.add(key);
        LOGGER.info("Report cached for saving: " + report.getReportId());
    }
    
    public Report loadReport(String reportId) {
        String key = "report_" + reportId;
        if (cache.containsKey(key)) {
            return (Report) cache.get(key);
        }
        
        try {
            Report report = (Report) deserialize(key);
            if (report != null) {
                cache.put(key, report);
            }
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load report: " + reportId, e);
            return null;
        }
    }
    
    // Compliance Rule Operations
    public void saveComplianceRule(ComplianceRule rule) {
        if (rule == null) return;
        String key = "rule_" + rule.getId();
        cache.put(key, rule);
        modifiedEntities.add(key);
        LOGGER.info("Compliance rule cached for saving: " + rule.getId());
    }
    
    public ComplianceRule loadComplianceRule(String ruleId) {
        String key = "rule_" + ruleId;
        if (cache.containsKey(key)) {
            return (ComplianceRule) cache.get(key);
        }
        
        try {
            ComplianceRule rule = (ComplianceRule) deserialize(key);
            if (rule != null) {
                cache.put(key, rule);
            }
            return rule;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load compliance rule: " + ruleId, e);
            return null;
        }
    }
    
    // Batch Operations
    public void saveAll() {
        for (String key : modifiedEntities) {
            try {
                Object entity = cache.get(key);
                if (entity != null) {
                    serialize(key, entity);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to save entity: " + key, e);
            }
        }
        modifiedEntities.clear();
        LOGGER.info("All modified entities saved");
    }
    
    public void createBackup() {
        String timestamp = LocalDateTime.now().format(BACKUP_FORMAT);
        String backupPath = BACKUP_DIR + "/backup_" + timestamp;
        
        try {
            Files.createDirectories(Paths.get(backupPath));
            Files.walk(Paths.get(DATA_DIR))
                .filter(path -> !path.toString().contains("backups"))
                .forEach(path -> {
                    try {
                        Path target = Paths.get(backupPath, path.getFileName().toString());
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to backup file: " + path, e);
                    }
                });
            LOGGER.info("Backup created: " + backupPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create backup", e);
        }
    }
    
    // Serialization Operations
    private void serialize(String key, Object obj) throws IOException {
        String filename = DATA_DIR + "/" + key + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(filename)))) {
            oos.writeObject(obj);
            LOGGER.info("Serialized " + key + " to " + filename);
        }
    }
    
    private Object deserialize(String key) throws IOException, ClassNotFoundException {
        String filename = DATA_DIR + "/" + key + ".ser";
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(filename)))) {
            return ois.readObject();
        }
    }
    
    // Cache Management
    public void clearCache() {
        cache.clear();
        modifiedEntities.clear();
        LOGGER.info("Cache cleared");
    }
    
    public void removeFromCache(String key) {
        cache.remove(key);
        modifiedEntities.remove(key);
        LOGGER.info("Removed from cache: " + key);
    }
    
    // Utility Methods
    public List<String> listBackups() {
        try {
            return Files.list(Paths.get(BACKUP_DIR))
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.startsWith("backup_"))
                .sorted(Comparator.reverseOrder())
                .toList();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to list backups", e);
            return Collections.emptyList();
        }
    }
    
    public boolean restoreBackup(String backupName) {
        String backupPath = BACKUP_DIR + "/" + backupName;
        try {
            Files.walk(Paths.get(backupPath))
                .forEach(path -> {
                    try {
                        Path target = Paths.get(DATA_DIR, path.getFileName().toString());
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to restore file: " + path, e);
                    }
                });
            LOGGER.info("Backup restored: " + backupName);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to restore backup: " + backupName, e);
            return false;
        }
    }
} 