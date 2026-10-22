package com.shoppingcart.strategy;

import com.shoppingcart.model.CartItem;
import java.util.List;

/**
 * Interface defining the strategy contract for polymorphic promotional discount algorithms.
 * Demonstrates Polymorphism and Strategy Pattern.
 */
public interface DiscountStrategy {
    /**
     * Unique promotional coupon code (e.g., "SAVE15").
     */
    String getCode();

    /**
     * User-facing description of the discount rules.
     */
    String getDescription();

    /**
     * Checks if cart state satisfies terms for this discount.
     */
    boolean isApplicable(double subtotal, List<CartItem> items);

    /**
     * Computes the calculated dollar savings.
     */
    double calculateDiscount(double subtotal, List<CartItem> items);
}
