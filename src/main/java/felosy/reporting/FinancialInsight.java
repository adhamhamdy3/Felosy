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
 */
public class FinancialInsight {
    private String insightId;
    private String portfolioId;
    private String insightType;
    private Date analysisDate;
    private List<String> insightsList;
    private Map<String, Float> metricsMap;
    private boolean isAnalyzed;
    
    /**
     * Constructor for financial insight
     * 
     * @param insightId Unique identifier for this insight
     * @param portfolioId Portfolio being analyzed
     * @param insightType Type of insight (e.g., "Trend Analysis", "Risk Assessment")
     */
    public FinancialInsight(String insightId, String portfolioId, String insightType) {
        this.insightId = insightId;
        this.portfolioId = portfolioId;
        this.insightType = insightType;
        this.analysisDate = new Date();
        this.insightsList = new ArrayList<>();
        this.metricsMap = new HashMap<>();
        this.isAnalyzed = false;
    }
    
    /**
     * Convenience constructor that auto-generates an insight ID
     * 
     * @param portfolioId Portfolio being analyzed
     * @param insightType Type of insight
     */
    public FinancialInsight(String portfolioId, String insightType) {
        this(UUID.randomUUID().toString(), portfolioId, insightType);
    }
    
    /**
     * Generate insights based on portfolio data
     * 
     * @return List of insights as strings
     */
    public List<String> generateInsights() {
        System.out.println("Generating insights for portfolio: " + portfolioId);
        
        // Clear previous insights if any
        insightsList.clear();
        
        // Implementation would analyze portfolio data and generate meaningful insights
        if (insightType.equals("Sector Analysis")) {
            insightsList.add("Portfolio is over-allocated in technology sector by 12.5%");
            insightsList.add("Healthcare sector is underrepresented compared to market benchmark");
            insightsList.add("Energy sector exposure is well-balanced and aligned with Shariah principles");
        } else if (insightType.equals("Compliance Analysis")) {
            insightsList.add("Halal compliance rate is 85%, with 3 stocks requiring review");
            insightsList.add("Interest-based income represents 2.1% of total returns");
            insightsList.add("Debt-to-market-cap ratio is within acceptable limits for 92% of portfolio");
        } else if (insightType.equals("Risk Analysis")) {
            insightsList.add("Portfolio volatility is 15% lower than market average");
            insightsList.add("Correlation with market indices suggests moderate risk exposure");
            insightsList.add("Geographic concentration in MENA region increases regional risk");
        } else {
            insightsList.add("General portfolio health is above average");
            insightsList.add("Annual returns of 8.2% exceed Shariah-compliant benchmark by 1.3%");
            insightsList.add("Liquidity position is strong with 15% in cash-equivalent assets");
        }
        
        isAnalyzed = true;
        return new ArrayList<>(insightsList);
    }
    
    /**
     * Recommend strategy changes based on analysis
     * 
     * @return List of strategy recommendations
     */
    public List<String> recommendStrategy() {
        if (!isAnalyzed) {
            generateInsights(); // Ensure insights are generated first
        }
        
        System.out.println("Recommending strategies for portfolio: " + portfolioId);
        
        List<String> recommendations = new ArrayList<>();
        
        // Implementation would analyze data and provide recommendations
        if (insightType.equals("Sector Analysis")) {
            recommendations.add("Reduce technology exposure by 5% to align with market benchmarks");
            recommendations.add("Increase allocation to healthcare sector by 3-4%");
            recommendations.add("Consider adding Shariah-compliant consumer staples for better balance");
        } else if (insightType.equals("Compliance Analysis")) {
            recommendations.add("Replace AAPL, MSFT, and GOOGL with Shariah-compliant alternatives");
            recommendations.add("Implement quarterly compliance screening to maintain halal status");
            recommendations.add("Consider Islamic ETFs for broader market exposure while maintaining compliance");
        } else if (insightType.equals("Risk Analysis")) {
            recommendations.add("Increase gold allocation for better risk management during market volatility");
            recommendations.add("Diversify geographic exposure beyond MENA region");
            recommendations.add("Consider reducing position sizes in highly volatile assets");
        } else {
            recommendations.add("Maintain current asset allocation with minor adjustments");
            recommendations.add("Consider profit-taking on assets that have exceeded target growth");
            recommendations.add("Explore Sukuk investments for stable income component");
        }
        
        return recommendations;
    }
    
