package com.shoppingcart.test;

import com.shoppingcart.model.BookEssentialProduct;
import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Category;
import com.shoppingcart.model.ClothingProduct;
import com.shoppingcart.model.DigitalProduct;
import com.shoppingcart.model.ElectronicsProduct;
import com.shoppingcart.model.HomeKitchenProduct;
import com.shoppingcart.model.Order;
import com.shoppingcart.model.Product;
import com.shoppingcart.model.Shippable;
import com.shoppingcart.model.Taxable;
import com.shoppingcart.model.User;
import com.shoppingcart.service.BundleRecommendation;
import com.shoppingcart.service.CartPricingBreakdown;
import com.shoppingcart.service.CartRepository;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.CreditCardPayment;
import com.shoppingcart.service.CryptoPayment;
import com.shoppingcart.service.InMemoryCartRepository;
import com.shoppingcart.service.InventoryService;
import com.shoppingcart.service.InventoryServiceImpl;
import com.shoppingcart.service.PaymentProcessor;
import com.shoppingcart.service.PaymentResult;
import com.shoppingcart.service.ReceiptService;
import com.shoppingcart.service.RecommendationService;
import com.shoppingcart.service.UpiPayment;
import com.shoppingcart.strategy.BulkBuyDiscount;
import com.shoppingcart.strategy.DiscountStrategy;
import com.shoppingcart.strategy.FlatDiscount;
import com.shoppingcart.strategy.PercentageDiscount;
import com.shoppingcart.util.AssetGenerator;
import com.shoppingcart.util.CurrencyUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Headless test harness verifying the architectural integrity, OOP pillars,
 * multi-category catalog, Indian Rupee formatting, discount strategies, and payments.
 */
public class ArchitectureVerificationTest {

    public static void main(String[] args) {
        System.out.println(">>> RUNNING FLIPMART ARCHITECTURE & VERIFICATION SUITE <<<\n");

        testIndianCurrencyFormatting();
        testAssetGeneration();
        testEncapsulation();
        testInheritance();
        testPolymorphism();
        testAbstraction();
        testDynamicStockReservation();
        testSmartBundling();
        testReceiptGenerationAndExport();

        System.out.println("\n>>> ALL FLIPMART ARCHITECTURAL TESTS PASSED SUCCESSFULLY! (100% OK) <<<");
    }

    private static void testIndianCurrencyFormatting() {
        System.out.print("[TEST 1] Indian Rupee (₹) & Numbering System Standard... ");
        assert CurrencyUtil.format(1499.0).equals("₹1,499") : "₹1,499 formatting mismatch";
        assert CurrencyUtil.format(24999.0).equals("₹24,999") : "₹24,999 formatting mismatch";
        assert CurrencyUtil.format(129999.0).equals("₹1,29,999") : "₹1,29,999 formatting mismatch";
        assert CurrencyUtil.format(10000000.0).equals("₹1,00,00,000") : "₹1 Crore formatting mismatch";
        assert CurrencyUtil.format(1499.50).equals("₹1,499.50") : "Decimal formatting mismatch";
        assert CurrencyUtil.formatWithoutSymbol(24999.0).equals("24,999") : "Without symbol mismatch";
        System.out.println("PASSED (₹1,499, ₹24,999, ₹1,29,999 verified)");
    }

    private static void testAssetGeneration() {
        System.out.print("[TEST 2] Asset Pre-generation Engine (1:1 Square Thumbnails)... ");
        AssetGenerator.generateAllAssetsIfMissing();
        File assetsDir = new File("assets");
        File[] files = assetsDir.listFiles((dir, name) -> name.endsWith(".png"));
        assert files != null && files.length >= 15 : "Expected multi-category generated assets";
        System.out.println("PASSED (" + files.length + " assets verified)");
    }

