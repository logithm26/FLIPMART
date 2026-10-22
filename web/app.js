// FlipMart Web Store Application Logic
// Smart URL & Asset Resolver for Local Server, file://, and GitHub Pages (e.g., https://logithm26.github.io/FLIPMART/)
function getRepoBaseUrl() {
    if (window.location.protocol === 'file:') {
        return 'http://localhost:8080/';
    }
    const origin = window.location.origin;
    let pathname = window.location.pathname;
    if (pathname.includes('.')) {
        pathname = pathname.substring(0, pathname.lastIndexOf('/') + 1);
    } else if (!pathname.endsWith('/')) {
        pathname += '/';
    }
    return origin + pathname;
}

const REPO_BASE = getRepoBaseUrl();
const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : (REPO_BASE + 'api');

let allProducts = [];
let currentCategory = 'ALL';
let currentSearch = '';
let activeBundleRecommendation = null;
let activePaymentMethod = 'CC';
let lastConfirmedReceipt = '';

// --- LOCAL CART ENGINE FOR GITHUB PAGES (STATIC MODE & RESILIENT FALLBACK) ---
const LocalCartEngine = {
    getItems() {
        try {
            return JSON.parse(localStorage.getItem('flipmart_cart_items') || '[]');
        } catch (_) {
            return [];
        }
    },
    saveItems(items) {
        localStorage.setItem('flipmart_cart_items', JSON.stringify(items));
    },
    getCoupon() {
        return localStorage.getItem('flipmart_cart_coupon') || null;
    },
    saveCoupon(code) {
        if (code) localStorage.setItem('flipmart_cart_coupon', code);
        else localStorage.removeItem('flipmart_cart_coupon');
    },
    addItem(productId, quantity = 1, isBundle = false) {
        const product = allProducts.find(p => p.id === productId);
        if (!product) return { success: false, message: 'Product not found' };
        const items = this.getItems();
        const existing = items.find(it => it.productId === productId);
        if (existing) {
            existing.quantity += quantity;
            if (isBundle) existing.isBundle = true;
        } else {
            items.push({
                productId: product.id,
                name: product.name,
                category: product.category,
                imagePath: product.imagePath,
                basePrice: product.basePrice,
                quantity: quantity,
                isBundle: isBundle,
                bundlePartnerId: product.bundlePartnerId
            });
        }
        this.saveItems(items);

        let bundleRecommendation = null;
        if (!isBundle && product.bundlePartnerId) {
            const partner = allProducts.find(p => p.id === product.bundlePartnerId);
            if (partner) {
                bundleRecommendation = {
                    baseName: product.name,
                    recommendedId: partner.id,
                    recommendedName: partner.name,
                    recommendedCategory: partner.category,
                    recommendedImage: partner.imagePath,
                    originalPrice: partner.basePrice,
                    discountedPrice: partner.basePrice * 0.90,
                    savings: partner.basePrice * 0.10
                };
            }
        }
        return { success: true, bundleRecommendation };
    },
    updateQuantity(productId, action) {
        const items = this.getItems();
        const existing = items.find(it => it.productId === productId);
        if (!existing) return { success: false };
        if (action === 'increment') {
            const p = allProducts.find(item => item.id === productId);
            if (p && existing.quantity >= (p.stockQuantity || 99)) {
                return { success: false, message: 'Stock limit reached' };
            }
            existing.quantity++;
        } else if (action === 'decrement') {
            existing.quantity--;
            if (existing.quantity <= 0) {
                return this.removeItem(productId);
            }
        }
        this.saveItems(items);
        return { success: true };
    },
    removeItem(productId) {
        let items = this.getItems();
        items = items.filter(it => it.productId !== productId);
        this.saveItems(items);
        return { success: true };
    },
    clear() {
        this.saveItems([]);
        this.saveCoupon(null);
    },
    applyCoupon(code) {
        if (!code) return { success: false, message: 'Please enter a coupon code' };
        const clean = code.trim().toUpperCase();
        const cart = this.getCartState();
        if (clean === 'SAVE15') {
            if (cart.breakdown.subtotal < 1000) return { success: false, message: 'Minimum order ₹1,000 required for SAVE15' };
            this.saveCoupon(clean);
            return { success: true };
        } else if (clean === 'FLAT500' || clean === 'FLAT25') {
            if (cart.breakdown.subtotal < 2000) return { success: false, message: 'Minimum order ₹2,000 required for FLAT500' };
            this.saveCoupon(clean);
            return { success: true };
        } else if (clean === 'BULK3') {
            if (cart.totalItems < 3) return { success: false, message: 'Minimum 3 items required for BULK3' };
            this.saveCoupon(clean);
            return { success: true };
        } else {
            return { success: false, message: `Invalid promo code "${clean}". Try SAVE15, FLAT500, or BULK3.` };
        }
    },
    removeCoupon() {
        this.saveCoupon(null);
    },
    getCartState() {
        const rawItems = this.getItems();
        let totalItems = 0;
        let subtotal = 0;
        let bundleSavings = 0;
        let taxTotal = 0;
        let shippingTotal = 0;
        let hasPhysical = false;

        const items = rawItems.map(it => {
            const p = allProducts.find(prod => prod.id === it.productId) || it;
            const lineSub = p.basePrice * it.quantity;
            const lineBundleDiscount = it.isBundle ? (lineSub * 0.10) : 0;
            const lineTotal = lineSub - lineBundleDiscount;

            subtotal += lineSub;
            bundleSavings += lineBundleDiscount;
            totalItems += it.quantity;

            // Category GST Rate
            let taxRate = 0.18;
            if (p.category === 'BOOKS_ESSENTIALS') taxRate = 0.05;
            else if (p.category === 'FASHION') taxRate = 0.12;
            else if (p.category === 'DIGITAL') taxRate = 0.10;
            taxTotal += lineTotal * taxRate;

            // Shipping
            if (p.category !== 'DIGITAL') {
                hasPhysical = true;
                shippingTotal += 99 * Math.min(it.quantity, 3);
            }

            return {
                productId: p.id,
                name: p.name,
                category: p.category,
                imagePath: p.imagePath,
                basePrice: p.basePrice,
                quantity: it.quantity,
                lineTotal: lineTotal,
                hasBundleDiscount: !!it.isBundle,
                bundleDiscountAmount: lineBundleDiscount
            };
        });

        if (!hasPhysical || subtotal >= 1499) {
            shippingTotal = 0;
        }

        // Coupon calculation
        let couponDiscount = 0;
        const coupon = this.getCoupon();
        if (coupon) {
            const effectiveSub = subtotal - bundleSavings;
            if (coupon === 'SAVE15' && subtotal >= 1000) {
                couponDiscount = effectiveSub * 0.15;
            } else if ((coupon === 'FLAT500' || coupon === 'FLAT25') && subtotal >= 2000) {
                couponDiscount = 500;
            } else if (coupon === 'BULK3' && totalItems >= 3) {
                couponDiscount = effectiveSub * 0.12;
            } else {
                this.saveCoupon(null);
            }
        }

        const totalDiscount = bundleSavings + couponDiscount;
        const grandTotal = Math.max(0, subtotal - totalDiscount) + taxTotal + shippingTotal;

        return {
            items,
            totalItems,
            appliedCoupon: this.getCoupon(),
            breakdown: {
                subtotal,
                bundleSavings,
                couponDiscount,
                totalDiscount,
                taxTotal,
                shippingTotal,
                grandTotal,
                appliedCoupon: this.getCoupon()
            }
        };
    },
    checkout(payload) {
        const cart = this.getCartState();
        if (cart.items.length === 0) return { success: false, message: 'Cart is empty' };
        const orderId = 'ORD-' + Math.random().toString(36).substring(2, 10).toUpperCase();
        const dateStr = new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' });

        let receiptText = "========================================================================\n" +
            "                      FLIPMART INDIA RETAIL PVT LTD                     \n" +
            "                   OFFICIAL GST TAX INVOICE & RECEIPT                   \n" +
            "========================================================================\n" +
            " GSTIN        : 29AABCF1234F1Z9 (Karnataka Hub)\n" +
            ` Order ID     : ${orderId}\n` +
            ` Order Date   : ${dateStr}\n` +
            " Order Status : COMPLETED & AUTHORIZED\n" +
            "------------------------------------------------------------------------\n" +
            " CUSTOMER DETAILS:\n" +
            ` Name         : ${payload.name} (VIP Plus Member)\n` +
            ` Email        : ${payload.email}\n` +
            ` Shipping To  : ${payload.address}\n` +
            "------------------------------------------------------------------------\n" +
            " SKU        ITEM DESCRIPTION             QTY   UNIT PRICE   TOTAL       \n" +
            "------------------------------------------------------------------------\n";

        cart.items.forEach(it => {
            let title = it.name.length > 27 ? it.name.substring(0, 24) + '...' : it.name.padEnd(27, ' ');
            receiptText += ` ${it.productId.toString().padEnd(10, ' ')} ${title} ${it.quantity.toString().padEnd(5, ' ')} ${formatInr(it.basePrice).padEnd(12, ' ')} ${formatInr(it.lineTotal)}\n`;
            if (it.hasBundleDiscount) {
                receiptText += `            ↳ Bundle Deal (10% Off - Saved ${formatInr(it.bundleDiscountAmount)})\n`;
            }
        });

        receiptText += "------------------------------------------------------------------------\n" +
            ` Subtotal                                         : ${formatInr(cart.breakdown.subtotal)}\n`;
        if (cart.breakdown.totalDiscount > 0) {
            receiptText += ` Discounts & Offers Applied                       : -${formatInr(cart.breakdown.totalDiscount)} (${cart.breakdown.appliedCoupon || 'Bundle Deals'})\n`;
        }
        receiptText += ` Estimated GST (Itemized Category Tax)            : ${formatInr(cart.breakdown.taxTotal)}\n` +
            ` Fulfillment & Express Delivery                   : ${cart.breakdown.shippingTotal === 0 ? 'FREE' : formatInr(cart.breakdown.shippingTotal)}\n` +
            "========================================================================\n" +
            ` TOTAL AMOUNT PAID                                : ${formatInr(cart.breakdown.grandTotal)} INR\n` +
            "========================================================================\n" +
            " PAYMENT VERIFICATION:\n" +
            ` Payment Gateway  : ${payload.method}\n` +
            ` Transaction Ref  : TXN-${Math.random().toString(36).substring(2, 12).toUpperCase()}\n` +
            " Settlement Status: AUTHORIZED & CLEARED VIA SECURE GATEWAY\n" +
            "========================================================================\n" +
            "     Thank you for shopping with FlipMart! 100% Genuine Products.       \n" +
            "        Support: support@flipmart.in | Helpline: 1800-208-9898          \n" +
            "========================================================================\n";

        this.clear();
        return {
            success: true,
            orderId,
            paymentMethod: payload.method,
            receiptText
        };
    }
};

