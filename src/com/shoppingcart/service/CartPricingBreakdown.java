package com.shoppingcart.service;

import com.shoppingcart.strategy.DiscountStrategy;

/**
 * Encapsulation of calculated financial totals for the cart.
 */
public class CartPricingBreakdown {
    private final double subtotal;
    private final double bundleDiscounts;
    private final double couponDiscount;
    private final double totalDiscount;
    private final double taxTotal;
    private final double shippingTotal;
    private final double grandTotal;
    private final DiscountStrategy appliedCoupon;

    public CartPricingBreakdown(double subtotal, double bundleDiscounts, double couponDiscount,
                                double taxTotal, double shippingTotal, DiscountStrategy appliedCoupon) {
        this.subtotal = subtotal;
        this.bundleDiscounts = bundleDiscounts;
        this.couponDiscount = couponDiscount;
        this.totalDiscount = bundleDiscounts + couponDiscount;
        this.taxTotal = taxTotal;
        this.shippingTotal = shippingTotal;
        this.grandTotal = Math.max(0.0, (subtotal - totalDiscount) + taxTotal + shippingTotal);
        this.appliedCoupon = appliedCoupon;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getBundleDiscounts() {
        return bundleDiscounts;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public double getTaxTotal() {
        return taxTotal;
    }

    public double getShippingTotal() {
        return shippingTotal;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public DiscountStrategy getAppliedCoupon() {
        return appliedCoupon;
    }
}
