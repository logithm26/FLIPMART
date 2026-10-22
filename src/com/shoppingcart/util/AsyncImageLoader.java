package com.shoppingcart.util;

import com.shoppingcart.model.Category;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Asynchronous Image Loader utilizing SwingWorker for background network fetching and processing.
 * Guarantees that the Swing Event Dispatch Thread (EDT) never stutters or locks during image downloads.
 * Implements high-quality smooth scaling (Image.SCALE_SMOOTH), multi-tier in-memory caching with
 * strict cache key isolation per product/URL, shimmer skeleton placeholders, and distinct
 * category-based offline fallbacks.
 */
public class AsyncImageLoader {
    private static final AsyncImageLoader INSTANCE = new AsyncImageLoader();
    private static final Map<String, ImageIcon> MEMORY_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ImageIcon> PLACEHOLDER_CACHE = new ConcurrentHashMap<>();

    private AsyncImageLoader() {}

    public static AsyncImageLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Loads a product image into the target JLabel asynchronously with strict cache key isolation.
     */
    public void loadImageIntoLabel(JLabel targetLabel, String productId, String imagePath, String productName, Category category, int width, int height) {
        String cacheKey = getCacheKey(productId, imagePath, productName, width, height);

        // 1. Fast Cache Hit: Instant display on EDT
        if (MEMORY_CACHE.containsKey(cacheKey)) {
            targetLabel.setIcon(MEMORY_CACHE.get(cacheKey));
            return;
        }

        // 2. Set attractive skeleton shimmer placeholder immediately
        ImageIcon placeholder = getSkeletonPlaceholder(category, width, height);
        targetLabel.setIcon(placeholder);

        // 3. Launch background SwingWorker for asynchronous fetching & smooth scaling
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return fetchAndScaleImage(imagePath, productName, category, width, height);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon resultIcon = get();
                    if (resultIcon != null) {
                        MEMORY_CACHE.put(cacheKey, resultIcon);
                        targetLabel.setIcon(resultIcon);
                        targetLabel.repaint();
                    }
                } catch (Exception e) {
                    // Safe category-specific offline fallback
                    ImageIcon fallback = getCategoryFallbackIcon(productName, category, width, height);
                    MEMORY_CACHE.put(cacheKey, fallback);
                    targetLabel.setIcon(fallback);
                    targetLabel.repaint();
                }
            }
        };

        worker.execute();
    }

    /**
     * Backwards-compatible convenience method.
     */
    public void loadImageIntoLabel(JLabel targetLabel, String imagePath, String productName, Category category, int width, int height) {
        loadImageIntoLabel(targetLabel, null, imagePath, productName, category, width, height);
    }

    /**
     * Generalized asynchronous image fetch with consumer callback on EDT.
     */
    public void loadImageAsync(String productId, String imagePath, String productName, Category category, int width, int height, Consumer<ImageIcon> onLoaded) {
        String cacheKey = getCacheKey(productId, imagePath, productName, width, height);
        if (MEMORY_CACHE.containsKey(cacheKey)) {
            if (onLoaded != null) onLoaded.accept(MEMORY_CACHE.get(cacheKey));
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return fetchAndScaleImage(imagePath, productName, category, width, height);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        MEMORY_CACHE.put(cacheKey, icon);
                        if (onLoaded != null) onLoaded.accept(icon);
                    }
                } catch (Exception e) {
                    ImageIcon fallback = getCategoryFallbackIcon(productName, category, width, height);
                    MEMORY_CACHE.put(cacheKey, fallback);
                    if (onLoaded != null) onLoaded.accept(fallback);
                }
            }
        };
        worker.execute();
    }

    public void loadImageAsync(String imagePath, String productName, Category category, int width, int height, Consumer<ImageIcon> onLoaded) {
        loadImageAsync(null, imagePath, productName, category, width, height, onLoaded);
    }

    /**
     * Background execution logic: Handles HTTP streaming, local files, and high-quality smooth scaling.
     */
    private ImageIcon fetchAndScaleImage(String imagePath, String productName, Category category, int targetWidth, int targetHeight) {
        BufferedImage originalImage = null;

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    URL url = new URL(imagePath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) FlipMart/2.0 Retail");
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(7000);
                    conn.connect();

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try (InputStream in = conn.getInputStream()) {
                            originalImage = ImageIO.read(in);
                        }
                    }
                } else {
                    File localFile = new File(imagePath);
                    if (localFile.exists() && localFile.canRead()) {
                        originalImage = ImageIO.read(localFile);
                    }
                }
            } catch (Exception ignored) {
                // Network or file exception handled gracefully
            }
        }

        // If network fetch fails or returned null, use category-specific fallback rather than headphone default
        if (originalImage == null) {
            String catAssetPath = getCategoryAssetPath(category);
            File catAssetFile = new File(catAssetPath);
            if (catAssetFile.exists() && catAssetFile.canRead()) {
                try {
                    originalImage = ImageIO.read(catAssetFile);
                } catch (Exception ignored) {}
            }
        }

        if (originalImage == null) {
            originalImage = ImageHelper.createHighContrastFallback(productName, category, targetWidth, targetHeight);
        }

        // Smooth aspect-ratio conscious scaling
        Image scaled = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Maps each category to a distinct fallback asset file.
     */
    public static String getCategoryAssetPath(Category category) {
        if (category == null) return "assets/aurasound_headphones.png";
        return switch (category) {
            case FASHION -> "assets/mens_slim_shirt.png";
            case HOME_KITCHEN -> "assets/electric_kettle.png";
            case BOOKS_ESSENTIALS -> "assets/bestseller_books.png";
            case DIGITAL -> "assets/devmaster_ide.png";
            default -> "assets/aurasound_headphones.png";
        };
    }

    public static ImageIcon getCategoryFallbackIcon(String productName, Category category, int width, int height) {
        BufferedImage img = null;
        String assetPath = getCategoryAssetPath(category);
        File f = new File(assetPath);
        if (f.exists() && f.canRead()) {
            try {
                img = ImageIO.read(f);
            } catch (Exception ignored) {}
        }
        if (img == null) {
            img = ImageHelper.createHighContrastFallback(productName, category, width, height);
        }
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Generates a sleek skeleton/placeholder icon with shimmer gradient and category iconography.
     */
    public ImageIcon getSkeletonPlaceholder(Category category, int width, int height) {
        String key = "placeholder_" + (category != null ? category.name() : "ALL") + "_" + width + "x" + height;
        if (PLACEHOLDER_CACHE.containsKey(key)) {
            return PLACEHOLDER_CACHE.get(key);
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Subtle shimmer skeleton background
        GradientPaint shimmer = new GradientPaint(
                0, 0, new Color(245, 247, 250),
                width, height, new Color(230, 235, 243)
        );
        g2.setPaint(shimmer);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 8, 8));

        // Subtle inner border
        g2.setColor(new Color(225, 230, 240));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, 8, 8));

        // Category icon badge in center
        String symbol = category != null ? category.getIconSymbol() : "🛍";
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.min(width, height) / 4));
        g2.setColor(new Color(160, 174, 192, 180));
        int strWidth = g2.getFontMetrics().stringWidth(symbol);
        int strHeight = g2.getFontMetrics().getAscent();
        g2.drawString(symbol, (width - strWidth) / 2, (height + strHeight) / 2 - 4);

        g2.dispose();
        ImageIcon icon = new ImageIcon(img);
        PLACEHOLDER_CACHE.put(key, icon);
        return icon;
    }

    private String getCacheKey(String productId, String imagePath, String productName, int width, int height) {
        return (productId != null ? productId : "P") + "_" + (imagePath != null ? imagePath : productName) + "_" + width + "x" + height;
    }
}
