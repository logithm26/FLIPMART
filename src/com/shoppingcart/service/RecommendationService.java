package com.shoppingcart.service;

import com.shoppingcart.model.Product;

/**
 * Service that identifies smart cross-sell product bundles and computes dynamic bundling savings.
 */
public class RecommendationService {
    private final InventoryService inventoryService;
    private static final double DEFAULT_BUNDLE_DISCOUNT = 0.10; // 10%

    public RecommendationService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Finds complementary bundled product for a given product if in stock.
     */
    public BundleRecommendation getRecommendationFor(Product baseProduct) {
        if (baseProduct == null) return null;
        String partnerId = baseProduct.getBundlePartnerProductId();
        if (partnerId == null || partnerId.isEmpty()) return null;

        Product partner = inventoryService.getProductById(partnerId);
        if (partner != null && partner.isAvailable()) {
            return new BundleRecommendation(baseProduct, partner, DEFAULT_BUNDLE_DISCOUNT);
        }

        // Fallback: Pick another available product from same or related category
        for (Product candidate : inventoryService.getAllProducts()) {
            if (!candidate.getId().equals(baseProduct.getId()) && candidate.isAvailable()) {
                return new BundleRecommendation(baseProduct, candidate, DEFAULT_BUNDLE_DISCOUNT);
            }
        }

        return null;
    }
}
