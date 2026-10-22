package com.shoppingcart.service;

import com.shoppingcart.model.Order;
import java.util.Map;

/**
 * Abstract Base Class defining the contract and workflow for payment gateway processors.
 * Demonstrates Abstraction and Template Method design pattern.
 */
public abstract class PaymentProcessor {
    private final String processorName;
    private final String iconSymbol;

    protected PaymentProcessor(String processorName, String iconSymbol) {
        this.processorName = processorName;
        this.iconSymbol = iconSymbol;
    }

    public String getProcessorName() {
        return processorName;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    /**
     * Abstract method enforcing validation of gateway-specific credentials.
     */
    public abstract boolean validateCredentials(Map<String, String> credentials, StringBuilder errorCollector);

    /**
     * Abstract method executing the payment transaction with the underlying network.
     */
    protected abstract PaymentResult executeTransaction(Order order, Map<String, String> credentials);

    /**
     * Template method coordinating validation and transaction execution.
     */
    public final PaymentResult processPayment(Order order, Map<String, String> credentials) {
        if (order == null || order.getGrandTotal() <= 0) {
            return PaymentResult.failure("Invalid order amount for payment processing.", processorName);
        }

        StringBuilder validationErrors = new StringBuilder();
        if (!validateCredentials(credentials, validationErrors)) {
            return PaymentResult.failure(validationErrors.toString(), processorName);
        }

        return executeTransaction(order, credentials);
    }
}
