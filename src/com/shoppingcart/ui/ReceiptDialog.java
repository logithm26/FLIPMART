package com.shoppingcart.ui;

import com.shoppingcart.model.Order;
import com.shoppingcart.service.ReceiptService;
import com.shoppingcart.ui.components.ModernButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

/**
 * Dialog displaying FlipMart confirmed order receipt with itemized breakdown and file export features.
 */
public class ReceiptDialog extends JDialog {
    private final Order order;
    private final ReceiptService receiptService = new ReceiptService();
    private JTextArea receiptTextArea;

    public ReceiptDialog(JFrame parent, Order order) {
        super(parent, "FlipMart - Order Confirmation & Invoice", true);
        this.order = order;

        initUI();
        // Automatically save an archival copy into the receipts folder
        File backupFile = new File("receipts", "receipt_" + order.getOrderId() + ".txt");
        receiptService.exportReceiptToFile(order, backupFile);
    }

    private void initUI() {
        setUndecorated(true);
        setSize(700, 660);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_DRAWER);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));
                g2.setColor(UITheme.SUCCESS);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 22, 22));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        // 1. HEADER WITH CELEBRATION ICON
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel iconCheck = new JLabel("✅");
        iconCheck.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconCheck.setAlignmentX(CENTER_ALIGNMENT);

        JLabel successTitle = new JLabel("Payment Confirmed & Order Placed on FlipMart!");
        successTitle.setFont(UITheme.FONT_HEADER_LARGE);
        successTitle.setForeground(UITheme.SUCCESS);
        successTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subNote = new JLabel(String.format("Order Reference: %s • Paid via %s",
                order.getOrderId(), order.getPaymentMethod()));
        subNote.setFont(UITheme.FONT_BODY);
        subNote.setForeground(UITheme.TEXT_SECONDARY);
        subNote.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(iconCheck);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(successTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subNote);

        root.add(headerPanel, BorderLayout.NORTH);

        // 2. RECEIPT TEXT AREA (MONOSPACE GST INVOICE)
        String receiptText = receiptService.generateTextReceipt(order);
        receiptTextArea = new JTextArea(receiptText);
        receiptTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiptTextArea.setBackground(new Color(248, 250, 252));
        receiptTextArea.setForeground(new Color(33, 37, 41));
        receiptTextArea.setCaretColor(UITheme.PRIMARY);
        receiptTextArea.setEditable(false);
        receiptTextArea.setMargin(new java.awt.Insets(12, 14, 12, 14));

        JScrollPane scrollPane = new JScrollPane(receiptTextArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true));
        root.add(scrollPane, BorderLayout.CENTER);

        // 3. ACTION BUTTONS (EXPORT, COPY, CONTINUE SHOPPING)
        JPanel footerAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerAction.setOpaque(false);

        ModernButton btnCopy = new ModernButton("📋 Copy Invoice", ModernButton.Style.SECONDARY);
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(receiptText), null);
            JOptionPane.showMessageDialog(this, "Tax invoice copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        ModernButton btnExport = new ModernButton("💾 Save Invoice (.txt)", ModernButton.Style.SECONDARY);
        btnExport.addActionListener(e -> handleExportToFile());

        ModernButton btnDone = new ModernButton("Continue Shopping 🛍️", ModernButton.Style.SUCCESS);
        btnDone.addActionListener(e -> dispose());

        footerAction.add(btnCopy);
        footerAction.add(btnExport);
        footerAction.add(btnDone);

        root.add(footerAction, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void handleExportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("flipmart_receipt_" + order.getOrderId() + ".txt"));
        int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            boolean success = receiptService.exportReceiptToFile(order, targetFile);
            if (success) {
                JOptionPane.showMessageDialog(this, "Invoice successfully saved to:\n" + targetFile.getAbsolutePath(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save invoice.", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
