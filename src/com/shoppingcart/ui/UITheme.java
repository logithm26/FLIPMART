package com.shoppingcart.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Design system tokens defining FlipMart's commercial retail e-commerce aesthetic
 * inspired by Flipkart, Amazon, and Meesho.
 * Features a clean light-mode canvas, crisp white product cards, Flipkart Blue and Amazon Navy accents,
 * high-contrast pricing hierarchy, and vibrant action states.
 */
public final class UITheme {
    private UITheme() {}

    // --- Core Canvas & Surface Palette (Flipkart / Amazon / Meesho Aesthetic) ---
    public static final Color BG_CANVAS = new Color(241, 243, 246);       // Clean light page canvas (#F1F3F6)
    public static final Color BG_HEADER = new Color(40, 116, 240);        // Top nav header (Flipkart Royal Blue #2874F0)
    public static final Color BG_HEADER_NAVY = new Color(19, 25, 33);     // Amazon Dark Navy (#131921)
    public static final Color BG_SURFACE = new Color(255, 255, 255);      // Pure white product card surface
    public static final Color BG_SURFACE_HOVER = new Color(255, 255, 255);// Hover elevation surface
    public static final Color BG_SURFACE_LIGHT = new Color(248, 249, 250);// Input & subtle chip background
    public static final Color BG_DRAWER = new Color(255, 255, 255);       // Slide-out cart & modal canvas (#FFFFFF)
    public static final Color BG_DRAWER_HEADER = new Color(248, 250, 252);// Drawer header background
    public static final Color BG_DARK = BG_CANVAS;                        // Compatible mapping for legacy background references

    // --- Borders & Outlines ---
    public static final Color BORDER_DEFAULT = new Color(224, 224, 224);   // Subtle 1px card border (#E0E0E0)
    public static final Color BORDER_LIGHT = new Color(240, 240, 240);     // Extra soft divider
    public static final Color BORDER_HOVER = new Color(40, 116, 240);      // Flipkart Blue hover glow
    public static final Color BORDER_FOCUS = new Color(40, 116, 240);      // Focus outline

    // --- Vibrant Action Accents (Flipkart / Amazon / Meesho) ---
    public static final Color PRIMARY = new Color(40, 116, 240);           // Flipkart Royal Blue (#2874F0)
    public static final Color PRIMARY_HOVER = new Color(26, 92, 199);
    public static final Color PRIMARY_LIGHT = new Color(40, 116, 240, 24);

    public static final Color BRAND_YELLOW = new Color(255, 229, 0);       // Flipkart "Explore Plus ✦" Yellow (#FFE500)
    public static final Color ACCENT_YELLOW = BRAND_YELLOW;
    public static final Color ACCENT_ORANGE = new Color(251, 100, 27);     // Flipkart "Buy Now" Orange (#FB641B)
    public static final Color ACCENT_ORANGE_HOVER = new Color(229, 85, 14);

    public static final Color CART_YELLOW = new Color(255, 159, 0);        // "Add to Cart" Vibrant Amber (#FF9F00)
    public static final Color CART_YELLOW_HOVER = new Color(230, 140, 0);

    public static final Color MEESHO_PINK = new Color(244, 51, 151);       // Meesho signature brand pink (#F43397)
    public static final Color WISHLIST_RED = new Color(239, 68, 68);       // Heart favorite red (#EF4444)

    public static final Color SUCCESS = new Color(56, 142, 60);            // Flipkart Green (#388E3C)
    public static final Color SUCCESS_LIGHT = new Color(232, 245, 233);    // Soft green badge background (#E8F5E9)
    public static final Color SUCCESS_BG = new Color(56, 142, 60, 25);
    public static final Color SUCCESS_HOVER = new Color(46, 125, 50);

    public static final Color WARNING = new Color(245, 158, 11);           // Amber Alert
    public static final Color WARNING_BG = new Color(245, 158, 11, 25);

    public static final Color DANGER = new Color(211, 47, 47);             // Discount tag / Sale Rose (#D32F2F)
    public static final Color DANGER_BG = new Color(239, 68, 68, 25);
    public static final Color DANGER_HOVER = new Color(198, 40, 40);

    // --- Typography Colors ---
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);         // High-contrast charcoal text (#212121)
    public static final Color TEXT_SECONDARY = new Color(113, 116, 120);    // Subtle subtitle slate (#717478)
    public static final Color TEXT_MUTED = new Color(135, 135, 135);       // Muted specifications (#878787)
    public static final Color TEXT_PRICE = new Color(17, 24, 39);          // Prominent Rupee Selling Price (#111827)
    public static final Color TEXT_MRP = new Color(135, 135, 135);         // Strike-through MRP (#878787)
    public static final Color TEXT_DISCOUNT = new Color(56, 142, 60);      // Flipkart Savings Green (#388E3C)
    public static final Color TEXT_ON_PRIMARY = new Color(255, 255, 255);  // Crisp white on blue header / buttons

    // --- Typography Fonts (Segoe UI) ---
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_HEADER_LARGE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_PRICE_LARGE = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_PRICE_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_MRP = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font(FONT_FAMILY, Font.BOLD, 11);
}
