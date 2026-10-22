package com.shoppingcart.ui;

import com.shoppingcart.model.Product;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.WishlistService;
import com.shoppingcart.ui.components.ModernBadge;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.util.AsyncImageLoader;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Visual card component representing a single catalog product in FlipMart
 * based on the Meesho / Flipkart product card formula.
 * Features 1:1 aspect ratio authentic product photography loaded asynchronously via SwingWorker,
 * floating wishlist heart, Flipkart-style green rating chip (★ 4.8 | 3.4k),
 * bold price hierarchy with strike-through MRP and discount tag,
 * Assured badge, and an animated "Add to Cart" button.
 */
public class ProductCardPanel extends JPanel {
    private final Product product;
    private final CartService cartService;
    private final Consumer<Product> onQuickViewRequested;
    private final Consumer<Product> onProductAddedToCart;
    private final Consumer<Product> onBuyNowRequested;

    private JLabel imageLabel;
    private ModernBadge stockBadge;
    private ModernButton btnAddToCart;
    private ModernButton btnBuyNow;
    private JButton btnWishlistHeart;
    private boolean isHovered = false;

    public ProductCardPanel(Product product, CartService cartService,
                            Consumer<Product> onQuickViewRequested,
                            Consumer<Product> onProductAddedToCart,
                            Consumer<Product> onBuyNowRequested) {
        this.product = product;
        this.cartService = cartService;
        this.onQuickViewRequested = onQuickViewRequested;
        this.onProductAddedToCart = onProductAddedToCart;
        this.onBuyNowRequested = onBuyNowRequested;

        initUI();
    }

