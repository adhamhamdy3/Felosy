package felosy.assetmanagement;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A simple concrete implementation of Asset for academic purposes
 */
public class SimpleAsset extends Asset {
    private String symbol;
    
    public SimpleAsset(String assetId, String name, String symbol, BigDecimal value) {
        super(assetId, name, new Date(), value, value);
        this.symbol = symbol;
    }
    
    @Override
    public BigDecimal fetchPrice() {
        return getCurrentValue();
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
} 