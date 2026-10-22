package com.shoppingcart.service;

import com.shoppingcart.model.Product;

/**
 * Data transfer object encapsulating a dynamic bundle recommendation deal.
 */
public class BundleRecommendation {
    private final Product baseProduct;
    private final Product recommendedProduct;
    private final double discountRate; // e.g. 0.10

    public BundleRecommendation(Product baseProduct, Product recommendedProduct, double discountRate) {
        this.baseProduct = baseProduct;
        this.recommendedProduct = recommendedProduct;
        this.discountRate = discountRate;
    }

    public Product getBaseProduct() {
        return baseProduct;
    }

    public Product getRecommendedProduct() {
        return recommendedProduct;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public double getOriginalPrice() {
        return recommendedProduct.getBasePrice();
    }

    public double getSavingsAmount() {
        return recommendedProduct.getBasePrice() * discountRate;
    }

    public double getDiscountedPrice() {
        return recommendedProduct.getBasePrice() * (1.0 - discountRate);
    }

    public String getHeadline() {
        return "Frequently Bought Together";
    }

    public String getSubtext() {
        return String.format("Pair with %s & Save %.0f%% ($%.2f off)!",
                recommendedProduct.getName(), discountRate * 100, getSavingsAmount());
    }
}
