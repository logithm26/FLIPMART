package com.shoppingcart.web;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Order;
import com.shoppingcart.model.Product;
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
import com.shoppingcart.service.ReceiptService;
import com.shoppingcart.service.RecommendationService;
import com.shoppingcart.service.UpiPayment;
import com.shoppingcart.util.AssetGenerator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Built-in HTTP Web Server exposing the Online Shopping Cart at http://localhost:8080.
 * Serves modern web UI and bridges JSON REST endpoints to the OOP business logic.
 */
public class WebServer {
    private static final int PORT = 8080;
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final RecommendationService recommendationService;
    private final ReceiptService receiptService;

    private final PaymentProcessor ccProcessor = new CreditCardPayment();
    private final PaymentProcessor upiProcessor = new UpiPayment();
    private final PaymentProcessor cryptoProcessor = new CryptoPayment();

    public WebServer() {
        AssetGenerator.generateAllAssetsIfMissing();
        this.inventoryService = new InventoryServiceImpl();
        this.cartRepository = new InMemoryCartRepository();
        this.cartService = new CartService(cartRepository, inventoryService);
        this.recommendationService = new RecommendationService(inventoryService);
        this.receiptService = new ReceiptService();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Static files handler
        server.createContext("/", new StaticFileHandler());
        server.createContext("/assets", new AssetsHandler());

        // REST API routes
        server.createContext("/api/products", new ProductsApiHandler());
        server.createContext("/api/cart", new CartApiHandler());
        server.createContext("/api/cart/add", new AddToCartHandler());
        server.createContext("/api/cart/update", new UpdateCartHandler());
        server.createContext("/api/cart/remove", new RemoveFromCartHandler());
        server.createContext("/api/cart/clear", new ClearCartHandler());
        server.createContext("/api/cart/coupon", new ApplyCouponHandler());
        server.createContext("/api/cart/remove-coupon", new RemoveCouponHandler());
        server.createContext("/api/checkout", new CheckoutApiHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==================================================================");
        System.out.println(" 🌐 FlipMart E-Commerce Web Server successfully running!");
        System.out.println(" 👉 Access via Browser at: http://localhost:" + PORT);
        System.out.println("==================================================================");
    }

    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer();
            webServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- STATIC ASSET HANDLER ---
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web", path.substring(1));
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";
            else if (path.endsWith(".png")) contentType = "image/png";

            sendFile(exchange, file, contentType);
        }
    }

    private class AssetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // e.g. /assets/aurasound_headphones.png
            File file = new File(path.substring(1));
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "text/plain", "Asset not found");
                return;
            }
            sendFile(exchange, file, "image/png");
        }
    }

    // --- REST API: GET PRODUCTS ---
    private class ProductsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
                return;
            }

            List<Product> products = inventoryService.getAllProducts();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(quote(p.getId())).append(",");
                sb.append("\"name\":").append(quote(p.getName())).append(",");
                sb.append("\"description\":").append(quote(p.getDescription())).append(",");
                sb.append("\"basePrice\":").append(p.getBasePrice()).append(",");
                sb.append("\"originalMrp\":").append(p.getOriginalMrp()).append(",");
                sb.append("\"discountPercent\":").append(p.getDiscountPercent()).append(",");
                sb.append("\"formattedPrice\":").append(quote(p.getFormattedPrice())).append(",");
                sb.append("\"formattedMrp\":").append(quote(p.getFormattedMrp())).append(",");
                sb.append("\"stockQuantity\":").append(p.getStockQuantity()).append(",");
                sb.append("\"category\":").append(quote(p.getCategory().name())).append(",");
                sb.append("\"categoryDisplay\":").append(quote(p.getCategory().getDisplayName())).append(",");
                sb.append("\"categoryIcon\":").append(quote(p.getCategory().getIconSymbol())).append(",");
                sb.append("\"imagePath\":").append(quote(p.getImagePath())).append(",");
                sb.append("\"rating\":").append(p.getRating()).append(",");
                sb.append("\"reviewCount\":").append(p.getReviewCount()).append(",");
                sb.append("\"highlight\":").append(quote(p.getCategorySpecificHighlight())).append(",");
                sb.append("\"specsHtml\":").append(quote(p.getDetailedSpecificationsHtml())).append(",");
                sb.append("\"bundlePartnerId\":").append(quote(p.getBundlePartnerProductId() != null ? p.getBundlePartnerProductId() : "")).append(",");
                sb.append("\"isAvailable\":").append(p.isAvailable());
                sb.append("}");
            }
            sb.append("]");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // --- REST API: GET CART ---
    private class CartApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<CartItem> items = cartRepository.getItems();
            CartPricingBreakdown b = cartService.calculateBreakdown();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalItems\":").append(cartRepository.getTotalItemCount()).append(",");
            sb.append("\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                CartItem it = items.get(i);
                Product p = it.getProduct();
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"productId\":").append(quote(p.getId())).append(",");
                sb.append("\"name\":").append(quote(p.getName())).append(",");
                sb.append("\"category\":").append(quote(p.getCategory().name())).append(",");
                sb.append("\"imagePath\":").append(quote(p.getImagePath())).append(",");
                sb.append("\"basePrice\":").append(p.getBasePrice()).append(",");
                sb.append("\"quantity\":").append(it.getQuantity()).append(",");
                sb.append("\"bundleDiscountRate\":").append(it.getBundleDiscountRate()).append(",");
                sb.append("\"hasBundleDiscount\":").append(it.hasBundleDiscount()).append(",");
                sb.append("\"lineSubtotal\":").append(it.getBaseSubtotal()).append(",");
                sb.append("\"bundleDiscountAmount\":").append(it.getBundleDiscountAmount()).append(",");
                sb.append("\"lineTotal\":").append(it.getLineTotal());
                sb.append("}");
            }
            sb.append("],");
            sb.append("\"breakdown\":{");
            sb.append("\"subtotal\":").append(b.getSubtotal()).append(",");
            sb.append("\"bundleDiscounts\":").append(b.getBundleDiscounts()).append(",");
            sb.append("\"couponDiscount\":").append(b.getCouponDiscount()).append(",");
            sb.append("\"totalDiscount\":").append(b.getTotalDiscount()).append(",");
            sb.append("\"taxTotal\":").append(b.getTaxTotal()).append(",");
            sb.append("\"shippingTotal\":").append(b.getShippingTotal()).append(",");
            sb.append("\"grandTotal\":").append(b.getGrandTotal()).append(",");
            sb.append("\"appliedCoupon\":").append(b.getAppliedCoupon() != null ? quote(b.getAppliedCoupon().getCode()) : "null");
            sb.append("}}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // --- REST API: ADD TO CART ---
    private class AddToCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"POST required\"}");
                return;
            }
            String body = readBody(exchange);
            String productId = extractJsonString(body, "productId");
            int qty = extractJsonInt(body, "quantity", 1);
            boolean isBundle = body.contains("\"isBundle\":true");

            Product p = inventoryService.getProductById(productId);
            if (p == null) {
                sendResponse(exchange, 404, "application/json", "{\"success\":false,\"message\":\"Product not found\"}");
                return;
            }

            boolean added = cartService.addProductToCart(p, qty, isBundle);
            if (!added) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"message\":\"Insufficient stock available\"}");
                return;
            }

            // Check for Smart Bundle recommendation if not already adding a bundle item
            BundleRecommendation rec = null;
            if (!isBundle) {
                rec = recommendationService.getRecommendationFor(p);
            }

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            if (rec != null) {
                Product r = rec.getRecommendedProduct();
                sb.append("\"bundleRecommendation\":{");
                sb.append("\"baseName\":").append(quote(rec.getBaseProduct().getName())).append(",");
                sb.append("\"recommendedId\":").append(quote(r.getId())).append(",");
                sb.append("\"recommendedName\":").append(quote(r.getName())).append(",");
                sb.append("\"recommendedCategory\":").append(quote(r.getCategory().name())).append(",");
                sb.append("\"recommendedImage\":").append(quote(r.getImagePath())).append(",");
                sb.append("\"originalPrice\":").append(rec.getOriginalPrice()).append(",");
                sb.append("\"discountedPrice\":").append(rec.getDiscountedPrice()).append(",");
                sb.append("\"savings\":").append(rec.getSavingsAmount());
                sb.append("}");
            } else {
                sb.append("\"bundleRecommendation\":null");
            }
            sb.append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // --- REST API: UPDATE QUANTITY ---
    private class UpdateCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            String productId = extractJsonString(body, "productId");
            String action = extractJsonString(body, "action"); // "increment" or "decrement"

            boolean success = false;
            if ("increment".equalsIgnoreCase(action)) {
                success = cartService.incrementItemQuantity(productId);
            } else {
                cartService.decrementItemQuantity(productId);
                success = true;
            }

            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    // --- REST API: REMOVE ITEM ---
    private class RemoveFromCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            String productId = extractJsonString(body, "productId");
            cartService.removeItem(productId);
            sendResponse(exchange, 200, "application/json", "{\"success\":true}");
        }
    }

    // --- REST API: CLEAR CART ---
    private class ClearCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            cartService.clearCart();
            sendResponse(exchange, 200, "application/json", "{\"success\":true}");
        }
    }

    // --- REST API: APPLY PROMO CODE ---
    private class ApplyCouponHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            String code = extractJsonString(body, "code");
            String err = cartService.applyCouponCode(code);

            if (err != null) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"message\":" + quote(err) + "}");
            } else {
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"message\":\"Coupon applied successfully!\"}");
            }
        }
    }

    // --- REST API: REMOVE ACTIVE PROMO ---
    private class RemoveCouponHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            cartService.removeActiveCoupon();
            sendResponse(exchange, 200, "application/json", "{\"success\":true}");
        }
    }

    // --- REST API: CHECKOUT ---
    private class CheckoutApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"POST required\"}");
                return;
            }

            String body = readBody(exchange);
            String name = extractJsonString(body, "name");
            String email = extractJsonString(body, "email");
            String address = extractJsonString(body, "address");
            String method = extractJsonString(body, "method"); // CC, UPI, CRYPTO

            PaymentProcessor processor = ccProcessor;
            Map<String, String> creds = new HashMap<>();

            if ("UPI".equalsIgnoreCase(method)) {
                processor = upiProcessor;
                creds.put("upiId", extractJsonString(body, "upiId"));
            } else if ("CRYPTO".equalsIgnoreCase(method)) {
                processor = cryptoProcessor;
                creds.put("cryptoNetwork", extractJsonString(body, "cryptoNetwork"));
                creds.put("walletAddress", extractJsonString(body, "walletAddress"));
            } else {
                processor = ccProcessor;
                creds.put("cardHolder", extractJsonString(body, "cardHolder"));
                creds.put("cardNumber", extractJsonString(body, "cardNumber"));
                creds.put("expiry", extractJsonString(body, "expiry"));
                creds.put("cvv", extractJsonString(body, "cvv"));
            }

            User user = new User("WEB-USR-100", name.isEmpty() ? "Web Customer" : name,
                    email.isEmpty() ? "customer@store.com" : email,
                    address.isEmpty() ? "123 Web Blvd, Cloud City" : address,
                    "VIP Prime Member");

            CartService.CheckoutExecutionResult result = cartService.processCheckout(user, processor, creds);
            if (!result.isSuccess()) {
                sendResponse(exchange, 400, "application/json",
                        "{\"success\":false,\"message\":" + quote(result.getMessage()) + "}");
                return;
            }

            Order order = result.getOrder();
            String receiptText = receiptService.generateTextReceipt(order);

            // Save to receipts folder
            File backup = new File("receipts", "receipt_" + order.getOrderId() + ".txt");
            receiptService.exportReceiptToFile(order, backup);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"message\":").append(quote(result.getMessage())).append(",");
            sb.append("\"orderId\":").append(quote(order.getOrderId())).append(",");
            sb.append("\"grandTotal\":").append(order.getGrandTotal()).append(",");
            sb.append("\"paymentMethod\":").append(quote(order.getPaymentMethod())).append(",");
            sb.append("\"transactionId\":").append(quote(order.getTransactionId())).append(",");
            sb.append("\"receiptText\":").append(quote(receiptText));
            sb.append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // --- UTILITIES ---
    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendFile(HttpExchange exchange, File file, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, file.length());
        try (InputStream is = new FileInputStream(file); OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) >= 0) {
                os.write(buf, 0, n);
            }
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String quote(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        sb.append("\"");
        return sb.toString();
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            pattern = "\"" + key + "\": \"";
            idx = json.indexOf(pattern);
        }
        if (idx == -1) return "";
        int start = idx + pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private static int extractJsonInt(String json, String key, int def) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return def;
        int start = idx + pattern.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start == end) return def;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return def;
        }
    }
}
