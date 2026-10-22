package com.shoppingcart.ui;

import com.shoppingcart.model.Product;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.WishlistService;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.util.AsyncImageLoader;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
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
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Slide-out / Modal Wishlist Drawer displaying saved favorite items
 * with one-click "Move to Cart" and "Remove" actions.
 */
public class WishlistDrawerDialog extends JDialog {
    private final CartService cartService;
    private final Consumer<Product> onMoveToCart;
    private JPanel itemsPanel;
    private JLabel lblHeaderCount;

    public WishlistDrawerDialog(JFrame parent, CartService cartService, Consumer<Product> onMoveToCart) {
        super(parent, "FlipMart My Wishlist", true);
        this.cartService = cartService;
        this.onMoveToCart = onMoveToCart;

        initUI();
        refreshWishlist();

        WishlistService.getInstance().addWishlistChangeListener((items, count) -> refreshWishlist());
    }

    private void initUI() {
        setUndecorated(true);
        setSize(480, 680);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout()) {
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

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_DEFAULT),
                new EmptyBorder(16, 20, 16, 20)
        ));

        lblHeaderCount = new JLabel("My Wishlist (0 items)");
        lblHeaderCount.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblHeaderCount.setForeground(UITheme.TEXT_PRIMARY);

        ModernButton btnClose = new ModernButton("✕", ModernButton.Style.GHOST);
        btnClose.setPreferredSize(new Dimension(36, 32));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.addActionListener(e -> dispose());

        header.add(lblHeaderCount, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Content
        itemsPanel = new JPanel();
        itemsPanel.setBackground(UITheme.BG_CANVAS);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void refreshWishlist() {
        itemsPanel.removeAll();
        List<Product> items = WishlistService.getInstance().getWishlistItems();
        lblHeaderCount.setText("My Wishlist (" + items.size() + " item" + (items.size() == 1 ? "" : "s") + ")");

        if (items.isEmpty()) {
            JPanel emptyBox = new JPanel();
            emptyBox.setOpaque(false);
            emptyBox.setLayout(new BoxLayout(emptyBox, BoxLayout.Y_AXIS));
            emptyBox.setBorder(new EmptyBorder(40, 20, 40, 20));

            JLabel heartIcon = new JLabel("♡", JLabel.CENTER);
            heartIcon.setFont(new Font("Segoe UI", Font.PLAIN, 52));
            heartIcon.setForeground(new Color(209, 213, 219));
            heartIcon.setAlignmentX(CENTER_ALIGNMENT);

            JLabel title = new JLabel("Your Wishlist is Empty", JLabel.CENTER);
            title.setFont(UITheme.FONT_HEADER_MEDIUM);
            title.setForeground(UITheme.TEXT_PRIMARY);
            title.setAlignmentX(CENTER_ALIGNMENT);

            JLabel sub = new JLabel("Explore catalog products and tap ♡ to save your favorite items.", JLabel.CENTER);
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(UITheme.TEXT_MUTED);
            sub.setAlignmentX(CENTER_ALIGNMENT);

            emptyBox.add(heartIcon);
            emptyBox.add(Box.createVerticalStrut(10));
            emptyBox.add(title);
            emptyBox.add(Box.createVerticalStrut(4));
            emptyBox.add(sub);
            itemsPanel.add(emptyBox);
        } else {
            for (Product p : items) {
                itemsPanel.add(createWishlistItemRow(p));
                itemsPanel.add(Box.createVerticalStrut(10));
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createWishlistItemRow(Product p) {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Thumbnail
        JLabel imgThumb = new JLabel();
        imgThumb.setPreferredSize(new Dimension(72, 72));
        imgThumb.setHorizontalAlignment(JLabel.CENTER);
        AsyncImageLoader.getInstance().loadImageIntoLabel(imgThumb, p.getId(), p.getImageUrl(), p.getName(), p.getCategory(), 70, 70);
        card.add(imgThumb, BorderLayout.WEST);

        // Center Details
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("<html><body style='width: 200px; font-weight: bold;'>" + p.getName() + "</body></html>");
        title.setFont(UITheme.FONT_BODY_BOLD);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        priceRow.setOpaque(false);
        JLabel price = new JLabel(p.getFormattedPrice());
        price.setFont(UITheme.FONT_PRICE_MEDIUM);
        price.setForeground(UITheme.TEXT_PRICE);
        JLabel mrp = new JLabel("<html><strike>" + p.getFormattedMrp() + "</strike></html>");
        mrp.setFont(UITheme.FONT_SMALL);
        mrp.setForeground(UITheme.TEXT_MRP);
        JLabel disc = new JLabel(p.getDiscountPercent() + "% OFF");
        disc.setFont(UITheme.FONT_BADGE);
        disc.setForeground(UITheme.TEXT_DISCOUNT);
        priceRow.add(price);
        priceRow.add(mrp);
        priceRow.add(disc);

        center.add(title);
        center.add(Box.createVerticalStrut(3));
        center.add(priceRow);
        card.add(center, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 6));
        actions.setOpaque(false);

        ModernButton btnMove = new ModernButton("Move to Cart 🛒", ModernButton.Style.CART_YELLOW);
        btnMove.setFont(UITheme.FONT_SMALL);
        btnMove.setPreferredSize(new Dimension(115, 30));
        btnMove.addActionListener(e -> {
            boolean added = cartService.addProductToCart(p, 1, false);
            if (added) {
                WishlistService.getInstance().removeFromWishlist(p.getId());
                if (onMoveToCart != null) onMoveToCart.accept(p);
            }
        });

        ModernButton btnDel = new ModernButton("Remove ✕", ModernButton.Style.GHOST);
        btnDel.setFont(UITheme.FONT_SMALL);
        btnDel.addActionListener(e -> WishlistService.getInstance().removeFromWishlist(p.getId()));

        actions.add(btnMove);
        actions.add(btnDel);
        card.add(actions, BorderLayout.EAST);

        return card;
    }
}
