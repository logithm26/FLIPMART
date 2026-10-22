package com.shoppingcart.service;

import com.shoppingcart.model.Category;
import com.shoppingcart.model.Product;
import java.util.List;

/**
 * Abstraction layer defining inventory queries, stock reservations, and event subscriptions.
 * Demonstrates Abstraction in the shopping cart domain.
 */
public interface InventoryService {
    List<Product> getAllProducts();
    Product getProductById(String id);
    List<Product> getProductsByCategory(Category category);
    List<Product> searchProducts(String query, Category category);
    boolean checkAvailability(String productId, int requestedQuantity);
    boolean deductStock(String productId, int quantity);
    void restoreStock(String productId, int quantity);
    int getAvailableStock(String productId);
    void addStockChangeListener(StockChangeListener listener);
    void removeStockChangeListener(StockChangeListener listener);
}
