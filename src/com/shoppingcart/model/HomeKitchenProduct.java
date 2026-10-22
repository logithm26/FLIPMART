package com.shoppingcart.model;

/**
 * Specialized subclass representing Home & Kitchen appliances, cookware, and dinnerware.
 * Demonstrates Inheritance (extends Product) and Polymorphism (implements Taxable, Shippable).
 */
public class HomeKitchenProduct extends Product implements Taxable, Shippable {
    private String material;
    private String capacityOrSize;
    private int powerConsumptionWatts;
    private double weightKg;

    public HomeKitchenProduct(String id, String name, String description, double basePrice, double originalMrp,
                              int stockQuantity, String imagePath, double rating, int reviewCount,
                              String bundlePartnerProductId, String material, String capacityOrSize,
                              int powerConsumptionWatts, double weightKg) {
        super(id, name, description, basePrice, originalMrp, stockQuantity, Category.HOME_KITCHEN, imagePath, rating, reviewCount, bundlePartnerProductId);
        this.material = material != null ? material : "Stainless Steel";
        this.capacityOrSize = capacityOrSize != null ? capacityOrSize : "Standard";
        this.powerConsumptionWatts = Math.max(0, powerConsumptionWatts);
        this.weightKg = Math.max(0.1, weightKg);
    }

    public HomeKitchenProduct(String id, String name, String description, double basePrice,
                              int stockQuantity, String imagePath, double rating, int reviewCount,
                              String bundlePartnerProductId, String material, String capacityOrSize,
                              int powerConsumptionWatts, double weightKg) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.4), stockQuantity, imagePath, rating,
             reviewCount, bundlePartnerProductId, material, capacityOrSize, powerConsumptionWatts, weightKg);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getCapacityOrSize() {
        return capacityOrSize;
    }

    public void setCapacityOrSize(String capacityOrSize) {
        this.capacityOrSize = capacityOrSize;
    }

    public int getPowerConsumptionWatts() {
        return powerConsumptionWatts;
    }

    public void setPowerConsumptionWatts(int powerConsumptionWatts) {
        this.powerConsumptionWatts = powerConsumptionWatts;
    }

    // --- Taxable implementation (Standard Indian Kitchen Appliances GST: 12%) ---
    @Override
    public double getTaxRate() {
        return 0.12;
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
        return "30 x 30 x 25 cm (Shock-absorbing reinforced corrugated box)";
    }

    @Override
    public String getCategorySpecificHighlight() {
        if (powerConsumptionWatts > 0) {
            return capacityOrSize + " • " + material + " • " + powerConsumptionWatts + "W";
        }
        return capacityOrSize + " • " + material;
    }

    @Override
    public String getDetailedSpecificationsHtml() {
        return "<html>"
                + "<b>Capacity / Dimensions:</b> " + capacityOrSize + "<br/>"
                + "<b>Food-Grade Material:</b> " + material + "<br/>"
                + (powerConsumptionWatts > 0 ? "<b>Rated Power:</b> " + powerConsumptionWatts + " Watts<br/>" : "")
                + "<b>Shipping Weight:</b> " + String.format("%.2f kg", weightKg) + "<br/>"
                + "<b>GST Rate:</b> 12% Home & Kitchen Appliances<br/>"
                + "</html>";
    }
}
