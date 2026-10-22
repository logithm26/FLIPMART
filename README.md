# FlipMart - Modern Consumer-Grade Java Desktop E-Commerce Experience

A flagship, consumer-grade desktop e-commerce application built in **Java (JDK 17+/26)** inspired by the industry-leading UI/UX design patterns of **Flipkart**, **Amazon**, and **Meesho**. Features a crisp Flipkart Royal Blue sticky navigation bar, pill search with real-time keystroke filtering, quick-scroll category chips, clean white product cards with green rating chips, genuine high-resolution CDN photography loaded asynchronously via `SwingWorker`, a live slide-out cart drawer with stepper buttons, and an intuitive 3-step checkout and payment simulation.

---

## 🌟 Key Highlights & Architectural Overhaul

### 1. Visual Design & Layout Overhaul (Flipkart / Amazon / Meesho Formula)
- **Sticky Navigation Bar (Tier 1 & Tier 2):**
  - **FlipMart Branding:** Bold white brand typography accented with Flipkart Gold (`Explore Plus ✦`).
  - **Universal Centered Search Pill:** 24px rounded pill search field with magnifying glass icon `🔍`, real-time dynamic filtering as the user types, and 1-click `✕` clear button.
  - **Quick-Scroll Category Chips Bar:** High-contrast filter pills (`🌟 All Categories`, `⚡ Electronics`, `👕 Fashion`, `🍳 Home & Kitchen`, `📚 Books & Essentials`, `💾 Digital & Software`, and `🔥 Daily Deals (35%+ OFF)`).
  - **Compact Action Icons:**
    - `♡ Wishlist (X)`: Reactive heart button with dynamic counter reflecting user bookmarks.
    - `🛒 Cart (X) • ₹XX,XXX`: Vibrant amber pill displaying live cart item count and real-time total payable amount.
    - `👤 Alex ▾`: VIP Diamond user account indicator.
- **Product Card Architecture (The Meesho/Flipkart Card Formula):**
  - Pure white card container (`#FFFFFF`), 8px border radius, subtle `#E0E0E0` border, soft drop-shadow, and hover lift elevation.
  - **Centered 1:1 Aspect Ratio Photography:** High-resolution commercial product shots streamed asynchronously.
  - **Floating Wishlist Heart (`♡` / `♥`):** Top-right floating circular button that toggles favorite status with reactive badge updates.
  - **Rating & Reviews Pill:** Flipkart-style green rating badge (`★ 4.8`) paired with review counts `(3.4k)` and `⚡ Assured` badge.
  - **Pricing Hierarchy:**
    - Bold, prominent Selling Price (e.g. `₹799`, `₹24,999`).
    - Strikethrough MRP (e.g. `₹1,999`, `₹29,999`).
    - High-contrast Discount Tag in green (e.g. `60% OFF`).
  - **Delivery Tag:** `✓ Free Express Delivery` guarantee.
  - **Action Button with Click Feedback:** "Add to Cart 🛒" button with animated state change (`✓ Added!`) and "Buy Now ⚡".

---

### 2. High-Quality Real-World Product Imagery & SwingWorker Async Pipeline
- **Verified Direct CDN Image URLs:** Every item in the catalog is configured with direct, high-resolution commercial product photography from verified Unsplash CDNs.
- **Dedicated `AsyncImageLoader` utilizing `SwingWorker`:**
  - Image fetching and smooth scaling (`Image.SCALE_SMOOTH`) execute strictly on background worker threads via `SwingWorker<ImageIcon, Void>`, guaranteeing the Swing Event Dispatch Thread (EDT) never stutters, freezes, or locks while scrolling.
  - Multi-tier in-memory caching (`ConcurrentHashMap<String, ImageIcon>`).
  - **Skeleton / Shimmer Placeholders:** Displays a clean skeleton placeholder with subtle shimmer gradient and category icon while the image streams in.
  - **Resilient Fallback:** If offline or if network times out, seamlessly falls back to procedural Graphics2D vector placeholders or local disk assets with zero broken layout shifts.

---

