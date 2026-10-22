package com.shoppingcart.ui;

import com.shoppingcart.model.CartItem;
import com.shoppingcart.model.Order;
import com.shoppingcart.model.User;
import com.shoppingcart.service.CartPricingBreakdown;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.CreditCardPayment;
import com.shoppingcart.service.PaymentProcessor;
import com.shoppingcart.service.UpiPayment;
import com.shoppingcart.ui.components.ModernButton;
import com.shoppingcart.ui.components.ModernTextField;
import com.shoppingcart.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Realistic consumer-grade multi-step checkout dialog inspired by Flipkart & Amazon:
 * - Step 1: Delivery Address Form (Name, Phone, PIN Code, City, House/Street)
 * - Step 2: Payment Gateway Selection (UPI / QR Code, Credit/Debit Card, Net Banking, Cash on Delivery)
 * - Step 3: One-Click Simulated Payment Confirmation with animated checkmark and itemized receipt popup.
 */
public class CheckoutDialog extends JDialog {
    private final JFrame parentFrame;
    private final CartService cartService;
    private final Consumer<Order> onOrderFinalized;

    // Multi-step Card Layout
    private CardLayout cardLayout;
    private JPanel stepsContainer;
    private int currentStep = 1;

    // Header step indicator labels
    private JLabel step1Label;
    private JLabel step2Label;
    private JLabel step3Label;

    // Step 1: Address Fields
    private ModernTextField txtName;
    private ModernTextField txtPhone;
    private ModernTextField txtPincode;
    private ModernTextField txtCity;
    private ModernTextField txtState;
    private ModernTextField txtHouse;
    private ModernTextField txtStreet;
    private JRadioButton rbHome;
    private JRadioButton rbWork;
    private JLabel lblAddressError;

    // Step 2: Payment Gateway
    private JRadioButton rbUpi;
    private JRadioButton rbCard;
    private JRadioButton rbNetBanking;
    private JRadioButton rbCod;

    private JPanel paymentDetailsCardPanel;
    private CardLayout paymentDetailsLayout;

    // UPI details
    private ModernTextField txtUpiId;

    // Card details
    private ModernTextField txtCardHolder;
    private ModernTextField txtCardNumber;
    private ModernTextField txtExpiry;
    private ModernTextField txtCvv;

    // Net Banking
    private JComboBox<String> cmbBank;

    // COD
    private ModernTextField txtCodCaptcha;
    private JLabel lblCaptchaTarget;

    // Step 3: Confirmation elements
    private JPanel step3Content;
    private JLabel lblConfirmedOrderId;
    private JLabel lblDeliveryEst;
    private JLabel lblConfirmedAddress;
    private JLabel lblConfirmedAmount;
    private Order finalizedOrder = null;

    public CheckoutDialog(JFrame parent, CartService cartService, Consumer<Order> onOrderFinalized) {
        super(parent, "FlipMart Secure Checkout", true);
        this.parentFrame = parent;
        this.cartService = cartService;
        this.onOrderFinalized = onOrderFinalized;

        initUI();
    }

    private void initUI() {
        setUndecorated(true);
        setSize(880, 680);
        setLocationRelativeTo(parentFrame);

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
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // TOP: FlipMart Brand Header + Close
        JPanel topHeader = createTopHeader();
        root.add(topHeader, BorderLayout.NORTH);

        // STEPPER PROGRESS BAR
        JPanel stepperBar = createStepperProgressBar();

        // MAIN CONTENT WRAPPER
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(UITheme.BG_CANVAS);
        mainWrapper.add(stepperBar, BorderLayout.NORTH);

        // STEPS CARD CONTAINER
        cardLayout = new CardLayout();
        stepsContainer = new JPanel(cardLayout);
        stepsContainer.setOpaque(false);

        // STEP 1: Address Form
        JPanel step1Panel = createStep1AddressPanel();
        stepsContainer.add(step1Panel, "STEP_1");

        // STEP 2: Payment Method
        JPanel step2Panel = createStep2PaymentPanel();
        stepsContainer.add(step2Panel, "STEP_2");

        // STEP 3: Animated Confirmation & Receipt
        JPanel step3Panel = createStep3ConfirmationPanel();
        stepsContainer.add(step3Panel, "STEP_3");

        mainWrapper.add(stepsContainer, BorderLayout.CENTER);
        root.add(mainWrapper, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createTopHeader() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(880, 56));
        bar.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel title = new JLabel("🔒 FlipMart Express Secure Checkout");
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(Color.WHITE);

        ModernButton btnClose = new ModernButton("✕", ModernButton.Style.GHOST);
        btnClose.setPreferredSize(new Dimension(36, 32));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.addActionListener(e -> dispose());

        bar.add(title, BorderLayout.WEST);
        bar.add(btnClose, BorderLayout.EAST);
        return bar;
    }

