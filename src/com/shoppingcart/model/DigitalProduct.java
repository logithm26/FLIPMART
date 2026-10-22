package com.shoppingcart.model;

/**
 * Specialized subclass representing downloadable, intangible digital software / media goods.
 * Demonstrates Inheritance (extends Product) and Selective Polymorphism (implements Taxable,
 * but deliberately DOES NOT implement Shippable since delivery is instantaneous and electronic).
 */
public class DigitalProduct extends Product implements Taxable {
    private String downloadUrl;
    private double fileSizeMb;
    private String licenseKeyFormat;
    private boolean isInstantDelivery;

    public DigitalProduct(String id, String name, String description, double basePrice, double originalMrp,
                          int stockQuantity, String imagePath, double rating, int reviewCount,
                          String bundlePartnerProductId, String downloadUrl, double fileSizeMb,
                          String licenseKeyFormat, boolean isInstantDelivery) {
        super(id, name, description, basePrice, originalMrp, stockQuantity, Category.DIGITAL, imagePath, rating, reviewCount, bundlePartnerProductId);
        this.downloadUrl = downloadUrl != null ? downloadUrl : "https://downloads.flipmart.in/get";
        this.fileSizeMb = Math.max(0.1, fileSizeMb);
        this.licenseKeyFormat = licenseKeyFormat != null ? licenseKeyFormat : "XXXX-XXXX-XXXX-XXXX";
        this.isInstantDelivery = isInstantDelivery;
    }

    public DigitalProduct(String id, String name, String description, double basePrice,
                          int stockQuantity, String imagePath, double rating, int reviewCount,
                          String bundlePartnerProductId, String downloadUrl, double fileSizeMb,
                          String licenseKeyFormat, boolean isInstantDelivery) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.5), stockQuantity, imagePath, rating,
             reviewCount, bundlePartnerProductId, downloadUrl, fileSizeMb, licenseKeyFormat, isInstantDelivery);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public double getFileSizeMb() {
        return fileSizeMb;
    }

    public void setFileSizeMb(double fileSizeMb) {
        this.fileSizeMb = Math.max(0.1, fileSizeMb);
    }

    public String getLicenseKeyFormat() {
        return licenseKeyFormat;
    }

    public void setLicenseKeyFormat(String licenseKeyFormat) {
        this.licenseKeyFormat = licenseKeyFormat;
    }

    public boolean isInstantDelivery() {
        return isInstantDelivery;
    }

    public void setInstantDelivery(boolean instantDelivery) {
        this.isInstantDelivery = instantDelivery;
    }

    // --- Polymorphic Taxable implementation ---
    @Override
    public double getTaxRate() {
        return 0.10;
    }

    @Override
    public String getCategorySpecificHighlight() {
        return String.format("%.0f MB • Instant Key Delivery", fileSizeMb);
    }

    @Override
    public String getDetailedSpecificationsHtml() {
        return "<html>"
                + "<b>Delivery Type:</b> Instant Electronic License via Email/Download<br/>"
                + "<b>File Package Size:</b> " + String.format("%.1f MB", fileSizeMb) + "<br/>"
                + "<b>License Pattern:</b> " + licenseKeyFormat + "<br/>"
                + "<b>GST Rate:</b> 10% Digital Goods & Software License<br/>"
                + "<b>Delivery Fee:</b> ₹0 (Instant Electronic Access)<br/>"
                + "<b>Download Mirror:</b> " + downloadUrl + "<br/>"
                + "</html>";
    }
}
