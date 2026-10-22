package com.shoppingcart.ui.components;

import com.shoppingcart.ui.UITheme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom modern styled button with smooth anti-aliased rounded corners and hover reactions.
 * Supports commercial e-commerce action styles: Add to Cart (Amber), Buy Now (Orange), Primary Blue,
 * Wishlist, Ghost, and Category Chip.
 */
public class ModernButton extends JButton {

    public enum Style {
        PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST, BUY_NOW, CART_YELLOW, CHIP, WISHLIST
    }

    private final Style style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isSelectedChip = false;
    private int cornerRadius = 8;

    public ModernButton(String text, Style style) {
        super(text);
        this.style = style;
        init();
    }

    public ModernButton(String text) {
        this(text, Style.PRIMARY);
    }

    private void init() {
        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setSelectedChip(boolean selected) {
        this.isSelectedChip = selected;
        repaint();
    }

    public boolean isSelectedChip() {
        return isSelectedChip;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg;
        Color fg = UITheme.TEXT_PRIMARY;
        Color borderColor = null;

        if (!isEnabled()) {
            bg = new Color(229, 231, 235);
            fg = new Color(156, 163, 175);
            borderColor = new Color(209, 213, 219);
        } else {
            switch (style) {
                case PRIMARY -> {
                    bg = isPressed ? UITheme.PRIMARY_HOVER.darker() : (isHovered ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY);
                    fg = Color.WHITE;
                }
                case BUY_NOW -> {
                    bg = isPressed ? UITheme.ACCENT_ORANGE_HOVER.darker() : (isHovered ? UITheme.ACCENT_ORANGE_HOVER : UITheme.ACCENT_ORANGE);
                    fg = Color.WHITE;
                }
                case CART_YELLOW -> {
                    bg = isPressed ? UITheme.CART_YELLOW_HOVER.darker() : (isHovered ? UITheme.CART_YELLOW_HOVER : UITheme.CART_YELLOW);
                    fg = new Color(17, 24, 39); // Deep contrast on amber
                }
                case SECONDARY -> {
                    bg = isPressed ? new Color(240, 242, 245) : (isHovered ? new Color(248, 250, 252) : Color.WHITE);
                    borderColor = isHovered ? UITheme.PRIMARY : UITheme.BORDER_DEFAULT;
                    fg = UITheme.TEXT_PRIMARY;
                }
                case SUCCESS -> {
                    bg = isPressed ? UITheme.SUCCESS_HOVER.darker() : (isHovered ? UITheme.SUCCESS_HOVER : UITheme.SUCCESS);
                    fg = Color.WHITE;
                }
                case DANGER -> {
                    bg = isPressed ? UITheme.DANGER_HOVER.darker() : (isHovered ? UITheme.DANGER_HOVER : UITheme.DANGER);
                    fg = Color.WHITE;
                }
                case GHOST -> {
                    bg = isHovered ? new Color(0, 0, 0, 15) : new Color(0, 0, 0, 0);
                    fg = isHovered ? UITheme.PRIMARY : UITheme.TEXT_SECONDARY;
                }
                case CHIP -> {
                    if (isSelectedChip) {
                        bg = UITheme.PRIMARY;
                        fg = Color.WHITE;
                        borderColor = UITheme.PRIMARY;
                    } else {
                        bg = isHovered ? new Color(238, 242, 255) : Color.WHITE;
                        fg = isHovered ? UITheme.PRIMARY : new Color(55, 65, 81);
                        borderColor = isHovered ? UITheme.PRIMARY : UITheme.BORDER_DEFAULT;
                    }
                }
                case WISHLIST -> {
                    bg = isHovered ? new Color(254, 242, 242) : Color.WHITE;
                    fg = UITheme.WISHLIST_RED;
                    borderColor = isHovered ? UITheme.WISHLIST_RED : UITheme.BORDER_DEFAULT;
                }
                default -> bg = UITheme.PRIMARY;
            }
        }

        // Fill rounded background
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));

        // Draw border if defined
        if (borderColor != null && isEnabled()) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Double(0.6, 0.6, width - 1.2, height - 1.2, cornerRadius, cornerRadius));
        }

        // Draw Text centered
        g2.setFont(getFont());
        g2.setColor(fg);
        int textW = g2.getFontMetrics().stringWidth(getText());
        int textH = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        g2.drawString(getText(), (width - textW) / 2, (height + textH) / 2 - 1);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 20, 75), Math.max(d.height + 10, 36));
    }
}
