package felosy.assetmanagement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;

public final class Gold extends Asset implements Serializable {
    private static final long serialVersionUID = 1L;
    private BigDecimal weightGrams;
    private BigDecimal purity;
    private static final BigDecimal DEFAULT_STORAGE_RATE = new BigDecimal("0.0015"); // 0.15% per annum
    
    public Gold(String assetId, String name, Date purchaseDate, BigDecimal purchasePrice, 
                BigDecimal currentValue, BigDecimal weightGrams, BigDecimal purity) {
        super(assetId, name, purchaseDate, purchasePrice, currentValue);
        setWeightGrams(weightGrams);
        setPurity(purity);
    }
    
    /**
     * Calculates the current market value of the gold
     * based on weight, purity, and current spot price per gram
     * @return The current value of the gold in the market
     */
    public BigDecimal calculateValue() {
        BigDecimal spotPricePerGram = fetchPrice();
        return spotPricePerGram.multiply(weightGrams).multiply(purity);
    }
    
    /**
     * Calculates the cost of storage for the gold over a specified period
     * @param period The time period for which to calculate storage costs
     * @return The cost of storage for the gold over the specified period
     */
    public BigDecimal calculateStorageCost(Duration period) {
        // Convert period to years (as a fraction)
        BigDecimal periodInYears = new BigDecimal(period.toDays()).divide(new BigDecimal("365"), 6, BigDecimal.ROUND_HALF_UP);
        
        // Calculate value of gold
        BigDecimal goldValue = calculateValue();
        
        // Calculate storage cost based on value, rate, and period
        return goldValue.multiply(DEFAULT_STORAGE_RATE).multiply(periodInYears);
    }
    
    /**
     * Refines the gold to a new purity level
     * Updates purity and adjusts weight accordingly
     * @param newPurity The new purity level (between 0 and 1)
     */
    public void refine(BigDecimal newPurity) {
        if (newPurity.compareTo(BigDecimal.ONE) > 0 || newPurity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purity must be between 0 and 1");
        }
        
        if (newPurity.compareTo(purity) > 0) {
            // When refining to higher purity, we lose some weight
            // The amount of pure gold remains the same
            BigDecimal pureGoldContent = weightGrams.multiply(purity);
            weightGrams = pureGoldContent.divide(newPurity, 6, BigDecimal.ROUND_HALF_UP);
            purity = newPurity;
        } else {
            // Can't refine to lower purity in reality, but we'll implement it anyway
            // by adding material to reduce purity while maintaining pure gold content
            BigDecimal pureGoldContent = weightGrams.multiply(purity);
            weightGrams = pureGoldContent.divide(newPurity, 6, BigDecimal.ROUND_HALF_UP);
            purity = newPurity;
        }
        
        // Update the current value after refining
        setCurrentValue(calculateValue());
    }
    
    /**
     * Fetches the current price of gold per gram from market
     * @return The current spot price of gold per gram
     */
    @Override
    public BigDecimal fetchPrice() {
        // Implementation to fetch current gold price
        // This is a placeholder - real implementation would query market APIs
        System.out.println("Fetching current price for gold");
        return new BigDecimal("65.0"); // Example current gold price per gram
    }
    
    @Override
    public BigDecimal getCurrentValue() {
        return calculateValue();
    }
    
    // Getters and setters
    public BigDecimal getWeightGrams() {
        return weightGrams;
    }
    
    public void setWeightGrams(BigDecimal weightGrams) {
        if (weightGrams.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }
        this.weightGrams = weightGrams;
    }
    
    public BigDecimal getPurity() {
        return purity;
    }
    
    public void setPurity(BigDecimal purity) {
        if (purity.compareTo(BigDecimal.ZERO) < 0 || purity.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Purity must be between 0 and 1");
        }
        this.purity = purity;
    }
    
    @Override
    public String toString() {
        return "Gold{" +
               "assetId='" + getAssetId() + '\'' +
               ", name='" + getName() + '\'' +
               ", purchaseDate=" + getPurchaseDate() +
               ", purchasePrice=" + getPurchasePrice() +
               ", currentValue=" + getCurrentValue() +
               ", weightGrams=" + weightGrams +
               ", purity=" + purity + " (" + purity.multiply(new BigDecimal("24")) + "K)" +
               ", return=" + calculateReturn() +
               '}';
    }
}