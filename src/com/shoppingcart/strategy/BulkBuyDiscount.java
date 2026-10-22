package com.shoppingcart.strategy;

import com.shoppingcart.model.CartItem;
import java.util.List;

/**
 * Strategy implementation rewarding volume purchasing across items.
 * Demonstrates Polymorphism.
 */
public class BulkBuyDiscount implements DiscountStrategy {
    private final String code;
    private final int minTotalQuantity;
    private final double discountPercentage;

    public BulkBuyDiscount(String code, int minTotalQuantity, double discountPercentage) {
        this.code = code;
        this.minTotalQuantity = minTotalQuantity;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return String.format("%.0f%% Off when purchasing %d or more total items", discountPercentage * 100, minTotalQuantity);
    }

    @Override
    public boolean isApplicable(double subtotal, List<CartItem> items) {
        if (items == null || items.isEmpty()) return false;
        int totalUnits = items.stream().mapToInt(CartItem::getQuantity).sum();
        return totalUnits >= minTotalQuantity;
    }

    @Override
    public double calculateDiscount(double subtotal, List<CartItem> items) {
        if (!isApplicable(subtotal, items)) {
            return 0.0;
        }
        return subtotal * discountPercentage;
    }
}
