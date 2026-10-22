package com.shoppingcart.model;

/**
 * Interface demonstrating Polymorphism for items subject to regional/category taxation.
 */
public interface Taxable {
    /**
     * @return The tax rate percentage (e.g. 0.18 for 18%).
     */
    double getTaxRate();

    /**
     * Calculates tax amount for a given base amount using the item's tax rate.
     * @param amount Base price or subtotal.
     * @return Calculated tax value.
     */
    default double calculateTax(double amount) {
        return amount * getTaxRate();
    }
}
