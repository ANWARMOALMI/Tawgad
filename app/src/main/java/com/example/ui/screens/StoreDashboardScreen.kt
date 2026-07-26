package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoreEntity
import com.example.data.StoreInventoryItemDetail
import com.example.ui.components.DeveloperFooter
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoPrimary
import com.example.util.Currency
import com.example.util.CurrencyFormatter
import com.example.util.CurrencyRates
import com.example.util.ProductImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDashboardScreen(
    store: StoreEntity,
    inventoryItems: List<StoreInventoryItemDetail>,
    currencyRates: CurrencyRates,
    selectedCurrency: Currency,
    isImporting: Boolean,
    onAddProductToStore: (name: String, category: String, activeIngredient: String, unit: String, priceYer: Double, stockQuantity: Int, stockStatus: String) -> Unit,
    onUpdateInventoryItem: (storeId: Long, productId: Long, priceYer: Double, stockQuantity: Int, stockStatus: String) -> Unit,
    onImportCsv: (Uri, Long) -> Unit,
    onUpdateStoreDetails: (StoreEntity) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<StoreInventoryItemDetail?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportCsv(it, store.id) }
    }

    val filteredItems = remember(inventoryItems, searchQuery, showLowStockOnly) {
        inventoryItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.productName.contains(searchQuery, ignoreCase = true) ||
                    item.productActiveIngredient.contains(searchQuery, ignoreCase = true)
            val matchesLowStock = !showLowStockOnly || (item.stockStatus == "كمية محدودة" || item.stockQuantity <= 5 || item.stockStatus == "غير متوفر" || item.stockQuantity == 0)
            matchesSearch && matchesLowStock
        }
    }

    val totalItems = inventoryItems.size
    val availableCount = inventoryItems.count { it.stockStatus == "متوفر" }
    val lowStockCount = inventoryItems.count { it.stockStatus == "كمية محدودة" || it.stockQuantity <= 5 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Store Header Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(IndigoPrimary, IndigoAccent)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = store.name,
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = store.category,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📍 ${store.city} - ${store.address}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("store_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "تسجيل خروج",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Summary KPI Cards ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "الأصناف بمتجرك",
                        value = "$totalItems صنف",
                        icon = Icons.Default.Inventory,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "أصناف متوفرة",
                        value = "$availableCount صنف",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "كميات محدودة",
                        value = "$lowStockCount صنف",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Tabs ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("المخزون والكميات", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.List, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("بيانات المتجر والتواصل", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ContactPhone, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Store Inventory Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // --- Store Dashboard Summary & Support Card ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_summary_support_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ملخص مؤشرات متجرك 📊",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IndigoPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "إجمالي المدرج: $totalItems صنف",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFE65100),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("الأصناف القليلة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$lowStockCount صنف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("المتوفر بالكامل", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$availableCount صنف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Quick Contact Support Information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "مطور النظام: المعلمي سوفت 💻",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "للدعم الفني والاستفسار: +967772991151",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IndigoPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+967772991151",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (lowStockCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (showLowStockOnly) Color(0xFFFFE0B2) else Color(0xFFFFF3E0)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stock_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "تنبيه المخزون",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "تنبيه المخزون المنخفض! ⚠️",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                        Text(
                                            text = "يوجد $lowStockCount صنف شارف على النفاد أو نفد بالكامل.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFBF360C)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showLowStockOnly = !showLowStockOnly },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (showLowStockOnly) Color(0xFFE65100) else Color(0xFFF57C00)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (showLowStockOnly) "عرض الكل" else "تصفية النواقص",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddProductDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("store_add_product_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة صنف جديد", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            enabled = !isImporting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("store_import_excel_btn")
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد Excel لمتجري", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search within store items
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث داخل مخزون متجرك...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة للبحث" else "لم تقم بإضافة أي أصناف لمخزون متجرك بعد",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredItems, key = { it.productId }) { item ->
                                StoreItemCard(
                                    item = item,
                                    currencyRates = currencyRates,
                                    selectedCurrency = selectedCurrency,
                                    onEdit = { editingItem = item }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Store Info & Contact Profile Tab
                StoreProfileEditor(
                    store = store,
                    onSave = onUpdateStoreDetails
                )
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddStoreProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, category, activeIngredient, unit, priceYer, stockQty, stockStatus ->
                onAddProductToStore(name, category, activeIngredient, unit, priceYer, stockQty, stockStatus)
                showAddProductDialog = false
            }
        )
    }

    // Edit Item Dialog
    editingItem?.let { item ->
        EditStoreItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { priceYer, stockQty, stockStatus ->
                onUpdateInventoryItem(store.id, item.productId, priceYer, stockQty, stockStatus)
                editingItem = null
            }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreInventoryItemDetail,
    currencyRates: CurrencyRates,
    selectedCurrency: Currency,
    onEdit: () -> Unit
) {
    val categoryColor = ProductImageUtils.getCategoryColor(item.productCategory)
    val imageUrl = ProductImageUtils.getProductImageUrl(item.productName, item.productCategory)
    val formattedPrice = CurrencyFormatter.formatPrice(item.priceYer, selectedCurrency, currencyRates)

    val (statusColor, statusBg) = when (item.stockStatus) {
        "متوفر" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
        "كمية محدودة" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0))
        else -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.productActiveIngredient.isNotBlank()) {
                    Text(
                        text = item.productActiveIngredient,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedPrice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = "${item.stockStatus} (${item.stockQuantity} ${item.productUnit})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل السعر والكمية",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StoreProfileEditor(
    store: StoreEntity,
    onSave: (StoreEntity) -> Unit
) {
    var name by remember(store) { mutableStateOf(store.name) }
    var city by remember(store) { mutableStateOf(store.city) }
    var address by remember(store) { mutableStateOf(store.address) }
    var phone by remember(store) { mutableStateOf(store.phone) }
    var whatsapp by remember(store) { mutableStateOf(store.whatsapp) }
    var workingHours by remember(store) { mutableStateOf(store.workingHours) }
    var username by remember(store) { mutableStateOf(store.username) }
    var password by remember(store) { mutableStateOf(store.password) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("بيانات المتجر والتواصل", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("تحديد أرقام الهاتف والواتساب والعنوان لتسهيل وصول العملاء إليك", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم المتجر / الصيدلية") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("المدينة") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("ساعات العمل") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("العنوان بالتفصيل") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الاتصال") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("رقم الواتساب") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("بيانات تسجيل الدخول للوحة التحكم", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم للكلمة المرور") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        store.copy(
                            name = name,
                            city = city,
                            address = address,
                            phone = phone,
                            whatsapp = whatsapp,
                            workingHours = workingHours,
                            username = username,
                            password = password
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ البيانات والتعديلات", fontWeight = FontWeight.Bold)
            }
        }

        item {
            DeveloperFooter()
        }
    }
}

@Composable
private fun AddStoreProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, activeIngredient: String, unit: String, priceYer: Double, stockQuantity: Int, stockStatus: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("صيدليات وأدوية") }
    var activeIngredient by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("علبة") }
    var priceText by remember { mutableStateOf("") }
    var stockQtyText by remember { mutableStateOf("10") }
    var stockStatus by remember { mutableStateOf("متوفر") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة صنف جديد لمخزون متجرك", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج / الدواء / القطعة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = activeIngredient,
                    onValueChange = { activeIngredient = it },
                    label = { Text("المادة الفعالة / رقم القطعة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("السعر (ريال يمني)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stockQtyText,
                        onValueChange = { stockQtyText = it },
                        label = { Text("الكمية المتاحة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة (علبة/قطعة)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stockStatus,
                        onValueChange = { stockStatus = it },
                        label = { Text("الحالة (متوفر/غير متوفر)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val qty = stockQtyText.toIntOrNull() ?: 0
                    if (name.isNotBlank() && price > 0) {
                        onConfirm(name, category, activeIngredient, unit, price, qty, stockStatus)
                    }
                }
            ) {
                Text("إضافة للمخزون")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun EditStoreItemDialog(
    item: StoreInventoryItemDetail,
    onDismiss: () -> Unit,
    onConfirm: (priceYer: Double, stockQuantity: Int, stockStatus: String) -> Unit
) {
    var priceText by remember { mutableStateOf(item.priceYer.toLong().toString()) }
    var stockQtyText by remember { mutableStateOf(item.stockQuantity.toString()) }
    var stockStatus by remember { mutableStateOf(item.stockStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل كمية وسعر: ${item.productName}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("السعر (بالريال اليمني YER)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stockQtyText,
                    onValueChange = { stockQtyText = it },
                    label = { Text("الكمية العددية المتبقية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("حالة التوفر بالمحل:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("متوفر", "كمية محدودة", "غير متوفر").forEach { status ->
                        FilterChip(
                            selected = stockStatus == status,
                            onClick = { stockStatus = status },
                            label = { Text(status, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: item.priceYer
                    val qty = stockQtyText.toIntOrNull() ?: item.stockQuantity
                    onConfirm(price, qty, stockStatus)
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
