package com.shoppingcart.service;

import com.shoppingcart.model.BookEssentialProduct;
import com.shoppingcart.model.Category;
import com.shoppingcart.model.ClothingProduct;
import com.shoppingcart.model.DigitalProduct;
import com.shoppingcart.model.ElectronicsProduct;
import com.shoppingcart.model.HomeKitchenProduct;
import com.shoppingcart.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Concrete implementation of InventoryService managing FlipMart's consolidated 52-product catalog.
 * Supports unique sequential IDs ("1" through "52"), legacy code mapping for backwards compatibility,
 * stock reservation, and real-time inventory levels in Indian Rupees (₹).
 * All items feature verified, direct high-resolution commercial product photography.
 */
public class InventoryServiceImpl implements InventoryService {
    private final Map<String, Product> productCatalog = new LinkedHashMap<>();
    private final Map<String, Product> legacyCodeMap = new LinkedHashMap<>();
    private final List<StockChangeListener> stockListeners = new CopyOnWriteArrayList<>();

    public InventoryServiceImpl() {
        initializeSampleCatalog();
    }

    private void initializeSampleCatalog() {
        // =========================================================================
        // 1. ELECTRONICS & GADGETS (IDs: 1 - 13)
        // =========================================================================
        addProduct(new ElectronicsProduct(
                "1",
                "Wireless Noise-Cancelling Headphones",
                "AuraSound Max studio-grade wireless over-ear headphones with 40mm beryllium drivers, active hybrid noise cancellation, spatial audio, and 45-hour quick-charge battery.",
                24999.0, 29999.0,
                6,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
                4.9, 1420,
                "41", // Bundle partner: Multi-port Fast Charger
                24, "AuraSound Labs", 5, "ASM-400X", 0.28
        ), "ELEC-01");

        addProduct(new ElectronicsProduct(
                "2",
                "Apex Pro Mechanical Keyboard",
                "Hot-swappable custom RGB mechanical keyboard with lubricated linear yellow switches, CNC aviation aluminum top plate, and double-shot PBT keycaps.",
                8499.0, 11999.0,
                3,
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80",
                4.8, 980,
                "40", // Bundle partner: Ergonomic Laptop Stand
                36, "ApexTech", 3, "AP-PRO-TKL", 0.95
        ), "ELEC-02");

        addProduct(new ElectronicsProduct(
                "3",
                "4K Gaming Monitor 34\" Curved UltraWide",
                "Immersive 34-inch 165Hz Nano-IPS curved HDR600 gaming display with 1ms G-Sync response, 98% DCI-P3 color gamut, and Thunderbolt 4 dock.",
                49999.0, 64999.0,
                4,
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=600&q=80",
                4.7, 640,
                "2", // Bundle partner: Mechanical Keyboard
                36, "TitanDisplay", 65, "T34-UW-IPS", 6.80
        ), "ELEC-03");

        addProduct(new ElectronicsProduct(
                "4",
                "PulseFit Ultra GPS Smartwatch",
                "Rugged aerospace titanium smartwatch with sapphire crystal glass, multi-band dual-frequency GPS, heart-rate ECG tracking, and 100m water resistance.",
                19999.0, 24999.0,
                8,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                4.6, 2100,
                "15", // Bundle partner: Women's Running Shoes
                12, "PulseFit Bio", 2, "PF-ULTRA-7", 0.07
        ), "ELEC-04");

        addProduct(new ElectronicsProduct(
                "5",
                "Lumix Pro 4K 60FPS Streaming Cam",
                "Broadcast studio 4K webcam with Sony STARVIS sensor, integrated dimmable halo ring light, dual beamforming noise-cancelling microphones, and privacy cover.",
                11499.0, 14999.0,
                7,
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=600&q=80",
                4.8, 1140,
                "2", // Bundle partner: Keyboard
                24, "LumixTech", 5, "LX-4K-PRO", 0.22
        ), "ELEC-05");

        addProduct(new ElectronicsProduct(
                "6",
                "Titan Ultra 49\" Curved OLED Gaming Display",
                "Flagship 49-inch 240Hz 0.03ms Dual QHD (5120x1440) quantum-dot OLED curved gaming workstation display with AI Neo Quantum processor.",
                129999.0, 159999.0,
                2,
                "https://images.unsplash.com/photo-1547119957-637f8679db1e?auto=format&fit=crop&w=600&q=80",
                4.9, 310,
                "2", // Bundle partner: Keyboard
                36, "TitanDisplay", 95, "T49-DQHD-OLED", 9.20
        ), "ELEC-06");

        addProduct(new ElectronicsProduct(
                "7",
                "Sony Alpha 7 IV Full-Frame Mirrorless Camera",
                "33MP full-frame Exmor R CMOS sensor with BIONZ XR processing engine, 4K 60p 10-bit 4:2:2 video, real-time eye AF, and 5-axis optical image stabilization.",
                189999.0, 219999.0,
                5,
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=600&q=80",
                4.9, 520,
                "41", // Bundle partner: Fast Charger
                24, "Sony Alpha", 15, "ILCE-7M4K", 0.66
        ), "ELEC-07");

        addProduct(new ElectronicsProduct(
                "8",
                "Apple iPad Air M2 11-inch Liquid Retina",
                "Ultra-fast Apple M2 chip with 8-core CPU, 12MP landscape front camera with Center Stage, Wi-Fi 6E, and all-day battery life.",
                59900.0, 69900.0,
                8,
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=600&q=80",
                4.8, 1140,
                "11", // Bundle partner: Logitech Mouse
                12, "Apple", 20, "MUWC3HN/A", 0.46
        ), "ELEC-08");

        addProduct(new ElectronicsProduct(
                "9",
                "Bose SoundLink Revolve+ II Bluetooth Speaker",
                "True 360-degree wireless Bluetooth speaker delivering deep, jaw-dropping sound with water- and dust-resistant design (IP55) and flexible fabric handle.",
                24500.0, 29900.0,
                7,
                "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=600&q=80",
                4.7, 830,
                "1", // Bundle partner: Headphones
                12, "Bose Audio", 10, "SL-REV-PLUS2", 0.90
        ), "ELEC-09");

        addProduct(new ElectronicsProduct(
                "10",
                "DJI Mini 4 Pro Drone with RC-2 Remote",
                "Sub-249g ultra-lightweight folding drone with omnidirectional obstacle sensing, 4K/60fps HDR true vertical shooting, and 34-min flight time.",
                89999.0, 104999.0,
                4,
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?auto=format&fit=crop&w=600&q=80",
                4.9, 610,
                "13", // Bundle partner: External SSD
                12, "DJI", 18, "DJI-MINI-4P", 0.25
        ), "ELEC-10");

        addProduct(new ElectronicsProduct(
                "11",
                "Logitech MX Master 3S Wireless Performance Mouse",
                "Quiet Click ergonomic mouse with 8K DPI any-surface glass tracking, MagSpeed electromagnetic scrolling, and customizable thumb buttons.",
                8995.0, 10995.0,
                15,
                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=600&q=80",
                4.9, 3200,
                "40", // Bundle partner: Laptop Stand
                24, "Logitech", 2, "MX-M3S-PRO", 0.14
        ), "ELEC-11");

        addProduct(new ElectronicsProduct(
                "12",
                "Anker Soundcore Motion Boom Outdoor Speaker",
                "Rugged waterproof IPX7 outdoor Bluetooth speaker with titanium drivers, BassUp technology, 24-hour playtime, and integrated powerbank.",
                7999.0, 11999.0,
                10,
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&w=600&q=80",
                4.6, 940,
                "15", // Bundle partner: Running Shoes
                18, "Anker Soundcore", 30, "A3118-BOOM", 1.50
        ), "ELEC-12");

        addProduct(new ElectronicsProduct(
                "13",
                "Seagate One Touch 2TB External USB-C SSD",
                "Ultra-fast portable external solid state drive with up to 1030MB/s NVMe transfer speeds, aluminum travel chassis, and Android/PC/Mac sync.",
                13999.0, 18999.0,
                12,
                "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=600&q=80",
                4.8, 1450,
                "2", // Bundle partner: Keyboard
                36, "Seagate", 4, "STKG2000400", 0.08
        ), "ELEC-13");

        // =========================================================================
        // 2. FASHION & APPAREL (IDs: 14 - 27)
        // =========================================================================
        addProduct(new ClothingProduct(
                "14",
                "Men's Slim Fit Casual Shirt",
                "Premium 100% long-staple breathable Oxford cotton button-down shirt with tailored modern slim silhouette, French seams, and wrinkle-resistant finish.",
                1499.0, 2499.0,
                15,
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=600&q=80",
                4.8, 3420,
                "16", // Bundle partner: Polarized Sunglasses
                "M", "Sky Blue Oxford", "100% Combed Cotton", "Men Slim Fit", 0.28
        ), "CLOTH-01");

        addProduct(new ClothingProduct(
                "15",
                "Women's Running Shoes",
                "High-performance marathon road running shoes engineered with supercritical nitrogen-infused foam midsole and full-length articulated carbon fiber propulsion plate.",
                4999.0, 7999.0,
                5,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                4.9, 1840,
                "4", // Bundle partner: PulseFit Smartwatch
                "7 UK / 8.5 US", "Coral Sunset / White", "Breathable Jacquard Knit & Carbon", "Women Performance Running", 0.38
        ), "CLOTH-02");

        addProduct(new ClothingProduct(
                "16",
                "Polarized Aviator Sunglasses",
                "Ultra-lightweight titanium alloy aviator frame with 9-layer TAC polarized UV400 anti-glare scratch-resistant mirrored lenses.",
                2499.0, 3999.0,
                12,
                "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=600&q=80",
                4.7, 980,
                "14", // Bundle partner: Casual Shirt
                "Standard 58mm", "Matte Gunmetal / Smoke Black", "Titanium Alloy & TAC Polarized", "Unisex Classic", 0.08
        ), "CLOTH-03");

        addProduct(new ClothingProduct(
                "17",
                "CyberWeave Techwear Hoodie",
                "Engineered waterproof softshell hoodie featuring thermal Merino fleece lining, Fidlock magnetic hood adjustments, and taped waterproof storm pockets.",
                3499.0, 4999.0,
                3,
                "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=600&q=80",
                4.9, 870,
                "18", // Bundle partner: Tactical Cargo Joggers
                "L", "Midnight Charcoal", "Merino Fleece / DWR Shell", "Unisex Relaxed", 0.65
        ), "CLOTH-04");

        addProduct(new ClothingProduct(
                "18",
                "Urban Nomad Tactical Cargo Joggers",
                "Articulated ergonomic cargo pants with waterproof ripstop weave, 6 modular utility pockets, key loop, and stretch cuff tapered ankles.",
                2199.0, 3299.0,
                8,
                "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=600&q=80",
                4.7, 1150,
                "17", // Bundle partner: Hoodie
                "32 W (M)", "Olive Tactical Drab", "98% Ripstop Cotton, 2% Spandex", "Men Slim-Tapered", 0.50
        ), "CLOTH-05");

        addProduct(new ClothingProduct(
                "19",
                "AeroStrobe Reflective Running Windbreaker",
                "Ultralight 3M reflective packable running jacket with laser-cut breathability back vents and DWR water-resistant micro-ripstop shell.",
                3999.0, 5499.0,
                2,
                "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=600&q=80",
                4.8, 530,
                "15", // Bundle partner: Running Shoes
                "L", "Reflective Platinum", "AeroShield Micro-Nylon", "Athletic Unisex", 0.32
        ), "CLOTH-06");

        addProduct(new ClothingProduct(
                "20",
                "Classic Heritage Denim Trucker Jacket",
                "Timeless 14oz rigid selvage denim jacket with antique brass buttons, point collar, dual chest flap pockets, and adjustable waist tabs.",
                3299.0, 4999.0,
                10,
                "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=600&q=80",
                4.8, 1620,
                "14", // Bundle partner: Casual Shirt
                "L", "Washed Indigo Blue", "100% Selvedge Denim", "Men Classic", 0.85
        ), "CLOTH-07");

        addProduct(new ClothingProduct(
                "21",
                "Women's Floral Print Chiffon Maxi Dress",
                "Graceful tiered bohemian maxi dress crafted from breathable lightweight chiffon with smocked bodice, ruffled cap sleeves, and botanical floral print.",
                2799.0, 4499.0,
                8,
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=600&q=80",
                4.9, 2140,
                "16", // Bundle partner: Aviator Sunglasses
                "M", "Blush Rose Floral", "Tiered Breathable Chiffon", "Women Evening Casual", 0.35
        ), "CLOTH-08");

        addProduct(new ClothingProduct(
                "22",
                "Premium Leather Minimalist Dress Watch",
                "Ultra-slim 7mm analog dress watch with surgical 316L stainless steel case, genuine Italian vegetable-tanned leather strap, and sapphire crystal glass.",
                6499.0, 9999.0,
                6,
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=600&q=80",
                4.8, 970,
                "14", // Bundle partner: Casual Shirt
                "40mm Dial", "Cognac Brown & Rose Gold", "Italian Full-Grain Leather & Sapphire Glass", "Unisex Formal", 0.12
        ), "CLOTH-09");

        addProduct(new ClothingProduct(
                "23",
                "Waterproof All-Weather Mountain Trekking Boots",
                "Rugged waterproof trail boots featuring oiled nubuck leather upper, breathable Sympatex waterproof membrane, and Vibram mountain claw rubber lug outsole.",
                6999.0, 9999.0,
                5,
                "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?auto=format&fit=crop&w=600&q=80",
                4.9, 1310,
                "18", // Bundle partner: Cargo Joggers
                "9 UK / 10 US", "Earth Umber & Black", "Nubuck Leather & Vibram Rubber", "Men Outdoor Expedition", 1.10
        ), "CLOTH-10");

        addProduct(new ClothingProduct(
                "24",
                "Heavyweight Oversized Cotton Graphic Tee",
                "Boxy dropped-shoulder streetwear t-shirt crafted with dense 240 GSM combed cotton jersey, reinforced ribbed neckband, and silicone enzyme wash.",
                999.0, 1799.0,
                22,
                "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80",
                4.7, 3150,
                "17", // Bundle partner: Techwear Hoodie
                "XL", "Off-White Bone", "240 GSM Combed French Terry Cotton", "Unisex Streetwear", 0.28
        ), "CLOTH-11");

        addProduct(new ClothingProduct(
                "25",
                "Thermal Merino Wool Winter Scarf",
                "Luxuriously soft and warm winter scarf woven from 100% fine Australian Merino wool with classic twisted fringe tassels.",
                1499.0, 2499.0,
                14,
                "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?auto=format&fit=crop&w=600&q=80",
                4.8, 760,
                "17", // Bundle partner: Techwear Hoodie
                "180cm x 35cm", "Charcoal Melange", "100% Australian Merino Wool", "Unisex Luxury", 0.22
        ), "CLOTH-12");

        addProduct(new ClothingProduct(
                "26",
                "Active Seamless High-Waist Yoga Leggings",
                "Squat-proof compression workout tights with seamless supportive high waistband, moisture-wicking four-way stretch knit, and hidden card pocket.",
                1899.0, 2999.0,
                18,
                "https://images.unsplash.com/photo-1506629082955-511b1aa562c8?auto=format&fit=crop&w=600&q=80",
                4.9, 2890,
                "15", // Bundle partner: Running Shoes
                "S", "Forest Sage Green", "4-Way Stretch Compression Nylon-Spandex", "Women Athletic", 0.24
        ), "CLOTH-13");

        addProduct(new ClothingProduct(
                "27",
                "Classic Aviator Leather Weekend Duffle Bag",
                "Handcrafted 45-liter weekend travel duffle made from rich pull-up cowhide leather, heavy duty YKK brass zippers, and detachable padded shoulder strap.",
                5499.0, 8499.0,
                7,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80",
                4.8, 880,
                "16", // Bundle partner: Aviator Sunglasses
                "45 Liters", "Vintage Saddle Tan", "Top-Grain Cowhide Leather & Brass Hardware", "Unisex Travel", 1.45
        ), "CLOTH-14");

        // =========================================================================
        // 3. HOME & KITCHEN (IDs: 28 - 38)
        // =========================================================================
        addProduct(new HomeKitchenProduct(
                "28",
                "Stainless Steel Electric Kettle",
                "1.8 Liter rapid-boil 1500W electric kettle featuring double-wall food-grade 304 stainless steel interior, British Strix thermostat, and auto shut-off boil-dry protection.",
                1799.0, 2999.0,
                14,
                "https://images.unsplash.com/photo-1585675100414-add2e465a136?auto=format&fit=crop&w=600&q=80",
                4.8, 2650,
                "30", // Bundle partner: Ceramic Coffee Mug Set
                "Food-Grade 304 Stainless Steel", "1.8 Liters", 1500, 1.10
        ), "HOME-01");

        addProduct(new HomeKitchenProduct(
                "29",
                "Digital Rapid-Air Touchscreen Air Fryer",
                "5.5 Liter family-size air fryer with 360° rapid hot air convection circulation, digital LED touchscreen with 8 one-touch cooking presets, and dishwasher-safe non-stick basket.",
                6499.0, 9999.0,
                6,
                "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?auto=format&fit=crop&w=600&q=80",
                4.9, 1920,
                "30", // Bundle partner: Ceramic Coffee Mug Set
                "BPA-Free Ceramic Non-Stick", "5.5 Liters", 1700, 4.80
        ), "HOME-02");

        addProduct(new HomeKitchenProduct(
                "30",
                "Ceramic Coffee Mug Set (Set of 6)",
                "Handcrafted artisanal Nordic stoneware coffee mug set of 6 (350ml each) with comfortable ergonomic loop handles, reactive glaze finish, and microwave/dishwasher safety.",
                1199.0, 1999.0,
                20,
                "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80",
                4.9, 1380,
                "28", // Bundle partner: Electric Kettle
                "Artisanal Glazed Stoneware Ceramic", "Set of 6 (350ml each)", 0, 1.95
        ), "HOME-03");

        addProduct(new HomeKitchenProduct(
                "31",
                "Barista Espresso & Cappuccino Machine",
                "Authentic 15-bar Italian high-pressure pump espresso maker with precision thermo-block heating, stainless steel steam milk frother wand, and dual-cup filter basket.",
                18999.0, 26999.0,
                4,
                "https://images.unsplash.com/photo-1534432182912-63863115e106?auto=format&fit=crop&w=600&q=80",
                4.8, 740,
                "30", // Bundle partner: Ceramic Mug Set
                "Brushed Stainless Steel & Die-Cast Aluminum", "1.5L Detachable Tank", 1350, 4.20
        ), "HOME-04");

        addProduct(new HomeKitchenProduct(
                "32",
                "Cast Iron Dutch Oven Pot with Enamel Glaze (5 Qt)",
                "Heavy-duty cast iron round Dutch oven with vibrant chip-resistant enamel coating, tight-fitting self-basting lid, and oven-safe stainless knob up to 260°C.",
                4499.0, 6999.0,
                9,
                "https://images.unsplash.com/photo-1584990347449-397e597df94d?auto=format&fit=crop&w=600&q=80",
                4.9, 1420,
                "34", // Bundle partner: Damascus Knife
                "Vibrant Cherry Red Enamel", "5 Quarts (4.7L)", 0, 5.20
        ), "HOME-05");

        addProduct(new HomeKitchenProduct(
                "33",
                "Ultra-Quiet Smart Ultrasonic Cool Mist Humidifier",
                "Top-fill 4-liter ultrasonic room humidifier with digital LED humidity readout, 360° dual mist nozzle, essential oil diffuser tray, and whisper-quiet 24dB sleep mode.",
                3499.0, 4999.0,
                11,
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?auto=format&fit=crop&w=600&q=80",
                4.7, 1890,
                "38", // Bundle partner: Aroma Diffuser
                "BPA-Free Matte Polycarbonate", "4.0 Liters", 25, 1.40
        ), "HOME-06");

        addProduct(new HomeKitchenProduct(
                "34",
                "Chef's Japanese Damascus Steel 8-inch Santoku Knife",
                "Master culinary knife featuring 67-layer VG-10 Damascus steel core, razor-sharp 15° hand-honed blade angle, and ergonomic G10 military-grade resin handle.",
                3999.0, 5999.0,
                14,
                "https://images.unsplash.com/photo-1593618998160-e34014e67546?auto=format&fit=crop&w=600&q=80",
                4.9, 2300,
                "36", // Bundle partner: Bamboo Cutting Board Set
                "67-Layer VG-10 Damascus High-Carbon Steel", "8 Inches (20cm)", 0, 0.26
        ), "HOME-07");

        addProduct(new HomeKitchenProduct(
                "35",
                "Smart Countertop High-Speed Blender 1200W",
                "Commercial-grade 1200W vortex blender with 6 laser-cut stainless steel blades, 5 smart preset programs, variable speed dial, and 2-liter BPA-free Tritan pitcher.",
                5299.0, 7999.0,
                8,
                "https://images.unsplash.com/photo-1570222094114-d054a817e56b?auto=format&fit=crop&w=600&q=80",
                4.8, 1120,
                "30", // Bundle partner: Ceramic Mugs
                "Tritan Shatter-Proof Pitcher & Stainless Base", "2.0 Liters", 1200, 3.80
        ), "HOME-08");

        addProduct(new HomeKitchenProduct(
                "36",
                "Natural Bamboo 3-Piece Organic Cutting Board Set",
                "Eco-friendly reversible cutting boards crafted from sustainable organic Moso bamboo with deep juice groove channels and easy-grip carved side handles.",
                1299.0, 1999.0,
                25,
                "https://images.unsplash.com/photo-1616401784845-180882ba9ba8?auto=format&fit=crop&w=600&q=80",
                4.9, 3100,
                "34", // Bundle partner: Damascus Knife
                "100% Organically Grown Moso Bamboo", "3-Piece Set (S, M, L)", 0, 1.75
        ), "HOME-09");

        addProduct(new HomeKitchenProduct(
                "37",
                "Professional Non-Stick Ceramic Cookware 10-Piece Set",
                "Complete non-toxic ceramic coated aluminum pots and pans set including 2 frying pans, 2 saucepans with tempered glass lids, sauté pan, and stockpot.",
                9999.0, 14999.0,
                6,
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=600&q=80",
                4.8, 960,
                "28", // Bundle partner: Electric Kettle
                "Hard-Anodized Aluminum & Ceramic Non-Stick", "10-Piece Culinary Set", 0, 7.80
        ), "HOME-10");

        addProduct(new HomeKitchenProduct(
                "38",
                "Smart Aroma Diffuser & Ambient Mood Lamp",
                "500ml ultrasonic essential oil aromatherapy diffuser with 7 ambient color-changing LED mood lights, waterless auto-off safety, and remote control.",
                1999.0, 2999.0,
                16,
                "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=600&q=80",
                4.7, 1540,
                "30", // Bundle partner: Ceramic Mugs
                "Natural Wood Grain & Frosted Glass", "500ml Reservoir", 12, 0.58
        ), "HOME-11");

        // =========================================================================
        // 4. BOOKS & ESSENTIALS (IDs: 39 - 47)
        // =========================================================================
        addProduct(new BookEssentialProduct(
                "39",
                "Hardcover Bestseller Novels Boxed Set",
                "Collector's deluxe slipcased 5-volume boxed set of internationally acclaimed bestselling literary fiction and historical epics, featuring embossed gold foil spine details.",
                1899.0, 2999.0,
                11,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80",
                4.9, 3210,
                "30", // Bundle partner: Coffee Mug Set
                "Penguin Classics & Bloomsbury", "Hardcover Slipcase Box Set", "5 Volumes Deluxe", 2.40
        ), "BOOK-01");

        addProduct(new BookEssentialProduct(
                "40",
                "Ergonomic Aluminum Laptop Stand",
                "Heavy-duty aerospace-grade sandblasted aluminum laptop riser with 360° swivel turntable base, dual-hinge infinite height adjustment, and heat-dissipating cooling cutouts.",
                2299.0, 3499.0,
                16,
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=600&q=80",
                4.9, 2450,
                "2", // Bundle partner: Mechanical Keyboard
                "ErgoCore Precision Labs", "Foldable Aluminum Desktop Riser", "Supports 11-17\" Laptops", 0.85
        ), "BOOK-02");

        addProduct(new BookEssentialProduct(
                "41",
                "Multi-port 140W GaN Fast Charger",
                "Ultra-compact GaN III wall charger featuring 3x USB-C Power Delivery 3.1 (140W max) + 1x USB-A QC 4.0 ports with intelligent dynamic power allocation.",
                3999.0, 5999.0,
                18,
                "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=600&q=80",
                4.9, 4120,
                "1", // Bundle partner: Headphones
                "Veloce Power Tech", "4-Port GaN III Wall Adapter", "140W PD 3.1 & PPS", 0.26
        ), "BOOK-03");

        addProduct(new BookEssentialProduct(
                "42",
                "Smart Desk Cable Management & Dock Hub",
                "Weighted anodized aluminum desktop cable organizer hub with magnetic lock channels, anti-slip silicone base, and integrated headphone docking rest.",
                899.0, 1499.0,
                25,
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=600&q=80",
                4.7, 890,
                "40", // Bundle partner: Laptop Stand
                "DeskCraft Organics", "Desktop Cable Management Hub", "5-Channel Magnetic", 0.35
        ), "BOOK-04");

        addProduct(new BookEssentialProduct(
                "43",
                "Atomic Habits & Peak Performance Collection (3 Books)",
                "Transformative 3-book boxed set on behavior design, mental models, and relentless focus, featuring bestselling titles printed on acid-free cream paper.",
                1299.0, 1999.0,
                20,
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80",
                4.9, 4800,
                "39", // Bundle partner: Novels Boxed Set
                "Random House Business", "Paperback Trilogy Edition", "Bestselling Psychology", 0.95
        ), "BOOK-05");

        addProduct(new BookEssentialProduct(
                "44",
                "Executive Leather Bound Refillable Journal & Pen Set",
                "Hand-stitched full-grain buffalo leather A5 travel notebook containing 240 pages of thick fountain-pen-friendly archival paper with brass twist pen.",
                1499.0, 2299.0,
                15,
                "https://images.unsplash.com/photo-1585776245991-cf89dd7fc73a?auto=format&fit=crop&w=600&q=80",
                4.8, 1650,
                "46", // Bundle partner: Reading Light
                "Craftsman Guild", "Hand-Stitched Distressed Leather A5", "240 Gilded Pages + Brass Pen", 0.65
        ), "BOOK-06");

        addProduct(new BookEssentialProduct(
                "45",
                "The Art of Clean Code & Software Engineering (Hardcover)",
                "The definitive collector's hardcover handbook for modern software craftsmanship, agile principles, design patterns, and scalable refactoring techniques.",
                2199.0, 3299.0,
                12,
                "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?auto=format&fit=crop&w=600&q=80",
                4.9, 2980,
                "48", // Bundle partner: DevMaster IDE
                "O'Reilly Media", "Hardcover Reference Edition", "Illustrated Technical Classic", 1.20
        ), "BOOK-07");

        addProduct(new BookEssentialProduct(
                "46",
                "Dual-Head Clip-On LED Reading & Craft Light",
                "Flexible dual-head reading lamp with 14 eye-caring LEDs, 3 color temperatures (warm, cool, neutral), stepless brightness, and USB-C rechargeable battery.",
                899.0, 1499.0,
                30,
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80",
                4.7, 1420,
                "39", // Bundle partner: Novels Boxed Set
                "Lumina Vision Tech", "Rechargeable USB-C LED Gooseneck", "3 Color Modes, Stepless Dimming", 0.18
        ), "BOOK-08");

        addProduct(new BookEssentialProduct(
                "47",
                "Premium Weighted Anti-Fatigue Desk Standing Mat",
                "High-density polyurethane ergonomic standing mat with beveled anti-trip edges, cushioned shock absorption, and textured acupressure massage mounds.",
                2799.0, 4199.0,
                14,
                "https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?auto=format&fit=crop&w=600&q=80",
                4.8, 890,
                "40", // Bundle partner: Laptop Stand
                "ErgoComfort Labs", "High-Density Polyurethane Foam", "80cm x 50cm Non-Slip Beveled", 1.90
        ), "BOOK-09");

        // =========================================================================
        // 5. DIGITAL & SOFTWARE (IDs: 48 - 52)
        // =========================================================================
        addProduct(new DigitalProduct(
                "48",
                "DevMaster Cloud IDE Suite",
                "Perpetual enterprise license for the next-gen AI-augmented developer environment with cloud workspace synchronization and remote pair coding.",
                5999.0, 8999.0,
                100,
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=600&q=80",
                5.0, 3120,
                "49", // Bundle partner: AI Masterclass
                "https://cdn.flipmart.in/releases/devmaster-ide-v3.pkg",
                520.0, "DVMST-ENT-XXXX-XXXX-99", true
        ), "DIGI-01");

        addProduct(new DigitalProduct(
                "49",
                "AI Architecture Masterclass 2026",
                "Comprehensive 60-hour hands-on video curriculum covering deep generative AI systems, agentic coding workflows, and production cloud infrastructure.",
                3499.0, 6999.0,
                100,
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80",
                4.9, 4500,
                "48", // Bundle partner: DevMaster IDE
                "https://academy.flipmart.in/curriculum/ai-arch-2026.zip",
                4200.0, "FLIP-CERT-LIFETIME-PASS", true
        ), "DIGI-02");

        addProduct(new DigitalProduct(
                "50",
                "SynthWave Cyber Audio & Sample Pack",
                "Royalty-free premium audio production sample pack with 1,200 analog synth loops, punchy 80s drum hits, and futuristic cyber sound effects.",
                1999.0, 3499.0,
                100,
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
                4.8, 1780,
                "1", // Bundle partner: Headphones
                "https://soundvault.flipmart.in/packs/synthwave-cyber-vault.zip",
                1850.0, "VAULT-PRO-ROYALTY-FREE", true
        ), "DIGI-03");

        addProduct(new DigitalProduct(
                "51",
                "SecureVPN Enterprise Lifetime Privacy License",
                "No-logs audited military-grade AES-256 encrypted VPN with unlimited device connections, high-speed 10Gbps WireGuard servers across 90 countries, and malware blocker.",
                2999.0, 5999.0,
                100,
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=600&q=80",
                4.9, 3850,
                "48", // Bundle partner: DevMaster IDE
                "https://vpn.flipmart.in/installer/securevpn-pro.exe",
                45.0, "SVPN-ENT-LIFETIME-PASS-99", true
        ), "DIGI-04");

        addProduct(new DigitalProduct(
                "52",
                "4K Cinematic LUTs & Video Presets Megapack",
                "Professional color grading suite containing 150+ high-dynamic range 3D LUTs for DaVinci Resolve, Premiere Pro, and Final Cut, calibrated for cinematic color spaces.",
                1499.0, 2999.0,
                100,
                "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?auto=format&fit=crop&w=600&q=80",
                4.8, 1220,
                "50", // Bundle partner: SynthWave Pack
                "https://filmlab.flipmart.in/luts/cinematic-masterpack.zip",
                850.0, "LUT-MASTER-ULTRA-2026", true
        ), "DIGI-05");
    }

