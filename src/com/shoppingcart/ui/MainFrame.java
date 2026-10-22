package com.shoppingcart.ui;

import com.shoppingcart.model.Category;
import com.shoppingcart.model.Order;
import com.shoppingcart.model.Product;
import com.shoppingcart.service.BundleRecommendation;
import com.shoppingcart.service.CartPricingBreakdown;
import com.shoppingcart.service.CartRepository;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.InMemoryCartRepository;
import com.shoppingcart.service.InventoryService;
import com.shoppingcart.service.InventoryServiceImpl;
import com.shoppingcart.service.RecommendationService;
import com.shoppingcart.service.WishlistService;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.ui.components.ModernTextField;
import com.shoppingcart.ui.components.WrapLayout;
import com.shoppingcart.util.AssetGenerator;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main application window for FlipMart.
 * Delivers a consumer-grade e-commerce experience inspired by Amazon, Flipkart, and Meesho:
 * - Flipkart Royal Blue sticky top navigation header
 * - Centered pill search bar with real-time filtering and ✕ clear button
 * - Quick-scroll category filter chips below search bar
 * - Wishlist (♡) and Cart (🛒) action icons with active badge counters
 * - Clean white product cards with 8px border radius, green rating chips, and authentic photography
 * - Sleek slide-out cart panel with live stepper buttons and Flipkart itemized price breakdown
 * - 3-step checkout simulation with instant payment confirmation
 */
public class MainFrame extends JFrame {
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final RecommendationService recommendationService;

    // UI Structure
    private JLayeredPane layeredPane;
    private JPanel mainContentPanel;
    private JPanel cardsGridPanel;
    private ModernTextField txtSearch;
    private JButton btnClearSearch;
    private ModernButton btnCartBadge;
    private ModernButton btnWishlistBadge;
    private JLabel lblStatus;

    // Slide-out Drawer Elements
    private JPanel scrimOverlay;
    private SlideOutCartPanel slideOutCart;
    private boolean isCartOpen = false;

    // Filters
    private Category selectedCategory = Category.ALL;
    private boolean isDailyDealsOnly = false;
    private final List<ModernButton> categoryButtons = new ArrayList<>();

