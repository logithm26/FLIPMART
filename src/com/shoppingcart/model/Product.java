package com.shoppingcart.model;

import com.shoppingcart.util.CurrencyUtil;
import java.util.Objects;

/**
 * Abstract Base Class establishing the foundation for all FlipMart catalog products.
 * Demonstrates clean OOP Encapsulation with validated state accessors, immutable identifier,
 * and commercial retail pricing structures.
 */
public abstract class Product {
    // Immutable ID guaranteeing identity integrity
    private final String id;

    // Encapsulated state fields
    private String name;
    private String description;
    private double basePrice;
    private double originalMrp;
    private int stockQuantity;
    private Category category;
    private String imagePath;
    private double rating;
    private int reviewCount;
    private String bundlePartnerProductId; // Recommendation target for smart bundles

    /**
     * Standard constructor for subclass instantiation with explicit MRP.
     */
    protected Product(String id, String name, String description, double basePrice, double originalMrp,
                      int stockQuantity, Category category, String imagePath,
                      double rating, int reviewCount, String bundlePartnerProductId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        this.id = id;
        this.name = Objects.requireNonNull(name, "Product name cannot be null.");
        this.description = Objects.requireNonNull(description, "Description cannot be null.");
        this.basePrice = basePrice;
        this.originalMrp = originalMrp > basePrice ? originalMrp : Math.round(basePrice * 1.35);
        this.stockQuantity = stockQuantity;
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
        this.imagePath = imagePath;
        this.rating = Math.max(0.0, Math.min(5.0, rating));
        this.reviewCount = Math.max(0, reviewCount);
        this.bundlePartnerProductId = bundlePartnerProductId;
    }

    /**
     * Overloaded constructor for backwards compatibility with legacy product instantiations.
     */
    protected Product(String id, String name, String description, double basePrice,
                      int stockQuantity, Category category, String imagePath,
                      double rating, int reviewCount, String bundlePartnerProductId) {
        this(id, name, description, basePrice, Math.round(basePrice * 1.35), stockQuantity, category,
             imagePath, rating, reviewCount, bundlePartnerProductId);
    }

    // --- Immutable Identifier Getter ---
    public String getId() {
        return id;
    }

    // --- Encapsulated Getters & Setters with Business Rules ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Product name cannot be null.");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description cannot be null.");
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.basePrice = basePrice;
        if (this.originalMrp < this.basePrice) {
            this.originalMrp = Math.round(this.basePrice * 1.35);
        }
    }

    public double getOriginalMrp() {
        return originalMrp;
    }

    public void setOriginalMrp(double originalMrp) {
        this.originalMrp = originalMrp;
    }

    public int getDiscountPercent() {
        if (originalMrp <= basePrice) return 0;
        return (int) Math.round(((originalMrp - basePrice) / originalMrp) * 100);
    }

    public String getFormattedPrice() {
        return CurrencyUtil.format(basePrice);
    }

    public String getFormattedMrp() {
        return CurrencyUtil.format(originalMrp);
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public synchronized void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.stockQuantity = stockQuantity;
    }

    /**
     * Decrements inventory safely. Returns true if successful.
     */
    public synchronized boolean decrementStock(int count) {
        if (count <= 0) return false;
        if (this.stockQuantity >= count) {
            this.stockQuantity -= count;
            return true;
        }
        return false;
    }

    /**
     * Restores inventory safely.
     */
    public synchronized void incrementStock(int count) {
        if (count > 0) {
            this.stockQuantity += count;
        }
    }

    public boolean isAvailable() {
        return stockQuantity > 0;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imagePath;
    }

    public void setImageUrl(String imageUrl) {
        this.imagePath = imageUrl;
    }

    public String getCategoryFallbackAsset() {
        if (category == null) return "assets/aurasound_headphones.png";
        return switch (category) {
            case FASHION -> "assets/mens_slim_shirt.png";
            case HOME_KITCHEN -> "assets/electric_kettle.png";
            case BOOKS_ESSENTIALS -> "assets/bestseller_books.png";
            case DIGITAL -> "assets/devmaster_ide.png";
            default -> "assets/aurasound_headphones.png";
        };
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(5.0, rating));
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = Math.max(0, reviewCount);
    }

    public String getBundlePartnerProductId() {
        return bundlePartnerProductId;
    }

    public void setBundlePartnerProductId(String bundlePartnerProductId) {
        this.bundlePartnerProductId = bundlePartnerProductId;
    }

    /**
     * Abstract method enforcing specialized category metadata display.
     */
    public abstract String getCategorySpecificHighlight();

    /**
     * Abstract method detailing technical / variant specifications for quick view modal.
     */
    public abstract String getDetailedSpecificationsHtml();

    /**
     * Returns a human-friendly string for the stock status.
     */
    public String getStockStatusLabel() {
        if (stockQuantity <= 0) {
            return "Out of Stock";
        } else if (stockQuantity <= 3) {
            return "Only " + stockQuantity + " left - High Demand";
        } else {
            return "In Stock (" + stockQuantity + " available)";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s (Stock: %d)", name, id, CurrencyUtil.format(basePrice), stockQuantity);
    }
}