    private void addProduct(Product p, String legacyCode) {
        productCatalog.put(p.getId(), p);
        if (legacyCode != null && !legacyCode.trim().isEmpty()) {
            legacyCodeMap.put(legacyCode.trim().toUpperCase(), p);
        }
    }

    private void addProduct(Product p) {
        addProduct(p, null);
    }

    @Override
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(new ArrayList<>(productCatalog.values()));
    }

    @Override
    public Product getProductById(String id) {
        if (id == null) return null;
        String trimmed = id.trim();
        Product p = productCatalog.get(trimmed);
        if (p != null) return p;
        return legacyCodeMap.get(trimmed.toUpperCase());
    }

    @Override
    public List<Product> getProductsByCategory(Category category) {
        if (category == null || category == Category.ALL) {
            return getAllProducts();
        }
        return productCatalog.values().stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String query, Category category) {
        String lowerQuery = query != null ? query.trim().toLowerCase() : "";
        return productCatalog.values().stream()
                .filter(p -> (category == null || category == Category.ALL || p.getCategory() == category))
                .filter(p -> lowerQuery.isEmpty() ||
                             p.getId().equals(lowerQuery) ||
                             p.getName().toLowerCase().contains(lowerQuery) ||
                             p.getDescription().toLowerCase().contains(lowerQuery) ||
                             p.getCategorySpecificHighlight().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkAvailability(String productId, int requestedQuantity) {
        Product p = getProductById(productId);
        return p != null && p.getStockQuantity() >= requestedQuantity;
    }

    @Override
    public synchronized boolean deductStock(String productId, int quantity) {
        Product p = getProductById(productId);
        if (p == null) return false;
        boolean success = p.decrementStock(quantity);
        if (success) {
            notifyStockListeners(p.getId(), p.getStockQuantity());
        }
        return success;
    }

    @Override
    public synchronized void restoreStock(String productId, int quantity) {
        Product p = getProductById(productId);
        if (p != null) {
            p.incrementStock(quantity);
            notifyStockListeners(p.getId(), p.getStockQuantity());
        }
    }

    @Override
    public int getAvailableStock(String productId) {
        Product p = getProductById(productId);
        return p != null ? p.getStockQuantity() : 0;
    }

    @Override
    public void addStockChangeListener(StockChangeListener listener) {
        if (listener != null) {
            stockListeners.add(listener);
        }
    }

    @Override
    public void removeStockChangeListener(StockChangeListener listener) {
        stockListeners.remove(listener);
    }

    private void notifyStockListeners(String productId, int newStock) {
        for (StockChangeListener listener : stockListeners) {
            try {
                listener.onStockChanged(productId, newStock);
            } catch (Exception ignored) {}
        }
    }
}
