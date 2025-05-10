/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.util.Date;

public abstract class Asset {
    private String assetId;
    private String name;
    private Date purchaseDate;

    public Asset(String assetId, String name, Date purchaseDate) {
        this.assetId = assetId;
        this.name = name;
        this.purchaseDate = purchaseDate;
    }

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

    // Getters and setters
    public String getAssetId() {
        return assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}