    private static void testEncapsulation() {
        System.out.print("[TEST 3] OOP Pillar: Encapsulation & Validation... ");
        InventoryService inv = new InventoryServiceImpl();
        Product p = inv.getProductById("ELEC-01");

        assert p.getId().equals("1") || p.getId().equals("ELEC-01") : "ID must be intact";
        int initialStock = p.getStockQuantity();

        // Safe decrement validation
        boolean dec = p.decrementStock(1);
        assert dec && p.getStockQuantity() == initialStock - 1 : "Stock decrement failed";
        p.incrementStock(1);
        assert p.getStockQuantity() == initialStock : "Stock restore failed";

        // Illegal arguments check
        try {
            p.setBasePrice(-50);
            assert false : "Price cannot be negative";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        System.out.println("PASSED (Encapsulated fields & boundary validations enforced)");
    }

    private static void testInheritance() {
        System.out.print("[TEST 4] OOP Pillar: Inheritance Subclasses Across Key Categories... ");
        InventoryService inv = new InventoryServiceImpl();
        Product elec = inv.getProductById("ELEC-01");
        Product cloth = inv.getProductById("CLOTH-01");
        Product home = inv.getProductById("HOME-01");
        Product book = inv.getProductById("BOOK-01");
        Product digi = inv.getProductById("DIGI-01");

        assert elec instanceof ElectronicsProduct : "Must be ElectronicsProduct";
        assert cloth instanceof ClothingProduct : "Must be ClothingProduct";
        assert home instanceof HomeKitchenProduct : "Must be HomeKitchenProduct";
        assert book instanceof BookEssentialProduct : "Must be BookEssentialProduct";
        assert digi instanceof DigitalProduct : "Must be DigitalProduct";

        ElectronicsProduct ep = (ElectronicsProduct) elec;
        assert ep.getWarrantyMonths() == 24 : "Warranty mismatch";

        ClothingProduct cp = (ClothingProduct) cloth;
        assert cp.getSize() != null : "Size missing";

        HomeKitchenProduct hp = (HomeKitchenProduct) home;
        assert hp.getPowerConsumptionWatts() == 1500 : "Power mismatch";

        BookEssentialProduct bp = (BookEssentialProduct) book;
        assert bp.getFormatOrType().contains("Hardcover") : "Format mismatch";

        DigitalProduct dp = (DigitalProduct) digi;
        assert dp.getFileSizeMb() == 520.0 : "File size mismatch";
        System.out.println("PASSED (All 5 specialized product subclasses properly extend Product)");
    }

    private static void testPolymorphism() {
        System.out.print("[TEST 5] OOP Pillar: Polymorphism (Taxable, Shippable, Strategies)... ");
        InventoryService inv = new InventoryServiceImpl();
        Product elec = inv.getProductById("ELEC-01");
        Product cloth = inv.getProductById("CLOTH-01");
        Product home = inv.getProductById("HOME-01");
        Product book = inv.getProductById("BOOK-01");
        Product digi = inv.getProductById("DIGI-01");

        // Taxable polymorphism (Standard Indian GST Brackets)
        assert elec instanceof Taxable : "Electronics must be Taxable";
        assert cloth instanceof Taxable : "Clothing must be Taxable";
        assert home instanceof Taxable : "Home & Kitchen must be Taxable";
        assert book instanceof Taxable : "Books must be Taxable";
        assert digi instanceof Taxable : "Digital must be Taxable";

        Taxable tElec = (Taxable) elec;
        Taxable tCloth = (Taxable) cloth;
        Taxable tHome = (Taxable) home;
        Taxable tBook = (Taxable) book;
        Taxable tDigi = (Taxable) digi;

        assert Math.abs(tElec.getTaxRate() - 0.18) < 0.001 : "Electronics 18% GST";
        assert Math.abs(tCloth.getTaxRate() - 0.05) < 0.001 : "Clothing 5% GST";
        assert Math.abs(tHome.getTaxRate() - 0.12) < 0.001 : "Home & Kitchen 12% GST";
        assert Math.abs(tBook.getTaxRate() - 0.05) < 0.001 : "Books 5% GST";
        assert Math.abs(tDigi.getTaxRate() - 0.10) < 0.001 : "Digital 10% GST";

        // Shippable polymorphism (Digital goods deliberately DO NOT implement Shippable)
        assert elec instanceof Shippable : "Electronics must be Shippable";
        assert cloth instanceof Shippable : "Clothing must be Shippable";
        assert home instanceof Shippable : "Home & Kitchen must be Shippable";
        assert book instanceof Shippable : "Books must be Shippable";
        assert !(digi instanceof Shippable) : "Digital products must NOT implement Shippable";

        // DiscountStrategy Polymorphism
        DiscountStrategy perc = new PercentageDiscount("SAVE15", 0.15, 1000.0);
        DiscountStrategy flat = new FlatDiscount("FLAT500", 500.0, 2500.0);
        DiscountStrategy bulk = new BulkBuyDiscount("BULK3", 3, 0.12);

        List<CartItem> testItems = List.of(new CartItem(elec, 3));
        double subtotal = testItems.get(0).getBaseSubtotal();

        assert Math.abs(perc.calculateDiscount(subtotal, testItems) - (subtotal * 0.15)) < 0.01;
        assert Math.abs(flat.calculateDiscount(subtotal, testItems) - 500.0) < 0.01;
        assert Math.abs(bulk.calculateDiscount(subtotal, testItems) - (subtotal * 0.12)) < 0.01;

        System.out.println("PASSED (Category GST, physical/digital shipping, and strategy discounts verified)");
    }

    private static void testAbstraction() {
        System.out.print("[TEST 6] OOP Pillar: Abstraction (PaymentProcessor, Repositories)... ");
        PaymentProcessor cc = new CreditCardPayment();
        PaymentProcessor upi = new UpiPayment();
        PaymentProcessor crypto = new CryptoPayment();

        User user = new User("U1", "Alex Morgan", "alex@flipmart.in", "Indiranagar, Bengaluru, KA", "VIP");
        Product p = new ElectronicsProduct("T1", "Test Item", "Desc", 10000.0, 12000.0, 10, null, 5.0, 1, null, 12, "Brand", 5, "M1", 0.5);
        Order dummyOrder = new Order("ORD-TEST", user, List.of(new CartItem(p, 1)), 10000.0, 0, 1800.0, 0, 11800.0, "NONE", "Card", "TXN");

        // CC validation & authorization
        Map<String, String> ccCreds = Map.of(
                "cardHolder", "Alex Morgan",
                "cardNumber", "4532890123456789",
                "expiry", "11/27",
                "cvv", "992"
        );
        PaymentResult resCC = cc.processPayment(dummyOrder, ccCreds);
        assert resCC.isSuccessful() : "Credit card must succeed";

        // UPI validation & authorization
        Map<String, String> upiCreds = Map.of("upiId", "alexmorgan@okaxis");
        PaymentResult resUPI = upi.processPayment(dummyOrder, upiCreds);
        assert resUPI.isSuccessful() : "UPI must succeed";

        // Crypto validation & authorization
        Map<String, String> cryptoCreds = Map.of(
                "cryptoNetwork", "Ethereum",
                "walletAddress", "0x71C8F4a59E92d6e491c32c4Fa1E6E562A43B11A9"
        );
        PaymentResult resCrypto = crypto.processPayment(dummyOrder, cryptoCreds);
        assert resCrypto.isSuccessful() : "Crypto must succeed";

        System.out.println("PASSED (All 3 abstract gateway processors validated & processed transactions)");
    }

    private static void testDynamicStockReservation() {
        System.out.print("[TEST 7] Real-time Dynamic Stock Reservation & Live Cart... ");
        InventoryService inv = new InventoryServiceImpl();
        CartRepository repo = new InMemoryCartRepository();
        CartService cart = new CartService(repo, inv);

        Product p = inv.getProductById("CLOTH-06"); // Initial stock: 2
        int origStock = p.getStockQuantity();

        // Add 1
        boolean added1 = cart.addProductToCart(p, 1, false);
        assert added1 && p.getStockQuantity() == origStock - 1 : "Stock must decrement by 1";

        // Add another 1
        boolean added2 = cart.incrementItemQuantity(p.getId());
        assert added2 && p.getStockQuantity() == 0 : "Stock must decrement to 0";
        assert !p.isAvailable() : "Product should be out of stock";

        // Add 3rd (exceeds inventory)
        boolean added3 = cart.incrementItemQuantity(p.getId());
        assert !added3 : "Cannot exceed stock boundary";

        // Remove item from cart
        cart.removeItem(p.getId());
        assert p.getStockQuantity() == origStock : "Stock must be fully restored";
        assert repo.getItems().isEmpty() : "Cart must be empty";

        System.out.println("PASSED (Real-time stock reservation and restoration confirmed)");
    }

    private static void testSmartBundling() {
        System.out.print("[TEST 8] Dynamic Smart Bundling (10% Bundling Discount)... ");
        InventoryService inv = new InventoryServiceImpl();
        CartRepository repo = new InMemoryCartRepository();
        CartService cart = new CartService(repo, inv);
        RecommendationService recService = new RecommendationService(inv);

        Product base = inv.getProductById("ELEC-01"); // Headphones
        BundleRecommendation rec = recService.getRecommendationFor(base);

        assert rec != null : "Recommendation must exist";
        assert Math.abs(rec.getDiscountRate() - 0.10) < 0.001 : "Must have 10% bundle discount";

        // Add base item
        cart.addProductToCart(base, 1, false);

        // Add bundled companion
        cart.addProductToCart(rec.getRecommendedProduct(), 1, true);

        CartPricingBreakdown breakdown = cart.calculateBreakdown();
        assert breakdown.getBundleDiscounts() > 0 : "Bundle discounts must be non-zero";
        System.out.println("PASSED (Recommended " + rec.getRecommendedProduct().getName() + " with 10% discount)");
    }

    private static void testReceiptGenerationAndExport() {
        System.out.print("[TEST 9] Itemized GST Receipt Generation & File Export... ");
        InventoryService inv = new InventoryServiceImpl();
        CartRepository repo = new InMemoryCartRepository();
        CartService cart = new CartService(repo, inv);

        Product p1 = inv.getProductById("ELEC-01");
        Product p2 = inv.getProductById("BOOK-03");

        cart.addProductToCart(p1, 1, false);
        cart.addProductToCart(p2, 1, true); // with bundle discount

        User user = new User("USR-VIP-1", "Priya Sharma", "priya.sharma@tech.in", "104 MG Road, Bengaluru, Karnataka", "VIP Diamond");
        PaymentProcessor cc = new CreditCardPayment();
        Map<String, String> ccCreds = Map.of(
                "cardHolder", "Priya Sharma",
                "cardNumber", "4111222233334444",
                "expiry", "08/29",
                "cvv", "777"
        );

        CartService.CheckoutExecutionResult checkout = cart.processCheckout(user, cc, ccCreds);
        assert checkout.isSuccess() : "Checkout must succeed";
        Order order = checkout.getOrder();

        ReceiptService receiptService = new ReceiptService();
        String receiptText = receiptService.generateTextReceipt(order);

        assert receiptText.contains("FLIPMART INDIA RETAIL") : "Header missing";
        assert receiptText.contains(order.getOrderId()) : "Order ID missing";
        assert receiptText.contains("Priya Sharma") : "Customer name missing";
        assert receiptText.contains("Bundle Deal") : "Bundle line missing";
        assert receiptText.contains("₹") : "Rupee symbol missing";

        File outFile = new File("receipts", "test_receipt_" + order.getOrderId() + ".txt");
        boolean exported = receiptService.exportReceiptToFile(order, outFile);
        assert exported && outFile.exists() && outFile.length() > 100 : "File export failed";

        System.out.println("PASSED (GST invoice generated in INR and exported to " + outFile.getName() + ")");
    }
}
