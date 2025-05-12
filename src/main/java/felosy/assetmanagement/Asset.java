package felosy.assetmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public abstract class Asset {
    private String assetId;
    private String name;
    private Date purchaseDate;
    private BigDecimal purchasePrice;
    private BigDecimal currentValue;
    private Date actionDate; // Date when asset was added

    public Asset(String assetId, String name, Date purchaseDate, BigDecimal purchasePrice, BigDecimal currentValue) {
        this.assetId = assetId;
        this.name = name;
        setPurchaseDate(purchaseDate);
        setPurchasePrice(purchasePrice);
        setCurrentValue(currentValue);
        this.actionDate = new Date(); // Default to now when constructed
    }

    // New constructor to allow setting actionDate explicitly
    public Asset(String assetId, String name, Date purchaseDate, BigDecimal purchasePrice, BigDecimal currentValue, Date actionDate) {
        this.assetId = assetId;
        this.name = name;
        setPurchaseDate(purchaseDate);
        setPurchasePrice(purchasePrice);
        setCurrentValue(currentValue);
        this.actionDate = actionDate != null ? actionDate : new Date();
    }

    /**
     *
     * @return current price of Asset
     */
    public abstract BigDecimal fetchPrice();
    
    // Getters and setters
    public String getAssetId() {
        return assetId;
    }

    public String getName() {
        return name;
    }
    
    public Date getPurchaseDate() {
        return purchaseDate;
    }
    
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    
    public BigDecimal getCurrentValue() {
        return currentValue;
    }
    
    public Date getActionDate() {
        return actionDate;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setPurchaseDate(Date purchaseDate) {
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null"); 
        }
        this.purchaseDate = purchaseDate;
    }
    
    public void setPurchasePrice(BigDecimal purchasePrice) {
        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than 0");    
        }
        this.purchasePrice = purchasePrice;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        if (currentValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Current value must be greater than 0");
        }
        this.currentValue = currentValue;
    }
    
    public void setActionDate(Date actionDate) {
        if (actionDate == null) {
            throw new IllegalArgumentException("Action date can not be null");
        }
        this.actionDate = actionDate;
    }
    
    public BigDecimal calculateReturn() {
        return currentValue.subtract(purchasePrice).divide(purchasePrice, 4, RoundingMode.HALF_UP);
    }

    
    @Override
    public String toString() {
        return "Asset{" +
           "assetId='" + assetId + '\'' +
           ", name='" + name + '\'' +
           ", purchaseDate=" + purchaseDate +
           ", purchasePrice=" + purchasePrice +
           ", currentValue=" + currentValue +
           ", actionDate=" + actionDate +
           ", return=" + calculateReturn() +
           '}';
    }

}