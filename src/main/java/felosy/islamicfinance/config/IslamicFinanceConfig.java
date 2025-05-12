package felosy.islamicfinance.config;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Manages configuration settings for Islamic finance operations
 */
public class IslamicFinanceConfig {
    private static final Logger LOGGER = Logger.getLogger(IslamicFinanceConfig.class.getName());
    private static IslamicFinanceConfig instance;
    private final Properties properties;

    // Default values
    private static final double DEFAULT_ZAKAT_RATE = 0.025; // 2.5%
    private static final double DEFAULT_NISAB_THRESHOLD = 5000.0;
    private static final double DEFAULT_DEBT_RATIO_THRESHOLD = 0.33; // 33%
    private static final double DEFAULT_NON_HALAL_INCOME_THRESHOLD = 0.05; // 5%

    private IslamicFinanceConfig() {
        properties = new Properties();
        loadDefaultProperties();
        loadCustomProperties();
    }

    public static synchronized IslamicFinanceConfig getInstance() {
        if (instance == null) {
            instance = new IslamicFinanceConfig();
        }
        return instance;
    }

    private void loadDefaultProperties() {
        properties.setProperty("zakat.rate", String.valueOf(DEFAULT_ZAKAT_RATE));
        properties.setProperty("nisab.threshold", String.valueOf(DEFAULT_NISAB_THRESHOLD));
        properties.setProperty("debt.ratio.threshold", String.valueOf(DEFAULT_DEBT_RATIO_THRESHOLD));
        properties.setProperty("non.halal.income.threshold", String.valueOf(DEFAULT_NON_HALAL_INCOME_THRESHOLD));
    }

    private void loadCustomProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("islamic-finance.properties")) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("Loaded custom Islamic finance properties");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load custom properties, using defaults", e);
        }
    }

    public double getZakatRate() {
        return Double.parseDouble(properties.getProperty("zakat.rate"));
    }

    public double getNisabThreshold() {
        return Double.parseDouble(properties.getProperty("nisab.threshold"));
    }

    public double getDebtRatioThreshold() {
        return Double.parseDouble(properties.getProperty("debt.ratio.threshold"));
    }

    public double getNonHalalIncomeThreshold() {
        return Double.parseDouble(properties.getProperty("non.halal.income.threshold"));
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        LOGGER.info("Updated property: " + key + " = " + value);
    }
} 