document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    fetchProducts();
    refreshCart();
});

// Indian Rupee & Numbering System Formatter
function formatInr(amount) {
    if (isNaN(amount) || amount === null || amount === undefined) return '₹0';
    const isNegative = amount < 0;
    const absVal = Math.abs(amount);
    let whole = Math.floor(absVal);
    let cents = Math.round((absVal - whole) * 100);
    if (cents >= 100) { whole++; cents = 0; }
    let s = whole.toString();
    let res = '';
    if (s.length <= 3) {
        res = s;
    } else {
        let lastThree = s.substring(s.length - 3);
        let remaining = s.substring(0, s.length - 3);
        let remRes = '';
        let count = 0;
        for (let i = remaining.length - 1; i >= 0; i--) {
            if (count > 0 && count % 2 === 0) {
                remRes = ',' + remRes;
            }
            remRes = remaining[i] + remRes;
            count++;
        }
        res = remRes + ',' + lastThree;
    }
    if (cents > 0) {
        res += '.' + cents.toString().padStart(2, '0');
    }
    return (isNegative ? '-' : '') + '₹' + res;
}

function initEventListeners() {
    // Search listener
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            currentSearch = e.target.value.toLowerCase();
            renderProducts();
        });
    }

    // Category pills listener
    document.querySelectorAll('.category-pills .pill').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.category-pills .pill').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentCategory = btn.getAttribute('data-category');
            renderProducts();
        });
    });

    // Drawer triggers
    const openCartBtn = document.getElementById('openCartBtn');
    if (openCartBtn) openCartBtn.addEventListener('click', openCartDrawer);

    const bannerCartTrigger = document.getElementById('bannerCartTrigger');
    if (bannerCartTrigger) bannerCartTrigger.addEventListener('click', openCartDrawer);

    const closeCartBtn = document.getElementById('closeCartBtn');
    if (closeCartBtn) closeCartBtn.addEventListener('click', closeCartDrawer);

    const cartDrawerBackdrop = document.getElementById('cartDrawerBackdrop');
    if (cartDrawerBackdrop) {
        cartDrawerBackdrop.addEventListener('click', (e) => {
            if (e.target.id === 'cartDrawerBackdrop') closeCartDrawer();
        });
    }

    // Clear cart
    const clearCartBtn = document.getElementById('clearCartBtn');
    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', async () => {
            try {
                await fetch(`${API_BASE}/cart/clear`, { method: 'POST' });
            } catch (_) {}
            LocalCartEngine.clear();
            showToast('FlipMart cart cleared. Inventory restored.');
            refreshCart();
            fetchProducts();
        });
    }

    // Promo code apply
    const applyPromoBtn = document.getElementById('applyPromoBtn');
    if (applyPromoBtn) {
        applyPromoBtn.addEventListener('click', () => {
            const code = document.getElementById('promoInput').value.trim();
            if (code) applyPromoDirectly(code);
        });
    }

    // Bundle modal actions
    const btnDismissBundle = document.getElementById('btnDismissBundle');
    if (btnDismissBundle) {
        btnDismissBundle.addEventListener('click', () => {
            closeModal('bundleModalBackdrop');
        });
    }

    const btnAddBundle = document.getElementById('btnAddBundle');
    if (btnAddBundle) {
        btnAddBundle.addEventListener('click', async () => {
            if (!activeBundleRecommendation) return;
            let success = false;
            try {
                const res = await fetch(`${API_BASE}/cart/add`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ productId: activeBundleRecommendation.recommendedId, quantity: 1, isBundle: true })
                });
                if (res.ok) {
                    const data = await res.json();
                    success = data.success;
                }
            } catch (_) {}
            if (!success) {
                const data = LocalCartEngine.addItem(activeBundleRecommendation.recommendedId, 1, true);
                success = data.success;
            }
            closeModal('bundleModalBackdrop');
            if (success) {
                showToast(`Added bundle: ${activeBundleRecommendation.recommendedName} with 10% discount! 🎁`);
                refreshCart();
                fetchProducts();
            }
        });
    }

    // Payment tab switcher
    document.querySelectorAll('.payment-tabs .pay-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const method = tab.getAttribute('data-method');
            switchPaymentTab(method);
        });
    });

    initCardLiveSync();
}

