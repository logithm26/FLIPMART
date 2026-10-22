package com.shoppingcart.service;

import com.shoppingcart.model.Order;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete PaymentProcessor implementation for Web3 / Decentralized Cryptocurrency settlement.
 * Demonstrates Abstraction and Inheritance.
 */
public class CryptoPayment extends PaymentProcessor {

    public CryptoPayment() {
        super("Cryptocurrency (BTC / ETH / SOL)", "₿");
    }

    @Override
    public boolean validateCredentials(Map<String, String> credentials, StringBuilder errorCollector) {
        if (credentials == null) {
            errorCollector.append("Missing crypto wallet context.");
            return false;
        }

        String network = credentials.getOrDefault("cryptoNetwork", "").trim();
        String wallet = credentials.getOrDefault("walletAddress", "").trim();

        if (network.isEmpty()) {
            errorCollector.append("Please select a blockchain network (e.g. Ethereum, Bitcoin, Solana).\n");
        }

        if (wallet.isEmpty()) {
            errorCollector.append("Payer wallet address is required for on-chain verification.\n");
        } else if (wallet.length() < 12) {
            errorCollector.append("Wallet address appears malformed or too short.\n");
        }

        return errorCollector.isEmpty();
    }

    @Override
    protected PaymentResult executeTransaction(Order order, Map<String, String> credentials) {
        String network = credentials.getOrDefault("cryptoNetwork", "ETH");
        String hash = "0x" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().substring(0, 16);
        String blockHeight = "#" + (19284000 + (int)(Math.random() * 5000));

        String message = String.format("On-chain transfer for $%.2f confirmed on %s Network in Block %s.",
                order.getGrandTotal(), network, blockHeight);

        return PaymentResult.success(hash, blockHeight, message, getProcessorName());
    }
}
