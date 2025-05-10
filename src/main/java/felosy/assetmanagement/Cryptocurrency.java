/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.util.Date;

public class Cryptocurrency extends Asset {
    private String coinSymbol;
    private float amount;

    public Cryptocurrency(String assetId, String name, Date purchaseDate, String coinSymbol, float amount) {
        super(assetId, name, purchaseDate);
        this.coinSymbol = coinSymbol;
        this.amount = amount;
    }

    public float fetchPrice() {
        // Implementation to fetch current cryptocurrency price
        // This is a placeholder - real implementation would query market APIs
        System.out.println("Fetching current price for: " + coinSymbol);

        float currentPrice;
        switch (coinSymbol.toUpperCase()) {
            case "BTC":
                currentPrice = 45000.0f;
                break;
            case "ETH":
                currentPrice = 2500.0f;
                break;
            default:
                currentPrice = 100.0f;
        }

        return currentPrice;
    }

    @Override
    public float getValue() {
        return fetchPrice() * amount;
    }

    // Getters and setters
    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}