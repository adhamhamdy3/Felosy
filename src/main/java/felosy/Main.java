package felosy;

import felosy.authentication.Authentication;
import felosy.authentication.User;
import felosy.assetmanagement.*;
import felosy.financialplanning.AssetAllocation;
import felosy.financialplanning.FinancialGoal;
import felosy.islamicfinance.HalalScreening;
import felosy.reporting.Report;
import felosy.reporting.FinancialInsight;
import felosy.reporting.Prediction;
import felosy.integration.*;
import felosy.utils.DataStorage;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat percentFormat = new DecimalFormat("#0.00%");
    private static List<User> users;
    private static List<Portfolio> portfolios;
    private static List<Asset> assets;
    private static User currentUser;
    private static Portfolio currentPortfolio;
    private static Authentication auth;
    private static boolean isRunning = true;
    private static LocalDateTime lastActivity;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public static void main(String[] args) {
        System.out.println("Welcome to Felosy - Islamic Finance Management System");
        System.out.println("==================================================");

        try {
            // Load data from files
            users = DataStorage.loadUsers();
            portfolios = DataStorage.loadPortfolios();
            assets = DataStorage.loadAssets();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
            users = new ArrayList<>();
            portfolios = new ArrayList<>();
            assets = new ArrayList<>();
        }

        while (isRunning) {
            if (currentUser != null && isSessionValid()) {
                showMainMenu();
            } else {
                showLoginMenu();
            }
        }

        // Save data before exiting
        try {
            DataStorage.saveUsers(users);
            DataStorage.savePortfolios(portfolios);
            DataStorage.saveAssets(assets);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }

        scanner.close();
    }

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

    private static void updateLastActivity() {
        lastActivity = LocalDateTime.now();
    }

    private static void showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 3);
        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleRegistration();
                break;
            case 3:
                isRunning = false;
                System.out.println("Thank you for using Felosy!");
                break;
        }
    }

    private static void showMainMenu() {
        if (!isSessionValid()) {
            return;
        }

        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Portfolio Management");
        System.out.println("2. Asset Management");
        System.out.println("3. Islamic Finance Compliance");
        System.out.println("4. Financial Planning");
        System.out.println("5. Reports & Insights");
        System.out.println("6. External Accounts");
        System.out.println("7. User Profile");
        System.out.println("8. Logout");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                handlePortfolioManagement();
                break;
            case 2:
                handleAssetManagement();
                break;
            case 3:
                handleIslamicCompliance();
                break;
            case 4:
                handleFinancialPlanning();
                break;
            case 5:
                handleReportsAndInsights();
                break;
            case 6:
                handleExternalAccounts();
                break;
            case 7:
                handleUserProfile();
                break;
            case 8:
                handleLogout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
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
            System.out.println("Login successful!");
            // Initialize session
            updateLastActivity();
            // Load user's portfolio
            currentPortfolio = portfolios.stream()
                    .filter(p -> p.getUserId().equals(currentUser.getUserId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Portfolio newPortfolio = new Portfolio(currentUser.getUserId());
                        portfolios.add(newPortfolio);
                        try {
                            DataStorage.savePortfolios(portfolios);
                        } catch (IOException e) {
                            System.out.println("Error saving portfolio: " + e.getMessage());
                        }
                        return newPortfolio;
                    });
        } else {
            System.out.println("Invalid username or password.");
            currentUser = null;
            currentPortfolio = null;
        }
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

        System.out.println("\n=== Islamic Finance Compliance ===");
        System.out.println("1. Run Halal Screening");
        System.out.println("2. View Compliance Report");
        System.out.println("3. Calculate Zakat");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        switch (choice) {
            case 1:
                runHalalScreening();
                break;
            case 2:
                viewComplianceReport();
                break;
            case 3:
                calculateZakat();
                break;
        }
    }

    private static void handleFinancialPlanning() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access financial planning features.");
            return;
        }

        System.out.println("\n=== Financial Planning ===");
        System.out.println("1. Set Financial Goals");
        System.out.println("2. View Asset Allocation");
        System.out.println("3. Generate Investment Strategy");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
        switch (choice) {
            case 1:
                setFinancialGoals();
                break;
            case 2:
                viewAssetAllocation();
                break;
            case 3:
                generateInvestmentStrategy();
                break;
        }
    }

    private static void handleReportsAndInsights() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access reports and insights.");
            return;
        }

        System.out.println("\n=== Reports & Insights ===");
        System.out.println("1. Generate Portfolio Report");
        System.out.println("2. View Financial Insights");
        System.out.println("3. Generate Performance Prediction");
        System.out.println("4. Export Reports");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 5);
        switch (choice) {
            case 1:
                generatePortfolioReport();
                break;
            case 2:
                viewFinancialInsights();
                break;
            case 3:
                generatePerformancePrediction();
                break;
            case 4:
                exportReports();
                break;
        }
    }

    private static void handleExternalAccounts() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to access external accounts.");
            return;
        }

        System.out.println("\n=== External Accounts ===");
        System.out.println("1. Connect Stock Market Account");
        System.out.println("2. Connect Crypto Exchange");
        System.out.println("3. Connect Bank Account");
        System.out.println("4. View Connected Accounts");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 5);
        switch (choice) {
            case 1:
                connectStockMarketAccount();
                break;
            case 2:
                connectCryptoExchange();
                break;
            case 3:
                connectBankAccount();
                break;
            case 4:
                viewConnectedAccounts();
                break;
        }
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
            System.out.printf("%s: %s\n",
                entry.getKey(),
                entry.getValue() ? "Compliant" : "Non-Compliant");
        }
    }

    private static void generatePortfolioReport() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to generate reports.");
            return;
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("=== Portfolio Report ===\n");
        reportContent.append("Generated: ").append(LocalDateTime.now()).append("\n");
        reportContent.append("User: ").append(currentUser.getUserName()).append("\n");
        reportContent.append("Portfolio ID: ").append(currentPortfolio.getPortfolioId()).append("\n\n");
        
        reportContent.append("Total Net Worth: ").append(currencyFormat.format(currentPortfolio.getNetWorth())).append("\n");
        reportContent.append("Number of Assets: ").append(currentPortfolio.getAssets().size()).append("\n\n");
        
        reportContent.append("Asset Distribution:\n");
        Map<String, BigDecimal> assetDistribution = new HashMap<>();
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            BigDecimal value = asset.getCurrentValue();
            assetDistribution.merge(assetType, value, BigDecimal::add);
        }

        for (Map.Entry<String, BigDecimal> entry : assetDistribution.entrySet()) {
            BigDecimal percentage = entry.getValue().divide(currentPortfolio.getNetWorth(), 4, RoundingMode.HALF_UP);
            reportContent.append(String.format("%s: %s (%.2f%%)\n", 
                entry.getKey(), 
                currencyFormat.format(entry.getValue()),
                percentage.multiply(new BigDecimal("100")).floatValue()));
        }

        try {
            DataStorage.saveReport("portfolio", reportContent.toString());
            System.out.println("Portfolio report generated successfully!");
            System.out.println("Report saved to: " + DataStorage.getReportPath("portfolio"));
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
        }
    }

    private static void generateComplianceReport() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to generate compliance reports.");
            return;
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("=== Islamic Compliance Report ===\n");
        reportContent.append("Generated: ").append(LocalDateTime.now()).append("\n");
        reportContent.append("User: ").append(currentUser.getUserName()).append("\n");
        reportContent.append("Portfolio ID: ").append(currentPortfolio.getPortfolioId()).append("\n\n");

        HalalScreening screening = new HalalScreening(currentPortfolio.getPortfolioId());
        Map<String, Boolean> results = screening.getScreeningResults();

        reportContent.append("Compliance Status:\n");
        int compliantCount = 0;
        for (Map.Entry<String, Boolean> entry : results.entrySet()) {
            if (entry.getValue()) {
                compliantCount++;
            }
            reportContent.append(String.format("%s: %s\n",
                entry.getKey(),
                entry.getValue() ? "Compliant" : "Non-Compliant"));
        }

        float complianceRate = (float) compliantCount / results.size() * 100;
        reportContent.append(String.format("\nOverall Compliance Rate: %.2f%%\n", complianceRate));

        try {
            DataStorage.saveReport("compliance", reportContent.toString());
            System.out.println("Compliance report generated successfully!");
            System.out.println("Report saved to: " + DataStorage.getReportPath("compliance"));
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
        }
    }

    private static void exportReports() {
        if (!isSessionValid() || currentUser == null) {
            System.out.println("Please login to export reports.");
            return;
        }

        System.out.println("\n=== Export Reports ===");
        System.out.println("1. Export Portfolio Report");
        System.out.println("2. Export Compliance Report");
        System.out.println("3. Export Financial Insights");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                generatePortfolioReport();
                break;
            case 2:
                generateComplianceReport();
                break;
            case 3:
                generateFinancialInsights();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void generateFinancialInsights() {
        if (!isSessionValid() || currentUser == null || currentPortfolio == null) {
            System.out.println("Please login to generate financial insights.");
            return;
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("=== Financial Insights Report ===\n");
        reportContent.append("Generated: ").append(LocalDateTime.now()).append("\n");
        reportContent.append("User: ").append(currentUser.getUserName()).append("\n");
        reportContent.append("Portfolio ID: ").append(currentPortfolio.getPortfolioId()).append("\n\n");

        // Add financial insights analysis
        reportContent.append("Portfolio Analysis:\n");
        reportContent.append("Total Value: ").append(currencyFormat.format(currentPortfolio.getNetWorth())).append("\n");
        reportContent.append("Asset Count: ").append(currentPortfolio.getAssets().size()).append("\n\n");

        // Add asset allocation insights
        Map<String, BigDecimal> assetDistribution = new HashMap<>();
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            BigDecimal value = asset.getCurrentValue();
            assetDistribution.merge(assetType, value, BigDecimal::add);
        }

        reportContent.append("Asset Allocation:\n");
        for (Map.Entry<String, BigDecimal> entry : assetDistribution.entrySet()) {
            BigDecimal percentage = entry.getValue().divide(currentPortfolio.getNetWorth(), 4, RoundingMode.HALF_UP);
            reportContent.append(String.format("%s: %s (%.2f%%)\n", 
                entry.getKey(), 
                currencyFormat.format(entry.getValue()),
                percentage.multiply(new BigDecimal("100")).floatValue()));
        }

        try {
            DataStorage.saveReport("insights", reportContent.toString());
            System.out.println("Financial insights report generated successfully!");
            System.out.println("Report saved to: " + DataStorage.getReportPath("insights"));
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
        }
    }

    private static void connectStockMarketAccount() {
        System.out.println("\n=== Connect Stock Market Account ===");
        System.out.print("Enter platform name (e.g., Robinhood, E*TRADE): ");
        String platform = scanner.nextLine();
        System.out.print("Enter account ID: ");
        String accountId = scanner.nextLine();

        StockMarketAccount account = new StockMarketAccount(accountId, platform);
        if (account.connect()) {
            System.out.println("Successfully connected to " + platform);
            List<StockMarketAccount.StockHolding> holdings = account.fetchHoldings();
            System.out.println("Found " + holdings.size() + " stock holdings");
        } else {
            System.out.println("Failed to connect to " + platform);
        }
    }

    private static void connectCryptoExchange() {
        System.out.println("\n=== Connect Crypto Exchange ===");
        System.out.print("Enter exchange name (e.g., Binance, Coinbase): ");
        String exchange = scanner.nextLine();
        System.out.print("Enter exchange ID: ");
        String exchangeId = scanner.nextLine();

        CryptoExchange cryptoExchange = new CryptoExchange(exchangeId, exchange);
        if (cryptoExchange.connect()) {
            System.out.println("Successfully connected to " + exchange);
            List<CryptoExchange.CryptoAsset> assets = cryptoExchange.fetchHoldings();
            System.out.println("Found " + assets.size() + " crypto assets");
        } else {
            System.out.println("Failed to connect to " + exchange);
        }
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
        HalalScreening screening = new HalalScreening(currentPortfolio.getPortfolioId());
        Map<String, Boolean> results = screening.getScreeningResults();

        System.out.println("Compliance Status:");
        int compliantCount = 0;
        for (Map.Entry<String, Boolean> entry : results.entrySet()) {
            if (entry.getValue()) {
                compliantCount++;
            }
            System.out.printf("%s: %s\n",
                entry.getKey(),
                entry.getValue() ? "Compliant" : "Non-Compliant");
        }

        float complianceRate = (float) compliantCount / results.size() * 100;
        System.out.printf("\nOverall Compliance Rate: %.2f%%\n", complianceRate);
    }

    private static void calculateZakat() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for Zakat calculation.");
            return;
        }

        System.out.println("\n=== Zakat Calculation ===");
        BigDecimal totalValue = currentPortfolio.getNetWorth();
        BigDecimal zakatRate = new BigDecimal("0.025"); // 2.5% Zakat rate
        BigDecimal zakatAmount = totalValue.multiply(zakatRate);

        System.out.printf("Total Portfolio Value: %s\n", currencyFormat.format(totalValue));
        System.out.printf("Zakat Rate: %.2f%%\n", zakatRate.multiply(new BigDecimal("100")).floatValue());
        System.out.printf("Zakat Amount Due: %s\n", currencyFormat.format(zakatAmount));
    }

    private static void setFinancialGoals() {
        System.out.println("\n=== Set Financial Goals ===");
        System.out.print("Enter goal name: ");
        String goalName = scanner.nextLine();
        System.out.print("Enter target amount: ");
        float targetAmount = getFloatInput();

        FinancialGoal goal = new FinancialGoal(goalName, targetAmount);
        if (goal.addGoal()) {
            System.out.println("Financial goal set successfully!");
            float progress = goal.trackProgress();
            System.out.printf("Current Progress: %.2f%%\n", progress);
        } else {
            System.out.println("Failed to set financial goal.");
        }
    }

    private static void viewAssetAllocation() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for asset allocation.");
            return;
        }

        System.out.println("\n=== Asset Allocation ===");
        AssetAllocation allocation = new AssetAllocation(currentPortfolio.getPortfolioId());
        Map<String, Float> suggestedAllocation = allocation.generateSuggestions();

        System.out.println("Suggested Asset Allocation:");
        for (Map.Entry<String, Float> entry : suggestedAllocation.entrySet()) {
            System.out.printf("%s: %.2f%%\n", 
                entry.getKey(), 
                entry.getValue() * 100);
        }
    }

    private static void generateInvestmentStrategy() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for investment strategy.");
            return;
        }

        System.out.println("\n=== Investment Strategy ===");
        AssetAllocation allocation = new AssetAllocation(currentPortfolio.getPortfolioId());
        Map<String, Float> currentAllocation = new HashMap<>();
        BigDecimal totalValue = currentPortfolio.getNetWorth();

        // Calculate current allocation
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            BigDecimal value = asset.getCurrentValue();
            float percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP).floatValue();
            currentAllocation.merge(assetType, percentage, Float::sum);
        }

        // Get suggested allocation
        Map<String, Float> suggestedAllocation = allocation.generateSuggestions();

        System.out.println("Current vs. Suggested Allocation:");
        for (String assetType : suggestedAllocation.keySet()) {
            float current = currentAllocation.getOrDefault(assetType, 0f);
            float suggested = suggestedAllocation.get(assetType);
            System.out.printf("%s:\n", assetType);
            System.out.printf("  Current: %.2f%%\n", current * 100);
            System.out.printf("  Suggested: %.2f%%\n", suggested);
            System.out.printf("  Difference: %.2f%%\n", (suggested - current * 100));
        }
    }

    private static void viewFinancialInsights() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for financial insights.");
            return;
        }

        System.out.println("\n=== Financial Insights ===");
        FinancialInsight insight = new FinancialInsight(currentPortfolio.getPortfolioId(), "Portfolio Analysis");
        System.out.println("Generating insights...");
        // Add implementation for viewing specific insights
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
        System.out.println("Prediction ID: " + prediction.getPredictionId());
    }

    private static void connectBankAccount() {
        System.out.println("\n=== Connect Bank Account ===");
        System.out.print("Enter bank name: ");
        String bankName = scanner.nextLine();
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        BankAccount bankAccount = new BankAccount(accountNumber, bankName);
        if (bankAccount.connect()) {
            System.out.println("Successfully connected to " + bankName);
            System.out.println("Account Balance: " + currencyFormat.format(bankAccount.getBalance()));
        } else {
            System.out.println("Failed to connect to " + bankName);
        }
    }

    private static void viewConnectedAccounts() {
        System.out.println("\n=== Connected Accounts ===");
        // Add implementation for viewing connected accounts
        System.out.println("No connected accounts found.");
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