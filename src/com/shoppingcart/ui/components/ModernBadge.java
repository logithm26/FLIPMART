package com.shoppingcart.ui.components;

import com.shoppingcart.ui.UITheme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Visual badge indicator component for stock levels, ratings, delivery, and category badges.
 */
public class ModernBadge extends JComponent {

    public enum Type {
        STOCK_IN, STOCK_LOW, STOCK_OUT, CATEGORY, PROMO, ASSURED, RATING
    }

    private String text;
    private Type type;
    private Font badgeFont = UITheme.FONT_BADGE;

    public ModernBadge(String text, Type type) {
        this.text = text;
        this.type = type;
        setOpaque(false);
    }

    public void updateStock(int stockQuantity) {
        if (stockQuantity <= 0) {
            this.text = "Out of Stock";
            this.type = Type.STOCK_OUT;
        } else if (stockQuantity <= 3) {
            this.text = "Only " + stockQuantity + " left!";
            this.type = Type.STOCK_LOW;
        } else {
            this.text = "In Stock (" + stockQuantity + ")";
            this.type = Type.STOCK_IN;
        }
        revalidate();
        repaint();
    }

    public void setTextAndType(String text, Type type) {
        this.text = text;
        this.type = type;
        revalidate();
        repaint();
    }

    public Type getType() {
        return type;
    }

    public void setBadgeFont(Font font) {
        this.badgeFont = font;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg;
        Color border;
        Color textCol;

        switch (type) {
            case RATING -> {
                bg = new Color(56, 142, 60);         // Flipkart green badge (#388E3C)
                border = new Color(56, 142, 60);
                textCol = Color.WHITE;
            }
            case ASSURED -> {
                bg = new Color(237, 242, 254);       // Soft Flipkart Blue tint
                border = new Color(199, 210, 254);
                textCol = UITheme.PRIMARY;
            }
            case STOCK_IN -> {
                bg = new Color(232, 245, 233);       // Soft emerald tint
                border = new Color(165, 214, 167);
                textCol = new Color(46, 125, 50);
            }
            case STOCK_LOW -> {
                bg = new Color(254, 243, 199);       // Soft amber alert
                border = new Color(252, 211, 77);
                textCol = new Color(180, 83, 9);
            }
            case STOCK_OUT -> {
                bg = new Color(254, 226, 226);       // Soft rose alert
                border = new Color(248, 113, 113);
                textCol = new Color(185, 28, 28);
            }
            case PROMO -> {
                bg = new Color(253, 230, 138);       // Gold banner
                border = new Color(245, 158, 11);
                textCol = new Color(146, 64, 14);
            }
            case CATEGORY -> {
                bg = new Color(243, 244, 246);       // Slate grey pill
                border = new Color(229, 231, 235);
                textCol = new Color(75, 85, 99);
            }
            default -> {
                bg = new Color(243, 244, 246);
                border = UITheme.BORDER_DEFAULT;
                textCol = UITheme.TEXT_SECONDARY;
            }
        }

        // Fill pill / rounded rectangle
        int arc = (type == Type.RATING) ? 6 : height;
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, arc, arc));

        // Draw pill border
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, arc, arc));

        // Draw text
        g2.setFont(badgeFont != null ? badgeFont : UITheme.FONT_BADGE);
        g2.setColor(textCol);
        if (text != null) {
            int tw = g2.getFontMetrics().stringWidth(text);
            int th = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
            g2.drawString(text, (width - tw) / 2, (height + th) / 2 - 1);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        if (text != null) {
            Font f = badgeFont != null ? badgeFont : UITheme.FONT_BADGE;
            int w = getFontMetrics(f).stringWidth(text) + 16;
            return new Dimension(w, 22);
        }
        return new Dimension(80, 22);
    }
}
