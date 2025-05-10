/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String portfolioId;
    private String userId;
    private List<Asset> assets;

    public Portfolio(String userId) {
        this.portfolioId = UUID.randomUUID().toString();
        this.userId = userId;
        this.assets = new ArrayList<>();
    }

    public boolean addAsset(Asset a) {
        if (a != null) {
            return assets.add(a);
        }
        return false;
    }

    public boolean removeAsset(String assetId) {
        return assets.removeIf(asset -> asset.getAssetId().equals(assetId));
    }

    public float getNetWorth() {
        float totalValue = 0.0f;
        for (Asset asset : assets) {
            totalValue += asset.getValue();
        }
        return totalValue;
    }

    // Getters and setters
    public String getPortfolioId() {
        return portfolioId;
    }

    public String getUserId() {
        return userId;
    }

    public List<Asset> getAssets() {
        return new ArrayList<>(assets); // Return a copy to maintain encapsulation
    }
}