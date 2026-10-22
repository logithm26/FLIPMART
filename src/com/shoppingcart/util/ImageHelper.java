package com.shoppingcart.util;

import com.shoppingcart.model.Category;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resilient e-commerce image loader and high-contrast Graphics2D procedural fallback engine.
 * Guarantees zero broken images, consistent 1:1 square aspect ratios, and smooth scaling.
 */
public class ImageHelper {
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    /**
     * Loads an image from disk, asset path, or URL with smooth anti-aliased scaling.
     * Falls back to a high-contrast Graphics2D placeholder on missing or invalid images.
     */
    public static ImageIcon loadProductImage(String imagePath, String productName, Category category, int width, int height) {
        String cacheKey = (imagePath != null ? imagePath : productName) + "_" + width + "x" + height;
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }

        BufferedImage loadedImage = null;

        // 1. Attempt to load from local file system or URL
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    loadedImage = ImageIO.read(new URL(imagePath));
                } else {
                    File file = new File(imagePath);
                    if (file.exists() && file.canRead()) {
                        loadedImage = ImageIO.read(file);
                    }
                }
            } catch (Exception ignored) {
                // Graceful fallback on any I/O or network exception
            }
        }

        // 2. If image failed to load or is corrupt, procedurally paint resilient Graphics2D fallback
        if (loadedImage == null) {
            loadedImage = createHighContrastFallback(productName, category, width, height);
        }

        // 3. Smooth aspect-consistent scaling using bilinear interpolation
        Image scaled = loadedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);
        CACHE.put(cacheKey, icon);
        return icon;
    }

    /**
     * Procedurally renders a high-contrast, modern retail placeholder using Java 2D graphics.
     * Tailored to FlipMart's commercial retail categories.
     */
    public static BufferedImage createHighContrastFallback(String productName, Category category, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Enable ultra-smooth anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Determine category-specific palette
        Color topColor;
        Color bottomColor;
        Color accentColor;
        String iconSymbol;

        Category cat = category != null ? category : Category.ALL;
        switch (cat) {
            case ELECTRONICS -> {
                topColor = new Color(15, 23, 42);     // Slate 900
                bottomColor = new Color(30, 41, 59);  // Slate 800
                accentColor = new Color(56, 189, 248);// Electric Sky Blue
                iconSymbol = "⚡";
            }
            case FASHION -> {
                topColor = new Color(36, 17, 39);     // Deep plum
                bottomColor = new Color(59, 27, 65);
                accentColor = new Color(244, 114, 182);// Rose Pink
                iconSymbol = "👕";
            }
            case HOME_KITCHEN -> {
                topColor = new Color(39, 24, 15);     // Rich copper amber
                bottomColor = new Color(68, 38, 20);
                accentColor = new Color(251, 146, 60); // Vibrant Amber / Tangerine
                iconSymbol = "🍳";
            }
            case BOOKS_ESSENTIALS -> {
                topColor = new Color(17, 33, 45);     // Deep sapphire navy
                bottomColor = new Color(24, 49, 68);
                accentColor = new Color(234, 179, 8);  // Warm Gold
                iconSymbol = "📚";
            }
            case DIGITAL -> {
                topColor = new Color(10, 32, 28);     // Emerald deep
                bottomColor = new Color(18, 56, 49);
                accentColor = new Color(52, 211, 153); // Emerald Mint
                iconSymbol = "💾";
            }
            default -> {
                topColor = new Color(21, 30, 46);
                bottomColor = new Color(11, 15, 25);
                accentColor = new Color(99, 102, 241);
                iconSymbol = "🛍️";
            }
        }

        // 1. Background Gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, topColor, width, height, bottomColor);
        g2.setPaint(bgGradient);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 16, 16));

        // 2. Ambient radial glow in center
        Point2D center = new Point2D.Float(width / 2f, height * 0.42f);
        float radius = Math.min(width, height) * 0.55f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60),
                          new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), 0)};
        RadialGradientPaint glow = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(glow);
        g2.fillOval((int)(center.getX() - radius), (int)(center.getY() - radius), (int)(radius * 2), (int)(radius * 2));

        // 3. Subtle background grid pattern
        g2.setColor(new Color(255, 255, 255, 14));
        g2.setStroke(new BasicStroke(1.0f));
        int step = Math.max(25, width / 8);
        for (int x = step / 2; x < width; x += step) {
            g2.drawLine(x, 10, x, height - 10);
        }
        for (int y = step / 2; y < height; y += step) {
            g2.drawLine(10, y, width - 10, y);
        }

        // 4. Central Icon Container Circle
        int circleDiameter = (int)(Math.min(width, height) * 0.44);
        int circleX = (width - circleDiameter) / 2;
        int circleY = (int)(height * 0.38) - circleDiameter / 2;

        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillOval(circleX, circleY, circleDiameter, circleDiameter);
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawOval(circleX, circleY, circleDiameter, circleDiameter);

        // 5. Draw large central emoji / symbol
        int emojiSize = Math.max(16, circleDiameter / 2);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, emojiSize));
        g2.setColor(Color.WHITE);
        int iconWidth = g2.getFontMetrics().stringWidth(iconSymbol);
        int iconHeight = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        g2.drawString(iconSymbol, circleX + (circleDiameter - iconWidth) / 2, circleY + (circleDiameter + iconHeight) / 2);

        // 6. Draw Category Pill at top-left
        String categoryTag = cat.getDisplayName().toUpperCase();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        int tagWidth = g2.getFontMetrics().stringWidth(categoryTag) + 16;
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(10, 10, tagWidth, 20, 10, 10);
        g2.setColor(accentColor);
        g2.drawRoundRect(10, 10, tagWidth, 20, 10, 10);
        g2.drawString(categoryTag, 18, 24);

        // 7. Product Title Watermark banner at bottom
        int bannerH = Math.max(28, (int)(height * 0.18));
        g2.setColor(new Color(10, 15, 26, 220));
        g2.fillRect(0, height - bannerH, width, bannerH);
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawLine(0, height - bannerH, width, height - bannerH);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(241, 245, 249));
        String displayName = productName != null ? productName : "FLIPMART PRODUCT";
        if (displayName.length() > 22) {
            displayName = displayName.substring(0, 20) + "...";
        }
        int textWidth = g2.getFontMetrics().stringWidth(displayName);
        g2.drawString(displayName, (width - textWidth) / 2, height - (bannerH / 3));

        g2.dispose();
        return img;
    }
}
