package com.shoppingcart.strategy;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.util.CurrencyUtil;
import java.util.List;

/**
 * Strategy implementation applying a fixed Rupee deduction from the subtotal.
 * Demonstrates Polymorphic behavior.
 */
public class FlatDiscount implements DiscountStrategy {
    private final String code;
    private final double flatAmount;
    private final double minimumSpend;

    public FlatDiscount(String code, double flatAmount, double minimumSpend) {
        this.code = code;
        this.flatAmount = flatAmount;
        this.minimumSpend = minimumSpend;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return String.format("%s Flat Off on cart totals above %s",
                CurrencyUtil.format(flatAmount), CurrencyUtil.format(minimumSpend));
    }

    @Override
    public boolean isApplicable(double subtotal, List<CartItem> items) {
        return subtotal >= minimumSpend && items != null && !items.isEmpty();
    }

    @Override
    public double calculateDiscount(double subtotal, List<CartItem> items) {
        if (!isApplicable(subtotal, items)) {
            return 0.0;
        }
        return Math.min(subtotal, flatAmount);
    }
}
