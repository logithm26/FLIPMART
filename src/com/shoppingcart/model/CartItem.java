package com.shoppingcart.model;

import java.util.Objects;

/**
 * Represents an individual line item inside the shopping cart.
 * Enforces Encapsulation and business logic calculation for line discounts and totals.
 */
public class CartItem {
    private final Product product;
    private int quantity;
    private double bundleDiscountRate; // e.g., 0.10 for 10% bundle discount

    public CartItem(Product product, int quantity) {
        this(product, quantity, 0.0);
    }

    public CartItem(Product product, int quantity, double bundleDiscountRate) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
        this.bundleDiscountRate = Math.max(0.0, Math.min(1.0, bundleDiscountRate));
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public double getBundleDiscountRate() {
        return bundleDiscountRate;
    }

    public void setBundleDiscountRate(double bundleDiscountRate) {
        this.bundleDiscountRate = Math.max(0.0, Math.min(1.0, bundleDiscountRate));
    }

    public boolean hasBundleDiscount() {
        return bundleDiscountRate > 0.001;
    }

    /**
     * Calculates base subtotal before any discounts.
     */
    public double getBaseSubtotal() {
        return product.getBasePrice() * quantity;
    }

    /**
     * Calculates discount dollar amount specifically coming from bundle deals.
     */
    public double getBundleDiscountAmount() {
        return getBaseSubtotal() * bundleDiscountRate;
    }

    /**
     * Calculates final line total after applying item-level discounts.
     */
    public double getLineTotal() {
        return getBaseSubtotal() - getBundleDiscountAmount();
    }

    /**
     * Calculates effective unit price taking bundle discount into account.
     */
    public double getEffectiveUnitPrice() {
        return product.getBasePrice() * (1.0 - bundleDiscountRate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem cartItem)) return false;
        return Objects.equals(product.getId(), cartItem.product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(product.getId());
    }
}
