package com.shoppingcart.ui;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Product;
import com.shoppingcart.service.CartPricingBreakdown;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.WishlistService;
import com.shoppingcart.strategy.DiscountStrategy;
import com.shoppingcart.ui.components.ModernBadge;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.ui.components.ModernTextField;
import com.shoppingcart.util.AsyncImageLoader;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Consumer-grade Slide-Out Cart Panel inspired by Flipkart & Amazon.
 * Features live stepper quantity selectors ([-] 1 [+]), instant real-time price recalculation,
 * Flipkart-style itemized price breakdown (Total MRP, Discount, Delivery, Total Payable, Savings Callout),
 * coupon code application, "Save for Later", and express checkout initiation.
 */
public class SlideOutCartPanel extends JPanel {
    private final CartService cartService;
    private final Runnable onCloseRequested;
    private final Runnable onProceedToCheckout;

    private JPanel itemsListPanel;
    private JLabel lblTotalMrp;
    private JLabel lblProductDiscount;
    private JLabel lblCouponDiscount;
    private JLabel lblDeliveryCharges;
    private JLabel lblTotalPayable;
    private JLabel lblFooterTotal;
    private JLabel lblSavingsCallout;
    private JLabel lblItemCountHeader;
    private ModernTextField txtPromoCode;
    private JLabel lblPromoFeedback;
    private ModernButton btnPlaceOrder;
    private JPanel activeCouponChipPanel;

