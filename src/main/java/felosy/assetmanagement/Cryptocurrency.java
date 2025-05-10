package felosy.assetmanagement;

import java.math.BigDecimal;
import java.util.Date;

public final class Cryptocurrency extends Asset {
    private CoinType coin;
    private BigDecimal amount;
    
    public Cryptocurrency(String assetId, String name, Date purchaseDate, BigDecimal purchasePrice, 
                         BigDecimal currentValue, CoinType coin, BigDecimal amount) {
        super(assetId, name, purchaseDate, purchasePrice, currentValue);
        this.coin = coin;
        setAmount(amount);
    }
    
    @Override
    public BigDecimal fetchPrice() {
        // Implementation to fetch current cryptocurrency price
        // This is a placeholder - real implementation would query market APIs
        System.out.println("Fetching current price for: " + coin);
        BigDecimal price;
        
        switch (coin) {
            case BTC:
                price = new BigDecimal("45000.0");
                break;
            case ETH:
                price = new BigDecimal("2500.0");
                break;
            default:
                price = new BigDecimal("100.0");
        }
        
        setCurrentValue(price);
        return price;
    }
    
    @Override
    public BigDecimal getCurrentValue() {
        return fetchPrice().multiply(amount);
    }
    
    // Getters and setters
    public CoinType getCoin() {
        return coin;
    }
    
    public void setCoin(CoinType coin) {
        this.coin = coin;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "Cryptocurrency{" +
               "assetId='" + getAssetId() + '\'' +
               ", name='" + getName() + '\'' +
               ", purchaseDate=" + getPurchaseDate() +
               ", purchasePrice=" + getPurchasePrice() +
               ", currentValue=" + getCurrentValue() +
               ", coin=" + coin +
               ", amount=" + amount +
               ", return=" + calculateReturn() +
               '}';
    }
}