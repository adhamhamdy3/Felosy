/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.util.Date;

public class Stock extends Asset {
    private String tickerSymbol;
    private int quantity;

    public Stock(String assetId, String name, Date purchaseDate, String tickerSymbol, int quantity) {
        super(assetId, name, purchaseDate);
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
    }

    public float getCurrentPrice() {
        // Implementation to fetch current stock price from market
        // This is a placeholder - real implementation would query market data
        System.out.println("Fetching current price for: " + tickerSymbol);
        return 150.75f; // Example price
    }

    @Override
    public float getValue() {
        return getCurrentPrice() * quantity;
    }

    // Getters and setters
    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}