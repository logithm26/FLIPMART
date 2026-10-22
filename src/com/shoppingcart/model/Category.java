package com.shoppingcart.model;

/**
 * Enumeration representing FlipMart's e-commerce product categories.
 */
public enum Category {
    ALL("All Categories", "🌟"),
    ELECTRONICS("Electronics & Gadgets", "⚡"),
    FASHION("Fashion & Apparel", "👕"),
    HOME_KITCHEN("Home & Kitchen", "🍳"),
    BOOKS_ESSENTIALS("Books & Essentials", "📚"),
    DIGITAL("Digital & Software", "💾");

    // Backwards-compatible alias for legacy references
    public static final Category CLOTHING = FASHION;

    private final String displayName;
    private final String iconSymbol;

    Category(String displayName, String iconSymbol) {
        this.displayName = displayName;
        this.iconSymbol = iconSymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    @Override
    public String toString() {
        return iconSymbol + " " + displayName;
    }
}
