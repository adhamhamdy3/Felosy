/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.util.Date;

public class Gold extends Asset {
    private float weight;
    private float purity;

    public Gold(String assetId, String name, Date purchaseDate, float weight, float purity) {
        super(assetId, name, purchaseDate);
        this.weight = weight;
        this.purity = purity;
    }

    public float getMarketValue() {
        // Implementation to get current gold market value
        // This is a placeholder - real implementation would fetch market data
        System.out.println("Fetching market value for gold");
        float goldPricePerGram = 65.0f; // Example current gold price per gram
        return weight * purity * goldPricePerGram;
    }

    @Override
    public float getValue() {
        return getMarketValue();
    }

    // Getters and setters
    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getPurity() {
        return purity;
    }

    public void setPurity(float purity) {
        this.purity = purity;
    }
}