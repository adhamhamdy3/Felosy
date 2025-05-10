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

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat percentFormat = new DecimalFormat("#0.00%");
    private static User currentUser;
    private static Portfolio currentPortfolio;
    private static Authentication auth;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("Welcome to Felosy - Islamic Finance Management System");
        System.out.println("==================================================");

        while (isRunning) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
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

        int choice = getIntInput(1, 8);
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
        }
    }

    private static void handleLogin() {
        System.out.println("\n=== Login ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        currentUser = new User(username, email);
        auth = new Authentication();
        if (auth.login(currentUser)) {
            System.out.println("Login successful!");
            currentPortfolio = new Portfolio(currentUser.getUserId());
            loadInitialData();
        } else {
            System.out.println("Login failed. Please try again.");
            currentUser = null;
        }
    }

    private static void handleRegistration() {
        System.out.println("\n=== Registration ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        currentUser = new User(username, email);
        if (currentUser.register()) {
            System.out.println("Registration successful! Please login.");
        } else {
            System.out.println("Registration failed. Please try again.");
            currentUser = null;
        }
    }

    private static void handlePortfolioManagement() {
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
        System.out.println("\n=== User Profile ===");
        System.out.println("1. View Profile");
        System.out.println("2. Update Profile");
        System.out.println("3. Change Password");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 4);
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
        }
    }

    private static void handleLogout() {
        if (auth != null) {
            auth.logout();
        }
        currentUser = null;
        currentPortfolio = null;
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
        Map<String, Float> assetDistribution = new HashMap<>();
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            float value = asset.getValue();
            assetDistribution.merge(assetType, value, Float::sum);
        }

        System.out.println("\nAsset Distribution:");
        for (Map.Entry<String, Float> entry : assetDistribution.entrySet()) {
            float percentage = entry.getValue() / currentPortfolio.getNetWorth();
            System.out.printf("%s: %s (%.2f%%)\n", 
                entry.getKey(), 
                currencyFormat.format(entry.getValue()),
                percentage * 100);
        }
    }

    private static void addNewAsset() {
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
                    newAsset = new Stock("STK" + System.currentTimeMillis(), symbol, purchaseDate, symbol, quantity);
                    break;
                case 2:
                    System.out.print("Enter property name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter location: ");
                    String location = scanner.nextLine();
                    System.out.print("Enter area (sq ft): ");
                    float area = getFloatInput();
                    newAsset = new RealEstate("RE" + System.currentTimeMillis(), name, purchaseDate, location, area);
                    break;
                case 3:
                    System.out.print("Enter gold name: ");
                    name = scanner.nextLine();
                    System.out.print("Enter weight (grams): ");
                    float weight = getFloatInput();
                    System.out.print("Enter purity (0-1): ");
                    float purity = getFloatInput();
                    newAsset = new Gold("GLD" + System.currentTimeMillis(), name, purchaseDate, weight, purity);
                    break;
                case 4:
                    System.out.print("Enter cryptocurrency symbol: ");
                    symbol = scanner.nextLine();
                    System.out.print("Enter amount: ");
                    float amount = getFloatInput();
                    newAsset = new Cryptocurrency("CRY" + System.currentTimeMillis(), symbol, purchaseDate, symbol, amount);
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
        if (currentPortfolio == null || currentPortfolio.getAssets().isEmpty()) {
            System.out.println("No assets to remove.");
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
                currencyFormat.format(asset.getValue()));
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
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for reporting.");
            return;
        }

        System.out.println("\n=== Generating Portfolio Report ===");
        Report report = new Report("REP" + System.currentTimeMillis(), "Portfolio Performance", new Date());
        
        // Add portfolio data
        report.addData("Total Value", currentPortfolio.getNetWorth());
        report.addData("Asset Count", currentPortfolio.getAssets().size());
        report.addData("Generation Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // Generate insights
        FinancialInsight insight = new FinancialInsight(currentPortfolio.getPortfolioId(), "Portfolio Analysis");
        System.out.println("Report generated successfully!");
        System.out.println(report);
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
            System.out.println("No user profile available.");
            return;
        }

        System.out.println("\n=== User Profile ===");
        System.out.println("Username: " + currentUser.getUserName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("User ID: " + currentUser.getUserId());
    }

    private static void updateProfile() {
        if (currentUser == null) {
            System.out.println("No user profile available.");
            return;
        }

        System.out.println("\n=== Update Profile ===");
        System.out.print("Enter new username (or press Enter to keep current): ");
        String newUsername = scanner.nextLine();
        if (!newUsername.isEmpty()) {
            currentUser.setUserName(newUsername);
        }

        System.out.print("Enter new email (or press Enter to keep current): ");
        String newEmail = scanner.nextLine();
        if (!newEmail.isEmpty()) {
            currentUser.setEmail(newEmail);
        }

        if (currentUser.updateProfile()) {
            System.out.println("Profile updated successfully!");
        } else {
            System.out.println("Failed to update profile.");
        }
    }

    private static void updateAssetDetails() {
        if (currentPortfolio == null || currentPortfolio.getAssets().isEmpty()) {
            System.out.println("No assets available to update.");
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
        }

        if (assetToUpdate.update()) {
            System.out.println("Asset updated successfully!");
        } else {
            System.out.println("Failed to update asset.");
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
                System.out.println("Value: " + currencyFormat.format(asset.getValue()));
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
                float value = asset.getValue();
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
        float totalValue = currentPortfolio.getNetWorth();
        float zakatRate = 0.025f; // 2.5% Zakat rate
        float zakatAmount = totalValue * zakatRate;

        System.out.printf("Total Portfolio Value: %s\n", currencyFormat.format(totalValue));
        System.out.printf("Zakat Rate: %.2f%%\n", zakatRate * 100);
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
        float totalValue = currentPortfolio.getNetWorth();

        // Calculate current allocation
        for (Asset asset : currentPortfolio.getAssets()) {
            String assetType = asset.getClass().getSimpleName();
            float value = asset.getValue();
            currentAllocation.merge(assetType, value / totalValue, Float::sum);
        }

        // Get suggested allocation
        Map<String, Float> suggestedAllocation = allocation.generateSuggestions();

        System.out.println("Current vs. Suggested Allocation:");
        for (String assetType : suggestedAllocation.keySet()) {
            float current = currentAllocation.getOrDefault(assetType, 0f);
            float suggested = suggestedAllocation.get(assetType);
            System.out.printf("%s:\n", assetType);
            System.out.printf("  Current: %.2f%%\n", current * 100);
            System.out.printf("  Suggested: %.2f%%\n", suggested * 100);
            System.out.printf("  Difference: %.2f%%\n", (suggested - current) * 100);
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

    private static void exportReports() {
        if (currentPortfolio == null) {
            System.out.println("No portfolio available for report export.");
            return;
        }

        System.out.println("\n=== Export Reports ===");
        System.out.println("1. Export Portfolio Report");
        System.out.println("2. Export Compliance Report");
        System.out.println("3. Export Financial Insights");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 3);
        Report report = null;

        switch (choice) {
            case 1:
                report = new Report("REP" + System.currentTimeMillis(), "Portfolio Performance", new Date());
                report.addData("Total Value", currentPortfolio.getNetWorth());
                report.addData("Asset Count", currentPortfolio.getAssets().size());
                break;
            case 2:
                report = new Report("REP" + System.currentTimeMillis(), "Compliance Report", new Date());
                HalalScreening screening = new HalalScreening(currentPortfolio.getPortfolioId());
                report.addData("Screening Results", screening.getScreeningResults());
                break;
            case 3:
                report = new Report("REP" + System.currentTimeMillis(), "Financial Insights", new Date());
                FinancialInsight insight = new FinancialInsight(currentPortfolio.getPortfolioId(), "Portfolio Analysis");
                // Add insight data to report
                break;
        }

        if (report != null) {
            System.out.println("Report generated successfully!");
            System.out.println(report);
        }
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
        if (currentUser == null || auth == null) {
            System.out.println("No user profile available.");
            return;
        }

        System.out.println("\n=== Change Password ===");
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        // Since we don't have a direct password change method, we'll simulate it
        // by logging out and logging back in with the new password
        auth.logout();
        if (auth.login(currentUser)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password.");
        }
    }

    // Additional helper methods can be implemented as needed
} 