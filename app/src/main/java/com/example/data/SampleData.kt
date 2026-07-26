package com.example.data

object SampleData {
    val sampleStores = listOf(
        StoreEntity(
            id = 1,
            name = "صيدلية الأمل الحديثة",
            category = "صيدلية",
            city = "صنعاء",
            address = "شارع الزبيري - بجانب المستشفى الجمهوري",
            phone = "01200100",
            whatsapp = "967771234567",
            workingHours = "24 ساعة",
            distanceKm = 0.8,
            username = "amal_pharmacy",
            password = "1234"
        ),
        StoreEntity(
            id = 2,
            name = "صيدلية العافية المركزية",
            category = "صيدلية",
            city = "صنعاء",
            address = "شارع حدة - دوار الرويشان",
            phone = "01400200",
            whatsapp = "967772345678",
            workingHours = "8:00 ص - 12:00 م",
            distanceKm = 1.5,
            username = "afia_pharmacy",
            password = "1234"
        ),
        StoreEntity(
            id = 3,
            name = "صيدلية عدن الدولية",
            category = "صيدلية",
            city = "عدن",
            address = "كريتر - شارع أروى مقابل البنك الأهلي",
            phone = "02250300",
            whatsapp = "967733456789",
            workingHours = "24 ساعة",
            distanceKm = 2.1,
            username = "aden_pharmacy",
            password = "1234"
        ),
        StoreEntity(
            id = 4,
            name = "صيدلية ابن سينا النموذجية",
            category = "صيدلية",
            city = "تعز",
            address = "شارع جمال - جولة المسبح",
            phone = "04210400",
            whatsapp = "967774567890",
            workingHours = "8:00 ص - 11:30 م",
            distanceKm = 0.5,
            username = "sina_pharmacy",
            password = "1234"
        ),
        StoreEntity(
            id = 5,
            name = "مركز التيسير لقطع غيار تويوتا",
            category = "قطع غيار سيارات",
            city = "صنعاء",
            address = "شارع ستين شمال - الحصبة",
            phone = "01310500",
            whatsapp = "967770112233",
            workingHours = "8:00 ص - 8:00 م",
            distanceKm = 3.2,
            username = "tayseer_auto",
            password = "1234"
        ),
        StoreEntity(
            id = 6,
            name = "المحارفي لقطع غيار الهونداي والكوري",
            category = "قطع غيار سيارات",
            city = "صنعاء",
            address = "شارع خولان - تقاطع بيت بوس",
            phone = "01610600",
            whatsapp = "967773445566",
            workingHours = "8:00 ص - 8:30 م",
            distanceKm = 2.8,
            username = "maharfi_auto",
            password = "1234"
        ),
        StoreEntity(
            id = 7,
            name = "القمة لقطع غيار نيسان وميتسوبيشي",
            category = "قطع غيار سيارات",
            city = "عدن",
            address = "الشيخ عثمان - شارع المدارس",
            phone = "02380700",
            whatsapp = "967734556677",
            workingHours = "8:30 ص - 8:00 م",
            distanceKm = 4.0,
            username = "qimma_auto",
            password = "1234"
        ),
        StoreEntity(
            id = 8,
            name = "العالمية للأدوات الصناعية والطاقة الشمسية",
            category = "أدوات ومعدات",
            city = "صنعاء",
            address = "شارع علي عبدالمغني - وسط البلد",
            phone = "01270800",
            whatsapp = "967775667788",
            workingHours = "8:00 ص - 7:30 م",
            distanceKm = 1.1,
            username = "alamia_tools",
            password = "1234"
        ),
        StoreEntity(
            id = 9,
            name = "التقنية الرقمية للإلكترونيات",
            category = "إلكترونيات وتكنولوجيا",
            city = "المكلا",
            address = "شارع الشرج - المجمع التجاري",
            phone = "05300900",
            whatsapp = "967716778899",
            workingHours = "9:00 ص - 10:00 م",
            distanceKm = 1.9,
            username = "tech_muka",
            password = "1234"
        )
    )

