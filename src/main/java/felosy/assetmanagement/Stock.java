package felosy.assetmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stock extends Asset {
    private TickerType ticker;
    private String exchange;
    private int sharesOwned;
    private BigDecimal dividendYield;
    private BigDecimal eps; // Earnings Per Share
    private List<Transaction> transactionHistory;
    
    /**
     * Represents a stock purchase or sale transaction
     */
    private static class Transaction {
        private LocalDate date;
        private int quantity;
        private BigDecimal pricePerShare;
        private boolean isBuy; // true for buy, false for sell
        
        public Transaction(LocalDate date, int quantity, BigDecimal pricePerShare, boolean isBuy) {
            this.date = date;
            this.quantity = quantity;
            this.pricePerShare = pricePerShare;
            this.isBuy = isBuy;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public BigDecimal getPricePerShare() {
            return pricePerShare;
        }
        
        public boolean isBuy() {
            return isBuy;
        }
        
        public BigDecimal getTransactionValue() {
            return pricePerShare.multiply(new BigDecimal(quantity));
        }
    }
    
    // Cache for historical prices to avoid repeated API calls
    private Map<LocalDate, BigDecimal> historicalPriceCache;
    
    public Stock(String assetId, String name, Date purchaseDate, 
                BigDecimal purchasePrice, BigDecimal currentValue,
                TickerType ticker, String exchange, int sharesOwned, 
                BigDecimal dividendYield, BigDecimal eps) {
        
        super(assetId, name, purchaseDate, purchasePrice, currentValue);
        setTicker(ticker);
        setExchange(exchange);
        setSharesOwned(sharesOwned);
        setDividendYield(dividendYield);
        setEps(eps);
        
        this.transactionHistory = new ArrayList<>();
        this.historicalPriceCache = new HashMap<>();
        
        // Add initial purchase as first transaction
        LocalDate purchaseLocalDate = new java.sql.Date(purchaseDate.getTime()).toLocalDate();
        BigDecimal pricePerShare = purchasePrice.divide(new BigDecimal(sharesOwned), 6, RoundingMode.HALF_UP);
        this.transactionHistory.add(new Transaction(purchaseLocalDate, sharesOwned, pricePerShare, true));
    }
    
    /**
     * Fetches the current market price for this stock
     * @return The current price per share
     */
    @Override
    public BigDecimal fetchPrice() {
        // Implementation to fetch current stock price from market
        // This is a placeholder - real implementation would query market data API
        System.out.println("Fetching current price for: " + ticker);
        
        // Example hard-coded prices for demonstration
        BigDecimal price;
        switch(ticker) {
            case AAPL:
                price = new BigDecimal("175.50");
                break;
            case MSFT:
                price = new BigDecimal("325.75");
                break;
            case GOOG:
            case GOOGL:
                price = new BigDecimal("2450.25");
                break;
            case AMZN:
                price = new BigDecimal("3250.00");
                break;
            default:
                price = new BigDecimal("150.75");
        }
        
        // Update the current value
        setCurrentValue(price.multiply(new BigDecimal(sharesOwned)));
        
        return price;
    }
    
    /**
     * Calculates the expected annual dividend based on current value and dividend yield
     * @return The expected annual dividend amount
     */
    public BigDecimal calculateDividend() {
        return getCurrentValue()
                .multiply(dividendYield)
                .divide(new BigDecimal(sharesOwned), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Fetches historical price data for a specific date
     * @param date The date to fetch the price for
     * @return The stock price on that date
     */
    public BigDecimal fetchHistoricalPrice(LocalDate date) {
        // Check cache first
        if (historicalPriceCache.containsKey(date)) {
            return historicalPriceCache.get(date);
        }
        
        // Implementation to fetch historical price
        // This is a placeholder - real implementation would query a financial data API
        System.out.println("Fetching historical price for " + ticker + " on " + date);
        
        // For demonstration, we'll generate a reasonable random price based on current price
        BigDecimal currentPrice = fetchPrice();
        
        // Calculate days between current date and requested date
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        
        // Apply a small random variation based on the number of days
        double variation = 1.0 - (daysBetween * 0.0002); // Slight downward trend into the past
        
        // Add some randomness (±10%)
        double randomFactor = 0.9 + (Math.random() * 0.2);
        
        BigDecimal historicalPrice = currentPrice.multiply(
                new BigDecimal(variation * randomFactor)).setScale(2, RoundingMode.HALF_UP);
        
        // Cache the result
        historicalPriceCache.put(date, historicalPrice);
        
        return historicalPrice;
    }
    
    /**
     * Buys additional shares of the stock
     * @param qty Quantity of shares to buy
     * @param pricePerShare Price per share
     */
    public void buyShares(int qty, BigDecimal pricePerShare) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (pricePerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        // Add to transaction history
        transactionHistory.add(new Transaction(LocalDate.now(), qty, pricePerShare, true));
        
        // Calculate new average purchase price
        BigDecimal totalShares = new BigDecimal(sharesOwned + qty);
        BigDecimal currentTotalCost = getPurchasePrice();
        BigDecimal additionalCost = pricePerShare.multiply(new BigDecimal(qty));
        BigDecimal newTotalCost = currentTotalCost.add(additionalCost);
        BigDecimal newAverageCost = newTotalCost.divide(totalShares, 6, RoundingMode.HALF_UP);
        
        // Update shares owned and purchase price
        sharesOwned += qty;
        setPurchasePrice(newTotalCost);
        
        System.out.println("Bought " + qty + " shares of " + ticker + " at $" + pricePerShare);
        System.out.println("New average cost: $" + newAverageCost);
        System.out.println("Total shares owned: " + sharesOwned);
    }
    
    /**
     * Sells shares of the stock
     * @param qty Quantity of shares to sell
     * @param pricePerShare Price per share
     * @return The realized profit or loss from this sale
     */
    public BigDecimal sellShares(int qty, BigDecimal pricePerShare) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (qty > sharesOwned) {
            throw new IllegalArgumentException("Cannot sell more shares than owned");
        }
        if (pricePerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        // Add to transaction history
        transactionHistory.add(new Transaction(LocalDate.now(), qty, pricePerShare, false));
        
        // Calculate average cost per share
        BigDecimal averageCost = getPurchasePrice().divide(new BigDecimal(sharesOwned), 6, RoundingMode.HALF_UP);
        
        // Calculate realized profit/loss
        BigDecimal realizedPL = pricePerShare.subtract(averageCost).multiply(new BigDecimal(qty));
        
        // Calculate remaining investment
        BigDecimal remainingShares = new BigDecimal(sharesOwned - qty);
        BigDecimal remainingInvestment;
        
        if (remainingShares.compareTo(BigDecimal.ZERO) > 0) {
            remainingInvestment = getPurchasePrice()
                    .multiply(remainingShares)
                    .divide(new BigDecimal(sharesOwned), 6, RoundingMode.HALF_UP);
        } else {
            remainingInvestment = BigDecimal.ZERO;
        }
        
        // Update shares owned and purchase price
        sharesOwned -= qty;
        setPurchasePrice(remainingInvestment);
        
        // Update current value based on remaining shares
        setCurrentValue(fetchPrice().multiply(new BigDecimal(sharesOwned)));
        
        System.out.println("Sold " + qty + " shares of " + ticker + " at $" + pricePerShare);
        System.out.println("Realized P/L: $" + realizedPL);
        System.out.println("Remaining shares: " + sharesOwned);
        
        return realizedPL;
    }
    
    /**
     * Calculates the price to earnings ratio
     * @return P/E ratio
     */
    public BigDecimal calculatePERatio() {
        if (eps.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("-1"); // Invalid P/E ratio when EPS is zero
        }
        BigDecimal pricePerShare = fetchPrice();
        return pricePerShare.divide(eps, 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal getCurrentValue() {
        BigDecimal currentPrice = fetchPrice();
        return currentPrice.multiply(new BigDecimal(sharesOwned));
    }
    
    // Getters and setters
    public TickerType getTicker() {
        return ticker;
    }
    
    public void setTicker(TickerType ticker) {
        if (ticker == null) {
            throw new IllegalArgumentException("Ticker symbol cannot be null");
        }
        this.ticker = ticker;
    }
    
    public String getExchange() {
        return exchange;
    }
    
    public void setExchange(String exchange) {
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Exchange cannot be null or empty");
        }
        this.exchange = exchange;
    }
    
    public int getSharesOwned() {
        return sharesOwned;
    }
    
    public void setSharesOwned(int sharesOwned) {
        if (sharesOwned < 0) {
            throw new IllegalArgumentException("Shares owned cannot be negative");
        }
        this.sharesOwned = sharesOwned;
    }
    
    public BigDecimal getDividendYield() {
        return dividendYield;
    }
    
    public void setDividendYield(BigDecimal dividendYield) {
        if (dividendYield == null || dividendYield.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dividend yield cannot be negative");
        }
        this.dividendYield = dividendYield;
    }
    
    public BigDecimal getEps() {
        return eps;
    }
    
    public void setEps(BigDecimal eps) {
        if (eps == null) {
            throw new IllegalArgumentException("EPS cannot be null");
        }
        this.eps = eps;
    }
    
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory); // Return a copy to preserve encapsulation
    }
    
    @Override
    public String toString() {
        return "Stock{" +
               "assetId='" + getAssetId() + '\'' +
               ", name='" + getName() + '\'' +
               ", purchaseDate=" + getPurchaseDate() +
               ", purchasePrice=" + getPurchasePrice() +
               ", currentValue=" + getCurrentValue() +
               ", ticker=" + ticker +
               ", exchange='" + exchange + '\'' +
               ", sharesOwned=" + sharesOwned +
               ", dividendYield=" + dividendYield +
               ", eps=" + eps +
               ", P/E=" + calculatePERatio() +
               ", annualDividend=" + calculateDividend() +
               ", return=" + calculateReturn() +
               '}';
    }
}