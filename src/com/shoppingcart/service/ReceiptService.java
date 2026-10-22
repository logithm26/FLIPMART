package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Order;
import com.shoppingcart.util.CurrencyUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service for generating commercial retail GST tax invoices and receipts in Indian Rupees (₹).
 */
public class ReceiptService {

    public String generateTextReceipt(Order order) {
        if (order == null) return "No order available.";

        StringBuilder sb = new StringBuilder();
        String line = "========================================================================\n";
        String dashed = "------------------------------------------------------------------------\n";

        sb.append(line);
        sb.append("                      FLIPMART INDIA RETAIL PVT LTD                     \n");
        sb.append("                   OFFICIAL GST TAX INVOICE & RECEIPT                   \n");
        sb.append("========================================================================\n");
        sb.append(" GSTIN        : 29AABCF1234F1Z9 (Karnataka Hub)\n");
        sb.append(String.format(" Order ID     : %s\n", order.getOrderId()));
        sb.append(String.format(" Order Date   : %s\n", order.getFormattedDate()));
        sb.append(String.format(" Order Status : %s\n", order.getStatus()));
        sb.append(dashed);
        sb.append(" CUSTOMER DETAILS:\n");
        sb.append(String.format(" Name         : %s (%s)\n", order.getUser().getName(), order.getUser().getLoyaltyTier()));
        sb.append(String.format(" Email        : %s\n", order.getUser().getEmail()));
        sb.append(String.format(" Shipping To  : %s\n", order.getUser().getShippingAddress()));
        sb.append(dashed);
        sb.append(String.format(" %-10s %-28s %-5s %-12s %-12s\n", "SKU", "ITEM DESCRIPTION", "QTY", "UNIT PRICE", "TOTAL"));
        sb.append(dashed);

        for (CartItem item : order.getItems()) {
            String title = item.getProduct().getName();
            if (title.length() > 27) {
                title = title.substring(0, 24) + "...";
            }
            sb.append(String.format(" %-10s %-28s %-5d %-12s %-12s\n",
                    item.getProduct().getId(),
                    title,
                    item.getQuantity(),
                    CurrencyUtil.format(item.getProduct().getBasePrice()),
                    CurrencyUtil.format(item.getLineTotal())
            ));

            if (item.hasBundleDiscount()) {
                sb.append(String.format("            ↳ Bundle Deal (%.0f%% Off - Saved %s)\n",
                        item.getBundleDiscountRate() * 100,
                        CurrencyUtil.format(item.getBundleDiscountAmount())));
            }
        }

        sb.append(dashed);
        sb.append(String.format(" %-48s : %s\n", "Subtotal", CurrencyUtil.format(order.getSubtotal())));

        if (order.getDiscountTotal() > 0) {
            String couponNote = order.getAppliedCouponCode().equals("NONE") ? "Bundle & Volume Deals" : order.getAppliedCouponCode();
            sb.append(String.format(" %-48s : -%s (%s)\n", "Discounts & Offers Applied",
                    CurrencyUtil.format(order.getDiscountTotal()), couponNote));
        }

        sb.append(String.format(" %-48s : %s\n", "Estimated GST (Itemized Category Tax)",
                CurrencyUtil.format(order.getTaxTotal())));
        sb.append(String.format(" %-48s : %s\n", "Fulfillment & Express Delivery",
                order.getShippingTotal() == 0 ? "FREE" : CurrencyUtil.format(order.getShippingTotal())));
        sb.append(line);
        sb.append(String.format(" %-48s : %s INR\n", "TOTAL AMOUNT PAID",
                CurrencyUtil.format(order.getGrandTotal())));
        sb.append(line);
        sb.append(" PAYMENT VERIFICATION:\n");
        sb.append(String.format(" Payment Gateway  : %s\n", order.getPaymentMethod()));
        sb.append(String.format(" Transaction Ref  : %s\n", order.getTransactionId()));
        sb.append(" Settlement Status: AUTHORIZED & CLEARED VIA SECURE GATEWAY\n");
        sb.append(line);
        sb.append("     Thank you for shopping with FlipMart! 100% Genuine Products.       \n");
        sb.append("        Support: support@flipmart.in | Helpline: 1800-208-9898          \n");
        sb.append(line);

        return sb.toString();
    }

    public boolean exportReceiptToFile(Order order, File destinationFile) {
        String receiptText = generateTextReceipt(order);
        try {
            if (destinationFile.getParentFile() != null) {
                destinationFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(destinationFile, StandardCharsets.UTF_8)) {
                writer.write(receiptText);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
