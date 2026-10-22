package com.shoppingcart.service;

/**
 * Observer listener interface for reactive visual stock updates across product cards.
 */
@FunctionalInterface
public interface StockChangeListener {
    void onStockChanged(String productId, int newAvailableStock);
}
