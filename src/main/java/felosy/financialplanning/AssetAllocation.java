package felosy.financialplanning;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssetAllocation {
    private static final Logger LOGGER = Logger.getLogger(AssetAllocation.class.getName());
    
    private final String portfolioId;
    private Map<String, Float> currentAllocation;
    private Map<String, Float> suggestedAllocation;
    private LocalDateTime lastUpdateDate;
    private Map<String, AssetClass> assetClasses;
    
    public static class AssetClass {
        private final String name;
        private final String description;
        private final float minAllocation;
        private final float maxAllocation;
        private final float expectedReturn;
        private final float riskLevel;
        
        public AssetClass(String name, String description, float minAllocation, 
                         float maxAllocation, float expectedReturn, float riskLevel) {
            this.name = name;
            this.description = description;
            this.minAllocation = minAllocation;
            this.maxAllocation = maxAllocation;
            this.expectedReturn = expectedReturn;
            this.riskLevel = riskLevel;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public float getMinAllocation() { return minAllocation; }
        public float getMaxAllocation() { return maxAllocation; }
        public float getExpectedReturn() { return expectedReturn; }
        public float getRiskLevel() { return riskLevel; }
    }
    
    public AssetAllocation(String portfolioId) {
        this.portfolioId = portfolioId;
        this.currentAllocation = new HashMap<>();
        this.suggestedAllocation = new HashMap<>();
        this.lastUpdateDate = LocalDateTime.now();
        this.assetClasses = new HashMap<>();
        initializeAssetClasses();
    }
    
    private void initializeAssetClasses() {
        // Define standard asset classes with their characteristics
        assetClasses.put("Stocks", new AssetClass(
            "Stocks",
            "Equity investments in publicly traded companies",
            0.0f,
            100.0f,
            8.0f,
            0.8f
        ));
        
        assetClasses.put("Bonds", new AssetClass(
            "Bonds",
            "Fixed-income securities",
            0.0f,
            100.0f,
            4.0f,
            0.3f
        ));
        
        assetClasses.put("Real Estate", new AssetClass(
            "Real Estate",
            "Property investments and REITs",
            0.0f,
            40.0f,
            6.0f,
            0.6f
        ));
        
        assetClasses.put("Cash", new AssetClass(
            "Cash",
            "Cash and cash equivalents",
            0.0f,
            30.0f,
            2.0f,
            0.1f
        ));
        
        assetClasses.put("Commodities", new AssetClass(
            "Commodities",
            "Physical commodities and commodity futures",
            0.0f,
            20.0f,
            5.0f,
            0.7f
        ));
        
        assetClasses.put("Cryptocurrency", new AssetClass(
            "Cryptocurrency",
            "Digital assets and blockchain investments",
            0.0f,
            10.0f,
            12.0f,
            0.9f
        ));
    }
    
    /**
     * Generates asset allocation suggestions based on risk profile and goals
     * @return Map of suggested allocations
     */
    public Map<String, Float> generateSuggestions() {
        try {
            suggestedAllocation.clear();
            
            // Example allocation strategy - in real implementation, this would be more sophisticated
            suggestedAllocation.put("Stocks", 50.0f);
            suggestedAllocation.put("Bonds", 30.0f);
            suggestedAllocation.put("Real Estate", 10.0f);
            suggestedAllocation.put("Cash", 5.0f);
            suggestedAllocation.put("Commodities", 3.0f);
            suggestedAllocation.put("Cryptocurrency", 2.0f);
            
            validateAllocation(suggestedAllocation);
            lastUpdateDate = LocalDateTime.now();
            
            LOGGER.info("Generated allocation suggestions for portfolio: " + portfolioId);
            return new HashMap<>(suggestedAllocation);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating allocation suggestions", e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * Compares current allocation with suggested allocation
     * @return Map of differences between current and suggested allocations
     */
    public Map<String, Float> compareAllocations() {
        Map<String, Float> differences = new HashMap<>();
        
        // Calculate differences for all asset classes
        for (String assetClass : assetClasses.keySet()) {
            float current = currentAllocation.getOrDefault(assetClass, 0.0f);
            float suggested = suggestedAllocation.getOrDefault(assetClass, 0.0f);
            differences.put(assetClass, suggested - current);
        }
        
        return differences;
    }
    
    /**
     * Applies the suggested allocation changes
     * @return true if changes were applied successfully
     */
    public boolean applyChanges() {
        try {
            if (validateAllocation(suggestedAllocation)) {
                currentAllocation = new HashMap<>(suggestedAllocation);
                lastUpdateDate = LocalDateTime.now();
                LOGGER.info("Applied allocation changes to portfolio: " + portfolioId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying allocation changes", e);
            return false;
        }
    }
    
    /**
     * Validates if an allocation meets all constraints
     * @param allocation The allocation to validate
     * @return true if allocation is valid
     */
    private boolean validateAllocation(Map<String, Float> allocation) {
        float total = 0.0f;
        
        for (Map.Entry<String, Float> entry : allocation.entrySet()) {
            String assetClass = entry.getKey();
            float percentage = entry.getValue();
            
            // Check if asset class exists
            if (!assetClasses.containsKey(assetClass)) {
                LOGGER.warning("Invalid asset class: " + assetClass);
                return false;
            }
            
            // Check allocation limits
            AssetClass ac = assetClasses.get(assetClass);
            if (percentage < ac.getMinAllocation() || percentage > ac.getMaxAllocation()) {
                LOGGER.warning("Allocation for " + assetClass + " outside allowed range");
                return false;
            }
            
            total += percentage;
        }
        
        // Check if total is 100%
        if (Math.abs(total - 100.0f) > 0.01f) {
            LOGGER.warning("Total allocation must be 100%, current: " + total);
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculates the expected portfolio return based on current allocation
     * @return Expected annual return percentage
     */
    public float calculateExpectedReturn() {
        float expectedReturn = 0.0f;
        
        for (Map.Entry<String, Float> entry : currentAllocation.entrySet()) {
            String assetClass = entry.getKey();
            float allocation = entry.getValue();
            float assetReturn = assetClasses.get(assetClass).getExpectedReturn();
            expectedReturn += (allocation / 100.0f) * assetReturn;
        }
        
        return expectedReturn;
    }
    
    /**
     * Calculates the portfolio risk level based on current allocation
     * @return Portfolio risk score (0-1)
     */
    public float calculatePortfolioRisk() {
        float portfolioRisk = 0.0f;
        
        for (Map.Entry<String, Float> entry : currentAllocation.entrySet()) {
            String assetClass = entry.getKey();
            float allocation = entry.getValue();
            float assetRisk = assetClasses.get(assetClass).getRiskLevel();
            portfolioRisk += (allocation / 100.0f) * assetRisk;
        }
        
        return portfolioRisk;
    }
    
    // Getters
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public Map<String, Float> getCurrentAllocation() {
        return new HashMap<>(currentAllocation);
    }
    
    public Map<String, Float> getSuggestedAllocation() {
        return new HashMap<>(suggestedAllocation);
    }
    
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
    
    public Map<String, AssetClass> getAssetClasses() {
        return Collections.unmodifiableMap(assetClasses);
    }
    
    // Setters
    public void setCurrentAllocation(Map<String, Float> currentAllocation) {
        if (validateAllocation(currentAllocation)) {
            this.currentAllocation = new HashMap<>(currentAllocation);
            this.lastUpdateDate = LocalDateTime.now();
        }
    }
}