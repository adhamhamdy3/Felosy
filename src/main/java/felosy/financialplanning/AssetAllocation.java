/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.financialplanning;

import java.util.HashMap;
import java.util.Map;

public class AssetAllocation {
    private String portfolioId;
    private Map<String, Float> currentAllocation;
    private Map<String, Float> suggestedAllocation;

    public AssetAllocation(String portfolioId) {
        this.portfolioId = portfolioId;
        this.currentAllocation = new HashMap<>();
        this.suggestedAllocation = new HashMap<>();
    }

    public Map<String, Float> generateSuggestions() {
        // Implementation for generating asset allocation suggestions
        // This is a placeholder - real implementation would use complex allocation models
        System.out.println("Generating allocation suggestions for portfolio: " + portfolioId);

        // Example suggested allocation - real implementation would analyze portfolio characteristics
        suggestedAllocation.put("Stocks", 50.0f);
        suggestedAllocation.put("Real Estate", 25.0f);
        suggestedAllocation.put("Gold", 15.0f);
        suggestedAllocation.put("Cryptocurrency", 5.0f);
        suggestedAllocation.put("Cash", 5.0f);

        return suggestedAllocation;
    }

    public Map<String, Float> compareAllocations() {
        // Implementation for comparing current vs suggested allocations
        Map<String, Float> differences = new HashMap<>();

        for (String assetClass : suggestedAllocation.keySet()) {
            float current = currentAllocation.getOrDefault(assetClass, 0.0f);
            float suggested = suggestedAllocation.get(assetClass);
            differences.put(assetClass, suggested - current);
        }

        return differences;
    }

    public boolean applyChanges() {
        // Implementation for applying suggested allocation changes
        System.out.println("Applying allocation changes to portfolio: " + portfolioId);
        this.currentAllocation = new HashMap<>(suggestedAllocation);
        return true;
    }

    // Getters and setters
    public String getPortfolioId() {
        return portfolioId;
    }

    public Map<String, Float> getCurrentAllocation() {
        return new HashMap<>(currentAllocation);
    }

    public void setCurrentAllocation(Map<String, Float> currentAllocation) {
        this.currentAllocation = new HashMap<>(currentAllocation);
    }

    public Map<String, Float> getSuggestedAllocation() {
        return new HashMap<>(suggestedAllocation);
    }
}