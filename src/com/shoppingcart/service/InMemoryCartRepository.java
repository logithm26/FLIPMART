package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory thread-safe implementation of CartRepository with reactive listener triggers.
 */
public class InMemoryCartRepository implements CartRepository {
    private final Map<String, CartItem> itemsMap = new LinkedHashMap<>();
    private final List<CartChangeListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void addItem(CartItem item) {
        if (item == null) return;
        String pId = item.getProduct().getId();
        if (itemsMap.containsKey(pId)) {
            CartItem existing = itemsMap.get(pId);
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            if (item.hasBundleDiscount()) {
                existing.setBundleDiscountRate(item.getBundleDiscountRate());
            }
        } else {
            itemsMap.put(pId, item);
        }
        notifyListeners();
    }

    @Override
    public synchronized void removeItem(String productId) {
        if (productId != null && itemsMap.remove(productId) != null) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void updateQuantity(String productId, int quantity) {
        if (productId == null) return;
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }
        CartItem item = itemsMap.get(productId);
        if (item != null) {
            item.setQuantity(quantity);
            notifyListeners();
        }
    }

    @Override
    public synchronized void clear() {
        itemsMap.clear();
        notifyListeners();
    }

    @Override
    public synchronized List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(itemsMap.values()));
    }

    @Override
    public synchronized CartItem findByProductId(String productId) {
        return itemsMap.get(productId);
    }

    @Override
    public synchronized int getTotalItemCount() {
        return itemsMap.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public void addCartChangeListener(CartChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeCartChangeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<CartItem> current = getItems();
        int total = getTotalItemCount();
        for (CartChangeListener listener : listeners) {
            try {
                listener.onCartUpdated(current, total);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
