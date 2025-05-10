/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.assetmanagement;

import java.util.Date;

public class RealEstate extends Asset {
    private String location;
    private float area;

    public RealEstate(String assetId, String name, Date purchaseDate, String location, float area) {
        super(assetId, name, purchaseDate);
        this.location = location;
        this.area = area;
    }

    public float estimateValue() {
        // Implementation to estimate real estate value
        // This is a placeholder - real implementation would use complex valuation models
        System.out.println("Estimating value for property at: " + location);
        return area * 2000.0f; // Example calculation: area * price per square unit
    }

    @Override
    public float getValue() {
        return estimateValue();
    }

    // Getters and setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }
}