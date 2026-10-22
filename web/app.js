// FlipMart Web Store Application Logic
const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';
let allProducts = [];
let currentCategory = 'ALL';
let currentSearch = '';
let activeBundleRecommendation = null;
let activePaymentMethod = 'CC';
let lastConfirmedReceipt = '';

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
            await fetch(`${API_BASE}/cart/clear`, { method: 'POST' });
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
            const res = await fetch(`${API_BASE}/cart/add`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ productId: activeBundleRecommendation.recommendedId, quantity: 1, isBundle: true })
            });
            const data = await res.json();
            closeModal('bundleModalBackdrop');
            if (data.success) {
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
            // Fallback for static GitHub Pages hosting requiring .json extension
            res = await fetch(`${API_BASE}/products.json`);
        }
        allProducts = await res.json();
        console.log(`Loaded ${allProducts.length} products from API`);
        renderProducts();
    } catch (e) {
        try {
            // Secondary fallback for relative directory paths on GitHub Pages
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
    const cleanPath = path.startsWith('/') ? path : '/' + path;
    if (window.location.protocol === 'file:') {
        return 'http://localhost:8080' + cleanPath;
    }
    return cleanPath;
}

function getCategoryFallbackUrl(category) {
    switch (category) {
        case 'FASHION':
            return '/assets/mens_slim_shirt.png';
        case 'HOME_KITCHEN':
            return '/assets/electric_kettle.png';
        case 'BOOKS_ESSENTIALS':
            return '/assets/bestseller_books.png';
        case 'DIGITAL':
            return '/assets/devmaster_ide.png';
        case 'ELECTRONICS':
        default:
            return '/assets/aurasound_headphones.png';
    }
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
    try {
        const res = await fetch(`${API_BASE}/cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, quantity: 1, isBundle: false })
        });
        const data = await res.json();
        if (data.success) {
            showToast('Item added to FlipMart cart! 🛒');
            refreshCart();
            fetchProducts();

            // Check if companion bundle recommended
            if (data.bundleRecommendation) {
                showBundleModal(data.bundleRecommendation);
            }
        } else {
            showToast(data.message || 'Could not add item to cart.', 'error');
        }
    } catch (e) {
        console.error('Cart add error:', e);
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
    try {
        const res = await fetch(`${API_BASE}/cart`);
        const cart = await res.json();

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
    } catch (e) {
        console.error('Error refreshing cart:', e);
    }
}

async function updateItemQty(productId, action) {
    const res = await fetch(`${API_BASE}/cart/update`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId, action })
    });
    const data = await res.json();
    if (!data.success && action === 'increment') {
        showToast('Reached maximum available stock!', 'error');
    }
    refreshCart();
    fetchProducts();
}

async function removeItem(productId) {
    await fetch(`${API_BASE}/cart/remove`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId })
    });
    showToast('Item removed from cart.');
    refreshCart();
    fetchProducts();
}

async function applyPromoDirectly(code) {
    document.getElementById('promoInput').value = code;
    const res = await fetch(`${API_BASE}/cart/coupon`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code })
    });
    const data = await res.json();
    const fb = document.getElementById('couponFeedback');
    if (data.success) {
        fb.className = 'coupon-feedback success';
        fb.textContent = `✓ Coupon "${code}" applied successfully!`;
        showToast(`Promo ${code} applied! 🎉`);
    } else {
        fb.className = 'coupon-feedback error';
        fb.textContent = `⚠ ${data.message}`;
    }
    refreshCart();
}

async function removeCoupon() {
    await fetch(`${API_BASE}/cart/remove-coupon`, { method: 'POST' });
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
    const res = await fetch(`${API_BASE}/cart`);
    const cart = await res.json();
    if (cart.items.length === 0) return;

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

    try {
        const res = await fetch(`${API_BASE}/checkout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!data.success) {
            errDiv.textContent = `⚠ ${data.message}`;
        } else {
            closeModal('checkoutBackdrop');
            lastConfirmedReceipt = data.receiptText;
            document.getElementById('receiptSubText').textContent = `Order Ref: ${data.orderId} • Paid via ${data.paymentMethod}`;
            document.getElementById('receiptContent').textContent = data.receiptText;
            openModal('receiptBackdrop');
            refreshCart();
            fetchProducts();
        }
    } catch (e) {
        errDiv.textContent = 'Connection error during payment processing.';
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