    public MainFrame() {
        super("FlipMart - India's Premier Online Shopping Platform");

        // 1. Ensure visual product assets exist for offline resilience
        AssetGenerator.generateAllAssetsIfMissing();

        // 2. Initialize Core Services
        this.inventoryService = new InventoryServiceImpl();
        this.cartRepository = new InMemoryCartRepository();
        this.cartService = new CartService(cartRepository, inventoryService);
        this.recommendationService = new RecommendationService(inventoryService);

        initUI();
        loadProducts();

        // 3. React to live cart updates
        cartRepository.addCartChangeListener((items, totalCount) -> {
            SwingUtilities.invokeLater(this::updateCartButtonBadge);
        });

        // 4. React to Wishlist updates
        WishlistService.getInstance().addWishlistChangeListener((items, count) -> {
            SwingUtilities.invokeLater(this::updateWishlistButtonBadge);
        });
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 880);
        setMinimumSize(new Dimension(1060, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_CANVAS);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        // 1. BASE LAYER: Main Application Content
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(UITheme.BG_CANVAS);

        // Top Navigation Header (Tier 1: Brand & Search + Tier 2: Category Chips)
        JPanel stickyHeader = createStickyHeader();
        mainContentPanel.add(stickyHeader, BorderLayout.NORTH);

        // Center Content: Promotional Ribbon + Products Card Grid
        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setBackground(UITheme.BG_CANVAS);
        centerContainer.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Promotional Banner
        JPanel promoBanner = createPromoBanner();
        centerContainer.add(promoBanner, BorderLayout.NORTH);

        // Product Cards Grid with responsive WrapLayout
        cardsGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
        cardsGridPanel.setBackground(UITheme.BG_CANVAS);

        JScrollPane scrollPane = new JScrollPane(cardsGridPanel);
        scrollPane.setBackground(UITheme.BG_CANVAS);
        scrollPane.getViewport().setBackground(UITheme.BG_CANVAS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(22);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        mainContentPanel.add(centerContainer, BorderLayout.CENTER);

        // Status Bar
        JPanel statusBar = createStatusBar();
        mainContentPanel.add(statusBar, BorderLayout.SOUTH);

        layeredPane.add(mainContentPanel, Integer.valueOf(0));

        // 2. OVERLAY LAYER: Semi-transparent Scrim Backdrop
        scrimOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        scrimOverlay.setOpaque(false);
        scrimOverlay.setVisible(false);
        scrimOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                closeCartDrawer();
            }
        });
        layeredPane.add(scrimOverlay, Integer.valueOf(100));

        // 3. SLIDE-OUT CART PANEL
        slideOutCart = new SlideOutCartPanel(cartService, this::closeCartDrawer, this::openCheckout);
        slideOutCart.setVisible(false);
        layeredPane.add(slideOutCart, Integer.valueOf(200));

        // Window resize listener to keep layered components aligned
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutLayeredComponents();
            }
        });

        setContentPane(layeredPane);
        layoutLayeredComponents();
    }

    private void layoutLayeredComponents() {
        int w = getWidth();
        int h = getHeight();

        mainContentPanel.setBounds(0, 0, w, h);
        scrimOverlay.setBounds(0, 0, w, h);

        int drawerW = Math.min(520, w - 80);
        slideOutCart.setBounds(w - drawerW, 0, drawerW, h);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private JPanel createStickyHeader() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UITheme.BG_HEADER);

        // --- TIER 1: BRANDING + CENTER PILL SEARCH + COMPACT ACTION ICONS ---
        JPanel tier1 = new JPanel(new BorderLayout(24, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        tier1.setOpaque(false);
        tier1.setPreferredSize(new Dimension(1320, 68));
        tier1.setBorder(new EmptyBorder(10, 24, 10, 24));

        // 1. Branding: Bold FlipMart Logo + Yellow Explore Plus
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        brandPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resetToAllProducts();
            }
        });

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        brandRow.setOpaque(false);

        JLabel logoIcon = new JLabel("🛍️");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel brandFlip = new JLabel("Flip");
        brandFlip.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        brandFlip.setForeground(Color.WHITE);

        JLabel brandMart = new JLabel("Mart");
        brandMart.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        brandMart.setForeground(UITheme.BRAND_YELLOW);

        brandRow.add(logoIcon);
        brandRow.add(brandFlip);
        brandRow.add(brandMart);

        JLabel subPlus = new JLabel("   Explore Plus ✦");
        subPlus.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC | Font.BOLD, 11));
        subPlus.setForeground(UITheme.BRAND_YELLOW);

        brandPanel.add(brandRow);
        brandPanel.add(subPlus);
        tier1.add(brandPanel, BorderLayout.WEST);

        // 2. Centered Pill Search Bar with Magnifying Glass & Clear Button
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        searchWrapper.setOpaque(false);

        JPanel searchPillContainer = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchPillContainer.setOpaque(false);
        searchPillContainer.setPreferredSize(new Dimension(460, 40));
        searchPillContainer.setBorder(new EmptyBorder(0, 14, 0, 8));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        searchIcon.setForeground(new Color(156, 163, 175));

        txtSearch = new ModernTextField("Search for products, brands and more...", 26);
        txtSearch.setCornerRadius(20);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchQueryChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchQueryChanged(); }
            public void changedUpdate(DocumentEvent e) { onSearchQueryChanged(); }
        });

        btnClearSearch = new JButton("✕");
        btnClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClearSearch.setForeground(new Color(156, 163, 175));
        btnClearSearch.setBorderPainted(false);
        btnClearSearch.setContentAreaFilled(false);
        btnClearSearch.setFocusPainted(false);
        btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearSearch.setVisible(false);
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearch.requestFocusInWindow();
        });

        searchPillContainer.add(searchIcon, BorderLayout.WEST);
        searchPillContainer.add(txtSearch, BorderLayout.CENTER);
        searchPillContainer.add(btnClearSearch, BorderLayout.EAST);
        searchWrapper.add(searchPillContainer);

        tier1.add(searchWrapper, BorderLayout.CENTER);

        // 3. Compact Right-Side Action Icons: Wishlist (♡), Cart (🛒), and VIP User
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        rightActions.setOpaque(false);

        // Wishlist Button with Red Badge
        btnWishlistBadge = new ModernButton("♡ Wishlist (0)", ModernButton.Style.SECONDARY);
        btnWishlistBadge.setFont(UITheme.FONT_BODY_BOLD);
        btnWishlistBadge.setCornerRadius(20);
        btnWishlistBadge.setPreferredSize(new Dimension(135, 38));
        btnWishlistBadge.addActionListener(e -> openWishlistDrawer());
        rightActions.add(btnWishlistBadge);

        // Cart Button with Dynamic Badge
        btnCartBadge = new ModernButton("🛒 Cart (0)", ModernButton.Style.CART_YELLOW);
        btnCartBadge.setFont(UITheme.FONT_BODY_BOLD);
        btnCartBadge.setCornerRadius(20);
        btnCartBadge.setPreferredSize(new Dimension(175, 38));
        btnCartBadge.addActionListener(e -> openCartDrawer());
        rightActions.add(btnCartBadge);

        // User Avatar Chip
        JLabel userAvatar = new JLabel("👤 Alex ▾");
        userAvatar.setFont(UITheme.FONT_BODY_BOLD);
        userAvatar.setForeground(Color.WHITE);
        userAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightActions.add(userAvatar);

        tier1.add(rightActions, BorderLayout.EAST);
        container.add(tier1);

        // --- TIER 2: QUICK-SCROLL CATEGORY TABS / PILLS BAR ---
        JPanel tier2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        tier2.setBackground(Color.WHITE);
        tier2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_DEFAULT));

        // All Categories Chip
        ModernButton btnAll = createCategoryChip("🌟 All Categories", Category.ALL, false);
        btnAll.setSelectedChip(true);
        tier2.add(btnAll);

        // Standard Categories
        tier2.add(createCategoryChip("⚡ Electronics", Category.ELECTRONICS, false));
        tier2.add(createCategoryChip("👕 Fashion", Category.FASHION, false));
        tier2.add(createCategoryChip("🍳 Home & Kitchen", Category.HOME_KITCHEN, false));
        tier2.add(createCategoryChip("📚 Books & Essentials", Category.BOOKS_ESSENTIALS, false));
        tier2.add(createCategoryChip("💾 Digital & Software", Category.DIGITAL, false));

        // Daily Deals Special Pill
        ModernButton btnDeals = new ModernButton("🔥 Daily Deals (35%+ OFF)", ModernButton.Style.CHIP);
        btnDeals.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        btnDeals.setCornerRadius(18);
        btnDeals.setPreferredSize(new Dimension(190, 32));
        btnDeals.addActionListener(e -> {
            isDailyDealsOnly = true;
            selectedCategory = Category.ALL;
            highlightActiveCategoryChip(btnDeals);
            loadProducts();
        });
        categoryButtons.add(btnDeals);
        tier2.add(btnDeals);

        container.add(tier2);
        return container;
    }

    private ModernButton createCategoryChip(String label, Category cat, boolean deals) {
        ModernButton btn = new ModernButton(label, ModernButton.Style.CHIP);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        btn.setCornerRadius(18);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 32));
        btn.addActionListener(e -> {
            isDailyDealsOnly = false;
            selectedCategory = cat;
            highlightActiveCategoryChip(btn);
            loadProducts();
        });
        categoryButtons.add(btn);
        return btn;
    }

    private void highlightActiveCategoryChip(ModernButton activeBtn) {
        for (ModernButton b : categoryButtons) {
            b.setSelectedChip(b == activeBtn);
        }
    }

    private void resetToAllProducts() {
        if (txtSearch != null) txtSearch.setText("");
        selectedCategory = Category.ALL;
        isDailyDealsOnly = false;
        if (!categoryButtons.isEmpty()) {
            highlightActiveCategoryChip(categoryButtons.get(0));
        }
        loadProducts();
    }

    private void onSearchQueryChanged() {
        String q = txtSearch.getText();
        btnClearSearch.setVisible(q != null && !q.isEmpty());
        loadProducts();
    }

    private JPanel createPromoBanner() {
        JPanel banner = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(237, 242, 254)); // Soft Flipkart Blue tint
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(199, 210, 254));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel promoText = new JLabel("<html><b style='color: #2874F0;'>⚡ FLIPMART BIG SAVINGS FESTIVAL:</b> "
                + "Extra ₹500 Instant Discount with code <b style='color: #111827;'>FLAT500</b> on orders over ₹2,500 • "
                + "Flat 15% off with <b style='color: #111827;'>SAVE15</b> • 10% Smart Bundle Deals • Free Express Delivery</html>");
        promoText.setFont(UITheme.FONT_BODY);
        promoText.setForeground(UITheme.TEXT_PRIMARY);

        JPanel quickChips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickChips.setOpaque(false);

        ModernButton btnCopy500 = new ModernButton("FLAT500", ModernButton.Style.SECONDARY);
        btnCopy500.setFont(UITheme.FONT_SMALL);
        btnCopy500.setPreferredSize(new Dimension(84, 28));
        btnCopy500.addActionListener(e -> {
            cartService.applyCouponCode("FLAT500");
            openCartDrawer();
        });

        ModernButton btnOpenCart = new ModernButton("View Cart 🛒", ModernButton.Style.BUY_NOW);
        btnOpenCart.setFont(UITheme.FONT_SMALL);
        btnOpenCart.setPreferredSize(new Dimension(110, 28));
        btnOpenCart.addActionListener(e -> openCartDrawer());

        quickChips.add(btnCopy500);
        quickChips.add(btnOpenCart);

        banner.add(promoText, BorderLayout.CENTER);
        banner.add(quickChips, BorderLayout.EAST);
        return banner;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        status.setOpaque(false);
        status.setPreferredSize(new Dimension(1320, 34));
        status.setBorder(new EmptyBorder(6, 24, 6, 24));

        lblStatus = new JLabel("Showing all products in catalog");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TEXT_MUTED);

        JLabel trustBadge = new JLabel("✓ 100% Genuine Products  •  7-Day Hassle-Free Returns  •  Safe & Secure UPI / Card Payments");
        trustBadge.setFont(UITheme.FONT_SMALL);
        trustBadge.setForeground(UITheme.TEXT_SECONDARY);

        status.add(lblStatus, BorderLayout.WEST);
        status.add(trustBadge, BorderLayout.EAST);
        return status;
    }

    private void loadProducts() {
        cardsGridPanel.removeAll();
        String query = txtSearch != null ? txtSearch.getText() : "";
        List<Product> products = inventoryService.searchProducts(query, selectedCategory);

        // Daily Deals filter
        if (isDailyDealsOnly) {
            products = products.stream()
                    .filter(p -> p.getDiscountPercent() >= 35)
                    .collect(Collectors.toList());
        }

        if (products.isEmpty()) {
            JPanel emptyNotice = new JPanel();
            emptyNotice.setOpaque(false);
            emptyNotice.setLayout(new BoxLayout(emptyNotice, BoxLayout.Y_AXIS));
            emptyNotice.setBorder(new EmptyBorder(60, 40, 60, 40));

            JLabel icon = new JLabel("🔍", JLabel.CENTER);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            icon.setAlignmentX(CENTER_ALIGNMENT);

            JLabel title = new JLabel("No matching products found", JLabel.CENTER);
            title.setFont(UITheme.FONT_HEADER_MEDIUM);
            title.setForeground(UITheme.TEXT_PRIMARY);
            title.setAlignmentX(CENTER_ALIGNMENT);

            JLabel sub = new JLabel("Try searching with different keywords or clear category filters.", JLabel.CENTER);
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(UITheme.TEXT_MUTED);
            sub.setAlignmentX(CENTER_ALIGNMENT);

            ModernButton btnReset = new ModernButton("Reset Catalog Filters", ModernButton.Style.PRIMARY);
            btnReset.setAlignmentX(CENTER_ALIGNMENT);
            btnReset.addActionListener(e -> resetToAllProducts());

            emptyNotice.add(icon);
            emptyNotice.add(Box.createVerticalStrut(10));
            emptyNotice.add(title);
            emptyNotice.add(Box.createVerticalStrut(4));
            emptyNotice.add(sub);
            emptyNotice.add(Box.createVerticalStrut(16));
            emptyNotice.add(btnReset);

            cardsGridPanel.add(emptyNotice);
        } else {
            for (Product product : products) {
                ProductCardPanel card = new ProductCardPanel(
                        product,
                        cartService,
                        this::openQuickView,
                        this::handleProductAdded,
                        this::handleBuyNow
                );
                cardsGridPanel.add(card);
            }
        }

        String filterNote = isDailyDealsOnly ? "Daily Deals" : selectedCategory.getDisplayName();
        lblStatus.setText(String.format("Displaying %d product%s in %s",
                products.size(),
                products.size() == 1 ? "" : "s",
                filterNote));

        cardsGridPanel.revalidate();
        cardsGridPanel.repaint();
    }

    private void handleProductAdded(Product product) {
        updateCartButtonBadge();

        // Check for Smart Bundle Recommendation
        BundleRecommendation bundle = recommendationService.getRecommendationFor(product);
        if (bundle != null) {
            SwingUtilities.invokeLater(() -> {
                BundleRecommendationDialog dialog = new BundleRecommendationDialog(
                        this,
                        bundle,
                        cartService,
                        () -> {
                            updateCartButtonBadge();
                            lblStatus.setText("✓ Added bundle deal: " + bundle.getRecommendedProduct().getName()
                                    + " with 10% discount!");
                        }
                );
                dialog.setVisible(true);
            });
        }
    }

    private void handleBuyNow(Product product) {
        updateCartButtonBadge();
        // Immediately transition into the express checkout workflow
        SwingUtilities.invokeLater(this::openCheckout);
    }

    private void updateCartButtonBadge() {
        int totalItems = cartRepository.getTotalItemCount();
        CartPricingBreakdown breakdown = cartService.calculateBreakdown();
        if (totalItems > 0) {
            btnCartBadge.setText(String.format("🛒 Cart (%d) • %s", totalItems, CurrencyUtil.format(breakdown.getGrandTotal())));
        } else {
            btnCartBadge.setText("🛒 Cart (0)");
        }
        if (slideOutCart != null && slideOutCart.isVisible()) {
            slideOutCart.refreshCart();
        }
    }

    private void updateWishlistButtonBadge() {
        int count = WishlistService.getInstance().getCount();
        btnWishlistBadge.setText("♡ Wishlist (" + count + ")");
    }

    public void openCartDrawer() {
        slideOutCart.refreshCart();
        layoutLayeredComponents();
        scrimOverlay.setVisible(true);
        slideOutCart.setVisible(true);
        isCartOpen = true;
    }

    public void closeCartDrawer() {
        scrimOverlay.setVisible(false);
        slideOutCart.setVisible(false);
        isCartOpen = false;
    }

    private void openWishlistDrawer() {
        WishlistDrawerDialog dialog = new WishlistDrawerDialog(this, cartService, this::handleProductAdded);
        dialog.setVisible(true);
    }

    private void openQuickView(Product product) {
        ProductQuickViewDialog dialog = new ProductQuickViewDialog(this, product, cartService, this::handleProductAdded);
        dialog.setVisible(true);
    }

    private void openCheckout() {
        closeCartDrawer();
        CheckoutDialog checkout = new CheckoutDialog(this, cartService, this::showReceipt);
        checkout.setVisible(true);
    }

    private void showReceipt(Order confirmedOrder) {
        ReceiptDialog receiptDialog = new ReceiptDialog(this, confirmedOrder);
        receiptDialog.setVisible(true);
        updateCartButtonBadge();
    }
}
