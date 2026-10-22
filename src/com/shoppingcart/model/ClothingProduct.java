package com.shoppingcart.model;

/**
 * Specialized subclass representing physical apparel, clothing, and fashion accessories.
 * Demonstrates Inheritance (extends Product) and Polymorphism (implements Taxable, Shippable).
 */
public class ClothingProduct extends Product implements Taxable, Shippable {
    private String size;
    private String color;
    private String material;
    private String genderCategory;
    private double weightKg;

    public ClothingProduct(String id, String name, String description, double basePrice, double originalMrp,
                           int stockQuantity, String imagePath, double rating, int reviewCount,
                           String bundlePartnerProductId, String size, String color,
                           String material, String genderCategory, double weightKg) {
        super(id, name, description, basePrice, originalMrp, stockQuantity, Category.FASHION, imagePath, rating, reviewCount, bundlePartnerProductId);
        this.size = size != null ? size : "M";
        this.color = color != null ? color : "Black";
        this.material = material != null ? material : "100% Cotton";
        this.genderCategory = genderCategory != null ? genderCategory : "Unisex";
        this.weightKg = Math.max(0.1, weightKg);
    }

    public ClothingProduct(String id, String name, String description, double basePrice,
                           int stockQuantity, String imagePath, double rating, int reviewCount,
                           String bundlePartnerProductId, String size, String color,
                           String material, String genderCategory, double weightKg) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.5), stockQuantity, imagePath, rating,
             reviewCount, bundlePartnerProductId, size, color, material, genderCategory, weightKg);
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getGenderCategory() {
        return genderCategory;
    }

    public void setGenderCategory(String genderCategory) {
        this.genderCategory = genderCategory;
    }

    // --- Taxable implementation (Standard Indian Apparel GST: 5%) ---
    @Override
    public double getTaxRate() {
        return 0.05;
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
        return "25 x 20 x 5 cm (Eco-friendly compostable polymailer)";
    }

    @Override
    public String getCategorySpecificHighlight() {
        return size + " • " + color + " • " + material;
    }

    @Override
    public String getDetailedSpecificationsHtml() {
        return "<html>"
                + "<b>Size:</b> " + size + "<br/>"
                + "<b>Color:</b> " + color + "<br/>"
                + "<b>Fabric Material:</b> " + material + "<br/>"
                + "<b>Fit & Category:</b> " + genderCategory + "<br/>"
                + "<b>Garment Weight:</b> " + String.format("%.2f kg", weightKg) + "<br/>"
                + "<b>GST Rate:</b> 5% Apparel Tax Bracket<br/>"
                + "</html>";
    }
}
