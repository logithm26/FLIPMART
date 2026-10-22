package com.shoppingcart.util;

import com.shoppingcart.model.Category;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility to pre-generate high-definition square 1:1 product visual assets for FlipMart.
 */
public class AssetGenerator {

    public static void main(String[] args) {
        generateAllAssetsIfMissing();
        System.out.println("All FlipMart product assets generated successfully!");
    }

    public static void generateAllAssetsIfMissing() {
        File assetsDir = new File("assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }

        // --- 1. Electronics & Gadgets ---
        generateAsset("assets/aurasound_headphones.png", "AuraSound Max ANC", Category.ELECTRONICS, "🎧", "Wireless Noise-Cancelling");
        generateAsset("assets/apex_keyboard.png", "Apex Pro Keyboard", Category.ELECTRONICS, "⌨️", "Hot-Swap RGB Mechanical");
        generateAsset("assets/titan_monitor.png", "Titan 4K Curved Display", Category.ELECTRONICS, "🖥️", "165Hz Nano-IPS Gaming");
        generateAsset("assets/pulsefit_watch.png", "PulseFit Ultra GPS", Category.ELECTRONICS, "⌚", "Titanium Aerospace Smartwatch");
        generateAsset("assets/lumix_webcam.png", "Lumix Pro 4K Cam", Category.ELECTRONICS, "📹", "4K 60FPS Streaming Studio");

        // --- 2. Fashion & Apparel ---
        generateAsset("assets/mens_slim_shirt.png", "Men's Slim Fit Shirt", Category.FASHION, "👔", "100% Oxford Breathable Cotton");
        generateAsset("assets/womens_running_shoes.png", "Women's Running Shoes", Category.FASHION, "👟", "Carbon Plate Cushion Midsole");
        generateAsset("assets/polarized_sunglasses.png", "Polarized Sunglasses", Category.FASHION, "🕶️", "Titanium UV400 Aviator");
        generateAsset("assets/cyberweave_hoodie.png", "CyberWeave Tech Hoodie", Category.FASHION, "🧥", "Merino Fleece • DWR Shell");
        generateAsset("assets/urban_joggers.png", "Urban Nomad Cargo Pants", Category.FASHION, "👖", "6-Pocket Tactical Ripstop");
        generateAsset("assets/aerostrobe_jacket.png", "AeroStrobe Windbreaker", Category.FASHION, "🦺", "3M Reflective Ultralight");

        // --- 3. Home & Kitchen ---
        generateAsset("assets/electric_kettle.png", "Stainless Steel Kettle", Category.HOME_KITCHEN, "🫖", "1.8L Fast Boil • 304 Steel");
        generateAsset("assets/digital_air_fryer.png", "Digital Air Fryer", Category.HOME_KITCHEN, "🍟", "5.5L 360° Rapid Touchscreen");
        generateAsset("assets/ceramic_mug_set.png", "Ceramic Coffee Mug Set", Category.HOME_KITCHEN, "☕", "Set of 6 Handcrafted Stoneware");
        generateAsset("assets/espresso_machine.png", "Barista Espresso Maker", Category.HOME_KITCHEN, "☕", "15-Bar Italian Pump & Frother");

        // --- 4. Books & Essentials ---
        generateAsset("assets/bestseller_books.png", "Hardcover Novels Set", Category.BOOKS_ESSENTIALS, "📚", "Collector's 5-Volume Boxed Set");
        generateAsset("assets/ergonomic_laptop_stand.png", "Aluminum Laptop Stand", Category.BOOKS_ESSENTIALS, "💻", "360° Rotating Dual-Hinge Riser");
        generateAsset("assets/multiport_charger.png", "140W GaN Fast Charger", Category.BOOKS_ESSENTIALS, "🔌", "3x USB-C + 1x USB-A PD 3.1");
        generateAsset("assets/desk_organizer.png", "Desk Organizer Hub", Category.BOOKS_ESSENTIALS, "🗄️", "Magnetic Cable Dock & Stand");

        // --- 5. Digital & Software ---
        generateAsset("assets/devmaster_ide.png", "DevMaster Cloud IDE", Category.DIGITAL, "💻", "Enterprise Cloud Sync License");
        generateAsset("assets/ai_masterclass.png", "AI Architecture 2026", Category.DIGITAL, "🎓", "60 Hours Video Curriculum");
        generateAsset("assets/synthwave_audio.png", "SynthWave Cyber Audio", Category.DIGITAL, "🎵", "1,200 Loops Royalty-Free");
    }

    private static void generateAsset(String filePath, String title, Category category, String emoji, String subtitle) {
        File file = new File(filePath);
        if (file.exists() && file.length() > 2000) {
            return; // Already generated
        }

        int width = 450;
        int height = 450;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Gradient Colors by Category
        Color c1, c2, accent;
        Category cat = category != null ? category : Category.ALL;
        switch (cat) {
            case ELECTRONICS -> {
                c1 = new Color(15, 23, 42); // Slate 900
                c2 = new Color(30, 41, 59); // Slate 800
                accent = new Color(56, 189, 248); // Electric Cyan
            }
            case FASHION -> {
                c1 = new Color(36, 17, 39);
                c2 = new Color(60, 27, 66);
                accent = new Color(244, 114, 182); // Rose Pink
            }
            case HOME_KITCHEN -> {
                c1 = new Color(39, 24, 15);
                c2 = new Color(70, 39, 21);
                accent = new Color(251, 146, 60); // Warm Copper Orange
            }
            case BOOKS_ESSENTIALS -> {
                c1 = new Color(17, 33, 45);
                c2 = new Color(25, 52, 72);
                accent = new Color(234, 179, 8); // Gold
            }
            case DIGITAL -> {
                c1 = new Color(10, 32, 28);
                c2 = new Color(18, 56, 49);
                accent = new Color(52, 211, 153); // Emerald Mint
            }
            default -> {
                c1 = new Color(21, 30, 46);
                c2 = new Color(11, 15, 25);
                accent = new Color(99, 102, 241);
            }
        }

        // Background
        g2.setPaint(new GradientPaint(0, 0, c1, width, height, c2));
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 20, 20));

