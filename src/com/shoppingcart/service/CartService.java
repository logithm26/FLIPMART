package com.shoppingcart.service;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Order;
import com.shoppingcart.model.Product;
import com.shoppingcart.model.Shippable;
import com.shoppingcart.model.Taxable;
import com.shoppingcart.model.User;
import com.shoppingcart.strategy.BulkBuyDiscount;
import com.shoppingcart.strategy.DiscountStrategy;
import com.shoppingcart.strategy.FlatDiscount;
import com.shoppingcart.strategy.PercentageDiscount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service managing shopping cart operations, polymorphic calculations (taxes, shipping, discounts),
 * and checkout orchestration.
 */
public class CartService {
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
    private final Map<String, DiscountStrategy> availableCoupons = new HashMap<>();
    private DiscountStrategy activeCoupon = null;

    public CartService(CartRepository cartRepository, InventoryService inventoryService) {
        this.cartRepository = cartRepository;
        this.inventoryService = inventoryService;
        initializeAvailableCoupons();
    }

    private void initializeAvailableCoupons() {
        // Standard coupons with realistic Indian Rupee thresholds
        registerCoupon(new PercentageDiscount("SAVE15", 0.15, 1000.0));
        registerCoupon(new FlatDiscount("FLAT500", 500.0, 2500.0));
        registerCoupon(new FlatDiscount("FLAT25", 500.0, 2000.0)); // Legacy alias
        registerCoupon(new BulkBuyDiscount("BULK3", 3, 0.12));
    }

    public void registerCoupon(DiscountStrategy strategy) {
        if (strategy != null) {
            availableCoupons.put(strategy.getCode().toUpperCase(), strategy);
        }
    }

    public List<DiscountStrategy> getAvailableCoupons() {
        return Collections.unmodifiableList(new ArrayList<>(availableCoupons.values()));
    }

    public DiscountStrategy getActiveCoupon() {
        return activeCoupon;
    }