    public SlideOutCartPanel(CartService cartService, Runnable onCloseRequested, Runnable onProceedToCheckout) {
        this.cartService = cartService;
        this.onCloseRequested = onCloseRequested;
        this.onProceedToCheckout = onProceedToCheckout;

        initUI();
        refreshCart();

        // Listen for live cart updates
        cartService.getCartRepository().addCartChangeListener((items, count) -> {
            SwingUtilities.invokeLater(this::refreshCart);
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 800));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_DEFAULT));

        // =====================================================================
        // 1. TOP HEADER: FlipMart Brand & Close
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_DEFAULT),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));

        lblItemCountHeader = new JLabel("My Cart (0 items)");
        lblItemCountHeader.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblItemCountHeader.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblAddressPin = new JLabel("📍 Deliver to: Bengaluru - 560001 (Express)");
        lblAddressPin.setFont(UITheme.FONT_SMALL);
        lblAddressPin.setForeground(UITheme.PRIMARY);

        headerLeft.add(lblItemCountHeader);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblAddressPin);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        ModernButton btnClear = new ModernButton("Clear All", ModernButton.Style.GHOST);
        btnClear.setFont(UITheme.FONT_SMALL);
        btnClear.addActionListener(e -> {
            cartService.clearCart();
            refreshCart();
        });

        ModernButton btnClose = new ModernButton("✕", ModernButton.Style.GHOST);
        btnClose.setPreferredSize(new Dimension(36, 32));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.addActionListener(e -> {
            if (onCloseRequested != null) onCloseRequested.run();
        });

        headerRight.add(btnClear);
        headerRight.add(btnClose);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // =====================================================================
        // 2. CENTER: Scrollable Items + Itemized Flipkart Price Breakdown
        // =====================================================================
        JPanel scrollContent = new JPanel();
        scrollContent.setBackground(UITheme.BG_CANVAS);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));

        // Items Container
        itemsListPanel = new JPanel();
        itemsListPanel.setOpaque(false);
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
        itemsListPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        scrollContent.add(itemsListPanel);

        // Coupon Section Box
        JPanel couponBox = createCouponSection();
        scrollContent.add(couponBox);

        // Itemized Price Breakdown Card
        JPanel priceDetailsCard = createItemizedPriceDetailsCard();
        scrollContent.add(priceDetailsCard);

        // Safe payments assurance note
        JPanel safetyNote = createSafetyAssuranceNote();
        scrollContent.add(safetyNote);

        scrollContent.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(UITheme.BG_CANVAS);
        add(scrollPane, BorderLayout.CENTER);

        // =====================================================================
        // 3. BOTTOM STICKY BAR: Total Payable + Prominent "PLACE ORDER"
        // =====================================================================
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setBackground(Color.WHITE);
        footerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel footerLeft = new JPanel();
        footerLeft.setOpaque(false);
        footerLeft.setLayout(new BoxLayout(footerLeft, BoxLayout.Y_AXIS));

        lblFooterTotal = new JLabel("₹0");
        lblFooterTotal.setFont(UITheme.FONT_PRICE_LARGE);
        lblFooterTotal.setForeground(UITheme.TEXT_PRICE);

        JLabel lblViewDetails = new JLabel("Total Payable (Incl. GST)");
        lblViewDetails.setFont(UITheme.FONT_SMALL);
        lblViewDetails.setForeground(UITheme.TEXT_MUTED);

        footerLeft.add(lblFooterTotal);
        footerLeft.add(lblViewDetails);

        btnPlaceOrder = new ModernButton("PLACE ORDER ➔", ModernButton.Style.BUY_NOW);
        btnPlaceOrder.setPreferredSize(new Dimension(200, 44));
        btnPlaceOrder.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        btnPlaceOrder.addActionListener(e -> {
            if (onProceedToCheckout != null && !cartService.getCartRepository().getItems().isEmpty()) {
                onProceedToCheckout.run();
            }
        });

        footerBar.add(footerLeft, BorderLayout.WEST);
        footerBar.add(btnPlaceOrder, BorderLayout.EAST);
        add(footerBar, BorderLayout.SOUTH);
    }

    private JPanel createCouponSection() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 16, 12, 16));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel title = new JLabel("🏷 Apply Coupons & Save Extra");
        title.setFont(UITheme.FONT_BODY_BOLD);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(LEFT_ALIGNMENT);

        txtPromoCode = new ModernTextField("Enter coupon code", 10);
        txtPromoCode.setPreferredSize(new Dimension(220, 34));

        ModernButton btnApply = new ModernButton("Apply", ModernButton.Style.PRIMARY);
        btnApply.setPreferredSize(new Dimension(80, 34));
        btnApply.addActionListener(e -> handleApplyCoupon(txtPromoCode.getText()));

        inputRow.add(txtPromoCode, BorderLayout.CENTER);
        inputRow.add(btnApply, BorderLayout.EAST);

        // Active coupon / Quick suggestions row
        activeCouponChipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        activeCouponChipPanel.setOpaque(false);
        activeCouponChipPanel.setAlignmentX(LEFT_ALIGNMENT);

        lblPromoFeedback = new JLabel(" ");
        lblPromoFeedback.setFont(UITheme.FONT_SMALL);
        lblPromoFeedback.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(inputRow);
        card.add(Box.createVerticalStrut(6));
        card.add(activeCouponChipPanel);
        card.add(Box.createVerticalStrut(2));
        card.add(lblPromoFeedback);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createItemizedPriceDetailsCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 16, 12, 16));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Header: PRICE DETAILS
        JLabel lblHeader = new JLabel("PRICE DETAILS");
        lblHeader.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        lblHeader.setForeground(UITheme.TEXT_MUTED);
        lblHeader.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblHeader);
        card.add(Box.createVerticalStrut(10));
        card.add(createDivider());
        card.add(Box.createVerticalStrut(10));

        // Line 1: Total MRP
        lblTotalMrp = new JLabel("₹0");
        card.add(createPriceRow("Total MRP", lblTotalMrp, UITheme.TEXT_PRIMARY));
        card.add(Box.createVerticalStrut(8));

        // Line 2: Product Discount
        lblProductDiscount = new JLabel("-₹0");
        card.add(createPriceRow("Discount on MRP", lblProductDiscount, UITheme.SUCCESS));
        card.add(Box.createVerticalStrut(8));

        // Line 3: Coupon Discount
        lblCouponDiscount = new JLabel("-₹0");
        card.add(createPriceRow("Coupons & Bundles", lblCouponDiscount, UITheme.SUCCESS));
        card.add(Box.createVerticalStrut(8));

        // Line 4: Delivery Charges
        lblDeliveryCharges = new JLabel("FREE");
        card.add(createPriceRow("Delivery Charges", lblDeliveryCharges, UITheme.SUCCESS));
        card.add(Box.createVerticalStrut(10));

        card.add(createDivider());
        card.add(Box.createVerticalStrut(10));

        // Line 5: Total Payable Amount
        lblTotalPayable = new JLabel("₹0");
        lblTotalPayable.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblTotalPayable.setForeground(UITheme.TEXT_PRICE);

        JLabel lblPayableTitle = new JLabel("Total Payable Amount");
        lblPayableTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblPayableTitle.setForeground(UITheme.TEXT_PRICE);

        JPanel payableRow = new JPanel(new BorderLayout());
        payableRow.setOpaque(false);
        payableRow.setAlignmentX(LEFT_ALIGNMENT);
        payableRow.add(lblPayableTitle, BorderLayout.WEST);
        payableRow.add(lblTotalPayable, BorderLayout.EAST);
        card.add(payableRow);
        card.add(Box.createVerticalStrut(12));

        // Savings Callout Banner: "You will save ₹X on this order!"
        JPanel savingsBanner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SUCCESS_LIGHT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(165, 214, 167));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        savingsBanner.setOpaque(false);
        savingsBanner.setBorder(new EmptyBorder(8, 12, 8, 12));
        savingsBanner.setAlignmentX(LEFT_ALIGNMENT);

        lblSavingsCallout = new JLabel("🎉 You will save ₹0 on this order!");
        lblSavingsCallout.setFont(UITheme.FONT_BODY_BOLD);
        lblSavingsCallout.setForeground(UITheme.SUCCESS);
        savingsBanner.add(lblSavingsCallout, BorderLayout.CENTER);

        card.add(savingsBanner);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSafetyAssuranceNote() {
        JPanel note = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        note.setOpaque(false);
        JLabel icon = new JLabel("🛡");
        JLabel text = new JLabel("Safe and Secure Payments. 100% Authentic Products.");
        text.setFont(UITheme.FONT_SMALL);
        text.setForeground(UITheme.TEXT_MUTED);
        note.add(icon);
        note.add(text);
        return note;
    }

    private JPanel createPriceRow(String title, JLabel valueLabel, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_BODY);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        valueLabel.setFont(UITheme.FONT_BODY_BOLD);
        valueLabel.setForeground(valueColor);

        row.add(titleLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createDivider() {
        JPanel div = new JPanel();
        div.setBackground(UITheme.BORDER_LIGHT);
        div.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        div.setPreferredSize(new Dimension(100, 1));
        div.setAlignmentX(LEFT_ALIGNMENT);
        return div;
    }

    public void refreshCart() {
        itemsListPanel.removeAll();
        List<CartItem> items = cartService.getCartRepository().getItems();
        int totalItems = cartService.getCartRepository().getTotalItemCount();

        lblItemCountHeader.setText("My Cart (" + totalItems + " item" + (totalItems == 1 ? "" : "s") + ")");
        btnPlaceOrder.setEnabled(!items.isEmpty());

        if (items.isEmpty()) {
            renderEmptyCartState();
        } else {
            for (CartItem item : items) {
                itemsListPanel.add(createCartItemRow(item));
                itemsListPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Recalculate price breakdown
        updatePriceBreakdown();

        itemsListPanel.revalidate();
        itemsListPanel.repaint();
    }

    private void renderEmptyCartState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel icon = new JLabel("🛒");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Your FlipMart Cart is empty!");
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Explore today's best deals and add items to your cart.");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        emptyPanel.add(icon);
        emptyPanel.add(Box.createVerticalStrut(10));
        emptyPanel.add(title);
        emptyPanel.add(Box.createVerticalStrut(4));
        emptyPanel.add(subtitle);

        itemsListPanel.add(emptyPanel);
    }

    private JPanel createCartItemRow(CartItem item) {
        Product product = item.getProduct();

        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // LEFT: 1:1 Thumbnail with AsyncImageLoader
        JLabel imgThumb = new JLabel();
        imgThumb.setPreferredSize(new Dimension(84, 84));
        imgThumb.setHorizontalAlignment(JLabel.CENTER);
        AsyncImageLoader.getInstance().loadImageIntoLabel(
                imgThumb,
                product.getId(),
                product.getImageUrl(),
                product.getName(),
                product.getCategory(),
                80, 80
        );
        card.add(imgThumb, BorderLayout.WEST);

        // CENTER: Product Details
        JPanel centerInfo = new JPanel();
        centerInfo.setOpaque(false);
        centerInfo.setLayout(new BoxLayout(centerInfo, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("<html><body style='width: 220px; font-weight: bold;'>" + product.getName() + "</body></html>");
        titleLabel.setFont(UITheme.FONT_BODY_BOLD);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sellerLabel = new JLabel("Seller: RetailNet • ⚡ Assured");
        sellerLabel.setFont(UITheme.FONT_SMALL);
        sellerLabel.setForeground(UITheme.TEXT_MUTED);

        // Price row
        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        priceRow.setOpaque(false);

        JLabel sellingPrice = new JLabel(CurrencyUtil.format(item.getEffectiveUnitPrice() * item.getQuantity()));
        sellingPrice.setFont(UITheme.FONT_PRICE_MEDIUM);
        sellingPrice.setForeground(UITheme.TEXT_PRICE);

        JLabel originalMrp = new JLabel("<html><strike>" + CurrencyUtil.format(product.getOriginalMrp() * item.getQuantity()) + "</strike></html>");
        originalMrp.setFont(UITheme.FONT_SMALL);
        originalMrp.setForeground(UITheme.TEXT_MRP);

        JLabel discountTag = new JLabel(product.getDiscountPercent() + "% OFF");
        discountTag.setFont(UITheme.FONT_BADGE);
        discountTag.setForeground(UITheme.TEXT_DISCOUNT);

        priceRow.add(sellingPrice);
        priceRow.add(originalMrp);
        priceRow.add(discountTag);

        centerInfo.add(titleLabel);
        centerInfo.add(Box.createVerticalStrut(2));
        centerInfo.add(sellerLabel);
        centerInfo.add(Box.createVerticalStrut(4));
        centerInfo.add(priceRow);

        card.add(centerInfo, BorderLayout.CENTER);

        // BOTTOM: Stepper Buttons ([-] 1 [+]) + "SAVE FOR LATER" & "REMOVE"
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_LIGHT),
                new EmptyBorder(8, 0, 0, 0)
        ));

        // Stepper: [-] [qty] [+]
        JPanel stepperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        stepperPanel.setOpaque(false);

        JButton btnMinus = createStepperButton("−");
        JLabel lblQty = new JLabel(String.valueOf(item.getQuantity()), JLabel.CENTER);
        lblQty.setPreferredSize(new Dimension(30, 26));
        lblQty.setFont(UITheme.FONT_BODY_BOLD);
        lblQty.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1));
        JButton btnPlus = createStepperButton("+");

        btnMinus.addActionListener(e -> {
            cartService.decrementItemQuantity(product.getId());
            refreshCart();
        });

        btnPlus.addActionListener(e -> {
            boolean incremented = cartService.incrementItemQuantity(product.getId());
            if (!incremented) {
                btnPlus.setEnabled(false);
            }
            refreshCart();
        });

        stepperPanel.add(btnMinus);
        stepperPanel.add(lblQty);
        stepperPanel.add(btnPlus);

        // Action links: "SAVE FOR LATER ♡" and "REMOVE 🗑"
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRow.setOpaque(false);

        JButton btnSaveLater = createTextActionButton("SAVE FOR LATER ♡");
        btnSaveLater.addActionListener(e -> {
            WishlistService.getInstance().addToWishlist(product);
            cartService.removeItem(product.getId());
            refreshCart();
        });

        JButton btnRemove = createTextActionButton("REMOVE 🗑");
        btnRemove.addActionListener(e -> {
            cartService.removeItem(product.getId());
            refreshCart();
        });

        actionsRow.add(btnSaveLater);
        actionsRow.add(btnRemove);

        bottomBar.add(stepperPanel, BorderLayout.WEST);
        bottomBar.add(actionsRow, BorderLayout.EAST);

        card.add(bottomBar, BorderLayout.SOUTH);
        return card;
    }

    private JButton createStepperButton(String symbol) {
        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(243, 244, 246));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createTextActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        btn.setForeground(new Color(55, 65, 81));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(UITheme.PRIMARY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(new Color(55, 65, 81));
            }
        });
        return btn;
    }

    private void updatePriceBreakdown() {
        List<CartItem> items = cartService.getCartRepository().getItems();
        double totalMrp = 0.0;
        double baseSubtotal = 0.0;

        for (CartItem item : items) {
            totalMrp += item.getProduct().getOriginalMrp() * item.getQuantity();
            baseSubtotal += item.getBaseSubtotal();
        }

        CartPricingBreakdown breakdown = cartService.calculateBreakdown();
        double mrpDiscount = Math.max(0.0, totalMrp - baseSubtotal);
        double extraDiscounts = breakdown.getTotalDiscount();
        double totalSavings = mrpDiscount + extraDiscounts;

        lblTotalMrp.setText(CurrencyUtil.format(totalMrp));
        lblProductDiscount.setText("- " + CurrencyUtil.format(mrpDiscount));
        lblCouponDiscount.setText("- " + CurrencyUtil.format(extraDiscounts));

        if (breakdown.getShippingTotal() <= 0.001) {
            lblDeliveryCharges.setText("FREE");
            lblDeliveryCharges.setForeground(UITheme.SUCCESS);
        } else {
            lblDeliveryCharges.setText(CurrencyUtil.format(breakdown.getShippingTotal()));
            lblDeliveryCharges.setForeground(UITheme.TEXT_PRIMARY);
        }

        lblTotalPayable.setText(CurrencyUtil.format(breakdown.getGrandTotal()));
        lblFooterTotal.setText(CurrencyUtil.format(breakdown.getGrandTotal()));
        lblSavingsCallout.setText(String.format("🎉 You will save %s on this order!", CurrencyUtil.format(totalSavings)));

        // Update Coupon Chips
        activeCouponChipPanel.removeAll();
        DiscountStrategy activeCoupon = cartService.getActiveCoupon();
        if (activeCoupon != null) {
            ModernBadge activeChip = new ModernBadge("✓ " + activeCoupon.getCode() + " Applied (-" + CurrencyUtil.format(breakdown.getCouponDiscount()) + ") ✕", ModernBadge.Type.PROMO);
            activeChip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            activeChip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cartService.removeActiveCoupon();
                    lblPromoFeedback.setText("Coupon removed.");
                    lblPromoFeedback.setForeground(UITheme.TEXT_MUTED);
                    refreshCart();
                }
            });
            activeCouponChipPanel.add(activeChip);
        } else {
            // Suggest coupons
            for (DiscountStrategy s : cartService.getAvailableCoupons()) {
                ModernBadge chip = new ModernBadge(s.getCode(), ModernBadge.Type.CATEGORY);
                chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
                chip.setToolTipText(s.getDescription());
                chip.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleApplyCoupon(s.getCode());
                    }
                });
                activeCouponChipPanel.add(chip);
            }
        }
        activeCouponChipPanel.revalidate();
        activeCouponChipPanel.repaint();
    }

    private void handleApplyCoupon(String code) {
        String err = cartService.applyCouponCode(code);
        if (err == null) {
            lblPromoFeedback.setText("✓ Coupon " + code.toUpperCase() + " applied successfully!");
            lblPromoFeedback.setForeground(UITheme.SUCCESS);
            txtPromoCode.setText("");
        } else {
            lblPromoFeedback.setText(err);
            lblPromoFeedback.setForeground(UITheme.DANGER);
        }
        refreshCart();
    }
}
