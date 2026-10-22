package com.shoppingcart.service;

import com.shoppingcart.model.Order;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete PaymentProcessor implementation for Credit and Debit Card processing.
 * Demonstrates Abstraction and Inheritance.
 */
public class CreditCardPayment extends PaymentProcessor {

    public CreditCardPayment() {
        super("Credit / Debit Card", "💳");
    }

    @Override
    public boolean validateCredentials(Map<String, String> credentials, StringBuilder errorCollector) {
        if (credentials == null) {
            errorCollector.append("Missing card credentials.");
            return false;
        }

        String cardNumber = credentials.getOrDefault("cardNumber", "").replaceAll("\\s+", "");
        String cardHolder = credentials.getOrDefault("cardHolder", "").trim();
        String expiry = credentials.getOrDefault("expiry", "").trim();
        String cvv = credentials.getOrDefault("cvv", "").trim();

        if (cardHolder.isEmpty()) {
            errorCollector.append("Cardholder name is required.\n");
        }

        if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
            errorCollector.append("Card number must be 16 digits.\n");
        }

        if (!expiry.matches("^(0[1-9]|1[0-2])/([2-3][0-9])$")) {
            errorCollector.append("Expiry date must be in MM/YY format.\n");
        }

        if (cvv.length() < 3 || cvv.length() > 4 || !cvv.matches("\\d+")) {
            errorCollector.append("CVV must be 3 or 4 digits.\n");
        }

        return errorCollector.isEmpty();
    }

    @Override
    protected PaymentResult executeTransaction(Order order, Map<String, String> credentials) {
        String cardNumber = credentials.getOrDefault("cardNumber", "").replaceAll("\\s+", "");
        String maskedCard = "•••• •••• •••• " + (cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "9821");
        String txId = "TXN-CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String authCode = "AUTH-" + (int)(100000 + Math.random() * 900000);

        String message = String.format("Payment of $%.2f authorized successfully via %s (%s).",
                order.getGrandTotal(), getProcessorName(), maskedCard);

        return PaymentResult.success(txId, authCode, message, getProcessorName());
    }
}
