package com.shoppingcart;

import com.shoppingcart.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point for FlipMart - India's Premier Online Shopping Desktop Platform.
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI scaling and optimal subpixel font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.opengl", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default cross-platform styling
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            System.out.println("=================================================");
            System.out.println(" FlipMart E-Commerce Desktop Store Initialized!   ");
            System.out.println(" Platform Features:                              ");
            System.out.println(" - Commercial Flipkart/Amazon E-Commerce Theme   ");
            System.out.println(" - Indian Rupee (₹) & Indian Numbering Standards ");
            System.out.println(" - Multi-Category Amazon-Scale Product Catalog   ");
            System.out.println(" - Resilient Graphics2D Vector Image Fallbacks   ");
            System.out.println(" - Live Cart Drawer with Dynamic Badge Count     ");
            System.out.println(" - Real-time Stock Reservation & Auto-Sync       ");
            System.out.println(" - Multi-Option Payments (Cards, UPI, Web3)      ");
            System.out.println(" - GST Itemized Tax Invoicing & File Export      ");
            System.out.println("=================================================");
        });
    }
}