    private JPanel createStepperProgressBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_DEFAULT),
                new EmptyBorder(12, 32, 12, 32)
        ));

        step1Label = createStepBadge("1. DELIVERY ADDRESS", true);
        step2Label = createStepBadge("2. PAYMENT OPTIONS", false);
        step3Label = createStepBadge("3. ORDER CONFIRMATION", false);

        bar.add(step1Label);
        bar.add(step2Label);
        bar.add(step3Label);
        return bar;
    }

    private JLabel createStepBadge(String text, boolean active) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        updateBadgeStyle(label, active);
        return label;
    }

    private void updateBadgeStyle(JLabel label, boolean active) {
        if (active) {
            label.setForeground(UITheme.PRIMARY);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.PRIMARY),
                    new EmptyBorder(0, 0, 6, 0)
            ));
        } else {
            label.setForeground(UITheme.TEXT_MUTED);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        }
    }

    // =========================================================================
    // STEP 1: Delivery Address Form
    // =========================================================================
    private JPanel createStep1AddressPanel() {
        JPanel panel = new JPanel(new BorderLayout(24, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));

        // LEFT: Form Inputs
        JPanel formCard = new JPanel();
        formCard.setBackground(Color.WHITE);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JLabel title = new JLabel("Delivery Address");
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Fields
        txtName = new ModernTextField("Full Name (e.g. Alex Morgan)");
        txtName.setText("Alex Morgan");

        txtPhone = new ModernTextField("10-digit Mobile Number (e.g. 9876543210)");
        txtPhone.setText("9876543210");

        txtPincode = new ModernTextField("6-digit PIN Code (e.g. 560001)");
        txtPincode.setText("560001");

        txtCity = new ModernTextField("City (e.g. Bengaluru)");
        txtCity.setText("Bengaluru");

        txtState = new ModernTextField("State (e.g. Karnataka)");
        txtState.setText("Karnataka");

        txtHouse = new ModernTextField("Flat, House no., Building, Apartment");
        txtHouse.setText("Flat 402, Prestige Tower");

        txtStreet = new ModernTextField("Area, Street, Sector, Village");
        txtStreet.setText("100 Feet Road, Indiranagar");

        JPanel row1 = new JPanel(new GridLayout(1, 2, 12, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.add(createFieldWrapper("NAME *", txtName));
        row1.add(createFieldWrapper("10-DIGIT MOBILE NUMBER *", txtPhone));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 12, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.add(createFieldWrapper("PIN CODE *", txtPincode));
        row2.add(createFieldWrapper("CITY / DISTRICT *", txtCity));
        row2.add(createFieldWrapper("STATE *", txtState));

        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.add(createFieldWrapper("FLAT / HOUSE NO. *", txtHouse));
        row3.add(createFieldWrapper("AREA / STREET *", txtStreet));

        // Address type
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        typeRow.setOpaque(false);
        typeRow.setAlignmentX(LEFT_ALIGNMENT);

        rbHome = new JRadioButton("Home (All day delivery)", true);
        rbWork = new JRadioButton("Work (Delivery 9 AM - 6 PM)");
        rbHome.setBackground(Color.WHITE);
        rbWork.setBackground(Color.WHITE);
        rbHome.setFont(UITheme.FONT_BODY);
        rbWork.setFont(UITheme.FONT_BODY);

        ButtonGroup bgType = new ButtonGroup();
        bgType.add(rbHome);
        bgType.add(rbWork);
        typeRow.add(new JLabel("ADDRESS TYPE: "));
        typeRow.add(rbHome);
        typeRow.add(rbWork);

        lblAddressError = new JLabel(" ");
        lblAddressError.setFont(UITheme.FONT_SMALL);
        lblAddressError.setForeground(UITheme.DANGER);
        lblAddressError.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnContinueToPayment = new ModernButton("DELIVER TO THIS ADDRESS ➔", ModernButton.Style.BUY_NOW);
        btnContinueToPayment.setPreferredSize(new Dimension(280, 44));
        btnContinueToPayment.setAlignmentX(LEFT_ALIGNMENT);
        btnContinueToPayment.addActionListener(e -> validateAndProceedToStep2());

        formCard.add(title);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(row1);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(row2);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(row3);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(typeRow);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(lblAddressError);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(btnContinueToPayment);

        panel.add(formCard, BorderLayout.CENTER);

        // RIGHT: Order Mini Summary
        JPanel summaryCard = createOrderSummarySidebar();
        panel.add(summaryCard, BorderLayout.EAST);

        return panel;
    }

    private void validateAndProceedToStep2() {
        if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() ||
            txtPincode.getText().trim().isEmpty() || txtCity.getText().trim().isEmpty() ||
            txtHouse.getText().trim().isEmpty()) {
            lblAddressError.setText("⚠️ Please fill in all required delivery fields.");
            return;
        }

        lblAddressError.setText(" ");
        currentStep = 2;
        updateBadgeStyle(step1Label, false);
        updateBadgeStyle(step2Label, true);
        cardLayout.show(stepsContainer, "STEP_2");
    }

    // =========================================================================
    // STEP 2: Payment Gateway Selection (UPI, Card, Net Banking, COD)
    // =========================================================================
    private JPanel createStep2PaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(24, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));

        // LEFT: Payment Selection & Sub-forms
        JPanel leftContainer = new JPanel();
        leftContainer.setBackground(Color.WHITE);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("Select Payment Mode");
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Radio Buttons
        rbUpi = new JRadioButton("UPI (Google Pay, PhonePe, Paytm, QR Code)", true);
        rbCard = new JRadioButton("Credit / Debit / ATM Card (Visa, MasterCard, RuPay)");
        rbNetBanking = new JRadioButton("Net Banking (All Indian Major Banks)");
        rbCod = new JRadioButton("Cash on Delivery (Pay at Doorstep)");

        ButtonGroup bgPay = new ButtonGroup();
        bgPay.add(rbUpi);
        bgPay.add(rbCard);
        bgPay.add(rbNetBanking);
        bgPay.add(rbCod);

        stylePaymentRadio(rbUpi);
        stylePaymentRadio(rbCard);
        stylePaymentRadio(rbNetBanking);
        stylePaymentRadio(rbCod);

        // Subforms CardPanel
        paymentDetailsLayout = new CardLayout();
        paymentDetailsCardPanel = new JPanel(paymentDetailsLayout);
        paymentDetailsCardPanel.setOpaque(false);
        paymentDetailsCardPanel.setAlignmentX(LEFT_ALIGNMENT);
        paymentDetailsCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UITheme.BORDER_LIGHT),
                new EmptyBorder(12, 10, 12, 10)
        ));

        // Form 1: UPI
        paymentDetailsCardPanel.add(createUpiSubform(), "UPI");
        // Form 2: Card
        paymentDetailsCardPanel.add(createCardSubform(), "CARD");
        // Form 3: Net Banking
        paymentDetailsCardPanel.add(createNetBankingSubform(), "NET_BANKING");
        // Form 4: COD
        paymentDetailsCardPanel.add(createCodSubform(), "COD");

        rbUpi.addActionListener(e -> paymentDetailsLayout.show(paymentDetailsCardPanel, "UPI"));
        rbCard.addActionListener(e -> paymentDetailsLayout.show(paymentDetailsCardPanel, "CARD"));
        rbNetBanking.addActionListener(e -> paymentDetailsLayout.show(paymentDetailsCardPanel, "NET_BANKING"));
        rbCod.addActionListener(e -> paymentDetailsLayout.show(paymentDetailsCardPanel, "COD"));

        // Bottom Actions
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(LEFT_ALIGNMENT);

        ModernButton btnBack = new ModernButton("← Back to Address", ModernButton.Style.SECONDARY);
        btnBack.setPreferredSize(new Dimension(160, 42));
        btnBack.addActionListener(e -> {
            currentStep = 1;
            updateBadgeStyle(step1Label, true);
            updateBadgeStyle(step2Label, false);
            cardLayout.show(stepsContainer, "STEP_1");
        });

        CartPricingBreakdown bd = cartService.calculateBreakdown();
        ModernButton btnPayOrder = new ModernButton("PAY & PLACE ORDER (" + CurrencyUtil.format(bd.getGrandTotal()) + ") ⚡", ModernButton.Style.BUY_NOW);
        btnPayOrder.setPreferredSize(new Dimension(280, 42));
        btnPayOrder.addActionListener(e -> executePaymentSimulation());

        actionRow.add(btnBack);
        actionRow.add(btnPayOrder);

        leftContainer.add(title);
        leftContainer.add(Box.createVerticalStrut(12));
        leftContainer.add(rbUpi);
        leftContainer.add(rbCard);
        leftContainer.add(rbNetBanking);
        leftContainer.add(rbCod);
        leftContainer.add(Box.createVerticalStrut(10));
        leftContainer.add(paymentDetailsCardPanel);
        leftContainer.add(Box.createVerticalStrut(14));
        leftContainer.add(actionRow);

        panel.add(leftContainer, BorderLayout.CENTER);

        // RIGHT: Order Mini Summary
        JPanel summaryCard = createOrderSummarySidebar();
        panel.add(summaryCard, BorderLayout.EAST);

        return panel;
    }

    private void stylePaymentRadio(JRadioButton rb) {
        rb.setBackground(Color.WHITE);
        rb.setFont(UITheme.FONT_BODY_BOLD);
        rb.setForeground(UITheme.TEXT_PRIMARY);
        rb.setAlignmentX(LEFT_ALIGNMENT);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rb.setBorder(new EmptyBorder(6, 4, 6, 4));
    }

    private JPanel createUpiSubform() {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 0));
        p.setOpaque(false);

        // QR simulation box
        JPanel qrBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UITheme.BORDER_DEFAULT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Simulated QR graphic pattern
                g2.setColor(new Color(30, 41, 59));
                int sz = 70;
                int startX = (getWidth() - sz) / 2;
                int startY = 16;
                g2.fillRect(startX, startY, sz, sz);
                g2.setColor(Color.WHITE);
                g2.fillRect(startX + 8, startY + 8, sz - 16, sz - 16);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRect(startX + 18, startY + 18, sz - 36, sz - 36);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        qrBox.setOpaque(false);
        qrBox.setPreferredSize(new Dimension(180, 140));

        JLabel qrNote = new JLabel("<html><center>Scan with Any UPI App<br><b>GPay • PhonePe • Paytm</b></center></html>", JLabel.CENTER);
        qrNote.setFont(UITheme.FONT_SMALL);
        qrNote.setForeground(UITheme.TEXT_MUTED);
        qrNote.setBorder(new EmptyBorder(96, 4, 8, 4));
        qrBox.add(qrNote, BorderLayout.SOUTH);

        // VPA Input
        JPanel vpaBox = new JPanel();
        vpaBox.setOpaque(false);
        vpaBox.setLayout(new BoxLayout(vpaBox, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("Or enter your UPI ID / VPA:");
        lbl.setFont(UITheme.FONT_BODY);

        txtUpiId = new ModernTextField("username@okhdfcbank");
        txtUpiId.setText("alexmorgan@okaxis");

        JLabel hint = new JLabel("A collect request will be sent to your UPI app.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);

        vpaBox.add(Box.createVerticalStrut(10));
        vpaBox.add(lbl);
        vpaBox.add(Box.createVerticalStrut(6));
        vpaBox.add(txtUpiId);
        vpaBox.add(Box.createVerticalStrut(4));
        vpaBox.add(hint);

        p.add(qrBox);
        p.add(vpaBox);
        return p;
    }

    private JPanel createCardSubform() {
        JPanel p = new JPanel(new GridLayout(2, 2, 12, 10));
        p.setOpaque(false);

        txtCardNumber = new ModernTextField("16-digit Card Number");
        txtCardNumber.setText("4532 8901 2345 6789");

        txtCardHolder = new ModernTextField("Cardholder Name");
        txtCardHolder.setText("Alex Morgan");

        txtExpiry = new ModernTextField("MM/YY");
        txtExpiry.setText("11/28");

        txtCvv = new ModernTextField("CVV (3 digits)");
        txtCvv.setText("992");

        p.add(createFieldWrapper("CARD NUMBER", txtCardNumber));
        p.add(createFieldWrapper("NAME ON CARD", txtCardHolder));
        p.add(createFieldWrapper("EXPIRY DATE", txtExpiry));
        p.add(createFieldWrapper("CVV", txtCvv));
        return p;
    }

    private JPanel createNetBankingSubform() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        p.setOpaque(false);

        JLabel lbl = new JLabel("Select Popular Indian Bank: ");
        lbl.setFont(UITheme.FONT_BODY);

        cmbBank = new JComboBox<>(new String[]{
                "HDFC Bank - Instant NetBanking",
                "State Bank of India (SBI)",
                "ICICI Bank - FastPay",
                "Axis Bank Internet Banking",
                "Kotak Mahindra Bank",
                "Punjab National Bank"
        });
        cmbBank.setFont(UITheme.FONT_BODY);
        cmbBank.setPreferredSize(new Dimension(320, 36));

        p.add(lbl);
        p.add(cmbBank);
        return p;
    }

    private JPanel createCodSubform() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        p.setOpaque(false);

        JLabel lbl = new JLabel("Enter verification code: ");
        lbl.setFont(UITheme.FONT_BODY);

        lblCaptchaTarget = new JLabel(" 5 9 3 ");
        lblCaptchaTarget.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblCaptchaTarget.setOpaque(true);
        lblCaptchaTarget.setBackground(new Color(230, 235, 245));
        lblCaptchaTarget.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT));

        txtCodCaptcha = new ModernTextField("Enter code", 6);
        txtCodCaptcha.setText("593");
        txtCodCaptcha.setPreferredSize(new Dimension(100, 34));

        p.add(lbl);
        p.add(lblCaptchaTarget);
        p.add(txtCodCaptcha);
        return p;
    }

    // =========================================================================
    // STEP 3: Animated Confirmation & Receipt Generation
    // =========================================================================
    private JPanel createStep3ConfirmationPanel() {
        step3Content = new JPanel(new BorderLayout());
        step3Content.setOpaque(false);
        step3Content.setBorder(new EmptyBorder(30, 40, 30, 40));
        return step3Content;
    }

    private void executePaymentSimulation() {
        // Build User object
        String userAddr = String.format("%s, %s, %s, %s - %s",
                txtHouse.getText().trim(), txtStreet.getText().trim(),
                txtCity.getText().trim(), txtState.getText().trim(), txtPincode.getText().trim());

        User customer = new User("USR-2026-01", txtName.getText().trim(),
                "alex.morgan@flipmart.in", userAddr, "VIP Diamond");

        // Determine payment method & processor
        PaymentProcessor processor;
        Map<String, String> creds = new HashMap<>();
        String paymentMethodName;

        if (rbCard.isSelected()) {
            processor = new CreditCardPayment();
            paymentMethodName = "Credit Card (Visa ending in " + txtCardNumber.getText().trim().substring(Math.max(0, txtCardNumber.getText().trim().length() - 4)) + ")";
            creds.put("cardHolder", txtCardHolder.getText().trim());
            creds.put("cardNumber", txtCardNumber.getText().replaceAll("\\s+", ""));
            creds.put("expiry", txtExpiry.getText().trim());
            creds.put("cvv", txtCvv.getText().trim());
        } else if (rbNetBanking.isSelected()) {
            processor = new UpiPayment(); // Simulated gateway
            paymentMethodName = cmbBank.getSelectedItem() != null ? cmbBank.getSelectedItem().toString() : "Net Banking";
            creds.put("upiId", "netbanking@bank");
        } else if (rbCod.isSelected()) {
            processor = new UpiPayment(); // Simulated COD
            paymentMethodName = "Cash on Delivery (Pay at Doorstep)";
            creds.put("upiId", "cod@doorstep");
        } else {
            processor = new UpiPayment();
            paymentMethodName = "UPI (" + txtUpiId.getText().trim() + ")";
            creds.put("upiId", txtUpiId.getText().trim());
        }

        // Show Processing Screen first
        currentStep = 3;
        updateBadgeStyle(step2Label, false);
        updateBadgeStyle(step3Label, true);
        renderProcessingState(paymentMethodName);
        cardLayout.show(stepsContainer, "STEP_3");

        // Simulate banking network round-trip (1.2 seconds) with Timer
        Timer timer = new Timer(1200, evt -> {
            CartService.CheckoutExecutionResult res = cartService.processCheckout(customer, processor, creds);
            if (res.isSuccess()) {
                finalizedOrder = res.getOrder();
                renderSuccessConfirmationCard(finalizedOrder);
                if (onOrderFinalized != null) {
                    onOrderFinalized.accept(finalizedOrder);
                }
            } else {
                renderErrorConfirmationCard(res.getMessage());
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void renderProcessingState(String method) {
        step3Content.removeAll();

        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(60, 40, 60, 40)
        ));

        JLabel spinner = new JLabel("⚡", JLabel.CENTER);
        spinner.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));
        spinner.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Contacting Payment Gateway...", JLabel.CENTER);
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Authorizing transaction securely via " + method, JLabel.CENTER);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        pb.setPreferredSize(new Dimension(300, 8));
        pb.setMaximumSize(new Dimension(300, 8));
        pb.setAlignmentX(CENTER_ALIGNMENT);

        p.add(spinner);
        p.add(Box.createVerticalStrut(16));
        p.add(title);
        p.add(Box.createVerticalStrut(8));
        p.add(sub);
        p.add(Box.createVerticalStrut(24));
        p.add(pb);

        step3Content.add(p, BorderLayout.CENTER);
        step3Content.revalidate();
        step3Content.repaint();
    }

    private void renderSuccessConfirmationCard(Order order) {
        step3Content.removeAll();

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(165, 214, 167), 1, true),
                new EmptyBorder(30, 36, 30, 36)
        ));

        // Animated Green Checkmark
        JLabel checkmark = new JLabel("✓", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SUCCESS_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.SUCCESS);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        checkmark.setFont(new Font("Segoe UI", Font.BOLD, 42));
        checkmark.setForeground(UITheme.SUCCESS);
        checkmark.setPreferredSize(new Dimension(74, 74));
        checkmark.setMaximumSize(new Dimension(74, 74));
        checkmark.setAlignmentX(CENTER_ALIGNMENT);

        JLabel celebration = new JLabel("Order Placed Successfully on FlipMart!", JLabel.CENTER);
        celebration.setFont(UITheme.FONT_HEADER_LARGE);
        celebration.setForeground(UITheme.SUCCESS);
        celebration.setAlignmentX(CENTER_ALIGNMENT);

        lblConfirmedOrderId = new JLabel("Order ID: " + order.getOrderId() + " • Paid via " + order.getPaymentMethod(), JLabel.CENTER);
        lblConfirmedOrderId.setFont(UITheme.FONT_BODY_BOLD);
        lblConfirmedOrderId.setForeground(UITheme.TEXT_PRIMARY);
        lblConfirmedOrderId.setAlignmentX(CENTER_ALIGNMENT);

        lblDeliveryEst = new JLabel("⚡ Estimated Delivery: Tomorrow, by 9:00 PM", JLabel.CENTER);
        lblDeliveryEst.setFont(UITheme.FONT_BODY_BOLD);
        lblDeliveryEst.setForeground(UITheme.PRIMARY);
        lblDeliveryEst.setAlignmentX(CENTER_ALIGNMENT);

        lblConfirmedAddress = new JLabel("📍 Shipping to: " + order.getUser().getShippingAddress(), JLabel.CENTER);
        lblConfirmedAddress.setFont(UITheme.FONT_SMALL);
        lblConfirmedAddress.setForeground(UITheme.TEXT_MUTED);
        lblConfirmedAddress.setAlignmentX(CENTER_ALIGNMENT);

        lblConfirmedAmount = new JLabel("Total Paid: " + CurrencyUtil.format(order.getGrandTotal()), JLabel.CENTER);
        lblConfirmedAmount.setFont(UITheme.FONT_PRICE_LARGE);
        lblConfirmedAmount.setForeground(UITheme.TEXT_PRICE);
        lblConfirmedAmount.setAlignmentX(CENTER_ALIGNMENT);

        // Buttons: View Receipt & Continue Shopping
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        ModernButton btnReceipt = new ModernButton("📄 View & Download Invoice", ModernButton.Style.PRIMARY);
        btnReceipt.setPreferredSize(new Dimension(240, 44));
        btnReceipt.addActionListener(e -> {
            ReceiptDialog rd = new ReceiptDialog(parentFrame, order);
            rd.setVisible(true);
        });

        ModernButton btnClose = new ModernButton("Continue Shopping ➔", ModernButton.Style.BUY_NOW);
        btnClose.setPreferredSize(new Dimension(200, 44));
        btnClose.addActionListener(e -> dispose());

        btnRow.add(btnReceipt);
        btnRow.add(btnClose);

        card.add(checkmark);
        card.add(Box.createVerticalStrut(14));
        card.add(celebration);
        card.add(Box.createVerticalStrut(6));
        card.add(lblConfirmedOrderId);
        card.add(Box.createVerticalStrut(4));
        card.add(lblDeliveryEst);
        card.add(Box.createVerticalStrut(4));
        card.add(lblConfirmedAddress);
        card.add(Box.createVerticalStrut(12));
        card.add(lblConfirmedAmount);
        card.add(Box.createVerticalStrut(20));
        card.add(btnRow);

        step3Content.add(card, BorderLayout.CENTER);
        step3Content.revalidate();
        step3Content.repaint();
    }

    private void renderErrorConfirmationCard(String errorMsg) {
        step3Content.removeAll();

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.DANGER, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel icon = new JLabel("⚠️", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Payment Failed / Interrupted", JLabel.CENTER);
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(UITheme.DANGER);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel msg = new JLabel(errorMsg != null ? errorMsg : "Transaction was declined by bank gateway.", JLabel.CENTER);
        msg.setFont(UITheme.FONT_BODY);
        msg.setForeground(UITheme.TEXT_MUTED);
        msg.setAlignmentX(CENTER_ALIGNMENT);

        ModernButton btnRetry = new ModernButton("Try Again", ModernButton.Style.PRIMARY);
        btnRetry.setAlignmentX(CENTER_ALIGNMENT);
        btnRetry.addActionListener(e -> {
            currentStep = 2;
            updateBadgeStyle(step2Label, true);
            updateBadgeStyle(step3Label, false);
            cardLayout.show(stepsContainer, "STEP_2");
        });

        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(msg);
        card.add(Box.createVerticalStrut(16));
        card.add(btnRetry);

        step3Content.add(card, BorderLayout.CENTER);
        step3Content.revalidate();
        step3Content.repaint();
    }

    private JPanel createOrderSummarySidebar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(280, 500));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("ORDER SUMMARY");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        title.setForeground(UITheme.TEXT_MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(10));

        List<CartItem> items = cartService.getCartRepository().getItems();
        for (CartItem item : items) {
            JLabel itm = new JLabel("• " + item.getQuantity() + "x " + item.getProduct().getName());
            itm.setFont(UITheme.FONT_SMALL);
            itm.setForeground(UITheme.TEXT_PRIMARY);
            itm.setAlignmentX(LEFT_ALIGNMENT);
            card.add(itm);
            card.add(Box.createVerticalStrut(3));
        }

        card.add(Box.createVerticalStrut(10));
        JPanel div = new JPanel();
        div.setBackground(UITheme.BORDER_LIGHT);
        div.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        card.add(div);
        card.add(Box.createVerticalStrut(10));

        CartPricingBreakdown bd = cartService.calculateBreakdown();
        card.add(createMiniRow("Total MRP", CurrencyUtil.format(bd.getSubtotal())));
        card.add(Box.createVerticalStrut(4));
        card.add(createMiniRow("Total Discount", "- " + CurrencyUtil.format(bd.getTotalDiscount())));
        card.add(Box.createVerticalStrut(4));
        card.add(createMiniRow("Delivery Charges", "FREE"));
        card.add(Box.createVerticalStrut(8));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblT = new JLabel("Amount Payable");
        lblT.setFont(UITheme.FONT_BODY_BOLD);
        JLabel lblV = new JLabel(CurrencyUtil.format(bd.getGrandTotal()));
        lblV.setFont(UITheme.FONT_PRICE_LARGE);
        lblV.setForeground(UITheme.TEXT_PRICE);
        totalRow.add(lblT, BorderLayout.WEST);
        totalRow.add(lblV, BorderLayout.EAST);
        card.add(totalRow);

        card.add(Box.createVerticalGlue());

        JLabel badge = new JLabel("🛡 100% Purchase Protection", JLabel.CENTER);
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(UITheme.SUCCESS);
        badge.setAlignmentX(CENTER_ALIGNMENT);
        card.add(badge);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMiniRow(String label, String value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_SMALL);
        v.setForeground(UITheme.TEXT_PRIMARY);
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private JPanel createFieldWrapper(String labelText, ModernTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(labelText);
        l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);

        field.setAlignmentX(LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }
}
