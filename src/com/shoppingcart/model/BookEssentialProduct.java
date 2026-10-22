package com.shoppingcart.model;

/**
 * Specialized subclass representing Books & Everyday Lifestyle Essentials.
 * Demonstrates Inheritance (extends Product) and Polymorphism (implements Taxable, Shippable).
 */
public class BookEssentialProduct extends Product implements Taxable, Shippable {
    private String publisherOrBrand;
    private String formatOrType;
    private String editionOrModel;
    private double weightKg;

    public BookEssentialProduct(String id, String name, String description, double basePrice, double originalMrp,
                                int stockQuantity, String imagePath, double rating, int reviewCount,
                                String bundlePartnerProductId, String publisherOrBrand, String formatOrType,
                                String editionOrModel, double weightKg) {
        super(id, name, description, basePrice, originalMrp, stockQuantity, Category.BOOKS_ESSENTIALS, imagePath, rating, reviewCount, bundlePartnerProductId);
        this.publisherOrBrand = publisherOrBrand != null ? publisherOrBrand : "Penguin Random House";
        this.formatOrType = formatOrType != null ? formatOrType : "Hardcover";
        this.editionOrModel = editionOrModel != null ? editionOrModel : "Collector Edition";
        this.weightKg = Math.max(0.1, weightKg);
    }

    public BookEssentialProduct(String id, String name, String description, double basePrice,
                                int stockQuantity, String imagePath, double rating, int reviewCount,
                                String bundlePartnerProductId, String publisherOrBrand, String formatOrType,
                                String editionOrModel, double weightKg) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.45), stockQuantity, imagePath, rating,
             reviewCount, bundlePartnerProductId, publisherOrBrand, formatOrType, editionOrModel, weightKg);
    }

    public String getPublisherOrBrand() {
        return publisherOrBrand;
    }

    public void setPublisherOrBrand(String publisherOrBrand) {
        this.publisherOrBrand = publisherOrBrand;
    }

    public String getFormatOrType() {
        return formatOrType;
    }

    public void setFormatOrType(String formatOrType) {
        this.formatOrType = formatOrType;
    }

    public String getEditionOrModel() {
        return editionOrModel;
    }

    public void setEditionOrModel(String editionOrModel) {
        this.editionOrModel = editionOrModel;
    }

    // --- Taxable implementation (Standard Indian Books & Essentials GST: 5%) ---
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
        return "22 x 16 x 6 cm (Eco-padded book carton)";
    }

    @Override
    public String getCategorySpecificHighlight() {
        return formatOrType + " • " + publisherOrBrand;
    }

    @Override
    public String getDetailedSpecificationsHtml() {
        return "<html>"
                + "<b>Format / Build:</b> " + formatOrType + "<br/>"
                + "<b>Publisher / Brand:</b> " + publisherOrBrand + "<br/>"
                + "<b>Edition / Specs:</b> " + editionOrModel + "<br/>"
                + "<b>Item Weight:</b> " + String.format("%.2f kg", weightKg) + "<br/>"
                + "<b>GST Rate:</b> 5% Books & Educational Essentials<br/>"
                + "</html>";
    }
}
