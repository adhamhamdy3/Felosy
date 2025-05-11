package felosy.financialplanning;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinancialPlan {
    private static final Logger LOGGER = Logger.getLogger(FinancialPlan.class.getName());
    
    private final String planId;
    private final String userId;
    private final LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private final List<FinancialGoal> goals;
    private final RiskAssessment riskAssessment;
    private final AssetAllocation assetAllocation;
    private double totalAssets;
    private double monthlyIncome;
    private double monthlyExpenses;
    
    public FinancialPlan(String userId) {
        this.planId = UUID.randomUUID().toString();
        this.userId = userId;
        this.creationDate = LocalDateTime.now();
        this.lastUpdateDate = creationDate;
        this.goals = new ArrayList<>();
        this.riskAssessment = new RiskAssessment(planId);
        this.assetAllocation = new AssetAllocation(planId);
        this.totalAssets = 0.0;
        this.monthlyIncome = 0.0;
        this.monthlyExpenses = 0.0;
    }
    
    /**
     * Adds a new financial goal to the plan
     * @param goalType Type of the goal (e.g., "Retirement", "House", "Education")
     * @param targetAmount Target amount for the goal
     * @return true if goal was added successfully
     */
    public boolean addGoal(String goalType, double targetAmount) {
        try {
            FinancialGoal goal = new FinancialGoal(goalType, targetAmount);
            goals.add(goal);
            updateLastUpdateDate();
            LOGGER.info("Added new goal: " + goalType + " with target: " + targetAmount);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add goal", e);
            return false;
        }
    }
    
    /**
     * Updates the financial plan with new asset information
     * @param assets Map of asset types and their values
     * @return true if update was successful
     */
    public boolean updateAssets(Map<String, Double> assets) {
        try {
            double newTotal = assets.values().stream().mapToDouble(Double::doubleValue).sum();
            this.totalAssets = newTotal;
            assetAllocation.setCurrentAllocation(convertToFloatMap(assets));
            updateLastUpdateDate();
            LOGGER.info("Updated assets. New total: " + newTotal);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update assets", e);
            return false;
        }
    }
    
    /**
     * Updates monthly income and expenses
     * @param income Monthly income
     * @param expenses Monthly expenses
     * @return true if update was successful
     */
    public boolean updateMonthlyCashFlow(double income, double expenses) {
        try {
            this.monthlyIncome = income;
            this.monthlyExpenses = expenses;
            updateLastUpdateDate();
            LOGGER.info("Updated monthly cash flow. Income: " + income + ", Expenses: " + expenses);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update monthly cash flow", e);
            return false;
        }
    }
    
    /**
     * Generates a comprehensive financial report
     * @return Map containing various financial metrics
     */
    public Map<String, Object> generateReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Basic financial metrics
        report.put("totalAssets", totalAssets);
        report.put("monthlyIncome", monthlyIncome);
        report.put("monthlyExpenses", monthlyExpenses);
        report.put("monthlySavings", monthlyIncome - monthlyExpenses);
        
        // Risk assessment
        report.put("riskScore", riskAssessment.calculateRisk());
        report.put("riskBreakdown", riskAssessment.getRiskBreakdown());
        report.put("riskMitigationSuggestions", riskAssessment.suggestMitigation());
        
        // Asset allocation
        report.put("currentAllocation", assetAllocation.getCurrentAllocation());
        report.put("suggestedAllocation", assetAllocation.generateSuggestions());
        report.put("allocationDifferences", assetAllocation.compareAllocations());
        
        // Goals progress
        Map<String, Double> goalsProgress = new HashMap<>();
        for (FinancialGoal goal : goals) {
            goalsProgress.put(goal.getGoalType(), goal.getProgressPercentage());
        }
        report.put("goalsProgress", goalsProgress);
        
        return report;
    }
    
    /**
     * Applies suggested asset allocation changes
     * @return true if changes were applied successfully
     */
    public boolean applySuggestedAllocation() {
        try {
            boolean success = assetAllocation.applyChanges();
            if (success) {
                updateLastUpdateDate();
                LOGGER.info("Applied suggested asset allocation changes");
            }
            return success;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to apply suggested allocation", e);
            return false;
        }
    }
    
    private void updateLastUpdateDate() {
        this.lastUpdateDate = LocalDateTime.now();
    }
    
    private Map<String, Float> convertToFloatMap(Map<String, Double> doubleMap) {
        Map<String, Float> floatMap = new HashMap<>();
        doubleMap.forEach((key, value) -> floatMap.put(key, value.floatValue()));
        return floatMap;
    }
    
    // Getters
    public String getPlanId() {
        return planId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
    
    public List<FinancialGoal> getGoals() {
        return Collections.unmodifiableList(goals);
    }
    
    public double getTotalAssets() {
        return totalAssets;
    }
    
    public double getMonthlyIncome() {
        return monthlyIncome;
    }
    
    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }
} 