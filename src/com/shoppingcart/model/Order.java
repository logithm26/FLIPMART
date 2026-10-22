package com.shoppingcart.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a finalized checkout order.
 * Demonstrates Encapsulation and immutable collections protection.
 */
public class Order {
    private final String orderId;
    private final User user;
    private final List<CartItem> items;
    private final double subtotal;
    private final double discountTotal;
    private final double taxTotal;
    private final double shippingTotal;
    private final double grandTotal;
    private final String appliedCouponCode;
    private final String paymentMethod;
    private final String transactionId;
    private final LocalDateTime orderTimestamp;
    private String status;

    public Order(String orderId, User user, List<CartItem> items, double subtotal,
                 double discountTotal, double taxTotal, double shippingTotal,
                 double grandTotal, String appliedCouponCode, String paymentMethod,
                 String transactionId) {
        this.orderId = Objects.requireNonNull(orderId);
        this.user = Objects.requireNonNull(user);
        this.items = new ArrayList<>(items); // Defensive copy
        this.subtotal = subtotal;
        this.discountTotal = discountTotal;
        this.taxTotal = taxTotal;
        this.shippingTotal = shippingTotal;
        this.grandTotal = grandTotal;
        this.appliedCouponCode = appliedCouponCode;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.orderTimestamp = LocalDateTime.now();
        this.status = "CONFIRMED & PROCESSING";
    }

    public String getOrderId() {
        return orderId;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items); // Enforce immutability
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountTotal() {
        return discountTotal;
    }

    public double getTaxTotal() {
        return taxTotal;
    }

    public double getShippingTotal() {
        return shippingTotal;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getOrderTimestamp() {
        return orderTimestamp;
    }

    public String getFormattedDate() {
        return orderTimestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
