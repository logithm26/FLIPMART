package com.shoppingcart.service;

/**
 * Result encapsulation returned by payment processors upon payment execution.
 */
public class PaymentResult {
    private final boolean successful;
    private final String transactionId;
    private final String authorizationCode;
    private final String message;
    private final String paymentMethodName;

    public PaymentResult(boolean successful, String transactionId, String authorizationCode,
                         String message, String paymentMethodName) {
        this.successful = successful;
        this.transactionId = transactionId;
        this.authorizationCode = authorizationCode;
        this.message = message;
        this.paymentMethodName = paymentMethodName;
    }

    public static PaymentResult success(String transactionId, String authCode, String message, String methodName) {
        return new PaymentResult(true, transactionId, authCode, message, methodName);
    }

    public static PaymentResult failure(String message, String methodName) {
        return new PaymentResult(false, null, null, message, methodName);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getMessage() {
        return message;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }
}
