package felosy.assetmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class RealEstate extends Asset {
    private String location;
    private BigDecimal areaSquareMeters;
    private PropertyType propertyType;
    private BigDecimal monthlyRentalIncome;
    private float occupancyRate; // 0.0 -> 1.0
    private BigDecimal annualPropertyTax;
    private BigDecimal annualMaintenanceCost;
    private BigDecimal annualInsuranceCost;
    
    public RealEstate(String assetId, String name, Date purchaseDate, 
            BigDecimal purchasePrice, BigDecimal currentValue, String location, 
            BigDecimal areaSquareMeters, PropertyType propertyType, 
            BigDecimal monthlyRentalIncome, float occupancyRate) {
        
        super(assetId, name, purchaseDate, purchasePrice, currentValue);
        setLocation(location);
        setAreaSquareMeters(areaSquareMeters);
        setPropertyType(propertyType);
        setMonthlyRentalIncome(monthlyRentalIncome);
        setOccupancyRate(occupancyRate);
        
        // Set default values for optional parameters
        this.annualPropertyTax = purchasePrice.multiply(new BigDecimal("0.015")); // Default 1.5% of purchase price
        this.annualMaintenanceCost = purchasePrice.multiply(new BigDecimal("0.01")); // Default 1% of purchase price
        this.annualInsuranceCost = purchasePrice.multiply(new BigDecimal("0.005")); // Default 0.5% of purchase price
    }
    
    /**
     * Estimates the current market value of the property based on 
     * location, area, property type, and other factors
     * @return The estimated current market value
     */
    public BigDecimal estimateValue() {
        // Implementation to estimate real estate value
        // This is a placeholder - real implementation would use complex valuation models
        System.out.println("Estimating value for property at: " + location);
        
        // Base value calculation using area and price per square meter
        BigDecimal baseValuePerSquareMeter;
        
        // Different property types have different base values
        switch(propertyType) {
            case OFFICE:
            case RETAIL:
                baseValuePerSquareMeter = new BigDecimal("3000");
                break;
            case INDUSTRIAL:
                baseValuePerSquareMeter = new BigDecimal("1500");
                break;
            case MULTI_FAMILY_RESIDENTIAL:
                baseValuePerSquareMeter = new BigDecimal("2500");
                break;
            case SINGLE_FAMILY_RESIDENTIAL:
                baseValuePerSquareMeter = new BigDecimal("2200");
                break;
            case MIXED_USE:
                baseValuePerSquareMeter = new BigDecimal("2800");
                break;
            case LAND:
                baseValuePerSquareMeter = new BigDecimal("1000");
                break;
            default:
                baseValuePerSquareMeter = new BigDecimal("2000");
        }
        
        // Calculate base value
        BigDecimal baseValue = areaSquareMeters.multiply(baseValuePerSquareMeter);
        
        // Apply income multiplier for income-producing properties
        if (monthlyRentalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal annualIncome = calculateAnnualNetIncome();
            BigDecimal incomeMultiplier = new BigDecimal("10"); // 10x annual income is a common valuation method
            BigDecimal incomeBasedValue = annualIncome.multiply(incomeMultiplier);
            
            // Take the average of area-based and income-based valuations
            return baseValue.add(incomeBasedValue).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        }
        
        return baseValue.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates the capitalization rate (cap rate) of the property
     * Cap rate = Net Operating Income / Current Value
     * @return The cap rate as a decimal
     */
    public BigDecimal calculateCapRate() {
        BigDecimal annualNetIncome = calculateAnnualNetIncome();
        return annualNetIncome.divide(getCurrentValue(), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates the annual net income from the property
     * Net Income = Gross Rental Income - Expenses
     * @return The annual net income
     */
    public BigDecimal calculateAnnualNetIncome() {
        // Calculate annual gross income
        BigDecimal annualGrossIncome = monthlyRentalIncome.multiply(new BigDecimal("12"))
                .multiply(BigDecimal.valueOf(occupancyRate));
        
        // Calculate total annual expenses
        BigDecimal totalAnnualExpenses = annualPropertyTax
                .add(annualMaintenanceCost)
                .add(annualInsuranceCost);
        
        // Calculate net income
        return annualGrossIncome.subtract(totalAnnualExpenses);
    }
    
    /**
     * Calculates the return on investment (ROI)
     * ROI = Annual Net Income / Purchase Price
     * @return The ROI as a decimal
     */
    public BigDecimal calculateROI() {
        BigDecimal annualNetIncome = calculateAnnualNetIncome();
        return annualNetIncome.divide(getPurchasePrice(), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Updates property value based on appreciation rate
     * @param annualAppreciationRate The annual appreciation rate as a decimal
     * @param years Number of years to project appreciation
     */
    public void applyAppreciation(BigDecimal annualAppreciationRate, int years) {
        if (years < 0) {
            throw new IllegalArgumentException("Years must be a positive number");
        }
        
        BigDecimal appreciationFactor = BigDecimal.ONE.add(annualAppreciationRate).pow(years);
        BigDecimal newValue = getCurrentValue().multiply(appreciationFactor);
        setCurrentValue(newValue);
    }
    
    @Override
    public BigDecimal getCurrentValue() {
        // Update the current value and return it
        BigDecimal estimatedValue = estimateValue();
        setCurrentValue(estimatedValue);
        return estimatedValue;
    }
    
    // Getters and setters
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        this.location = location;
    }
    
    public BigDecimal getAreaSquareMeters() {
        return areaSquareMeters;
    }
    
    public void setAreaSquareMeters(BigDecimal areaSquareMeters) {
        if (areaSquareMeters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Area must be greater than 0");
        }
        this.areaSquareMeters = areaSquareMeters;
    }
    
    public PropertyType getPropertyType() {
        return propertyType;
    }
    
    public void setPropertyType(PropertyType propertyType) {
        if (propertyType == null) {
            throw new IllegalArgumentException("Property type cannot be null");
        }
        this.propertyType = propertyType;
    }
    
    public BigDecimal getMonthlyRentalIncome() {
        return monthlyRentalIncome;
    }
    
    public void setMonthlyRentalIncome(BigDecimal monthlyRentalIncome) {
        if (monthlyRentalIncome == null || monthlyRentalIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monthly rental income cannot be negative");
        }
        this.monthlyRentalIncome = monthlyRentalIncome;
    }
    
    public float getOccupancyRate() {
        return occupancyRate;
    }
    
    public void setOccupancyRate(float occupancyRate) {
        if (occupancyRate < 0.0 || occupancyRate > 1.0) {
            throw new IllegalArgumentException("Occupancy rate must be between 0 and 1");
        }
        this.occupancyRate = occupancyRate;
    }
    
    public BigDecimal getAnnualPropertyTax() {
        return annualPropertyTax;
    }
    
    public void setAnnualPropertyTax(BigDecimal annualPropertyTax) {
        if (annualPropertyTax == null || annualPropertyTax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual property tax cannot be negative");
        }
        this.annualPropertyTax = annualPropertyTax;
    }
    
    public BigDecimal getAnnualMaintenanceCost() {
        return annualMaintenanceCost;
    }
    
    public void setAnnualMaintenanceCost(BigDecimal annualMaintenanceCost) {
        if (annualMaintenanceCost == null || annualMaintenanceCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual maintenance cost cannot be negative");
        }
        this.annualMaintenanceCost = annualMaintenanceCost;
    }
    
    public BigDecimal getAnnualInsuranceCost() {
        return annualInsuranceCost;
    }
    
    public void setAnnualInsuranceCost(BigDecimal annualInsuranceCost) {
        if (annualInsuranceCost == null || annualInsuranceCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual insurance cost cannot be negative");
        }
        this.annualInsuranceCost = annualInsuranceCost;
    }
    
    @Override
    public String toString() {
        return "RealEstate{" +
               "assetId='" + getAssetId() + '\'' +
               ", name='" + getName() + '\'' +
               ", purchaseDate=" + getPurchaseDate() +
               ", purchasePrice=" + getPurchasePrice() +
               ", currentValue=" + getCurrentValue() +
               ", location='" + location + '\'' +
               ", area=" + areaSquareMeters + " sq.m" +
               ", propertyType=" + propertyType +
               ", monthlyRentalIncome=" + monthlyRentalIncome +
               ", occupancyRate=" + occupancyRate +
               ", capRate=" + calculateCapRate() +
               ", ROI=" + calculateROI() +
               ", return=" + calculateReturn() +
               '}';
    }

    @Override
    public BigDecimal fetchPrice() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}