package felosy.islamicfinance.model;

import java.util.UUID;

/**
 * Represents a compliance rule for Islamic finance screening
 */
public class ComplianceRule {
    private final String id;
    private final String name;
    private final String description;
    private final RuleType type;
    private final double threshold;
    private boolean isActive;

    public enum RuleType {
        INTEREST_BASED,
        ETHICAL_BUSINESS,
        DEBT_RATIO,
        NON_HALAL_INCOME,
        CUSTOM
    }

    public ComplianceRule(String name, String description, RuleType type, double threshold) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.type = type;
        this.threshold = threshold;
        this.isActive = true;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RuleType getType() { return type; }
    public double getThreshold() { return threshold; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", name, type, description);
    }
} 