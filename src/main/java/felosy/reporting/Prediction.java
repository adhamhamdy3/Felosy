/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.reporting;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Prediction class for storing and managing financial forecasts
 * Simplified for academic purposes
 */
public class Prediction extends BaseReport {
    private String portfolioId;
    private Date targetDate;
    private float confidenceLevel;
    private Map<String, Float> forecastValues;
    private List<String> assumptions;
    private String scenarioType;
    
    /**
     * Constructor for creating a new prediction
     * 
     * @param predictionId Unique identifier for the prediction
     * @param portfolioId Portfolio the prediction applies to
     * @param targetDate Future date the prediction is for
     * @param confidenceLevel Confidence level (0-1) of the prediction
     * @param scenarioType Type of scenario this prediction represents
     */
    public Prediction(String predictionId, String portfolioId, Date targetDate, 
                     float confidenceLevel, String scenarioType) {
        super(predictionId, "Prediction Report");
        this.portfolioId = portfolioId;
        this.targetDate = targetDate;
        this.confidenceLevel = confidenceLevel;
        this.scenarioType = scenarioType;
        this.forecastValues = new HashMap<>();
        this.assumptions = new ArrayList<>();
    }
    
    /**
     * Add a forecast value for a specific metric
     * 
     * @param metric Name of the metric being forecasted
     * @param value Predicted value
     */
    public void addForecastValue(String metric, float value) {
        forecastValues.put(metric, value);
    }
    
    /**
     * Add an assumption that went into making this prediction
     * 
     * @param assumption Assumption statement
     */
    public void addAssumption(String assumption) {
        assumptions.add(assumption);
    }
    
    /**
     * Calculate days until target date
     * 
     * @return Number of days until target date
     */
    public long getDaysUntilTarget() {
        long diff = targetDate.getTime() - new Date().getTime();
        return diff / (24 * 60 * 60 * 1000);
    }
    
    /**
     * Check if the prediction is still valid (not expired)
     * 
     * @param validityDays Number of days a prediction is considered valid
     * @return True if the prediction is still valid
     */
    public boolean isValid(int validityDays) {
        long diff = targetDate.getTime() - new Date().getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return diffDays <= validityDays;
    }
    /**
     * Adjust the confidence level based on new data
     * 
     * @param adjustment Amount to adjust by (-1 to 1)
     * @return New confidence level
     */
    public float adjustConfidence(float adjustment) {
        confidenceLevel += adjustment;
        // Ensure confidence level stays within bounds
        if (confidenceLevel > 1.0f) {
            confidenceLevel = 1.0f;
        } else if (confidenceLevel < 0.0f) {
            confidenceLevel = 0.0f;
        }
        return confidenceLevel;
    }
    
    /**
     * Get the forecasted value for a specific metric
     * 
     * @param metric Name of the metric
     * @return Forecasted value, or null if not found
     */
    public Float getForecastedValue(String metric) {
        return forecastValues.get(metric);
    }
    
    @Override
    public String generateContent() {
        StringBuilder content = new StringBuilder();
        content.append("=== Financial Prediction Report ===\n\n");
        content.append("Portfolio ID: ").append(portfolioId).append("\n");
        content.append("Target Date: ").append(targetDate).append("\n");
        content.append("Confidence Level: ").append(confidenceLevel * 100).append("%\n");
        content.append("Scenario Type: ").append(scenarioType).append("\n\n");
        
        content.append("Forecast Values:\n");
        for (Map.Entry<String, Float> entry : forecastValues.entrySet()) {
            content.append("- ").append(entry.getKey())
                   .append(": ").append(entry.getValue()).append("\n");
        }
        
        content.append("\nAssumptions:\n");
        for (String assumption : assumptions) {
            content.append("- ").append(assumption).append("\n");
        }
        
        return content.toString();
    }
    
    // Getters
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public Date getTargetDate() {
        return targetDate;
    }
    
    public float getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public Map<String, Float> getForecastValues() {
        return new HashMap<>(forecastValues);
    }
    
    public List<String> getAssumptions() {
        return new ArrayList<>(assumptions);
    }
    
    public String getScenarioType() {
        return scenarioType;
    }
    
    @Override
    public String toString() {
        return String.format("Prediction [id=%s, portfolio=%s, scenario=%s, confidence=%.2f]",
            reportId, portfolioId, scenarioType, confidenceLevel);
    }
}