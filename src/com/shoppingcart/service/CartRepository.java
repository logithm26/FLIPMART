package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import java.util.List;

/**
 * Abstraction layer defining the persistence and retrieval contract for cart items.
 * Demonstrates Abstraction in the shopping cart domain.
 */
public interface CartRepository {
    void addItem(CartItem item);
    void removeItem(String productId);
    void updateQuantity(String productId, int quantity);
    void clear();
    List<CartItem> getItems();
    CartItem findByProductId(String productId);
    int getTotalItemCount();
    void addCartChangeListener(CartChangeListener listener);
    void removeCartChangeListener(CartChangeListener listener);
}
