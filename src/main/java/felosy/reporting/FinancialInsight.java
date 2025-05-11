/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.reporting;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * FinancialInsight class for generating insights and predictions about financial data
 * Simplified for academic purposes
 */
public class FinancialInsight extends BaseReport {
    private String portfolioId;
    private Date analysisDate;
    private Map<String, Double> metrics;
    private List<String> recommendations;
    private String riskLevel;
    private double confidenceScore;
    
    public FinancialInsight(String insightId, String portfolioId) {
        super(insightId, "Financial Insight Report");
        this.portfolioId = portfolioId;
        this.analysisDate = new Date();
        this.metrics = new HashMap<>();
        this.recommendations = new ArrayList<>();
        this.riskLevel = "Medium";
        this.confidenceScore = 0.0;
    }
    
    public void addMetric(String name, double value) {
        metrics.put(name, value);
    }
    
    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public void setConfidenceScore(double score) {
        this.confidenceScore = score;
    }
    
    @Override
    public String generateContent() {
        StringBuilder content = new StringBuilder();
        content.append("=== Financial Insight Report ===\n\n");
        content.append("Portfolio ID: ").append(portfolioId).append("\n");
        content.append("Analysis Date: ").append(analysisDate).append("\n");
        content.append("Risk Level: ").append(riskLevel).append("\n");
        content.append("Confidence Score: ").append(confidenceScore * 100).append("%\n\n");
        
        content.append("Key Metrics:\n");
        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            content.append("- ").append(entry.getKey())
                   .append(": ").append(entry.getValue()).append("\n");
        }
        
        content.append("\nRecommendations:\n");
        for (String recommendation : recommendations) {
            content.append("- ").append(recommendation).append("\n");
        }
        
        return content.toString();
    }
    
    // Getters
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public Date getAnalysisDate() {
        return analysisDate;
    }
    
    public Map<String, Double> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    @Override
    public String toString() {
        return String.format("FinancialInsight [id=%s, portfolio=%s, risk=%s, confidence=%.2f]",
            reportId, portfolioId, riskLevel, confidenceScore);
    }
}