package felosy.islamicfinance;

import java.util.*;
import java.util.logging.Level;
import java.math.BigDecimal;
import felosy.assetmanagement.Asset;
import felosy.assetmanagement.Portfolio;
import felosy.reporting.Report;
import felosy.authentication.User;
import felosy.islamicfinance.model.ComplianceRule;
import felosy.islamicfinance.config.IslamicFinanceConfig;

/**
 * Screens assets for Islamic compliance
 */
public class HalalScreening extends IslamicFinanceBase {
    private final List<ComplianceRule> complianceRules;
    private final Map<String, Boolean> screeningResults;
    private final Portfolio portfolio;
    private final IslamicFinanceConfig config;
    private Date lastScreeningDate;

    public HalalScreening(Portfolio portfolio) {
        super(portfolio.getPortfolioId());
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio cannot be null");
        }
        this.portfolio = portfolio;
        this.config = IslamicFinanceConfig.getInstance();
        this.complianceRules = initializeRules();
        this.screeningResults = new HashMap<>();
        this.lastScreeningDate = null;
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

    @Override
    public boolean checkCompliance() {
        try {
            boolean allCompliant = true;
            screeningResults.clear();
            
            for (Asset asset : portfolio.getAssets()) {
                String assetId = asset.getAssetId();
                boolean isCompliant = checkAssetCompliance(asset);
                screeningResults.put(assetId, isCompliant);
                allCompliant &= isCompliant;
                
                LOGGER.info(String.format("Asset %s compliance check: %s", 
                    assetId, isCompliant ? "Compliant" : "Non-Compliant"));
            }
            
            isCompliant = allCompliant;
            lastScreeningDate = new Date();
            logComplianceCheck("Portfolio screening", isCompliant);
            updateLastUpdateDate();
            return isCompliant;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking portfolio compliance", e);
            return false;
        }
    }

    private boolean checkAssetCompliance(Asset asset) {
        if (asset == null) {
            LOGGER.warning("Null asset encountered during compliance check");
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
                LOGGER.info(String.format("Asset %s failed compliance rule: %s", 
                    asset.getAssetId(), rule.getName()));
                return false;
            }
        }
        return true;
    }

    private boolean checkInterestBasedCompliance(Asset asset) {
        // In a real implementation, this would check:
        // 1. If the asset is interest-bearing
        // 2. If the company's business model is based on interest
        // 3. If the company's financial statements show significant interest income
        return !asset.getName().equals("AAPL"); // Simplified example
    }

    private boolean checkEthicalBusinessCompliance(Asset asset) {
        // In a real implementation, this would check:
        // 1. If the company is involved in non-halal activities
        // 2. If the company's products/services are halal
        // 3. If the company's business practices are ethical
        return true; // Simplified example
    }

    private boolean checkDebtRatioCompliance(Asset asset, double threshold) {
        // In a real implementation, this would:
        // 1. Calculate the company's debt ratio
        // 2. Compare it against the threshold
        // 3. Consider the industry average
        return true; // Simplified example
    }

    private boolean checkNonHalalIncomeCompliance(Asset asset, double threshold) {
        // In a real implementation, this would:
        // 1. Calculate the percentage of non-halal income
        // 2. Compare it against the threshold
        // 3. Consider the industry context
        return true; // Simplified example
    }

    public List<Asset> filterNonCompliant() {
        return portfolio.getAssets().stream()
            .filter(asset -> !screeningResults.getOrDefault(asset.getAssetId(), false))
            .toList();
    }

    public Report generateComplianceReport(User user) {
        Report report = new Report(
            "HLR-" + portfolio.getPortfolioId().substring(0, 8),
            "Halal Compliance Report",
            new Date(),
            user
        );
        
        // Add portfolio information
        report.addData("Portfolio Name", portfolio.getName());
        report.addData("Portfolio Description", portfolio.getDescription());
        report.addData("Total Portfolio Value", portfolio.getNetWorth());
        
        // Add compliance summary
        report.addData("Total Assets", portfolio.getAssets().size());
        report.addData("Compliant Assets", screeningResults.values().stream().filter(b -> b).count());
        report.addData("Non-Compliant Assets", screeningResults.values().stream().filter(b -> !b).count());
        report.addData("Last Screening Date", lastScreeningDate);
        
        // Add asset-wise compliance status
        for (Asset asset : portfolio.getAssets()) {
            String assetId = asset.getAssetId();
            boolean isCompliant = screeningResults.getOrDefault(assetId, false);
            report.addData(asset.getName(), 
                String.format("%s (Value: %s)", 
                    isCompliant ? "Compliant" : "Non-Compliant",
                    asset.getCurrentValue()));
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
    
    public Date getLastScreeningDate() {
        return lastScreeningDate != null ? new Date(lastScreeningDate.getTime()) : null;
    }
    
    public Portfolio getPortfolio() {
        return portfolio;
    }
}