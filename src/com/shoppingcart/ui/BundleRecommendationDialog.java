package com.shoppingcart.ui;

import com.shoppingcart.model.Product;
import com.shoppingcart.service.BundleRecommendation;
import com.shoppingcart.service.CartService;
import com.shoppingcart.ui.components.ModernBadge;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.util.CurrencyUtil;
import com.shoppingcart.util.ImageHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Modal dialog presenting dynamic smart bundle deals with integrated 10% discount in Indian Rupees.
 */
public class BundleRecommendationDialog extends JDialog {
    private final BundleRecommendation bundle;
    private final CartService cartService;
    private final Runnable onBundleAddedCallback;

    public BundleRecommendationDialog(JFrame parent, BundleRecommendation bundle,
                                      CartService cartService, Runnable onBundleAddedCallback) {
        super(parent, "⚡ FlipMart Smart Bundle Offer", true);
        this.bundle = bundle;
        this.cartService = cartService;
        this.onBundleAddedCallback = onBundleAddedCallback;

        initUI();
    }

    private void initUI() {
        setUndecorated(true);
        setSize(540, 500);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_DRAWER);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 24, 24));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // 1. Header with Badge
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        ModernBadge promoBadge = new ModernBadge("⚡ SMART BUNDLE OFFER • EXTRA 10% SAVINGS", ModernBadge.Type.PROMO);
        promoBadge.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Frequently Bought Together");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(String.format("Customers who bought %s also added this item:",
                bundle.getBaseProduct().getName()));
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(promoBadge);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitle);

        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Card showing the Recommended Companion Product
        Product rec = bundle.getRecommendedProduct();
        JPanel productCard = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(UITheme.BORDER_HOVER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Double(0.6, 0.6, getWidth() - 1.2, getHeight() - 1.2, 16, 16));
                g2.dispose();
            }
        };
        productCard.setOpaque(false);
        productCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel imgLabel = new JLabel();
        com.shoppingcart.util.AsyncImageLoader.getInstance().loadImageIntoLabel(imgLabel, rec.getId(), rec.getImageUrl(), rec.getName(), rec.getCategory(), 140, 140);
        productCard.add(imgLabel, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(rec.getName());
        nameLbl.setFont(UITheme.FONT_TITLE);
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel catLbl = new JLabel(rec.getCategorySpecificHighlight());
        catLbl.setFont(UITheme.FONT_SMALL);
        catLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel priceOriginal = new JLabel("Regular Price: <html><strike>" + CurrencyUtil.format(bundle.getOriginalPrice()) + "</strike></html>");
        priceOriginal.setFont(UITheme.FONT_BODY);
        priceOriginal.setForeground(UITheme.TEXT_MUTED);

        JLabel priceBundle = new JLabel(String.format("Bundle Price: %s (Save %s)",
                CurrencyUtil.format(bundle.getDiscountedPrice()),
                CurrencyUtil.format(bundle.getSavingsAmount())));
        priceBundle.setFont(UITheme.FONT_PRICE_LARGE);
        priceBundle.setForeground(UITheme.SUCCESS);

        details.add(Box.createVerticalStrut(4));
        details.add(nameLbl);
        details.add(Box.createVerticalStrut(4));
        details.add(catLbl);
        details.add(Box.createVerticalStrut(10));
        details.add(priceOriginal);
        details.add(Box.createVerticalStrut(4));
        details.add(priceBundle);

        productCard.add(details, BorderLayout.CENTER);
        root.add(productCard, BorderLayout.CENTER);

        // 3. Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actionPanel.setOpaque(false);

        ModernButton btnDecline = new ModernButton("No thanks, just my item", ModernButton.Style.GHOST);
        btnDecline.addActionListener(e -> dispose());

        ModernButton btnAccept = new ModernButton(
                String.format("Add Bundle & Save %s 🎁", CurrencyUtil.format(bundle.getSavingsAmount())),
                ModernButton.Style.BUY_NOW
        );
        btnAccept.addActionListener(e -> {
            boolean success = cartService.addProductToCart(bundle.getRecommendedProduct(), 1, true);
            dispose();
            if (success && onBundleAddedCallback != null) {
                onBundleAddedCallback.run();
            }
        });

        actionPanel.add(btnDecline);
        actionPanel.add(btnAccept);

        root.add(actionPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
