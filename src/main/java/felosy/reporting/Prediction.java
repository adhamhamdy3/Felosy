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
 */
public class Prediction {
    private String predictionId;
    private String portfolioId;
    private Date creationDate;
    private Date targetDate;
    private float confidenceLevel;
    private Map<String, Float> forecastValues;
    private List<String> assumptions;
    private String scenarioType; // e.g., "Baseline", "Conservative", "Optimistic"
    
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
        this.predictionId = predictionId;
        this.portfolioId = portfolioId;
        this.creationDate = new Date(); // Current date
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
     * Add multiple forecast values
     * 
     * @param forecasts Map of metrics to their predicted values
     */
    public void addForecastValues(Map<String, Float> forecasts) {
        forecastValues.putAll(forecasts);
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
        long diff = new Date().getTime() - creationDate.getTime();
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
    
    /**
     * Create a report of this prediction
     * 
     * @return Report object containing the prediction data
     */
    public Report generateReport() {
        String reportId = "PRED-" + predictionId.substring(0, 8);
        Report report = new Report(reportId, "Prediction Report", new Date());
        
        // Add prediction data to the report
        report.addData("predictionId", predictionId);
        report.addData("portfolioId", portfolioId);
        report.addData("creationDate", creationDate);
        report.addData("targetDate", targetDate);
        report.addData("confidenceLevel", confidenceLevel);
        report.addData("scenarioType", scenarioType);
        report.addData("forecasts", new HashMap<>(forecastValues));
        report.addData("assumptions", new ArrayList<>(assumptions));
        
        return report;
    }
    
    // Getters and Setters
    public String getPredictionId() {
        return predictionId;
    }
    
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public Date getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }
    
    public float getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public void setConfidenceLevel(float confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
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
    
    public void setScenarioType(String scenarioType) {
        this.scenarioType = scenarioType;
    }
    
    @Override
    public String toString() {
        return "Prediction [id=" + predictionId + 
               ", portfolio=" + portfolioId + 
               ", scenario=" + scenarioType +
               ", confidence=" + confidenceLevel + 
               ", metrics=" + forecastValues.size() + "]";
    }
}