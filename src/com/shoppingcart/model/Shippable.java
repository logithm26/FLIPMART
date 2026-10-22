package com.shoppingcart.model;

/**
 * Interface demonstrating Polymorphism for physical products requiring fulfillment shipping.
 */
public interface Shippable {
    /**
     * @return Physical weight of product in kilograms.
     */
    double getWeightKg();

    /**
     * @return Dimensions descriptor (e.g. "30x20x10 cm").
     */
    String getShippingDimensions();

    /**
     * Calculates shipping fee based on weight and standard base rates in INR (₹).
     * @return Shipping cost in Rupees.
     */
    default double calculateShippingCost() {
        double weight = getWeightKg();
        if (weight <= 0.5) {
            return 49.0;
        } else if (weight <= 2.0) {
            return 99.0;
        } else {
            return 99.0 + (weight - 2.0) * 40.0;
        }
    }
}