    /**
     * Analyze trends in portfolio performance
     * 
     * @return Map of trend labels to numerical values
     */
    public Map<String, Float> analyzeTrends() {
        System.out.println("Analyzing trends for portfolio: " + portfolioId);
        
        // Clear previous metrics
        metricsMap.clear();
        
        // Implementation would analyze historical data to identify trends
        metricsMap.put("1M Return", 2.5f);
        metricsMap.put("3M Return", 4.8f);
        metricsMap.put("6M Return", 7.2f);
        metricsMap.put("1Y Return", 12.1f);
        metricsMap.put("3Y Annualized", 9.7f);
        metricsMap.put("Sharpe Ratio", 1.3f);
        metricsMap.put("Sortino Ratio", 1.5f);
        metricsMap.put("Max Drawdown", -12.4f);
        metricsMap.put("Volatility", 8.7f);
        
        isAnalyzed = true;
        return new HashMap<>(metricsMap);
    }
    
    /**
     * Generate predictions for future portfolio performance
     * 
     * @return Prediction object with forecast data
     */
    public Prediction predict() {
        if (!isAnalyzed) {
            analyzeTrends(); // Ensure trends are analyzed first
        }
        
        System.out.println("Generating predictions for portfolio: " + portfolioId);
        
        // Create future date for prediction (1 year ahead)
        Date futureDate = new Date();
        futureDate.setTime(futureDate.getTime() + 365L * 24 * 60 * 60 * 1000);
        
        // Create a new prediction with confidence level and scenario type
        Prediction prediction = new Prediction(
            "PRED-" + UUID.randomUUID().toString().substring(0, 8),
            portfolioId,
            futureDate,
            0.75f, // 75% confidence level
            "Baseline"
        );
        
        // Add forecast values
        prediction.addForecastValue("Projected Return", 10.5f);
        prediction.addForecastValue("Projected Volatility", 9.2f);
        prediction.addForecastValue("Expected Sharpe", 1.2f);
        prediction.addForecastValue("Halal Compliance Score", 90.0f);
        
        // Add assumptions
        prediction.addAssumption("Market conditions remain stable");
        prediction.addAssumption("No major changes in interest rates");
        prediction.addAssumption("Current asset allocation maintained");
        prediction.addAssumption("No significant geopolitical events");
        
        return prediction;
    }
    
    /**
     * Generate a comprehensive report with insights, recommendations, and predictions
     * 
     * @return Report object with all analysis data
     */
    public Report generateReport() {
        if (!isAnalyzed) {
            generateInsights();
            analyzeTrends();
        }
        
        String reportId = "INS-" + insightId.substring(0, 8);
        Report report = new Report(reportId, "Financial Insight Report", new Date());
        
        // Add basic information
        report.addData("portfolioId", portfolioId);
        report.addData("insightType", insightType);
        report.addData("analysisDate", analysisDate);
        
        // Add insights
        report.addData("insights", new ArrayList<>(insightsList));
        
        // Add recommendations
        report.addData("recommendations", recommendStrategy());
        
        // Add trends
        report.addData("trends", new HashMap<>(metricsMap));
        
        // Add prediction
        Prediction prediction = predict();
        report.addData("prediction", prediction);
        
        return report;
    }
    
    // Getters and Setters
    public String getInsightId() {
        return insightId;
    }
    
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public String getInsightType() {
        return insightType;
    }
    
    public void setInsightType(String insightType) {
        this.insightType = insightType;
        // Reset analysis when type changes
        this.isAnalyzed = false;
    }
    
    public Date getAnalysisDate() {
        return analysisDate;
    }
    
    public List<String> getInsightsList() {
        return new ArrayList<>(insightsList);
    }
    
    public Map<String, Float> getMetricsMap() {
        return new HashMap<>(metricsMap);
    }
    
    public boolean isAnalyzed() {
        return isAnalyzed;
    }
    
    @Override
    public String toString() {
        return "FinancialInsight [id=" + insightId + 
               ", portfolio=" + portfolioId + 
               ", type=" + insightType + 
               ", date=" + analysisDate + 
               ", analyzed=" + isAnalyzed + "]";
    }
}