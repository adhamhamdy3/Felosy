package felosy.islamicfinance;

import java.util.*;
import java.util.logging.Level;
import java.math.BigDecimal;
import felosy.assetmanagement.SimpleAsset;
import felosy.reporting.Report;
import felosy.authentication.User;
import felosy.islamicfinance.model.ComplianceRule;
import felosy.islamicfinance.config.IslamicFinanceConfig;

/**
 * Screens assets for Islamic compliance
 * Simplified for academic purposes
 */
public class HalalScreening extends IslamicFinanceBase {
    private final List<ComplianceRule> complianceRules;
    private final Map<String, Boolean> screeningResults;
    private final Map<String, SimpleAsset> portfolioAssets;
    private final IslamicFinanceConfig config;

    public HalalScreening(String portfolioId) {
        super(portfolioId);
        this.config = IslamicFinanceConfig.getInstance();
        this.complianceRules = initializeRules();
        this.screeningResults = new HashMap<>();
        this.portfolioAssets = new HashMap<>();
        initializePortfolioAssets();
    }

    private List<ComplianceRule> initializeRules() {
        List<ComplianceRule> rules = new ArrayList<>();
        rules.add(new ComplianceRule(
            "No Interest",
            "Asset must not be based on interest (riba)",
            ComplianceRule.RuleType.INTEREST_BASED,
            0.0
        ));
        rules.add(new ComplianceRule(
            "Ethical Business",
            "Company must not be involved in non-halal activities",
            ComplianceRule.RuleType.ETHICAL_BUSINESS,
            0.0
        ));
        rules.add(new ComplianceRule(
            "Debt Ratio",
            "Company's debt ratio must not exceed threshold",
            ComplianceRule.RuleType.DEBT_RATIO,
            config.getDebtRatioThreshold()
        ));
        rules.add(new ComplianceRule(
            "Non-Halal Income",
            "Non-compliant income must not exceed threshold",
            ComplianceRule.RuleType.NON_HALAL_INCOME,
            config.getNonHalalIncomeThreshold()
        ));
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
        SimpleAsset asset = portfolioAssets.get(assetId);
        if (asset == null) {
            LOGGER.warning("Asset not found: " + assetId);
            return false;
        }

        for (ComplianceRule rule : complianceRules) {
            if (!rule.isActive()) continue;

            boolean ruleCompliant = switch (rule.getType()) {
                case INTEREST_BASED -> checkInterestBasedCompliance(asset);
                case ETHICAL_BUSINESS -> checkEthicalBusinessCompliance(asset);
                case DEBT_RATIO -> checkDebtRatioCompliance(asset, rule.getThreshold());
                case NON_HALAL_INCOME -> checkNonHalalIncomeCompliance(asset, rule.getThreshold());
                case CUSTOM -> true; // Custom rules would be implemented separately
            };

            if (!ruleCompliant) {
                LOGGER.info(String.format("Asset %s failed compliance rule: %s", assetId, rule.getName()));
                return false;
            }
        }
        return true;
    }

    private boolean checkInterestBasedCompliance(SimpleAsset asset) {
        // Simplified check - in reality, this would involve detailed analysis
        return !asset.getSymbol().equals("AAPL"); // Example: Apple is non-compliant
    }

    private boolean checkEthicalBusinessCompliance(SimpleAsset asset) {
        // Simplified check - in reality, this would involve detailed analysis
        return true; // All assets are considered ethical in this example
    }

    private boolean checkDebtRatioCompliance(SimpleAsset asset, double threshold) {
        // Simplified check - in reality, this would involve detailed analysis
        return true; // All assets are considered compliant in this example
    }

    private boolean checkNonHalalIncomeCompliance(SimpleAsset asset, double threshold) {
        // Simplified check - in reality, this would involve detailed analysis
        return true; // All assets are considered compliant in this example
    }

    public List<SimpleAsset> filterNonCompliant() {
        return portfolioAssets.values().stream()
            .filter(asset -> !screeningResults.getOrDefault(asset.getAssetId(), false))
            .toList();
    }

    public Report generateComplianceReport(User user) {
        Report report = new Report(
            "HLR-" + portfolioId.substring(0, 8),
            "Halal Compliance Report",
            new Date(),
            user
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
    public List<ComplianceRule> getComplianceRules() {
        return new ArrayList<>(complianceRules);
    }

    public Map<String, Boolean> getScreeningResults() {
        return new HashMap<>(screeningResults);
    }
    
    public Map<String, SimpleAsset> getPortfolioAssets() {
        return new HashMap<>(portfolioAssets);
    }
}