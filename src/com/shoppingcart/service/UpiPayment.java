package com.shoppingcart.service;

import com.shoppingcart.model.Order;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete PaymentProcessor implementation for Instant Unified Payments Interface (UPI).
 * Demonstrates Abstraction and Inheritance.
 */
public class UpiPayment extends PaymentProcessor {

    public UpiPayment() {
        super("Unified Payments Interface (UPI)", "📱");
    }

    @Override
    public boolean validateCredentials(Map<String, String> credentials, StringBuilder errorCollector) {
        if (credentials == null) {
            errorCollector.append("Missing UPI parameters.");
            return false;
        }

        String upiId = credentials.getOrDefault("upiId", "").trim();
        if (upiId.isEmpty()) {
            errorCollector.append("UPI ID / VPA cannot be empty.\n");
        } else if (!upiId.matches("^[a-zA-Z0-9.\\-_]{2,49}@[a-zA-Z]{2,}$")) {
            errorCollector.append("Invalid UPI ID format. Expected format: username@bank (e.g. buyer@okaxis).\n");
        }

        return errorCollector.isEmpty();
    }

    @Override
    protected PaymentResult executeTransaction(Order order, Map<String, String> credentials) {
        String upiId = credentials.getOrDefault("upiId", "").trim();
        String txId = "UPI-NPCI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String authCode = "RRN-" + (long)(100000000000L + Math.random() * 900000000000L);

        String message = String.format("Instant payment of $%.2f collected successfully from UPI VPA [%s].",
                order.getGrandTotal(), upiId);

        return PaymentResult.success(txId, authCode, message, getProcessorName());
    }
}