    /**
     * Attempts to apply a promotional coupon code.
     * @return Result message or null if successfully applied.
     */
    public synchronized String applyCouponCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "Please enter a valid promo code.";
        }
        String cleanCode = code.trim().toUpperCase();
        DiscountStrategy strategy = availableCoupons.get(cleanCode);
        if (strategy == null) {
            return "Invalid promo code: \"" + cleanCode + "\". Try SAVE15, FLAT500, or BULK3.";
        }

        List<CartItem> items = cartRepository.getItems();
        double subtotal = items.stream().mapToDouble(CartItem::getBaseSubtotal).sum();
        if (!strategy.isApplicable(subtotal, items)) {
            return "Coupon \"" + cleanCode + "\" requirements not met (" + strategy.getDescription() + ").";
        }

        this.activeCoupon = strategy;
        return null; // Null indicates success
    }

    public synchronized void removeActiveCoupon() {
        this.activeCoupon = null;
    }

    /**
     * Adds a product to the cart with real-time stock reservation.
     */
    public synchronized boolean addProductToCart(Product product, int quantity, boolean isBundleDeal) {
        if (product == null || quantity <= 0) return false;

        // Verify stock in inventory
        if (!inventoryService.checkAvailability(product.getId(), quantity)) {
            return false;
        }

        // Deduct inventory
        boolean deducted = inventoryService.deductStock(product.getId(), quantity);
        if (!deducted) {
            return false;
        }

        double discountRate = isBundleDeal ? 0.10 : 0.0; // 10% smart bundle discount
        CartItem newItem = new CartItem(product, quantity, discountRate);
        cartRepository.addItem(newItem);
        return true;
    }

    /**
     * Increments the quantity of a cart item by 1 if stock allows.
     */
    public synchronized boolean incrementItemQuantity(String productId) {
        CartItem item = cartRepository.findByProductId(productId);
        if (item == null) return false;

        if (!inventoryService.checkAvailability(productId, 1)) {
            return false; // Stock limit reached
        }

        if (inventoryService.deductStock(productId, 1)) {
            cartRepository.updateQuantity(productId, item.getQuantity() + 1);
            return true;
        }
        return false;
    }

    /**
     * Decrements the quantity of a cart item by 1, returning stock to inventory.
     */
    public synchronized void decrementItemQuantity(String productId) {
        CartItem item = cartRepository.findByProductId(productId);
        if (item == null) return;

        inventoryService.restoreStock(productId, 1);
        if (item.getQuantity() > 1) {
            cartRepository.updateQuantity(productId, item.getQuantity() - 1);
        } else {
            cartRepository.removeItem(productId);
        }
    }

    /**
     * Removes an item completely from the cart and restores all held stock.
     */
    public synchronized void removeItem(String productId) {
        CartItem item = cartRepository.findByProductId(productId);
        if (item != null) {
            inventoryService.restoreStock(productId, item.getQuantity());
            cartRepository.removeItem(productId);
        }
    }

    /**
     * Empties the cart and restores all inventory quantities.
     */
    public synchronized void clearCart() {
        List<CartItem> items = cartRepository.getItems();
        for (CartItem item : items) {
            inventoryService.restoreStock(item.getProduct().getId(), item.getQuantity());
        }
        cartRepository.clear();
        this.activeCoupon = null;
    }

    /**
     * Calculates the full financial breakdown polymorphically.
     */
    public synchronized CartPricingBreakdown calculateBreakdown() {
        List<CartItem> items = cartRepository.getItems();
        if (items.isEmpty()) {
            return new CartPricingBreakdown(0.0, 0.0, 0.0, 0.0, 0.0, null);
        }

        double subtotal = 0.0;
        double bundleSavings = 0.0;
        double taxTotal = 0.0;
        double shippingTotal = 0.0;
        boolean hasPhysicalItems = false;

        for (CartItem item : items) {
            Product product = item.getProduct();
            double lineSubtotal = item.getBaseSubtotal();
            double lineBundleSavings = item.getBundleDiscountAmount();
            double lineEffectiveSubtotal = lineSubtotal - lineBundleSavings;

            subtotal += lineSubtotal;
            bundleSavings += lineBundleSavings;

            // --- Polymorphic Tax Calculation ---
            if (product instanceof Taxable taxable) {
                taxTotal += taxable.calculateTax(lineEffectiveSubtotal);
            }

            // --- Polymorphic Shipping Calculation ---
            if (product instanceof Shippable shippable) {
                hasPhysicalItems = true;
                // Item shipping scaled by quantity
                shippingTotal += shippable.calculateShippingCost() * Math.min(item.getQuantity(), 3);
            }
        }

        // Free shipping policy: If physical order exceeds ₹1,499, shipping is free!
        if (!hasPhysicalItems || subtotal >= 1499.0) {
            shippingTotal = 0.0;
        }

        // Validate active coupon validity against current subtotal
        double couponSavings = 0.0;
        DiscountStrategy currentActiveCoupon = this.activeCoupon;
        if (currentActiveCoupon != null) {
            if (currentActiveCoupon.isApplicable(subtotal, items)) {
                couponSavings = currentActiveCoupon.calculateDiscount(subtotal - bundleSavings, items);
            } else {
                // Requirements no longer met due to cart modifications
                this.activeCoupon = null;
                currentActiveCoupon = null;
            }
        }

        return new CartPricingBreakdown(subtotal, bundleSavings, couponSavings, taxTotal, shippingTotal, currentActiveCoupon);
    }

    /**
     * Executes order checkout with the chosen payment processor.
     */
    public synchronized CheckoutExecutionResult processCheckout(User user, PaymentProcessor processor,
                                                                 Map<String, String> paymentCredentials) {
        List<CartItem> items = cartRepository.getItems();
        if (items.isEmpty()) {
            return new CheckoutExecutionResult(false, null, "Cart is empty. Please add items before checkout.");
        }

        CartPricingBreakdown breakdown = calculateBreakdown();
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String couponCode = breakdown.getAppliedCoupon() != null ? breakdown.getAppliedCoupon().getCode() : "NONE";

        Order order = new Order(
                orderId,
                user,
                items,
                breakdown.getSubtotal(),
                breakdown.getTotalDiscount(),
                breakdown.getTaxTotal(),
                breakdown.getShippingTotal(),
                breakdown.getGrandTotal(),
                couponCode,
                processor.getProcessorName(),
                "PENDING"
        );

        PaymentResult paymentResult = processor.processPayment(order, paymentCredentials);
        if (!paymentResult.isSuccessful()) {
            return new CheckoutExecutionResult(false, null, paymentResult.getMessage());
        }

        // Finalize order with confirmed transaction ID
        Order finalizedOrder = new Order(
                orderId,
                user,
                items,
                breakdown.getSubtotal(),
                breakdown.getTotalDiscount(),
                breakdown.getTaxTotal(),
                breakdown.getShippingTotal(),
                breakdown.getGrandTotal(),
                couponCode,
                processor.getProcessorName(),
                paymentResult.getTransactionId()
        );

        // Clear cart items (inventory already reserved and permanently consumed)
        cartRepository.clear();
        this.activeCoupon = null;

        return new CheckoutExecutionResult(true, finalizedOrder, paymentResult.getMessage());
    }

    public CartRepository getCartRepository() {
        return cartRepository;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public static class CheckoutExecutionResult {
        private final boolean success;
        private final Order order;
        private final String message;

        public CheckoutExecutionResult(boolean success, Order order, String message) {
            this.success = success;
            this.order = order;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public Order getOrder() {
            return order;
        }

        public String getMessage() {
            return message;
        }
    }
}
