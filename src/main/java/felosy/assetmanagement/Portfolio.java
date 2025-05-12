// Add more attributes and methods
package felosy.assetmanagement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents a user's investment portfolio
 */
public final class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Portfolio.class.getName());
    
    private final String portfolioId;
    private final String userId;
    private final Map<String, Asset> assets;
    private Date lastUpdated;
    private String name;
    private String description;

    /**
     * Creates a new portfolio for a user
     * @param userId The ID of the user who owns this portfolio
     * @throws IllegalArgumentException if userId is null or empty
     */
    public Portfolio(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.portfolioId = UUID.randomUUID().toString();
        this.userId = userId;
        this.assets = new HashMap<>();
        this.lastUpdated = new Date();
        this.name = "Portfolio " + portfolioId.substring(0, 8);
        this.description = "Portfolio created on " + new Date();
    }

    /**
     * Adds an asset to the portfolio
     * @param asset The asset to add
     * @return true if the asset was added successfully
     * @throws IllegalArgumentException if asset is null or already exists
     */
    public boolean addAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset cannot be null");
        }
        if (assets.containsKey(asset.getAssetId())) {
            throw new IllegalArgumentException("Asset with ID " + asset.getAssetId() + " already exists in portfolio");
        }
        
        assets.put(asset.getAssetId(), asset);
        lastUpdated = new Date();
        LOGGER.info("Added asset " + asset.getAssetId() + " to portfolio " + portfolioId);
        return true;
    }

    /**
     * Removes an asset from the portfolio
     * @param assetId The ID of the asset to remove
     * @return true if the asset was removed successfully
     */
    public boolean removeAsset(String assetId) {
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset ID cannot be null or empty");
        }
        
        boolean removed = assets.remove(assetId) != null;
        if (removed) {
            lastUpdated = new Date();
            LOGGER.info("Removed asset " + assetId + " from portfolio " + portfolioId);
        }
        return removed;
    }

    /**
     * Calculates the total net worth of the portfolio
     * @return The total value of all assets in the portfolio
     */
    public BigDecimal getNetWorth() {
        try {
            return assets.values().stream()
                .map(Asset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating portfolio net worth", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Gets the percentage of total portfolio value for a specific asset
     * @param assetId The ID of the asset
     * @return The percentage as a decimal (e.g., 0.25 for 25%)
     * @throws IllegalArgumentException if assetId is null or empty
     */
    public BigDecimal getAssetPercentage(String assetId) {
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset ID cannot be null or empty");
        }
        
        Asset asset = assets.get(assetId);
        if (asset == null) {
            throw new IllegalArgumentException("Asset not found in portfolio");
        }
        
        BigDecimal totalValue = getNetWorth();
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return asset.getCurrentValue().divide(totalValue, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Gets all assets of a specific type
     * @param assetType The type of assets to retrieve
     * @return List of assets of the specified type
     */
    public List<Asset> getAssetsByType(Class<? extends Asset> assetType) {
        return assets.values().stream()
            .filter(assetType::isInstance)
            .toList();
    }

    // Getters and setters
    public String getPortfolioId() {
        return portfolioId;
    }

    public String getUserId() {
        return userId;
    }

    public List<Asset> getAssets() {
        return new ArrayList<>(assets.values());
    }
    
    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime());
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
            lastUpdated = new Date();
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        lastUpdated = new Date();
    }
    
    @Override
    public String toString() {
        return String.format("Portfolio[id=%s, name=%s, assets=%d, netWorth=%s]",
            portfolioId, name, assets.size(), getNetWorth());
    }
}