function switchPaymentTab(method) {
    activePaymentMethod = method;
    document.querySelectorAll('.payment-tabs .pay-tab').forEach(t => {
        if (t.getAttribute('data-method') === method) {
            t.classList.add('active');
        } else {
            t.classList.remove('active');
        }
    });

    const forms = {
        'CC': document.getElementById('formCC'),
        'UPI': document.getElementById('formUPI'),
        'CRYPTO': document.getElementById('formCRYPTO')
    };

    Object.keys(forms).forEach(key => {
        const el = forms[key];
        if (el) {
            if (key === method) {
                el.classList.add('active');
                el.style.display = 'block';
            } else {
                el.classList.remove('active');
                el.style.display = 'none';
            }
        }
    });
}

function initCardLiveSync() {
    const ccHolder = document.getElementById('ccHolder');
    const ccNumber = document.getElementById('ccNumber');
    const ccExpiry = document.getElementById('ccExpiry');

    if (ccHolder) {
        ccHolder.addEventListener('input', (e) => {
            const val = e.target.value.trim();
            const disp = document.getElementById('vcHolderDisplay');
            if (disp) disp.textContent = val ? val.toUpperCase() : 'ALEX MORGAN';
        });
    }

    if (ccNumber) {
        ccNumber.addEventListener('input', (e) => {
            let digits = e.target.value.replace(/\D/g, '').substring(0, 16);
            let formatted = '';
            for (let i = 0; i < digits.length; i++) {
                if (i > 0 && i % 4 === 0) formatted += '  ';
                formatted += digits[i];
            }
            const disp = document.getElementById('vcNumberDisplay');
            if (disp) disp.textContent = formatted || '4532  8901  2345  6789';

            // Detect card brand
            const brand = document.getElementById('vcBrandLogo');
            if (brand) {
                if (digits.startsWith('4')) brand.textContent = 'VISA';
                else if (digits.startsWith('5')) brand.textContent = 'MasterCard';
                else if (digits.startsWith('6') || digits.startsWith('8')) brand.textContent = 'RuPay';
                else if (digits.startsWith('3')) brand.textContent = 'AMEX';
                else brand.textContent = 'RuPay';
            }
        });
    }

    if (ccExpiry) {
        ccExpiry.addEventListener('input', (e) => {
            let val = e.target.value.trim();
            const disp = document.getElementById('vcExpiryDisplay');
            if (disp) disp.textContent = val || '12/28';
        });
    }
}

