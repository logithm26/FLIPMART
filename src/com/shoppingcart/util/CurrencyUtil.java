package com.shoppingcart.util;

/**
 * Utility for Indian Rupee (₹) currency formatting using the Indian numbering system:
 * Groups of 2 digits after the primary hundreds group (e.g., ₹1,499, ₹24,999, ₹1,29,999).
 */
public final class CurrencyUtil {
    public static final String RUPEE_SYMBOL = "₹";

    private CurrencyUtil() {}

    /**
     * Formats an amount using Indian numbering system with Rupee symbol (e.g., ₹1,499, ₹24,999, ₹1,29,999).
     * If the amount has a decimal fractional part, it is appended (e.g., ₹1,499.50).
     */
    public static String format(double amount) {
        return format(amount, true, false);
    }

    /**
     * Formats an amount using Indian numbering system with Rupee symbol and guaranteed 2 decimal places
     * (e.g., ₹1,499.00, ₹24,999.00, ₹1,29,999.00). Ideal for receipts, invoices, and ledger tables.
     */
    public static String formatWithDecimals(double amount) {
        return format(amount, true, true);
    }

    /**
     * Formats an amount without the Rupee symbol (e.g., "1,499", "24,999", "1,29,999").
     */
    public static String formatWithoutSymbol(double amount) {
        return format(amount, false, false);
    }

    /**
     * Core formatting logic implementing standard Indian numbering conventions.
     */
    public static String format(double amount, boolean includeSymbol, boolean alwaysDecimals) {
        double absVal = Math.abs(amount);
        long whole = (long) Math.floor(absVal);
        long cents = Math.round((absVal - whole) * 100);
        if (cents >= 100) {
            whole++;
            cents = 0;
        }

        String s = String.valueOf(whole);
        StringBuilder sb = new StringBuilder();

        if (s.length() <= 3) {
            sb.append(s);
        } else {
            // Rightmost 3 digits (hundreds, tens, units)
            String lastThree = s.substring(s.length() - 3);
            String remaining = s.substring(0, s.length() - 3);

            // Group the remaining digits by 2 from right to left (thousands, lakhs, crores)
            StringBuilder remSb = new StringBuilder();
            int count = 0;
            for (int i = remaining.length() - 1; i >= 0; i--) {
                if (count > 0 && count % 2 == 0) {
                    remSb.insert(0, ",");
                }
                remSb.insert(0, remaining.charAt(i));
                count++;
            }
            sb.append(remSb).append(",").append(lastThree);
        }

        if (alwaysDecimals) {
            sb.append(String.format(".%02d", cents));
        } else if (cents > 0) {
            sb.append(String.format(".%02d", cents));
        }

        String prefix = (amount < 0 ? "-" : "");
        return prefix + (includeSymbol ? RUPEE_SYMBOL : "") + sb.toString();
    }
}
