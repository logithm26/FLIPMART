package com.shoppingcart.model;

/**
 * Specialized subclass representing physical consumer electronics & gadgets.
 * Demonstrates Inheritance (extends Product) and Polymorphism (implements Taxable, Shippable).
 */
public class ElectronicsProduct extends Product implements Taxable, Shippable {
    private int warrantyMonths;
    private String brand;
    private int powerConsumptionWatts;
    private String modelNumber;
    private double weightKg;

    public ElectronicsProduct(String id, String name, String description, double basePrice, double originalMrp,
                              int stockQuantity, String imagePath, double rating, int reviewCount,
                              String bundlePartnerProductId, int warrantyMonths, String brand,
                              int powerConsumptionWatts, String modelNumber, double weightKg) {
        super(id, name, description, basePrice, originalMrp, stockQuantity, Category.ELECTRONICS, imagePath, rating, reviewCount, bundlePartnerProductId);
        this.warrantyMonths = Math.max(0, warrantyMonths);
        this.brand = brand != null ? brand : "Unknown";
        this.powerConsumptionWatts = Math.max(0, powerConsumptionWatts);
        this.modelNumber = modelNumber != null ? modelNumber : "GEN-01";
        this.weightKg = Math.max(0.1, weightKg);
    }

    public ElectronicsProduct(String id, String name, String description, double basePrice,
                              int stockQuantity, String imagePath, double rating, int reviewCount,
                              String bundlePartnerProductId, int warrantyMonths, String brand,
                              int powerConsumptionWatts, String modelNumber, double weightKg) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.25), stockQuantity, imagePath, rating,
             reviewCount, bundlePartnerProductId, warrantyMonths, brand, powerConsumptionWatts, modelNumber, weightKg);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = Math.max(0, warrantyMonths);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getPowerConsumptionWatts() {
        return powerConsumptionWatts;
    }

    public void setPowerConsumptionWatts(int powerConsumptionWatts) {
        this.powerConsumptionWatts = powerConsumptionWatts;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    // --- Taxable implementation (Standard Indian Electronics GST: 18%) ---
    @Override
    public double getTaxRate() {
        return 0.18;
    }

    // --- Shippable implementation ---
    @Override
    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = Math.max(0.1, weightKg);
    }

    @Override
    public String getShippingDimensions() {
        return "35 x 25 x 12 cm (Rigid protective carton with foam packaging)";
    }

    @Override
    public String getCategorySpecificHighlight() {
        return brand + " • " + warrantyMonths + "M Warranty";
    }

    @Override
    public String getDetailedSpecificationsHtml() {
        return "<html>"
                + "<b>Brand:</b> " + brand + "<br/>"
                + "<b>Model:</b> " + modelNumber + "<br/>"
                + "<b>Warranty:</b> " + warrantyMonths + " Months Official Manufacturer Coverage<br/>"
                + "<b>Power Draw:</b> " + powerConsumptionWatts + " Watts<br/>"
                + "<b>Item Weight:</b> " + String.format("%.2f kg", weightKg) + "<br/>"
                + "<b>GST Rate:</b> 18% Consumer Electronics (Included in MRP)<br/>"
                + "</html>";
    }
}