        // Soft Radial Glow
        Point2D center = new Point2D.Float(width / 2f, height * 0.44f);
        float radius = 200f;
        RadialGradientPaint glow = new RadialGradientPaint(center, radius,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 75),
                            new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 0)});
        g2.setPaint(glow);
        g2.fillOval((int)(center.getX() - radius), (int)(center.getY() - radius), (int)(radius * 2), (int)(radius * 2));

        // Grid lines
        g2.setColor(new Color(255, 255, 255, 14));
        g2.setStroke(new BasicStroke(1.0f));
        for (int x = 25; x < width; x += 40) {
            g2.drawLine(x, 20, x, height - 20);
        }
        for (int y = 25; y < height; y += 40) {
            g2.drawLine(20, y, width - 20, y);
        }

        // Central Circle Emblem Badge
        int emblemSize = 150;
        int emblemX = (width - emblemSize) / 2;
        int emblemY = 85;

        g2.setColor(new Color(11, 15, 25, 235));
        g2.fillOval(emblemX, emblemY, emblemSize, emblemSize);
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawOval(emblemX, emblemY, emblemSize, emblemSize);

        // Center Emoji
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        int ew = g2.getFontMetrics().stringWidth(emoji);
        int eh = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        g2.drawString(emoji, emblemX + (emblemSize - ew) / 2, emblemY + (emblemSize + eh) / 2);

        // Category Tag Badge at Top-Left
        String catName = cat.getDisplayName().toUpperCase();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        int badgeW = g2.getFontMetrics().stringWidth(catName) + 22;
        g2.setColor(new Color(15, 23, 42, 230));
        g2.fillRoundRect(20, 20, badgeW, 26, 13, 13);
        g2.setColor(accent);
        g2.drawRoundRect(20, 20, badgeW, 26, 13, 13);
        g2.setColor(Color.WHITE);
        g2.drawString(catName, 31, 37);

        // FlipMart Brand watermark at top-right
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(255, 255, 255, 130));
        g2.drawString("FlipMart", width - 85, 37);

        // Bottom Banner
        int bannerH = 95;
        g2.setColor(new Color(11, 17, 30, 235));
        g2.fillRect(0, height - bannerH, width, bannerH);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawLine(0, height - bannerH, width, height - bannerH);

        // Title in Banner
        g2.setFont(new Font("Segoe UI", Font.BOLD, 19));
        g2.setColor(new Color(248, 250, 252));
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (width - tw) / 2, height - bannerH + 36);

        // Subtitle in Banner
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(new Color(148, 163, 184));
        int sw = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (width - sw) / 2, height - bannerH + 66);

        g2.dispose();

        try {
            ImageIO.write(img, "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
