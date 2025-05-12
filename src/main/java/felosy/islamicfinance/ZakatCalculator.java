/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.islamicfinance;

import java.util.*;
import java.util.logging.Level;
import felosy.reporting.Report;
import felosy.authentication.User;
import felosy.islamicfinance.config.IslamicFinanceConfig;

/**
 * Calculates Zakat for a portfolio
 * Simplified for academic purposes
 */
public class ZakatCalculator extends IslamicFinanceBase {
    private final IslamicFinanceConfig config;
    private final Map<String, Float> assetValues;

    public ZakatCalculator(String portfolioId) {
        super(portfolioId);
        this.config = IslamicFinanceConfig.getInstance();
        this.assetValues = new HashMap<>();
        initializeAssetValues();
    }
    
    private void initializeAssetValues() {
        // Simplified asset values for academic purposes
        assetValues.put("Stocks", 50000.0f);
        assetValues.put("Gold", 15000.0f);
        assetValues.put("Cash", 5000.0f);
    }

    @Override
    public boolean checkCompliance() {
        try {
            float totalValue = calculateTotalValue();
            isCompliant = totalValue >= config.getNisabThreshold();
            logComplianceCheck("Zakat threshold check", isCompliant);
            updateLastUpdateDate();
            return isCompliant;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking Zakat compliance", e);
            return false;
        }
    }

    public float calculateZakat() {
        if (!checkCompliance()) {
            LOGGER.info("Portfolio value below Nisab threshold. No Zakat due.");
            return 0.0f;
        }

        float totalValue = calculateTotalValue();
        float zakatAmount = totalValue * (float)config.getZakatRate();
        LOGGER.info("Zakat calculated: " + zakatAmount + " for portfolio: " + portfolioId);
        return zakatAmount;
    }

    public Map<String, Float> getZakatByAsset() {
        Map<String, Float> zakatByAsset = new HashMap<>();
        
        for (Map.Entry<String, Float> entry : assetValues.entrySet()) {
            float assetValue = entry.getValue();
            if (assetValue > 0) {
                float assetZakat = assetValue * (float)config.getZakatRate();
                zakatByAsset.put(entry.getKey(), assetZakat);
            }
        }
        
        return zakatByAsset;
    }

    public Report generateReport(User user) {
        float totalZakat = calculateZakat();
        Map<String, Float> zakatByAsset = getZakatByAsset();
        
        Report report = new Report(
            "ZKT-" + portfolioId.substring(0, 8),
            "Zakat Calculation Report",
            new Date(),
            user
        );
        
        // Add report details
        report.addData("Total Portfolio Value", calculateTotalValue());
        report.addData("Nisab Threshold", config.getNisabThreshold());
        report.addData("Zakat Rate", config.getZakatRate() * 100 + "%");
        report.addData("Total Zakat Due", totalZakat);
        
        // Add asset-wise breakdown
        for (Map.Entry<String, Float> entry : zakatByAsset.entrySet()) {
            report.addData(entry.getKey() + " Zakat", entry.getValue());
        }
        
        return report;
    }

    private float calculateTotalValue() {
        return assetValues.values().stream()
            .reduce(0.0f, Float::sum);
    }

    public Map<String, Float> getAssetValues() {
        return new HashMap<>(assetValues);
    }
    
    public void setAssetValue(String assetType, float value) {
        assetValues.put(assetType, value);
        updateLastUpdateDate();
    }
}