    val sampleProducts = listOf(
        // Pharmaceuticals
        ProductEntity(
            id = 1,
            name = "بنادول أدفانس 500 ملغ (24 قرص)",
            category = "صيدليات وأدوية",
            description = "مسكن سريع للآلام الخفيفة والمتوسطة ومخفض للحرارة",
            activeIngredient = "Paracetamol 500mg",
            unit = "علبة"
        ),
        ProductEntity(
            id = 2,
            name = "أوجمنتين 1 غرام (14 قرص)",
            category = "صيدليات وأدوية",
            description = "مضاد حيوي واسع المجال لالتهابات الجهاز التنفسي والمسالك",
            activeIngredient = "Amoxicillin 875mg + Clavulanic acid 125mg",
            unit = "علبة"
        ),
        ProductEntity(
            id = 3,
            name = "فولتارين كبسولات 50 ملغ",
            category = "صيدليات وأدوية",
            description = "مضاد للالتهابات ومسكن لآلام المفاصل والعضلات",
            activeIngredient = "Diclofenac Potassium 50mg",
            unit = "شريط"
        ),
        ProductEntity(
            id = 4,
            name = "أوتريفين بخاخ أنف للكبار",
            category = "صيدليات وأدوية",
            description = "مزيل للاحتقان الأنفي والتورم الناتجة عن الزكام والحساسية",
            activeIngredient = "Xylometazoline 0.1%",
            unit = "عبوة"
        ),
        ProductEntity(
            id = 5,
            name = "كبسولات أوميغا 3 زيت السمك (60 كبسولة)",
            category = "صيدليات وأدوية",
            description = "مكمل غذائي لدعم صحة القلب والدماغ والعيون",
            activeIngredient = "Fish Oil 1000mg (EPA/DHA)",
            unit = "علبة"
        ),

        // Auto Spare Parts
        ProductEntity(
            id = 6,
            name = "فلتر زيت تويوتا كامري 2018-2024 الأصلي",
            category = "قطع غيار سيارات",
            description = "فلتر محرك أصلي من وكالة تويوتا حماية متكاملة للمحرك",
            activeIngredient = "رقم القطعة: 15601-YZZT1",
            unit = "قطعة"
        ),
        ProductEntity(
            id = 7,
            name = "فحمات فرامل أمامي هونداي سوناتا / توسان",
            category = "قطع غيار سيارات",
            description = "طقم فحمات فرامل سيراميك أصلية هادئة وقوية التوقف",
            activeIngredient = "رقم القطعة: 58101-C1A00",
            unit = "طقم"
        ),
        ProductEntity(
            id = 8,
            name = "شمعات احتراق (بواجي) نيسان باترول / بتمول (4 قطع)",
            category = "قطع غيار سيارات",
            description = "بواجي ليزر إيريديوم أصلية لرفع كفاءة الاحتراق واستهلاك الوقود",
            activeIngredient = "رقم القطعة: 22401-ED815",
            unit = "طقم (4)"
        ),
        ProductEntity(
            id = 9,
            name = "مساعدات فرامل خلفية تويوتا كورولا 2015-2020",
            category = "قطع غيار سيارات",
            description = "مساعد غاز وزيت لامتصاص الصدمات وتحسين ثبات المركبة",
            activeIngredient = "رقم القطعة: 48530-09R00",
            unit = "حبة"
        ),

        // Hardware & Solar Equipment
        ProductEntity(
            id = 10,
            name = "مثقاب كهربائي بوش 13 ملغ 650 واط",
            category = "أدوات ومعدات",
            description = "دريل ثقب احترافي مع خاصية الطرق وحقيبة ألومنيوم كاملة",
            activeIngredient = "Bosch GSB 13 RE Professional",
            unit = "جهاز"
        ),
        ProductEntity(
            id = 11,
            name = "محول طاقة شمسية إنفرتر 3.5 كيلو واط هايبرد",
            category = "أدوات ومعدات",
            description = "إنفرتر هجين ذكي يعمل مع البطاريات والشبكة وشاشات التحكم",
            activeIngredient = "Must / Felicity 3.5KW 24V",
            unit = "جهاز"
        ),

        // Electronics
        ProductEntity(
            id = 12,
            name = "شاحن سريع أنكر 65 واط GaNPrime منفذين",
            category = "إلكترونيات وتكنولوجيا",
            description = "شاحن جداري ذكي فائق السرعة للهواتف واللابتوبات",
            activeIngredient = "Anker 735 Charger 65W",
            unit = "قطعة"
        )
    )

    val sampleInventory = listOf(
        // Panadol
        StoreInventoryEntity(storeId = 1, productId = 1, priceYer = 1200.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 2, productId = 1, priceYer = 1100.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 3, productId = 1, priceYer = 1300.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 4, productId = 1, priceYer = 1150.0, stockStatus = "كمية محدودة"),

        // Augmentin
        StoreInventoryEntity(storeId = 1, productId = 2, priceYer = 6800.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 2, productId = 2, priceYer = 6500.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 4, productId = 2, priceYer = 6700.0, stockStatus = "متوفر"),

        // Voltaren
        StoreInventoryEntity(storeId = 1, productId = 3, priceYer = 2800.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 3, productId = 3, priceYer = 2900.0, stockStatus = "متوفر"),

        // Otrivin
        StoreInventoryEntity(storeId = 2, productId = 4, priceYer = 2200.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 4, productId = 4, priceYer = 2100.0, stockStatus = "متوفر"),

        // Omega 3
        StoreInventoryEntity(storeId = 1, productId = 5, priceYer = 14500.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 2, productId = 5, priceYer = 13800.0, stockStatus = "كمية محدودة"),

        // Toyota Oil Filter
        StoreInventoryEntity(storeId = 5, productId = 6, priceYer = 4500.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 6, productId = 6, priceYer = 4800.0, stockStatus = "متوفر"),

        // Hyundai Brake Pads
        StoreInventoryEntity(storeId = 6, productId = 7, priceYer = 12500.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 7, productId = 7, priceYer = 13000.0, stockStatus = "متوفر"),

        // Nissan Spark Plugs
        StoreInventoryEntity(storeId = 7, productId = 8, priceYer = 8000.0, stockStatus = "متوفر"),
        StoreInventoryEntity(storeId = 5, productId = 8, priceYer = 8500.0, stockStatus = "كمية محدودة"),

        // Toyota Shock Absorber
        StoreInventoryEntity(storeId = 5, productId = 9, priceYer = 22000.0, stockStatus = "متوفر"),

        // Bosch Drill
        StoreInventoryEntity(storeId = 8, productId = 10, priceYer = 38000.0, stockStatus = "متوفر"),

        // Solar Inverter
        StoreInventoryEntity(storeId = 8, productId = 11, priceYer = 185000.0, stockStatus = "متوفر"),

        // Anker Charger
        StoreInventoryEntity(storeId = 9, productId = 12, priceYer = 18500.0, stockStatus = "متوفر")
    )
}
