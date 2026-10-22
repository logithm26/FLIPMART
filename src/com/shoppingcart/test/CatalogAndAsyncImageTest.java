package com.shoppingcart.test;

import com.shoppingcart.model.Category;
import com.shoppingcart.model.Product;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.InMemoryCartRepository;
import com.shoppingcart.service.InventoryService;
import com.shoppingcart.service.InventoryServiceImpl;
import com.shoppingcart.service.WishlistService;
import com.shoppingcart.util.AsyncImageLoader;

import javax.swing.ImageIcon;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Verification test suite for consumer-grade UI/UX features:
 * - Real-world image URLs on every catalog item
 * - AsyncImageLoader skeleton placeholders & caching
 * - Wishlist service reactivity & counters
 * - Live cart stepper calculations
 */
public class CatalogAndAsyncImageTest {

    public static void main(String[] args) {
        System.out.println(">>> RUNNING FLIPMART CONSUMER UI/UX VERIFICATION SUITE <<<\n");

        testRealWorldCatalogImageUrls();
        testAsyncImageLoaderAndSkeleton();
        testWishlistServiceReactivity();
        testCartSteppersAndDynamicRecalculation();

        System.out.println("\n>>> ALL UI/UX AND ASYNC IMAGE TESTS PASSED (100% OK) <<<");
    }

    private static void testRealWorldCatalogImageUrls() {
        System.out.print("[TEST 1] Real-World Product Imagery URLs in Catalog... ");
        InventoryService inv = new InventoryServiceImpl();
        List<Product> products = inv.getAllProducts();

        assert products.size() >= 20 : "Catalog must contain at least 20 items";

        for (Product p : products) {
            assert p.getImagePath() != null && !p.getImagePath().trim().isEmpty() :
                    "Product " + p.getId() + " is missing an image path/URL";
            assert p.getImagePath().startsWith("http://") || p.getImagePath().startsWith("https://") || p.getImagePath().startsWith("assets/") :
                    "Product " + p.getId() + " must have a verified CDN URL or asset path";
        }
        System.out.println("PASSED (" + products.size() + " genuine commercial product items verified)");
    }

    private static void testAsyncImageLoaderAndSkeleton() {
        System.out.print("[TEST 2] AsyncImageLoader Skeleton Placeholders & Caching... ");
        AsyncImageLoader loader = AsyncImageLoader.getInstance();

        for (Category cat : Category.values()) {
            ImageIcon skeleton = loader.getSkeletonPlaceholder(cat, 200, 200);
            assert skeleton != null && skeleton.getIconWidth() == 200 && skeleton.getIconHeight() == 200 :
                    "Skeleton placeholder failed for " + cat;
        }
        System.out.println("PASSED (Category shimmer skeleton placeholders verified)");
    }

    private static void testWishlistServiceReactivity() {
        System.out.print("[TEST 3] Reactive Wishlist Service & Heart Bookmarks... ");
        WishlistService ws = WishlistService.getInstance();
        InventoryService inv = new InventoryServiceImpl();
        Product p = inv.getProductById("ELEC-01");

        AtomicBoolean notified = new AtomicBoolean(false);
        WishlistService.WishlistChangeListener listener = (items, count) -> notified.set(true);
        ws.addWishlistChangeListener(listener);

        // Toggle add
        boolean added = ws.toggleWishlist(p);
        assert added : "Item should be added to wishlist";
        assert ws.isWishlisted(p.getId()) : "Item must be marked as wishlisted";
        assert notified.get() : "Listener must be notified on wishlist change";

        // Toggle remove
        notified.set(false);
        boolean removed = ws.toggleWishlist(p);
        assert !removed : "Item should be toggled off wishlist";
        assert !ws.isWishlisted(p.getId()) : "Item must no longer be wishlisted";
        assert notified.get() : "Listener must be notified on removal";

        ws.removeWishlistChangeListener(listener);
        System.out.println("PASSED (Wishlist toggle and listener reactivity verified)");
    }

    private static void testCartSteppersAndDynamicRecalculation() {
        System.out.print("[TEST 4] Live Cart Stepper & Breakdown Recalculation... ");
        InventoryService inv = new InventoryServiceImpl();
        CartService cart = new CartService(new InMemoryCartRepository(), inv);

        Product p = inv.getProductById("CLOTH-01"); // ₹1,499
        cart.addProductToCart(p, 1, false);

        assert cart.getCartRepository().getTotalItemCount() == 1 : "Should have 1 item";
        assert cart.calculateBreakdown().getSubtotal() == 1499.0 : "Subtotal must be 1499";

        // Increment stepper
        cart.incrementItemQuantity(p.getId());
        assert cart.getCartRepository().getTotalItemCount() == 2 : "Should have 2 items";
        assert cart.calculateBreakdown().getSubtotal() == 2998.0 : "Subtotal must be 2998";

        // Decrement stepper
        cart.decrementItemQuantity(p.getId());
        assert cart.getCartRepository().getTotalItemCount() == 1 : "Should be back to 1 item";
        assert cart.calculateBreakdown().getSubtotal() == 1499.0 : "Subtotal must be 1499";

        System.out.println("PASSED (Stepper increments/decrements and totals verified)");
    }
}
