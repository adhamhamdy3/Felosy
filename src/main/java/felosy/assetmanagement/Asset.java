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

    public Asset(String assetId, String name, Date purchaseDate, BigDecimal purchasePrice, BigDecimal currentValue) {
        this.assetId = assetId;
        this.name = name;
        setPurchaseDate(purchaseDate);
        setPurchasePrice(purchasePrice);
        setCurrentValue(currentValue);
    }

    /*
    public abstract float getValue();

    public boolean update() {
        // Implementation for updating asset details
        System.out.println("Updating asset: " + name);
        return true;
    }

    public boolean delete() {
        // Implementation for deleting an asset
        System.out.println("Deleting asset: " + name);
        return true;
    }
    */

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
           ", return=" + calculateReturn() +
           '}';
    }

}