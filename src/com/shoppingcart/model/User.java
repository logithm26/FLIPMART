package com.shoppingcart.model;

import java.util.Objects;

/**
 * Model representing an active shopping customer.
 * Demonstrates Encapsulation with immutable user ID.
 */
public class User {
    private final String id;
    private String name;
    private String email;
    private String shippingAddress;
    private String loyaltyTier;

    public User(String id, String name, String email, String shippingAddress, String loyaltyTier) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "User name cannot be null.");
        this.email = Objects.requireNonNull(email, "User email cannot be null.");
        this.shippingAddress = shippingAddress != null ? shippingAddress : "123 Tech Boulevard, Silicon Valley, CA 94016";
        this.loyaltyTier = loyaltyTier != null ? loyaltyTier : "VIP Club Member";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email);
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }
}
