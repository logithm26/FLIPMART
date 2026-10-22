package com.shoppingcart.ui;

import com.shoppingcart.service.CartService;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

/**
 * Slide-out right-docked Cart Drawer Dialog.
 * Positions itself smoothly along the right edge of the parent application window.
 */
public class CartDrawerDialog extends JDialog {
    private final SlideOutCartPanel cartPanel;

    public CartDrawerDialog(JFrame parent, CartService cartService, Runnable onCheckoutProceed) {
        super(parent, "FlipMart Shopping Cart", true);
        setUndecorated(true);
        setSize(520, parent != null ? parent.getHeight() - 40 : 780);

        if (parent != null) {
            Point loc = parent.getLocationOnScreen();
            int x = loc.x + parent.getWidth() - getWidth() - 10;
            int y = loc.y + 20;
            setLocation(Math.max(loc.x, x), Math.max(loc.y, y));
        } else {
            setLocationRelativeTo(null);
        }

        cartPanel = new SlideOutCartPanel(cartService, this::dispose, () -> {
            dispose();
            if (onCheckoutProceed != null) {
                onCheckoutProceed.run();
            }
        });

        setLayout(new BorderLayout());
        add(cartPanel, BorderLayout.CENTER);
    }

    public void refreshCartContent() {
        if (cartPanel != null) {
            cartPanel.refreshCart();
        }
    }
}