    public ProductCardPanel(Product product, CartService cartService,
                            Consumer<Product> onQuickViewRequested,
                            Consumer<Product> onProductAddedToCart) {
        this(product, cartService, onQuickViewRequested, onProductAddedToCart, null);
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setPreferredSize(new Dimension(285, 475));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Hover listener for modern card elevation effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });

        // =====================================================================
        // 1. TOP: Centered 1:1 Image Container with Floating Overlays
        // =====================================================================
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setOpaque(false);
        imageContainer.setPreferredSize(new Dimension(260, 205));

        // Main Image Label with asynchronous SwingWorker loader & smooth scaling
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onQuickViewRequested != null) {
                    onQuickViewRequested.accept(product);
                }
            }
        });

        // Asynchronously stream genuine commercial product image on background thread with isolated binding
        AsyncImageLoader.getInstance().loadImageIntoLabel(
                imageLabel,
                product.getId(),
                product.getImageUrl(),
                product.getName(),
                product.getCategory(),
                256, 195
        );

        // Overlay Header Bar: Category/Stock pill on the left, Wishlist Heart on the right
        JPanel overlayBar = new JPanel(new BorderLayout());
        overlayBar.setOpaque(false);
        overlayBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));

        // Demand or Category badge
        stockBadge = new ModernBadge("", ModernBadge.Type.STOCK_IN);
        stockBadge.updateStock(product.getStockQuantity());
        overlayBar.add(stockBadge, BorderLayout.WEST);

        // Floating Wishlist Heart Icon Button
        boolean isFavorited = WishlistService.getInstance().isWishlisted(product.getId());
        btnWishlistHeart = createWishlistButton(isFavorited);
        overlayBar.add(btnWishlistHeart, BorderLayout.EAST);

        imageContainer.add(overlayBar, BorderLayout.NORTH);
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        add(imageContainer, BorderLayout.NORTH);

        // =====================================================================
        // 2. CENTER: Information Content Panel (The Flipkart Formula)
        // =====================================================================
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // Rating & Reviews Pill: e.g. "★ 4.8 | 3.4k" (Flipkart Green Pill)
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(LEFT_ALIGNMENT);

        String ratingStr = String.format("★ %.1f", product.getRating());
        ModernBadge ratingPill = new ModernBadge(ratingStr, ModernBadge.Type.RATING);
        ratingPill.setBadgeFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));

        String countFormatted = CurrencyUtil.formatWithoutSymbol(product.getReviewCount());
        JLabel reviewsLabel = new JLabel("(" + countFormatted + " reviews)");
        reviewsLabel.setFont(UITheme.FONT_SMALL);
        reviewsLabel.setForeground(UITheme.TEXT_MUTED);

        ratingRow.add(ratingPill);
        ratingRow.add(reviewsLabel);

        // Assured / Prime Badge
        ModernBadge assuredBadge = new ModernBadge("⚡ Assured", ModernBadge.Type.ASSURED);
        ratingRow.add(assuredBadge);

        // Product Title
        JLabel titleLabel = new JLabel("<html><body style='width: 220px; text-overflow: ellipsis;'>"
                + product.getName() + "</body></html>");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onQuickViewRequested != null) {
                    onQuickViewRequested.accept(product);
                }
            }
        });

        // Spec Highlight chip
        JLabel specLabel = new JLabel(product.getCategorySpecificHighlight());
        specLabel.setFont(UITheme.FONT_SMALL);
        specLabel.setForeground(UITheme.TEXT_SECONDARY);
        specLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Pricing Hierarchy:
        // - Bold Selling Price (₹799)
        // - Strikethrough MRP (₹1,999)
        // - High-contrast Discount Tag (60% OFF)
        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        priceRow.setOpaque(false);
        priceRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel priceLabel = new JLabel(product.getFormattedPrice());
        priceLabel.setFont(UITheme.FONT_PRICE_LARGE);
        priceLabel.setForeground(UITheme.TEXT_PRICE);

        JLabel mrpLabel = new JLabel("<html><strike>" + product.getFormattedMrp() + "</strike></html>");
        mrpLabel.setFont(UITheme.FONT_MRP);
        mrpLabel.setForeground(UITheme.TEXT_MRP);

        JLabel discountBadge = new JLabel(product.getDiscountPercent() + "% OFF");
        discountBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        discountBadge.setForeground(UITheme.TEXT_DISCOUNT);

        priceRow.add(priceLabel);
        priceRow.add(mrpLabel);
        priceRow.add(discountBadge);

        // Delivery Tag
        JLabel deliveryLabel = new JLabel("✓ Free Express Delivery");
        deliveryLabel.setFont(UITheme.FONT_SMALL);
        deliveryLabel.setForeground(UITheme.SUCCESS);
        deliveryLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(ratingRow);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(specLabel);
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(priceRow);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(deliveryLabel);
        infoPanel.add(Box.createVerticalStrut(6));

        add(infoPanel, BorderLayout.CENTER);

        // =====================================================================
        // 3. BOTTOM: Action Buttons Panel with Animation Feedback
        // =====================================================================
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actionPanel.setOpaque(false);

        btnAddToCart = new ModernButton("Add to Cart 🛒", ModernButton.Style.CART_YELLOW);
        btnAddToCart.setCornerRadius(8);
        btnAddToCart.setPreferredSize(new Dimension(125, 36));
        btnAddToCart.setEnabled(product.isAvailable());
        btnAddToCart.addActionListener(e -> handleAddToCart());

        btnBuyNow = new ModernButton("Buy Now ⚡", ModernButton.Style.BUY_NOW);
        btnBuyNow.setCornerRadius(8);
        btnBuyNow.setPreferredSize(new Dimension(125, 36));
        btnBuyNow.setEnabled(product.isAvailable());
        btnBuyNow.addActionListener(e -> handleBuyNow());

        actionPanel.add(btnAddToCart);
        actionPanel.add(btnBuyNow);

        add(actionPanel, BorderLayout.SOUTH);

        // Listen for real-time stock updates
        cartService.getInventoryService().addStockChangeListener((productId, newStock) -> {
            if (product.getId().equals(productId)) {
                stockBadge.updateStock(newStock);
                boolean available = newStock > 0;
                btnAddToCart.setEnabled(available);
                btnBuyNow.setEnabled(available);
                if (!available) {
                    btnAddToCart.setText("Out of Stock");
                    btnBuyNow.setText("Unavailable");
                } else {
                    btnAddToCart.setText("Add to Cart 🛒");
                    btnBuyNow.setText("Buy Now ⚡");
                }
            }
        });

        // Listen for Wishlist changes
        WishlistService.getInstance().addWishlistChangeListener((items, count) -> {
            boolean active = WishlistService.getInstance().isWishlisted(product.getId());
            btnWishlistHeart.setText(active ? "♥" : "♡");
            btnWishlistHeart.setForeground(active ? UITheme.WISHLIST_RED : new Color(156, 163, 175));
        });
    }

    private JButton createWishlistButton(boolean isFavorited) {
        JButton heart = new JButton(isFavorited ? "♥" : "♡") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(229, 231, 235));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        heart.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heart.setForeground(isFavorited ? UITheme.WISHLIST_RED : new Color(156, 163, 175));
        heart.setPreferredSize(new Dimension(34, 34));
        heart.setFocusPainted(false);
        heart.setBorderPainted(false);
        heart.setContentAreaFilled(false);
        heart.setOpaque(false);
        heart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        heart.setToolTipText("Add to Wishlist");

        heart.addActionListener(e -> {
            boolean favorited = WishlistService.getInstance().toggleWishlist(product);
            heart.setText(favorited ? "♥" : "♡");
            heart.setForeground(favorited ? UITheme.WISHLIST_RED : new Color(156, 163, 175));
        });

        return heart;
    }

    private void handleAddToCart() {
        if (!product.isAvailable()) return;

        boolean added = cartService.addProductToCart(product, 1, false);
        if (added) {
            // Visual click feedback animation
            String originalText = btnAddToCart.getText();
            btnAddToCart.setText("✓ Added!");
            btnAddToCart.repaint();

            Timer timer = new Timer(1200, evt -> {
                btnAddToCart.setText(originalText);
                btnAddToCart.repaint();
            });
            timer.setRepeats(false);
            timer.start();

            if (onProductAddedToCart != null) {
                onProductAddedToCart.accept(product);
            }
        }
    }

    private void handleBuyNow() {
        if (!product.isAvailable()) return;

        boolean added = cartService.addProductToCart(product, 1, false);
        if (added && onProductAddedToCart != null) {
            onProductAddedToCart.accept(product);
        }

        if (onBuyNowRequested != null) {
            onBuyNowRequested.accept(product);
        }
    }

    public Product getProduct() {
        return product;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 8px border radius container (Flipkart/Meesho standard)
        int arc = 8;

        if (isHovered) {
            // Soft drop shadow and hover elevation
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 4, arc + 4, arc + 4));

            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Double(1, 2, width - 2, height - 2, arc + 2, arc + 2));
        }

        // Clean white card background
        g2.setColor(UITheme.BG_SURFACE);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));

        // Subtle 1px border (#E0E0E0), turns Flipkart Blue on hover
        g2.setColor(isHovered ? UITheme.BORDER_HOVER : UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(isHovered ? 1.6f : 1.0f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1.0, height - 1.0, arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }
}