function setUpiHandle(suffix) {
    const input = document.getElementById('upiId');
    if (!input) return;
    let base = input.value.split('@')[0] || 'alexmorgan';
    input.value = base + suffix;
    input.focus();
}

function selectBank(btn, name) {
    document.querySelectorAll('.bank-chip').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
}

// --- FETCH & RENDER PRODUCTS ---
async function fetchProducts() {
    try {
        let res = await fetch(`${API_BASE}/products`);
        if (!res.ok) {
            res = await fetch(`${API_BASE}/products.json`);
        }
        if (!res.ok) {
            res = await fetch('api/products.json');
        }
        allProducts = await res.json();
        console.log(`Loaded ${allProducts.length} products`);
        renderProducts();
    } catch (e) {
        try {
            const relRes = await fetch('api/products.json');
            if (relRes.ok) {
                allProducts = await relRes.json();
                renderProducts();
                return;
            }
        } catch (_) {}
        console.error('Error fetching products:', e);
    }
}

// --- IMAGE UTILITY WITH CATEGORY-BASED FALLBACKS ---
function resolveImageUrl(path, category) {
    if (!path || path.trim() === '') return getCategoryFallbackUrl(category);
    if (path.startsWith('http://') || path.startsWith('https://')) {
        return path;
    }
    const cleanPath = path.startsWith('/') ? path.substring(1) : path;
    return REPO_BASE + cleanPath;
}

function getCategoryFallbackUrl(category) {
    let rel = 'assets/aurasound_headphones.png';
    switch (category) {
        case 'FASHION':
            rel = 'assets/mens_slim_shirt.png'; break;
        case 'HOME_KITCHEN':
            rel = 'assets/electric_kettle.png'; break;
        case 'BOOKS_ESSENTIALS':
            rel = 'assets/bestseller_books.png'; break;
        case 'DIGITAL':
            rel = 'assets/devmaster_ide.png'; break;
        case 'ELECTRONICS':
        default:
            rel = 'assets/aurasound_headphones.png'; break;
    }
    return REPO_BASE + rel;
}

