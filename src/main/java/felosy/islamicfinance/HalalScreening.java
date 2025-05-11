package felosy.islamicfinance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.math.BigDecimal;
import felosy.assetmanagement.SimpleAsset;
import felosy.reporting.Report;

/**
 * Screens assets for Islamic compliance
 * Simplified for academic purposes
 */
public class HalalScreening extends IslamicFinanceBase {
    private List<Rule> complianceRules;
    private Map<String, Boolean> screeningResults;
    private Map<String, SimpleAsset> portfolioAssets;

    public HalalScreening(String portfolioId) {
        super(portfolioId);
        this.complianceRules = initializeRules();
        this.screeningResults = new HashMap<>();
        this.portfolioAssets = new HashMap<>();
        initializePortfolioAssets();
    }

    private List<Rule> initializeRules() {
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("No Interest", "Asset must not be based on interest (riba)"));
        rules.add(new Rule("Ethical Business", "Company must not be involved in non-halal activities"));
        rules.add(new Rule("Debt Ratio", "Company's debt ratio must not exceed 33%"));
        rules.add(new Rule("Non-Halal Income", "Non-compliant income must not exceed 5% of total revenue"));
        return rules;
    }
    
    private void initializePortfolioAssets() {
        // Simplified portfolio assets for academic purposes
        portfolioAssets.put("Stock-AAPL", createAsset("AAPL", "Apple Inc.", 100.0));
        portfolioAssets.put("Stock-ADNOC", createAsset("ADNOC", "Abu Dhabi National Oil Company", 200.0));
        portfolioAssets.put("RealEstate-Dubai1", createAsset("DXB1", "Dubai Property", 500.0));
        portfolioAssets.put("Gold-Investment1", createAsset("GOLD1", "Gold Investment", 50.0));
    }
    
    private SimpleAsset createAsset(String symbol, String name, double value) {
        return new SimpleAsset(
            symbol + "-" + System.currentTimeMillis(),
            name,
            symbol,
            BigDecimal.valueOf(value)
        );
    }

    @Override
    public boolean checkCompliance() {
        try {
            boolean allCompliant = true;
            for (Map.Entry<String, SimpleAsset> entry : portfolioAssets.entrySet()) {
                String assetId = entry.getKey();
                boolean isCompliant = checkAssetCompliance(assetId);
                screeningResults.put(assetId, isCompliant);
                allCompliant &= isCompliant;
            }
            
            isCompliant = allCompliant;
            logComplianceCheck("Portfolio screening", isCompliant);
            updateLastUpdateDate();
            return isCompliant;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking portfolio compliance", e);
            return false;
        }
    }

    private boolean checkAssetCompliance(String assetId) {
        // Simplified compliance check for academic purposes
        switch (assetId) {
            case "Stock-AAPL":
                return false; // Example non-compliant
            case "Stock-ADNOC":
            case "RealEstate-Dubai1":
            case "Gold-Investment1":
                return true; // Example compliant
            default:
                return false;
        }
    }

    public List<SimpleAsset> filterNonCompliant() {
        List<SimpleAsset> nonCompliantAssets = new ArrayList<>();
        
        for (Map.Entry<String, Boolean> entry : screeningResults.entrySet()) {
            if (!entry.getValue()) {
                SimpleAsset asset = portfolioAssets.get(entry.getKey());
                if (asset != null) {
                    nonCompliantAssets.add(asset);
                }
            }
        }
        
        return nonCompliantAssets;
    }

    public Report generateComplianceReport() {
        Report report = new Report(
            "HLR-" + portfolioId.substring(0, 8),
            "Halal Compliance Report",
            new java.util.Date()
        );
        
        // Add compliance summary
        report.addData("Total Assets", portfolioAssets.size());
        report.addData("Compliant Assets", screeningResults.values().stream().filter(b -> b).count());
        report.addData("Non-Compliant Assets", screeningResults.values().stream().filter(b -> !b).count());
        
        // Add asset-wise compliance status
        for (Map.Entry<String, Boolean> entry : screeningResults.entrySet()) {
            report.addData(entry.getKey(), entry.getValue() ? "Compliant" : "Non-Compliant");
        }
        
        return report;
    }

    // Getters
    public List<Rule> getComplianceRules() {
        return new ArrayList<>(complianceRules);
    }

    public Map<String, Boolean> getScreeningResults() {
        return new HashMap<>(screeningResults);
    }
    
    public Map<String, SimpleAsset> getPortfolioAssets() {
        return new HashMap<>(portfolioAssets);
    }

    // Inner class for compliance rules
    public static class Rule {
        private String name;
        private String description;

        public Rule(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}