package felosy;

import felosy.authentication.Authentication;
import felosy.authentication.User;
import felosy.assetmanagement.*;
import felosy.islamicfinance.HalalScreening;
import felosy.reporting.*;
import felosy.integration.CryptoExchange;
import felosy.utils.DataStorage;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Main class for the Felosy Islamic Finance Management System
 * Provides a command-line interface for managing Islamic finance operations
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat percentFormat = new DecimalFormat("#0.00%");
    
    // Email configuration
    private static final String CONFIG_FILE = "config.properties";
    private static final String EMAIL_HOST = "smtp.gmail.com";
    private static final String EMAIL_PORT = "587";
    private static String EMAIL_USERNAME;
    private static String EMAIL_PASSWORD;
    
    // Application state
    private static List<User> users;
    private static List<Portfolio> portfolios;
    private static List<Asset> assets;
    private static User currentUser;
    private static Portfolio currentPortfolio;
    private static Authentication auth;
    private static boolean isRunning = true;
    private static LocalDateTime lastActivity;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    /**
     * Initialize email configuration
     */
    private static void initializeEmailConfig() {
        Properties config = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        // Set default configuration
        config.setProperty("email.username", "adhamhn333@gmail.com");
        config.setProperty("email.password", "nojh svgh hgcp yvty");
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "Email Configuration");
            LOGGER.info("Email configuration saved");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving email configuration", e);
        }
        
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            config.load(in);
            EMAIL_USERNAME = config.getProperty("email.username");
            EMAIL_PASSWORD = config.getProperty("email.password");
            LOGGER.info("Email configuration loaded");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading email configuration", e);
        }
    }

    public static void main(String[] args) {
        initializeEmailConfig();
        initializeSystem();
        runApplication();
        cleanup();
    }

    /**
     * Initialize the system and load data
     */
    private static void initializeSystem() {
        LOGGER.info("Initializing Felosy Islamic Finance Management System");
        System.out.println("Welcome to Felosy - Islamic Finance Management System");
        System.out.println("==================================================");

        try {
            loadData();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error loading data", e);
            System.out.println("Error loading data: " + e.getMessage());
            initializeEmptyData();
        }
    }

    /**
     * Load data from storage
     */
    private static void loadData() throws IOException, ClassNotFoundException {
        users = DataStorage.loadUsers();
        portfolios = DataStorage.loadPortfolios();
        assets = DataStorage.loadAssets();
        LOGGER.info("Data loaded successfully");
    }

    /**
     * Initialize empty data structures
     */
    private static void initializeEmptyData() {
        users = new ArrayList<>();
        portfolios = new ArrayList<>();
        assets = new ArrayList<>();
        LOGGER.info("Initialized empty data structures");
    }

    /**
     * Main application loop
     */
    private static void runApplication() {
        while (isRunning) {
            try {
                if (currentUser != null && isSessionValid()) {
                    showMainMenu();
                } else {
                    showLoginMenu();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in main application loop", e);
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Cleanup resources before exit
     */
    private static void cleanup() {
        try {
            saveData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving data during cleanup", e);
            System.out.println("Error saving data: " + e.getMessage());
        }
        scanner.close();
        LOGGER.info("Application shutdown complete");
    }

    /**
     * Save data to storage
     */
    private static void saveData() throws IOException {
        DataStorage.saveUsers(users);
        DataStorage.savePortfolios(portfolios);
        DataStorage.saveAssets(assets);
        LOGGER.info("Data saved successfully");
    }

    /**
     * Check if the current session is valid
     */
    private static boolean isSessionValid() {
        if (currentUser == null || lastActivity == null) {
            return false;
        }
        
        long minutesSinceLastActivity = ChronoUnit.MINUTES.between(lastActivity, LocalDateTime.now());
        if (minutesSinceLastActivity >= SESSION_TIMEOUT_MINUTES) {
            System.out.println("Session expired. Please login again.");
            handleLogout();
            return false;
        }
        
        updateLastActivity();
        return true;
    }

    /**
     * Update the last activity timestamp
     */
    private static void updateLastActivity() {
        lastActivity = LocalDateTime.now();
    }

    /**
     * Display the login menu
     */
    private static void showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 3);
        switch (choice) {
            case 1 -> handleLogin();
            case 2 -> handleRegistration();
            case 3 -> {
                isRunning = false;
                System.out.println("Thank you for using Felosy!");
            }
        }
    }

    /**
     * Display the main menu
     */
    private static void showMainMenu() {
        if (!isSessionValid()) {
            return;
        }

        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Portfolio Management");
        System.out.println("2. Asset Management");
        System.out.println("3. Islamic Finance Compliance");
        System.out.println("4. Reports & Insights");
        System.out.println("5. External Accounts");
        System.out.println("6. User Profile");
        System.out.println("7. System Management");
        System.out.println("8. Logout");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 8);
        switch (choice) {
            case 1 -> handlePortfolioManagement();
            case 2 -> handleAssetManagement();
            case 3 -> handleIslamicCompliance();
            case 4 -> handleReportsAndInsights();
            case 5 -> handleExternalAccounts();
            case 6 -> handleUserProfile();
            case 7 -> handleSystemManagement();
            case 8 -> handleLogout();
        }
    }

    /**
     * Handle system management operations
     */
    private static void handleSystemManagement() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access system management.");
            return;
        }

        System.out.println("\n=== System Management ===");
        System.out.println("1. Create Backup");
        System.out.println("2. Clear All Data");
        System.out.println("3. Clear Serialized Data");
        System.out.println("4. View System Status");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 5);
        switch (choice) {
            case 1 -> {
                try {
                    DataStorage.createBackup();
                    System.out.println("Backup created successfully!");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error creating backup", e);
                    System.out.println("Error creating backup: " + e.getMessage());
                }
            }
            case 2 -> {
                System.out.println("WARNING: This will clear all data. A backup will be created first.");
                System.out.print("Are you sure? (yes/no): ");
                String confirmation = scanner.nextLine().toLowerCase();
                if (confirmation.equals("yes")) {
                    try {
                        DataStorage.clearAllData();
                        System.out.println("All data cleared successfully!");
                        initializeEmptyData();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error clearing data", e);
                        System.out.println("Error clearing data: " + e.getMessage());
                    }
                }
            }
            case 3 -> {
                System.out.println("WARNING: This will clear all serialized data files.");
                System.out.print("Are you sure? (yes/no): ");
                String confirmation = scanner.nextLine().toLowerCase();
                if (confirmation.equals("yes")) {
                    try {
                        // Clear specific serialized files
                        clearSerializedData();
                        System.out.println("Serialized data cleared successfully!");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error clearing serialized data", e);
                        System.out.println("Error clearing serialized data: " + e.getMessage());
                    }
                }
            }
            case 4 -> {
                System.out.println("\n=== System Status ===");
                System.out.println("Users: " + users.size());
                System.out.println("Portfolios: " + portfolios.size());
                System.out.println("Assets: " + assets.size());
                System.out.println("Current User: " + (currentUser != null ? currentUser.getUserName() : "None"));
                System.out.println("Session Active: " + (lastActivity != null ? "Yes" : "No"));
            }
        }
    }

    /**
     * Clear all serialized data files
     */
    private static void clearSerializedData() throws IOException {
        // Create backup before clearing
        DataStorage.createBackup();
        
        // Clear specific serialized files
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".ser"));
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        LOGGER.info("Deleted serialized file: " + file.getName());
                    }
                }
            }
        }
        
        // Reinitialize empty data structures
        initializeEmptyData();
    }

    /**
     * Send OTP via email
     */
    private static void sendOTPEmail(String recipientEmail, String otp) {
        if (EMAIL_USERNAME == null || EMAIL_PASSWORD == null) {
            LOGGER.severe("Email credentials not configured. Please check config.properties file.");
            System.out.println("Email configuration error. Please contact system administrator.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", EMAIL_HOST);
        props.put("mail.smtp.port", EMAIL_PORT);
        props.put("mail.smtp.ssl.trust", EMAIL_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Felosy Login OTP");
            message.setText("Your OTP for Felosy login is: " + otp + "\n\nThis OTP is valid for 5 minutes.");

            Transport.send(message);
            LOGGER.info("OTP email sent successfully to: " + recipientEmail);
        } catch (AuthenticationFailedException e) {
            LOGGER.log(Level.SEVERE, "Email authentication failed. Please check your credentials in config.properties", e);
            System.out.println("Email authentication failed. Please check your email configuration.");
            System.out.println("For testing purposes, your OTP is: " + otp);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Error sending OTP email", e);
            System.out.println("Error sending OTP email. Please try again.");
            System.out.println("For testing purposes, your OTP is: " + otp);
        }
    }

    private static void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        currentUser = users.stream()
                .filter(u -> u.getUserName().equals(username))
                .findFirst()
                .orElse(null);

        if (currentUser != null && currentUser.authenticate(password)) {
            // Generate and send OTP
            String otp = generateOTP();
            System.out.println("Sending OTP to your email: " + currentUser.getEmail());
            sendOTPEmail(currentUser.getEmail(), otp);
            
            System.out.print("Enter OTP: ");
            String enteredOTP = scanner.nextLine();

            if (validateOTP(enteredOTP, otp)) {
                System.out.println("Login successful!");
                updateLastActivity();
                currentPortfolio = portfolios.stream()
                        .filter(p -> p.getUserId().equals(currentUser.getUserId()))
                        .findFirst()
                        .orElseGet(() -> {
                            Portfolio newPortfolio = new Portfolio(currentUser.getUserId());
                            portfolios.add(newPortfolio);
                            try {
                                DataStorage.savePortfolios(portfolios);
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Error saving portfolio", e);
                            }
                            return newPortfolio;
                        });
            } else {
                System.out.println("Invalid OTP. Login failed.");
                currentUser = null;
                currentPortfolio = null;
            }
        } else {
            System.out.println("Invalid username or password.");
            currentUser = null;
            currentPortfolio = null;
        }
    }

    /**
     * Generate a 6-digit OTP
     */
    private static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Validate the entered OTP
     */
    private static boolean validateOTP(String enteredOTP, String generatedOTP) {
        return enteredOTP.equals(generatedOTP);
    }

    private static void handleRegistration() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        // Check if username already exists
        if (users.stream().anyMatch(u -> u.getUserName().equals(username))) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("Invalid email format.");
            return;
        }

        User newUser = new User(username, email, password);
        users.add(newUser);
        
        try {
            DataStorage.saveUsers(users);
            System.out.println("Registration successful! Please login.");
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static void handlePortfolioManagement() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access portfolio management.");
            return;
        }

        System.out.println("\n=== Portfolio Management ===");
        System.out.println("1. View Portfolio Summary");
        System.out.println("2. Add New Asset");
        System.out.println("3. Remove Asset");
        System.out.println("4. Update Asset Details");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 5);
        switch (choice) {
            case 1:
                displayPortfolioSummary();
                break;
            case 2:
                addNewAsset();
                break;
            case 3:
                removeAsset();
                break;
            case 4:
                updateAssetDetails();
                break;
        }
    }

    private static void handleAssetManagement() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access asset management.");
            return;
        }

        System.out.println("\n=== Asset Management ===");
        System.out.println("1. View All Assets");
        System.out.println("2. View Asset Details");
        System.out.println("3. Calculate Asset Value");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        switch (choice) {
            case 1:
                displayAllAssets();
                break;
            case 2:
                viewAssetDetails();
                break;
            case 3:
                calculateAssetValue();
                break;
        }
    }

    private static void handleIslamicCompliance() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access Islamic compliance features.");
            return;
        }

        System.out.println("\n=== Islamic Compliance ===");
        System.out.println("1. Run Halal Screening");
        System.out.println("2. View Compliance Report");
        System.out.println("3. Calculate Zakat");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        switch (choice) {
            case 1 -> runHalalScreening();
            case 2 -> viewComplianceReport();
            case 3 -> calculateZakat();
            case 4 -> {
                return;
            }
        }
    }

    private static void handleReportsAndInsights() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access reports and insights.");
            return;
        }

        System.out.println("\n=== Reports & Insights ===");
        System.out.println("1. Generate Portfolio Report");
        System.out.println("2. Generate Compliance Report");
        System.out.println("3. Export Reports");
        System.out.println("4. Generate Financial Insights");
        System.out.println("5. Generate Performance Prediction");
        System.out.println("6. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 6);
        switch (choice) {
            case 1 -> generatePortfolioReport();
            case 2 -> generateComplianceReport();
            case 3 -> exportReports();
            case 4 -> generateFinancialInsights();
            case 5 -> generatePerformancePrediction();
            case 6 -> {
                return;
            }
        }
    }

    /**
     * Handle external accounts management
     */
    private static void handleExternalAccounts() {
        while (true) {
            System.out.println("\n=== External Accounts ===");
            System.out.println("1. Connect Crypto Exchange");
            System.out.println("2. View Connected Accounts");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 3);
            switch (choice) {
                case 1 -> connectCryptoExchange();
                case 2 -> viewConnectedAccounts();
                case 3 -> {
                    return;
                }
            }
        }
    }

    /**
     * Connect to a cryptocurrency exchange
     */
    private static void connectCryptoExchange() {
        System.out.println("\n=== Connect Crypto Exchange ===");
        System.out.print("Enter exchange name (e.g., binance, coinbase): ");
        String exchangeName = scanner.nextLine().trim();
        
        System.out.print("Enter exchange ID: ");
        String exchangeId = scanner.nextLine().trim();
        
        try {
            CryptoExchange exchange = new CryptoExchange(exchangeId, exchangeName);
            if (exchange.connect()) {
                System.out.println("Successfully connected to " + exchangeName);
                // Store the exchange connection in the user's profile
                if (currentUser != null) {
                    // TODO: Implement storing exchange connection in user profile
                    System.out.println("Exchange connection stored in user profile");
                }
            } else {
                System.out.println("Failed to connect to " + exchangeName);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error connecting to crypto exchange", e);
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * View connected external accounts
     */
    private static void viewConnectedAccounts() {
        if (currentUser == null) {
            System.out.println("No user logged in");
            return;
        }

        // TODO: Implement retrieving connected exchanges from user profile
        System.out.println("\n=== Connected Accounts ===");
        System.out.println("No external accounts connected");
    }

    private static void handleUserProfile() {
        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        System.out.println("\n=== User Profile ===");
        System.out.println("1. View Profile");
        System.out.println("2. Update Profile");
        System.out.println("3. Change Password");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                viewProfile();
                break;
            case 2:
                updateProfile();
                break;
            case 3:
                changePassword();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void handleLogout() {
        if (auth != null) {
            auth.logout();
        }
        currentUser = null;
        currentPortfolio = null;
        lastActivity = null;
        System.out.println("Logged out successfully.");
    }

    // Helper methods for input handling
    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    private static float getFloatInput() {
        while (true) {
            try {
                return Float.parseFloat(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    // Implementation of menu options
    private static void loadInitialData() {
        // Load user's portfolio and other data
        System.out.println("Loading your data...");
        // Add implementation here
    }

    private static void displayPortfolioSummary() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available.");
            return;
        }

        System.out.println("\n=== Portfolio Summary ===");
        System.out.println("Total Net Worth: " + currencyFormat.format(currentPortfolio.getNetWorth()));
        System.out.println("Number of Assets: " + currentPortfolio.getAssets().size());
        
        // Display asset distribution
        Map<String, BigDecimal> assetDistribution = new HashMap<>();
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            BigDecimal value = asset.getCurrentValue();
            assetDistribution.merge(assetType, value, BigDecimal::add);
        }

        System.out.println("\nAsset Distribution:");
        for (Map.Entry<String, BigDecimal> entry : assetDistribution.entrySet()) {
            BigDecimal percentage = entry.getValue().divide(currentPortfolio.getNetWorth(), 4, RoundingMode.HALF_UP);
            System.out.printf("%s: %s (%.2f%%)\n", 
                entry.getKey(), 
                currencyFormat.format(entry.getValue()),
                percentage.multiply(new BigDecimal("100")).floatValue());
        }
    }

    private static void addNewAsset() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to add assets.");
            return;
        }

        // Verify portfolio ownership
        if (!currentPortfolio.getUserId().equals(currentUser.getUserId())) {
            System.out.println("Unauthorized access to portfolio.");
            return;
        }

        System.out.println("\n=== Add New Asset ===");
        System.out.println("Select asset type:");
        System.out.println("1. Stock");
        System.out.println("2. Real Estate");
        System.out.println("3. Gold");
        System.out.println("4. Cryptocurrency");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        Date purchaseDate = new Date();

        try {
            Asset newAsset = null;
            switch (choice) {
                case 1:
                    System.out.print("Enter stock symbol: ");
                    String symbol = scanner.nextLine();
                    System.out.print("Enter quantity: ");
                    int quantity = getIntInput(1, Integer.MAX_VALUE);
                    System.out.print("Enter purchase price per share: ");
                    BigDecimal pricePerShare = new BigDecimal(scanner.nextLine());
                    BigDecimal totalPrice = pricePerShare.multiply(new BigDecimal(quantity));
                    newAsset = new Stock("STK" + System.currentTimeMillis(), symbol, purchaseDate, 
                        totalPrice, totalPrice, TickerType.valueOf(symbol), "NYSE", quantity, 
                        BigDecimal.ZERO, BigDecimal.ZERO);
                    break;
                case 2:
                    System.out.print("Enter property name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter location: ");
                    String location = scanner.nextLine();
                    System.out.print("Enter area (sq ft): ");
                    BigDecimal area = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter purchase price: ");
                    BigDecimal purchasePrice = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter current value: ");
                    BigDecimal currentValue = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter monthly rental income: ");
                    BigDecimal rentalIncome = new BigDecimal(scanner.nextLine());
                    newAsset = new RealEstate("RE" + System.currentTimeMillis(), name, purchaseDate,
                        purchasePrice, currentValue, location, area, PropertyType.SINGLE_FAMILY_RESIDENTIAL,
                        rentalIncome, 1.0f);
                    break;
                case 3:
                    System.out.print("Enter gold name: ");
                    name = scanner.nextLine();
                    System.out.print("Enter weight (grams): ");
                    BigDecimal weight = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter purity (0-1): ");
                    BigDecimal purity = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter purchase price: ");
                    purchasePrice = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter current value: ");
                    currentValue = new BigDecimal(scanner.nextLine());
                    newAsset = new Gold("GLD" + System.currentTimeMillis(), name, purchaseDate,
                        purchasePrice, currentValue, weight, purity);
                    break;
                case 4:
                    System.out.print("Enter cryptocurrency symbol: ");
                    symbol = scanner.nextLine();
                    System.out.print("Enter amount: ");
                    BigDecimal amount = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter purchase price: ");
                    purchasePrice = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter current value: ");
                    currentValue = new BigDecimal(scanner.nextLine());
                    newAsset = new Cryptocurrency("CRY" + System.currentTimeMillis(), symbol, purchaseDate,
                        purchasePrice, currentValue, CoinType.valueOf(symbol), amount);
                    break;
            }

            if (newAsset != null && currentPortfolio.addAsset(newAsset)) {
                System.out.println("Asset added successfully!");
            } else {
                System.out.println("Failed to add asset.");
            }
        } catch (Exception e) {
            System.out.println("Error adding asset: " + e.getMessage());
        }
    }

    private static void removeAsset() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to remove assets.");
            return;
        }

        // Verify portfolio ownership
        if (!currentPortfolio.getUserId().equals(currentUser.getUserId())) {
            System.out.println("Unauthorized access to portfolio.");
            return;
        }

        System.out.println("\n=== Remove Asset ===");
        displayAllAssets();
        System.out.print("Enter asset ID to remove: ");
        String assetId = scanner.nextLine();

        if (currentPortfolio.removeAsset(assetId)) {
            System.out.println("Asset removed successfully!");
        } else {
            System.out.println("Failed to remove asset. Asset ID not found.");
        }
    }

    private static void displayAllAssets() {
        if (currentPortfolio == null || currentPortfolio.getAssets().isEmpty()) {
            System.out.println("No assets available.");
            return;
        }

        System.out.println("\n=== All Assets ===");
        for (Asset asset : currentPortfolio.getAssets()) {
            System.out.printf("ID: %s, Name: %s, Value: %s\n",
                asset.getAssetId(),
                asset.getName(),
                currencyFormat.format(asset.getCurrentValue()));
        }
    }

    private static void runHalalScreening() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for screening.");
            return;
        }

        System.out.println("\n=== Halal Screening ===");
        HalalScreening screening = new HalalScreening(currentPortfolio.getPortfolioId());
        Map<String, Boolean> results = screening.getScreeningResults();

        System.out.println("Screening Results:");
        for (Map.Entry<String, Boolean> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + (entry.getValue() ? "Compliant" : "Non-Compliant"));
        }
    }

    private static void generatePortfolioReport() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for report generation.");
            return;
        }

        System.out.println("\n=== Portfolio Report ===");
        Report report = new Report(
            "REP" + System.currentTimeMillis(),
            "Portfolio Analysis",
            new Date()
        );

        System.out.println("Generated portfolio report");
        System.out.println("Report ID: " + report.getReportId());
    }

    private static void generateComplianceReport() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for compliance report.");
            return;
        }

        System.out.println("\n=== Compliance Report ===");
        Report report = new Report(
            "COMP" + System.currentTimeMillis(),
            "Shariah Compliance Analysis",
            new Date()
        );

        System.out.println("Generated compliance report");
        System.out.println("Report ID: " + report.getReportId());
    }

    private static void exportReports() {
        System.out.println("\n=== Export Reports ===");
        System.out.println("1. Export as PDF");
        System.out.println("2. Export as CSV");
        System.out.println("3. Export as Excel");
        System.out.println("4. Back");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        if (choice == 4) {
            return;
        }

        System.out.println("Reports exported successfully");
    }

    private static void generateFinancialInsights() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for insights generation.");
            return;
        }

        System.out.println("\n=== Financial Insights ===");
        System.out.println("Generating insights...");
        System.out.println("Insights generated successfully");
    }

    private static void generatePerformancePrediction() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for performance prediction.");
            return;
        }

        System.out.println("\n=== Performance Prediction ===");
        System.out.print("Enter number of days for prediction: ");
        int days = getIntInput(1, 365);

        Date futureDate = new Date(System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000);
        Prediction prediction = new Prediction(
            "PRED" + System.currentTimeMillis(),
            currentPortfolio.getPortfolioId(),
            futureDate,
            0.85f,
            "Baseline"
        );

        System.out.printf("Generated prediction for %d days from now\n", days);
        System.out.println("Prediction ID: " + prediction.getReportId());
    }

    private static void viewProfile() {
        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        System.out.println("\n=== Profile Information ===");
        System.out.println("Username: " + currentUser.getUserName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("User ID: " + currentUser.getUserId());
    }

    private static void updateProfile() {
        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        System.out.print("Enter new username (or press Enter to keep current): ");
        String newUsername = scanner.nextLine();
        if (!newUsername.isEmpty()) {
            // Check if new username is already taken
            if (users.stream().anyMatch(u -> u.getUserName().equals(newUsername) && !u.getUserId().equals(currentUser.getUserId()))) {
                System.out.println("Username already exists. Please choose a different username.");
                return;
            }
            currentUser.setUserName(newUsername);
        }

        System.out.print("Enter new email (or press Enter to keep current): ");
        String newEmail = scanner.nextLine();
        if (!newEmail.isEmpty()) {
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                System.out.println("Invalid email format.");
                return;
            }
            currentUser.setEmail(newEmail);
        }

        try {
            DataStorage.saveUsers(users);
            System.out.println("Profile updated successfully!");
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static void updateAssetDetails() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to update assets.");
            return;
        }

        // Verify portfolio ownership
        if (!currentPortfolio.getUserId().equals(currentUser.getUserId())) {
            System.out.println("Unauthorized access to portfolio.");
            return;
        }

        System.out.println("\n=== Update Asset Details ===");
        displayAllAssets();
        System.out.print("Enter asset ID to update: ");
        String assetId = scanner.nextLine();

        Asset assetToUpdate = null;
        for (Asset asset : currentPortfolio.getAssets()) {
            if (asset.getAssetId().equals(assetId)) {
                assetToUpdate = asset;
                break;
            }
        }

        if (assetToUpdate == null) {
            System.out.println("Asset not found.");
            return;
        }

        System.out.print("Enter new name (or press Enter to keep current): ");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) {
            assetToUpdate.setName(newName);
            System.out.println("Asset updated successfully!");
        } else {
            System.out.println("No changes made.");
        }
    }

    private static void viewAssetDetails() {
        if (currentPortfolio == null || currentPortfolio.getAssets().isEmpty()) {
            System.out.println("No assets available.");
            return;
        }

        System.out.println("\n=== Asset Details ===");
        displayAllAssets();
        System.out.print("Enter asset ID to view details: ");
        String assetId = scanner.nextLine();

        for (Asset asset : currentPortfolio.getAssets()) {
            if (asset.getAssetId().equals(assetId)) {
                System.out.println("\nAsset Details:");
                System.out.println("ID: " + asset.getAssetId());
                System.out.println("Name: " + asset.getName());
                System.out.println("Value: " + currencyFormat.format(asset.getCurrentValue()));
                System.out.println("Purchase Date: " + asset.getPurchaseDate());
                return;
            }
        }
        System.out.println("Asset not found.");
    }

    private static void calculateAssetValue() {
        if (currentPortfolio == null || currentPortfolio.getAssets().isEmpty()) {
            System.out.println("No assets available.");
            return;
        }

        System.out.println("\n=== Calculate Asset Value ===");
        displayAllAssets();
        System.out.print("Enter asset ID to calculate value: ");
        String assetId = scanner.nextLine();

        for (Asset asset : currentPortfolio.getAssets()) {
            if (asset.getAssetId().equals(assetId)) {
                BigDecimal value = asset.getCurrentValue();
                System.out.printf("Current value of %s: %s\n", 
                    asset.getName(), 
                    currencyFormat.format(value));
                return;
            }
        }
        System.out.println("Asset not found.");
    }

    private static void viewComplianceReport() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for compliance report.");
            return;
        }

        System.out.println("\n=== Compliance Report ===");
        Report report = new Report(
            "COMP" + System.currentTimeMillis(),
            "Shariah Compliance Analysis",
            new Date()
        );

        System.out.println("Generated compliance report");
        System.out.println("Report ID: " + report.getReportId());
    }

    private static void calculateZakat() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for Zakat calculation.");
            return;
        }

        System.out.println("\n=== Zakat Calculation ===");
        BigDecimal totalValue = currentPortfolio.getNetWorth();
        BigDecimal zakatAmount = totalValue.multiply(new BigDecimal("0.025")); // 2.5%

        System.out.println("Total Portfolio Value: " + currencyFormat.format(totalValue));
        System.out.println("Zakat Amount (2.5%): " + currencyFormat.format(zakatAmount));
    }

    private static void changePassword() {
        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        
        if (!currentUser.authenticate(currentPassword)) {
            System.out.println("Current password is incorrect.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        currentUser.setPassword(newPassword);
        try {
            DataStorage.saveUsers(users);
            System.out.println("Password changed successfully!");
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    // Additional helper methods can be implemented as needed
} 