function renderProducts() {
    const grid = document.getElementById('productsGrid');
    if (!grid) return;
    grid.innerHTML = '';

    const filtered = allProducts.filter(p => {
        const matchCategory = currentCategory === 'ALL' || p.category === currentCategory;
        const matchSearch = !currentSearch ||
            p.name.toLowerCase().includes(currentSearch) ||
            p.description.toLowerCase().includes(currentSearch) ||
            p.highlight.toLowerCase().includes(currentSearch);
        return matchCategory && matchSearch;
    });

    const counter = document.getElementById('productCounter');
    if (counter) {
        counter.textContent = `Displaying ${filtered.length} product${filtered.length === 1 ? '' : 's'}`;
    }

    filtered.forEach(p => {
        const card = document.createElement('div');
        card.className = 'product-card';

        // Stock badge determination
        let stockBadgeClass = 'badge-stock-in';
        let stockText = `In Stock (${p.stockQuantity})`;
        if (p.stockQuantity <= 0) {
            stockBadgeClass = 'badge-stock-out';
            stockText = 'Out of Stock';
        } else if (p.stockQuantity <= 3) {
            stockBadgeClass = 'badge-stock-low';
            stockText = `Only ${p.stockQuantity} left - High Demand`;
        }

        const priceDisplay = p.formattedPrice || formatInr(p.basePrice);
        const mrpDisplay = p.formattedMrp || formatInr(p.originalMrp || (p.basePrice * 1.35));
        const discPercent = p.discountPercent || Math.round(((p.originalMrp - p.basePrice) / p.originalMrp) * 100) || 20;

        const productImgUrl = resolveImageUrl(p.imagePath, p.category);
        const fallbackUrl = getCategoryFallbackUrl(p.category);

        card.innerHTML = `
            <div class="card-image-wrap" onclick="openQuickView('${p.id}')">
                <img src="${productImgUrl}" alt="${p.name}" onerror="this.onerror=null; this.src='${fallbackUrl}';">
                <div class="card-badges-overlay">
                    <span class="badge badge-cat">${p.categoryIcon} ${p.categoryDisplay}</span>
                    <span class="badge ${stockBadgeClass}">${stockText}</span>
                </div>
            </div>
            <div class="card-body">
                <h3 class="card-title" onclick="openQuickView('${p.id}')">${p.name}</h3>
                <div class="card-highlight">${p.highlight}</div>
                <div class="card-rating">⭐ ${p.rating.toFixed(1)} (${formatInr(p.reviewCount).replace('₹', '')} ratings)</div>
                <div class="card-price-row">
                    <span class="card-price">${priceDisplay}</span>
                    <span class="card-mrp"><strike>${mrpDisplay}</strike></span>
                    <span class="card-disc">${discPercent}% off</span>
                </div>
                <div class="card-delivery">✓ Free Express Delivery</div>
                <div class="card-actions">
                    <button class="btn-cart" onclick="addToCart('${p.id}')" ${!p.isAvailable ? 'disabled' : ''}>
                        ${p.isAvailable ? 'Add to Cart 🛒' : 'Out of Stock'}
                    </button>
                    <button class="btn-buynow" onclick="buyNow('${p.id}')" ${!p.isAvailable ? 'disabled' : ''}>
                        Buy Now ⚡
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

// --- ADD TO CART & BUY NOW ---
async function addToCart(productId) {
    let success = false;
    let data = null;
    try {
        const res = await fetch(`${API_BASE}/cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, quantity: 1, isBundle: false })
        });
        if (res.ok) {
            data = await res.json();
            success = data.success;
        }
    } catch (_) {}

    if (!success) {
        data = LocalCartEngine.addItem(productId, 1, false);
        success = data.success;
    }

    if (success) {
        showToast('Item added to FlipMart cart! 🛒');
        refreshCart();
        fetchProducts();
        if (data && data.bundleRecommendation) {
            showBundleModal(data.bundleRecommendation);
        }
    } else {
        showToast((data && data.message) || 'Could not add item to cart.', 'error');
    }
}

async function buyNow(productId) {
    await addToCart(productId);
    openCheckoutModal();
}

function showBundleModal(rec) {
    activeBundleRecommendation = rec;
    document.getElementById('bundleModalSubtitle').textContent = `Customers who added ${rec.baseName} also paired this item:`;
    const bundleImgElem = document.getElementById('bundleImg');
    bundleImgElem.src = resolveImageUrl(rec.recommendedImage, rec.recommendedCategory);
    bundleImgElem.onerror = function() {
        this.onerror = null;
        this.src = getCategoryFallbackUrl(rec.recommendedCategory);
    };
    document.getElementById('bundleName').textContent = rec.recommendedName;
    document.getElementById('bundleOldPrice').innerHTML = `<strike>${formatInr(rec.originalPrice)}</strike>`;
    document.getElementById('bundleNewPrice').textContent = formatInr(rec.discountedPrice);
    document.getElementById('bundleSavings').textContent = `Save ${formatInr(rec.savings)} (10% off)`;
    openModal('bundleModalBackdrop');
}