### 3. Realistic Cart & Step-by-Step Checkout Journey
1. **Slide-Out Cart Panel (`SlideOutCartPanel`):**
   - Sleek right-docked drawer panel with dark translucent scrim backdrop overlay.
   - **Quantity Steppers:** `[-] 1 [+]` buttons that adjust quantities and instantly recalculate inventory and order totals without page reloads.
   - **Item Actions:** Clean text buttons for `SAVE FOR LATER ♡` (moves to Wishlist) and `REMOVE 🗑` (restores stock to inventory).
2. **Itemized Price Breakdown (Flipkart Style):**
   - Clean card breakdown:
     - Total MRP
     - Discount on MRP (`-₹X,XXX` in bold green)
     - Coupons & Smart Bundles (`-₹X,XXX` in green)
     - Delivery Charges (`FREE` in green)
     - **Total Payable Amount** highlighted in bold
   - **Savings Callout Banner:** *"🎉 You will save ₹X,XXX on this order!"* inside a soft green highlight banner.
   - Promo code application with quick-apply tags for `SAVE15`, `FLAT500`, and `BULK3`.
3. **Multi-Step Checkout & Simulated Payment (`CheckoutDialog`):**
   - **Step 1: Delivery Address Form** (Full Name, 10-digit Phone, 6-digit PIN Code, House/Building, Area/Street, City, State, Home/Work type).
   - **Step 2: Payment Gateway Selection** (UPI / QR Code with simulated scan pattern, Credit/Debit Card with CVV verification, Net Banking with popular Indian banks, Cash on Delivery with captcha).
   - **Step 3: One-Click Simulated Payment Confirmation** with animated banking gateway spinner, animated green checkmark (`✓`), generated Order ID, estimated delivery promise, and itemized invoice receipt popup.
4. **Wishlist Drawer (`WishlistDrawerDialog`):**
   - Dedicated modal drawer to view bookmarked items, see live stock, and move items to cart with one click.

---

### 4. Technical Architecture (Clean Modular OOP)
- **`model`**: Abstract `Product` base class extended by specialized subclasses (`ElectronicsProduct`, `ClothingProduct`, `HomeKitchenProduct`, `BookEssentialProduct`, `DigitalProduct`), `Order`, `CartItem`, `Category`, `User`, `Taxable`, `Shippable`.
- **`service`**: `CartService`, `InventoryService`, `WishlistService`, `RecommendationService`, `ReceiptService`, `PaymentProcessor` (`CreditCardPayment`, `UpiPayment`, `CryptoPayment`).
- **`util`**: `AsyncImageLoader` (SwingWorker non-blocking async pipeline), `ImageHelper`, `CurrencyUtil`, `AssetGenerator`.
- **`ui`**: `MainFrame`, `ProductCardPanel`, `SlideOutCartPanel`, `CartDrawerDialog`, `CheckoutDialog`, `WishlistDrawerDialog`, `ProductQuickViewDialog`, `ReceiptDialog`, `UITheme`, and custom `components`.

---

## 🚀 Running the Application

### 1. Launch FlipMart Desktop Application (Recommended)
Double-click `run.bat` or launch via PowerShell:
```powershell
.\run.ps1
```
Or directly from command line:
```bash
javac -d bin -sourcepath src src/com/shoppingcart/Main.java
java -cp bin com.shoppingcart.Main
```

### 2. Run Headless Architectural Verification Tests
Runs all 9 core OOP architectural and business logic tests:
```bash
javac -d bin -sourcepath src src/com/shoppingcart/test/ArchitectureVerificationTest.java
java -ea -cp bin com.shoppingcart.test.ArchitectureVerificationTest
```

### 3. Run UI/UX and Async Image Test Suite
Runs tests verifying real-world CDN URLs, AsyncImageLoader skeleton placeholders, and reactive Wishlist:
```bash
javac -d bin -sourcepath src src/com/shoppingcart/test/CatalogAndAsyncImageTest.java
java -ea -cp bin com.shoppingcart.test.CatalogAndAsyncImageTest
```
