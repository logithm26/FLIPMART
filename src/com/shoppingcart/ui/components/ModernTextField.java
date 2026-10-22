package com.shoppingcart.ui.components;

import com.shoppingcart.ui.UITheme;

import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern styled text input field with placeholder text, customizable corner radius (e.g. 24px pill),
 * and focus glow border.
 */
public class ModernTextField extends JTextField implements FocusListener {
    private String placeholder;
    private boolean isFocused = false;
    private int cornerRadius = 8;
    private String prefixIcon = null;

    public ModernTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        init();
    }

    public ModernTextField(String placeholder) {
        this(placeholder, 15);
    }

    private void init() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.PRIMARY);
        setFont(UITheme.FONT_BODY);
        setMargin(new Insets(8, 14, 8, 14));
        addFocusListener(this);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setPrefixIcon(String icon) {
        this.prefixIcon = icon;
        if (icon != null) {
            setMargin(new Insets(8, 32, 8, 14));
        } else {
            setMargin(new Insets(8, 14, 8, 14));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // White background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));

        // Border
        g2.setColor(isFocused ? UITheme.BORDER_FOCUS : UITheme.BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(isFocused ? 1.8f : 1.0f));
        g2.draw(new RoundRectangle2D.Double(0.8, 0.8, width - 1.6, height - 1.6, cornerRadius, cornerRadius));

        // Draw Prefix Icon if specified
        if (prefixIcon != null) {
            g2.setFont(UITheme.FONT_BODY);
            g2.setColor(new Color(156, 163, 175));
            int iconH = (height + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2 - 1;
            g2.drawString(prefixIcon, 10, iconH);
        }

        super.paintComponent(g2);

        // Draw placeholder if empty
        if (getText().isEmpty() && placeholder != null && !isFocused) {
            g2.setColor(new Color(156, 163, 175));
            g2.setFont(getFont());
            Insets insets = getInsets();
            int phY = (height + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2 - 1;
            g2.drawString(placeholder, insets.left, phY);
        }

        g2.dispose();
    }

    @Override
    public void focusGained(FocusEvent e) {
        isFocused = true;
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        isFocused = false;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 38));
    }
}
