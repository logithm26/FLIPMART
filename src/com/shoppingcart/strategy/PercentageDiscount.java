package com.shoppingcart.strategy;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.util.CurrencyUtil;
import java.util.List;

/**
 * Strategy implementation applying a proportional percentage markdown across the subtotal.
 * Demonstrates Polymorphic behavior.
 */
public class PercentageDiscount implements DiscountStrategy {
    private final String code;
    private final double percentage; // e.g., 0.15 for 15%
    private final double minSubtotal;

    public PercentageDiscount(String code, double percentage, double minSubtotal) {
        this.code = code;
        this.percentage = percentage;
        this.minSubtotal = minSubtotal;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return String.format("%.0f%% Off orders above %s", percentage * 100, CurrencyUtil.format(minSubtotal));
    }

    @Override
    public boolean isApplicable(double subtotal, List<CartItem> items) {
        return subtotal >= minSubtotal && items != null && !items.isEmpty();
    }

    @Override
    public double calculateDiscount(double subtotal, List<CartItem> items) {
        if (!isApplicable(subtotal, items)) {
            return 0.0;
        }
        return subtotal * percentage;
    }
}
