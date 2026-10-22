package com.shoppingcart.service;

import com.shoppingcart.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service managing user Wishlist (Favorites) with reactive change listeners.
 */
public class WishlistService {
    private static final WishlistService INSTANCE = new WishlistService();

    private final Map<String, Product> wishlistItems = new LinkedHashMap<>();
    private final List<WishlistChangeListener> listeners = new CopyOnWriteArrayList<>();

    public interface WishlistChangeListener {
        void onWishlistChanged(List<Product> items, int count);
    }

    public static WishlistService getInstance() {
        return INSTANCE;
    }

    public synchronized boolean toggleWishlist(Product product) {
        if (product == null) return false;
        boolean added;
        if (wishlistItems.containsKey(product.getId())) {
            wishlistItems.remove(product.getId());
            added = false;
        } else {
            wishlistItems.put(product.getId(), product);
            added = true;
        }
        notifyListeners();
        return added;
    }

    public synchronized void addToWishlist(Product product) {
        if (product != null && !wishlistItems.containsKey(product.getId())) {
            wishlistItems.put(product.getId(), product);
            notifyListeners();
        }
    }

    public synchronized void removeFromWishlist(String productId) {
        if (productId != null && wishlistItems.remove(productId) != null) {
            notifyListeners();
        }
    }

    public synchronized boolean isWishlisted(String productId) {
        return productId != null && wishlistItems.containsKey(productId);
    }

    public synchronized List<Product> getWishlistItems() {
        return Collections.unmodifiableList(new ArrayList<>(wishlistItems.values()));
    }

    public synchronized int getCount() {
        return wishlistItems.size();
    }

    public void addWishlistChangeListener(WishlistChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeWishlistChangeListener(WishlistChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<Product> snapshot = getWishlistItems();
        int count = snapshot.size();
        for (WishlistChangeListener listener : listeners) {
            try {
                listener.onWishlistChanged(snapshot, count);
            } catch (Exception ignored) {}
        }
    }
}
