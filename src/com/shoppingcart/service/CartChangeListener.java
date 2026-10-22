package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import java.util.List;

/**
 * Observer listener interface for reactive real-time cart updates.
 */
@FunctionalInterface
public interface CartChangeListener {
    void onCartUpdated(List<CartItem> currentItems, int totalItemCount);
}
