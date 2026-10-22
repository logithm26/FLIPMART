package com.shoppingcart.ui;

import com.shoppingcart.model.Product;
import com.shoppingcart.service.CartService;
import com.shoppingcart.ui.components.ModernBadge;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.util.AsyncImageLoader;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Modal dialog presenting high-resolution product inspection, specifications, and retail pricing.
 * Styled cleanly in Flipkart / Amazon white surface format with AsyncImageLoader.
 */
public class ProductQuickViewDialog extends JDialog {
    private final Product product;
    private final CartService cartService;
    private final Consumer<Product> onAddedCallback;
    private int quantitySelected = 1;
    private JLabel qtyLabel;

    public ProductQuickViewDialog(JFrame parent, Product product, CartService cartService, Consumer<Product> onAddedCallback) {
        super(parent, product.getName(), true);
        this.product = product;
        this.cartService = cartService;
        this.onAddedCallback = onAddedCallback;

        initUI();
    }

    private void initUI() {
        setUndecorated(true);
        setSize(760, 540);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(24, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // LEFT: 1:1 High-res square image preview with AsyncImageLoader
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(300, 460));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        AsyncImageLoader.getInstance().loadImageIntoLabel(imageLabel, product.getId(), product.getImageUrl(), product.getName(), product.getCategory(), 290, 260);

        // Stock status card underneath image
        JPanel stockCard = new JPanel(new BorderLayout());
        stockCard.setOpaque(false);
        stockCard.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        ModernBadge stockBadge = new ModernBadge("", ModernBadge.Type.STOCK_IN);
        stockBadge.updateStock(product.getStockQuantity());
        stockCard.add(stockBadge, BorderLayout.CENTER);

        leftPanel.add(imageLabel, BorderLayout.CENTER);
        leftPanel.add(stockCard, BorderLayout.SOUTH);
        root.add(leftPanel, BorderLayout.WEST);

        // RIGHT: Detailed Specifications & Pricing
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // Close button in top-right
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        ModernBadge catBadge = new ModernBadge(product.getCategory().getIconSymbol() + " " + product.getCategory().getDisplayName(), ModernBadge.Type.CATEGORY);
        ModernButton btnClose = new ModernButton("✕", ModernButton.Style.GHOST);
        btnClose.setPreferredSize(new Dimension(36, 32));
        btnClose.addActionListener(e -> dispose());
        topBar.add(catBadge, BorderLayout.WEST);
        topBar.add(btnClose, BorderLayout.EAST);
        rightPanel.add(topBar);

        // Title
        JLabel title = new JLabel("<html><body style='width: 360px; font-weight: bold;'>" + product.getName() + "</body></html>");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(title);

        // Rating
        JLabel ratingLabel = new JLabel(String.format("★ %.1f rating  |  %s verified ratings",
                product.getRating(), CurrencyUtil.formatWithoutSymbol(product.getReviewCount())));
        ratingLabel.setFont(UITheme.FONT_SMALL);
        ratingLabel.setForeground(new Color(56, 142, 60));
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(ratingLabel);

        // Description
        JLabel desc = new JLabel("<html><body style='width: 360px; color: #4B5563;'>" + product.getDescription() + "</body></html>");
        desc.setFont(UITheme.FONT_BODY);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(desc);

        // Specifications Box
        JPanel specsBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        specsBox.setOpaque(false);
        specsBox.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel specsHtml = new JLabel("<html><body style='width: 340px; color: #374151; font-family: Segoe UI; font-size: 11px;'>"
                + "<div style='color: #2874F0; font-weight: bold; margin-bottom: 4px;'>PRODUCT SPECIFICATIONS & FEATURES:</div>"
                + product.getDetailedSpecificationsHtml()
                + "</body></html>");
        specsBox.add(specsHtml, BorderLayout.CENTER);

        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(specsBox);

        // Commercial Price Row with MRP and Discount
        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        priceRow.setOpaque(false);

        JLabel priceLbl = new JLabel(product.getFormattedPrice());
        priceLbl.setFont(UITheme.FONT_PRICE_LARGE);
        priceLbl.setForeground(UITheme.TEXT_PRICE);

        JLabel mrpLbl = new JLabel("<html><strike>" + product.getFormattedMrp() + "</strike></html>");
        mrpLbl.setFont(UITheme.FONT_PRICE_MEDIUM);
        mrpLbl.setForeground(UITheme.TEXT_MRP);

        JLabel discBadge = new JLabel("(" + product.getDiscountPercent() + "% OFF)");
        discBadge.setFont(UITheme.FONT_BODY_BOLD);
        discBadge.setForeground(UITheme.TEXT_DISCOUNT);

        priceRow.add(priceLbl);
        priceRow.add(mrpLbl);
        priceRow.add(discBadge);

        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(priceRow);

        // Quantity Stepper and Add to Cart Action
        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomAction.setOpaque(false);

        ModernButton btnMinus = new ModernButton("-", ModernButton.Style.SECONDARY);
        btnMinus.setPreferredSize(new Dimension(36, 36));
        qtyLabel = new JLabel("1", JLabel.CENTER);
        qtyLabel.setFont(UITheme.FONT_TITLE);
        qtyLabel.setForeground(UITheme.TEXT_PRIMARY);
        qtyLabel.setPreferredSize(new Dimension(30, 36));

        ModernButton btnPlus = new ModernButton("+", ModernButton.Style.SECONDARY);
        btnPlus.setPreferredSize(new Dimension(36, 36));

        btnMinus.addActionListener(e -> {
            if (quantitySelected > 1) {
                quantitySelected--;
                qtyLabel.setText(String.valueOf(quantitySelected));
            }
        });

        btnPlus.addActionListener(e -> {
            if (quantitySelected < product.getStockQuantity()) {
                quantitySelected++;
                qtyLabel.setText(String.valueOf(quantitySelected));
            }
        });

        ModernButton btnAdd = new ModernButton("Add to Cart 🛒", ModernButton.Style.CART_YELLOW);
        btnAdd.setPreferredSize(new Dimension(150, 38));
        btnAdd.setEnabled(product.isAvailable());
        btnAdd.addActionListener(e -> {
            boolean success = cartService.addProductToCart(product, quantitySelected, false);
            dispose();
            if (success && onAddedCallback != null) {
                onAddedCallback.accept(product);
            }
        });

        bottomAction.add(btnMinus);
        bottomAction.add(qtyLabel);
        bottomAction.add(btnPlus);
        bottomAction.add(btnAdd);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(bottomAction);

        root.add(rightPanel, BorderLayout.CENTER);
        setContentPane(root);
    }
}