// --- REFRESH CART CONTENT ---
async function refreshCart() {
    let cart = null;
    try {
        const res = await fetch(`${API_BASE}/cart`);
        if (res.ok) {
            cart = await res.json();
        }
    } catch (_) {}

    if (!cart || !cart.items || (cart.items.length === 0 && LocalCartEngine.getItems().length > 0)) {
        cart = LocalCartEngine.getCartState();
    }

    renderCartUI(cart);
}

function renderCartUI(cart) {
    if (!cart) return;

    // Update nav badge
    const navCount = document.getElementById('navCartCount');
    if (navCount) navCount.textContent = cart.totalItems;

    const navTotal = document.getElementById('navCartTotal');
    if (navTotal) navTotal.textContent = formatInr(cart.breakdown.grandTotal);

    const drawerCount = document.getElementById('drawerItemCount');
    if (drawerCount) drawerCount.textContent = `(${cart.totalItems} item${cart.totalItems === 1 ? '' : 's'})`;

    const list = document.getElementById('drawerItemsList');
    if (!list) return;
    list.innerHTML = '';

    if (cart.items.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">🛒</span>
                <h4>Your FlipMart cart is empty</h4>
                <p>Discover products in our catalog and add them to your cart!</p>
            </div>
        `;
        const btnOpenCo = document.getElementById('btnOpenCheckout');
        if (btnOpenCo) btnOpenCo.disabled = true;
    } else {
        const btnOpenCo = document.getElementById('btnOpenCheckout');
        if (btnOpenCo) btnOpenCo.disabled = false;

        cart.items.forEach(it => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'cart-item-card';
            const itemImgUrl = resolveImageUrl(it.imagePath, it.category);
            const itemFallbackUrl = getCategoryFallbackUrl(it.category);
            itemDiv.innerHTML = `
                <img class="item-thumb" src="${itemImgUrl}" alt="${it.name}" onerror="this.onerror=null; this.src='${itemFallbackUrl}';">
                <div class="item-info">
                    <div class="item-name">${it.name}</div>
                    <div class="item-price-meta">${formatInr(it.basePrice)} each</div>
                    ${it.hasBundleDiscount ? `<span class="item-bundle-tag">🔥 10% Bundle Discount</span>` : ''}
                </div>
                <div class="item-controls">
                    <button class="stepper-btn" onclick="updateItemQty('${it.productId}', 'decrement')">-</button>
                    <span class="item-qty">${it.quantity}</span>
                    <button class="stepper-btn" onclick="updateItemQty('${it.productId}', 'increment')">+</button>
                </div>
                <div class="item-line-total">${formatInr(it.lineTotal)}</div>
                <button class="btn-trash" onclick="removeItem('${it.productId}')">🗑</button>
            `;
            list.appendChild(itemDiv);
        });
    }

    // Breakdown card in Rupees
    const b = cart.breakdown;
    document.getElementById('bdSubtotal').textContent = formatInr(b.subtotal);
    document.getElementById('bdDiscounts').textContent = `-${formatInr(b.totalDiscount)}`;
    document.getElementById('bdTax').textContent = formatInr(b.taxTotal);
    document.getElementById('bdShipping').textContent = b.shippingTotal === 0 ? 'FREE' : formatInr(b.shippingTotal);
    document.getElementById('bdGrandTotal').textContent = formatInr(b.grandTotal);
    document.getElementById('checkoutBtnTotal').textContent = formatInr(b.grandTotal);

    // Coupon display
    const fb = document.getElementById('couponFeedback');
    if (b.appliedCoupon && b.appliedCoupon !== 'null') {
        fb.className = 'coupon-feedback success';
        fb.innerHTML = `✓ Active Coupon: <strong>${b.appliedCoupon}</strong> (-${formatInr(b.couponDiscount)}) <a href="#" onclick="removeCoupon(); return false;" style="color:#ef4444; margin-left:8px;">[Remove]</a>`;
    } else {
        fb.innerHTML = '';
    }
}

async function updateItemQty(productId, action) {
    let handled = false;
    try {
        const res = await fetch(`${API_BASE}/cart/update`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, action })
        });
        if (res.ok) {
            const data = await res.json();
            handled = true;
            if (!data.success && action === 'increment') {
                showToast('Reached maximum available stock!', 'error');
            }
        }
    } catch (_) {}

    if (!handled) {
        const res = LocalCartEngine.updateQuantity(productId, action);
        if (!res.success && action === 'increment') {
            showToast('Reached maximum available stock!', 'error');
        }
    }
    refreshCart();
    fetchProducts();
}

async function removeItem(productId) {
    try {
        await fetch(`${API_BASE}/cart/remove`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId })
        });
    } catch (_) {}
    LocalCartEngine.removeItem(productId);
    showToast('Item removed from cart.');
    refreshCart();
    fetchProducts();
}

async function applyPromoDirectly(code) {
    document.getElementById('promoInput').value = code;
    let handled = false;
    const fb = document.getElementById('couponFeedback');
    try {
        const res = await fetch(`${API_BASE}/cart/coupon`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code })
        });
        if (res.ok) {
            const data = await res.json();
            handled = true;
            if (data.success) {
                fb.className = 'coupon-feedback success';
                fb.textContent = `✓ Coupon "${code}" applied successfully!`;
                showToast(`Promo ${code} applied! 🎉`);
            } else {
                fb.className = 'coupon-feedback error';
                fb.textContent = `⚠ ${data.message}`;
            }
        }
    } catch (_) {}

    if (!handled) {
        const data = LocalCartEngine.applyCoupon(code);
        if (data.success) {
            fb.className = 'coupon-feedback success';
            fb.textContent = `✓ Coupon "${code}" applied successfully!`;
            showToast(`Promo ${code} applied! 🎉`);
        } else {
            fb.className = 'coupon-feedback error';
            fb.textContent = `⚠ ${data.message}`;
        }
    }
    refreshCart();
}

async function removeCoupon() {
    try {
        await fetch(`${API_BASE}/cart/remove-coupon`, { method: 'POST' });
    } catch (_) {}
    LocalCartEngine.removeCoupon();
    document.getElementById('couponFeedback').innerHTML = '';
    showToast('Coupon removed.');
    refreshCart();
}

function openCartDrawer() {
    document.getElementById('cartDrawerBackdrop').classList.add('open');
}

function closeCartDrawer() {
    document.getElementById('cartDrawerBackdrop').classList.remove('open');
}

// --- QUICK VIEW MODAL ---
function openQuickView(productId) {
    const p = allProducts.find(item => item.id === productId);
    if (!p) return;

    const qvImg = document.getElementById('qvImage');
    qvImg.src = resolveImageUrl(p.imagePath, p.category);
    qvImg.onerror = function() {
        this.onerror = null;
        this.src = getCategoryFallbackUrl(p.category);
    };
    document.getElementById('qvCategory').textContent = `${p.categoryIcon} ${p.categoryDisplay}`;
    document.getElementById('qvTitle').textContent = p.name;
    document.getElementById('qvRating').textContent = `⭐ ${p.rating.toFixed(1)} (${formatInr(p.reviewCount).replace('₹', '')} verified reviews)`;
    document.getElementById('qvDesc').textContent = p.description;
    document.getElementById('qvSpecsHtml').innerHTML = p.specsHtml;
    document.getElementById('qvPrice').textContent = p.formattedPrice || formatInr(p.basePrice);

    const stockTag = document.getElementById('qvStockBadge');
    if (p.stockQuantity <= 0) {
        stockTag.className = 'qv-stock-tag badge-stock-out';
        stockTag.textContent = 'Out of Stock';
        document.getElementById('qvAddToCartBtn').disabled = true;
    } else if (p.stockQuantity <= 3) {
        stockTag.className = 'qv-stock-tag badge-stock-low';
        stockTag.textContent = `Only ${p.stockQuantity} left - High Demand`;
        document.getElementById('qvAddToCartBtn').disabled = false;
    } else {
        stockTag.className = 'qv-stock-tag badge-stock-in';
        stockTag.textContent = `In Stock (${p.stockQuantity} available)`;
        document.getElementById('qvAddToCartBtn').disabled = false;
    }

    document.getElementById('qvAddToCartBtn').onclick = () => {
        closeModal('quickViewBackdrop');
        addToCart(p.id);
    };

    openModal('quickViewBackdrop');
}

// --- CHECKOUT & RECEIPT ---
async function openCheckoutModal() {
    closeCartDrawer();
    let cart = null;
    try {
        const res = await fetch(`${API_BASE}/cart`);
        if (res.ok) cart = await res.json();
    } catch (_) {}
    if (!cart || !cart.items || cart.items.length === 0) {
        cart = LocalCartEngine.getCartState();
    }
    if (cart.items.length === 0) {
        showToast('Cart is empty. Add items to proceed.', 'error');
        return;
    }

    // Render summary items
    const list = document.getElementById('checkoutItemsList');
    list.innerHTML = '';
    cart.items.forEach(it => {
        const div = document.createElement('div');
        div.className = 'co-item-row';
        div.innerHTML = `
            <div class="co-item-name-wrap">
                <span class="co-item-qty">${it.quantity}x</span>
                <span title="${it.name}">${it.name}</span>
            </div>
            <span style="font-weight: 700; color: #f8fafc;">${formatInr(it.lineTotal)}</span>
        `;
        list.appendChild(div);
    });

    const countBadge = document.getElementById('coSidebarCount');
    if (countBadge) {
        countBadge.textContent = `${cart.totalItems} item${cart.totalItems === 1 ? '' : 's'}`;
    }

    const b = cart.breakdown;
    document.getElementById('coSubtotal').textContent = formatInr(b.subtotal);
    document.getElementById('coDiscounts').textContent = `-${formatInr(b.totalDiscount)}`;
    document.getElementById('coTax').textContent = formatInr(b.taxTotal);
    document.getElementById('coShipping').textContent = b.shippingTotal === 0 ? 'FREE' : formatInr(b.shippingTotal);
    document.getElementById('coGrand').textContent = formatInr(b.grandTotal);

    const btnSubmit = document.getElementById('btnSubmitOrder');
    if (btnSubmit) {
        btnSubmit.innerHTML = `<span class="btn-lock-icon">🔒</span><span>Confirm & Pay ${formatInr(b.grandTotal)}</span><span class="btn-arrow-icon">➔</span>`;
    }

    document.getElementById('checkoutError').textContent = '';

    // Synchronize payment tabs and active form
    switchPaymentTab(activePaymentMethod || 'CC');

    // Synchronize virtual card initial values
    const ccHolder = document.getElementById('ccHolder');
    const ccNumber = document.getElementById('ccNumber');
    const ccExpiry = document.getElementById('ccExpiry');
    const dispHolder = document.getElementById('vcHolderDisplay');
    const dispNumber = document.getElementById('vcNumberDisplay');
    const dispExpiry = document.getElementById('vcExpiryDisplay');
    const dispBrand = document.getElementById('vcBrandLogo');

    if (ccHolder && dispHolder) {
        dispHolder.textContent = ccHolder.value.trim().toUpperCase() || 'ALEX MORGAN';
    }
    if (ccNumber && dispNumber) {
        let digits = ccNumber.value.replace(/\D/g, '').substring(0, 16);
        let formatted = '';
        for (let i = 0; i < digits.length; i++) {
            if (i > 0 && i % 4 === 0) formatted += '  ';
            formatted += digits[i];
        }
        dispNumber.textContent = formatted || '4532  8901  2345  6789';
        if (dispBrand) {
            if (digits.startsWith('4')) dispBrand.textContent = 'VISA';
            else if (digits.startsWith('5')) dispBrand.textContent = 'MasterCard';
            else if (digits.startsWith('6') || digits.startsWith('8')) dispBrand.textContent = 'RuPay';
            else if (digits.startsWith('3')) dispBrand.textContent = 'AMEX';
            else dispBrand.textContent = 'RuPay';
        }
    }
    if (ccExpiry && dispExpiry) {
        dispExpiry.textContent = ccExpiry.value.trim() || '12/28';
    }

    openModal('checkoutBackdrop');
}

async function handleOrderSubmission() {
    const errDiv = document.getElementById('checkoutError');
    errDiv.textContent = '';

    const payload = {
        name: document.getElementById('custName').value.trim(),
        email: document.getElementById('custEmail').value.trim(),
        address: document.getElementById('custAddress').value.trim(),
        method: activePaymentMethod,
        cardHolder: document.getElementById('ccHolder').value.trim(),
        cardNumber: document.getElementById('ccNumber').value.trim(),
        expiry: document.getElementById('ccExpiry').value.trim(),
        cvv: document.getElementById('ccCvv').value.trim(),
        upiId: document.getElementById('upiId').value.trim(),
        cryptoNetwork: document.getElementById('cryptoNetwork').value,
        walletAddress: document.getElementById('cryptoWallet').value.trim()
    };

    if (!payload.name || !payload.email || !payload.address) {
        errDiv.textContent = '⚠ Please fill in all required customer name, email, and address fields.';
        return;
    }

    let handled = false;
    let data = null;
    try {
        const res = await fetch(`${API_BASE}/checkout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            data = await res.json();
            handled = true;
        }
    } catch (_) {}

    if (!handled) {
        data = LocalCartEngine.checkout(payload);
    }

    if (!data || !data.success) {
        errDiv.textContent = `⚠ ${(data && data.message) || 'Error during payment processing.'}`;
    } else {
        closeModal('checkoutBackdrop');
        lastConfirmedReceipt = data.receiptText;
        document.getElementById('receiptSubText').textContent = `Order Ref: ${data.orderId} • Paid via ${data.paymentMethod}`;
        document.getElementById('receiptContent').textContent = data.receiptText;
        openModal('receiptBackdrop');
        refreshCart();
        fetchProducts();
    }
}

function copyReceiptText() {
    navigator.clipboard.writeText(lastConfirmedReceipt);
    showToast('Tax invoice copied to clipboard! 📋');
}

function downloadReceiptFile() {
    const blob = new Blob([lastConfirmedReceipt], { type: 'text/plain;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `flipmart_invoice_${Date.now()}.txt`;
    a.click();
    showToast('Invoice download started! 💾');
}

// --- MODAL UTILS ---
function openModal(id) {
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 3500);
}
