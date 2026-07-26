package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.TopAppBarHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductDetailSheet
import com.example.ui.screens.StoresScreen
import com.example.ui.screens.SupabaseSearchScreen
import androidx.compose.material.icons.filled.CloudQueue
import com.example.ui.theme.MutawajidTheme
import com.example.ui.theme.IndigoPrimary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.theme.IndigoAccent

import com.example.ui.UserSession
import com.example.ui.screens.StoreDashboardScreen
import androidx.compose.material.icons.filled.Key

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MutawajidTheme {
                MutawajidApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MutawajidApp(viewModel: MainViewModel) {
    val context = LocalContext.current

    // State Collection
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val storeInventoryItems by viewModel.storeInventoryItems.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val currencyRates by viewModel.currencyRates.collectAsStateWithLifecycle()

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val favoriteProducts by viewModel.favoriteProducts.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedProductInventory by viewModel.selectedProductInventory.collectAsStateWithLifecycle()

    val allStores by viewModel.allStores.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val isSupabaseSyncing by viewModel.isSupabaseSyncing.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val supabaseSearchResults by viewModel.supabaseSearchResults.collectAsStateWithLifecycle()
    val isSearchingSupabase by viewModel.isSearchingSupabase.collectAsStateWithLifecycle()
    val supabaseSearchError by viewModel.supabaseSearchError.collectAsStateWithLifecycle()

    // Navigation Tab state: 0: Supabase Search, 1: Local Search, 2: Stores, 3: Favorites, 4: Private Dashboard
    var currentTab by remember { mutableIntStateOf(0) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showRatesDialog by remember { mutableStateOf(false) }

    // Toast message trigger
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarHeader(
                userSession = userSession,
                selectedCity = selectedCity,
                onCitySelected = { viewModel.setCityFilter(it) },
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { viewModel.setCurrency(it) },
                onOpenRatesDialog = { showRatesDialog = true },
                onOpenLoginDialog = { showLoginDialog = true },
                onNavigateToDashboard = { currentTab = 4 }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.CloudQueue, contentDescription = "بحث Supabase") },
                    label = { Text("بحث Supabase", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoPrimary,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_supabase_search_tab")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "البحث المحلي") },
                    label = { Text("البحث المحلي", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoPrimary,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_local_search_tab")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Store, contentDescription = "المتاجر") },
                    label = { Text("المتاجر", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoPrimary,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_stores_tab")
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "المفضلة") },
                    label = { Text("المفضلة", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoPrimary,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_favorites_tab")
                )

                // Tab 4: Private Control Panel (for logged-in Store Owners or Super Admin)
                when (userSession) {
                    is UserSession.SuperAdmin -> {
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "لوحة الأدمن") },
                            label = { Text("لوحة الأدمن 👑", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_admin_tab")
                        )
                    }

                    is UserSession.StoreOwner -> {
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "لوحة متجري") },
                            label = { Text("لوحة متجري 🏥", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_store_dashboard_tab")
                        )
                    }

                    is UserSession.Guest -> {
                        // Guest can click Login
                        NavigationBarItem(
                            selected = false,
                            onClick = { showLoginDialog = true },
                            icon = { Icon(Icons.Default.Key, contentDescription = "تسجيل الدخول") },
                            label = { Text("دخول لوحة التحكم", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = IndigoPrimary,
                                unselectedTextColor = IndigoPrimary
                            ),
                            modifier = Modifier.testTag("nav_login_tab")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> SupabaseSearchScreen(
                    searchQuery = searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::setCategoryFilter,
                    isSearching = isSearchingSupabase,
                    searchResults = supabaseSearchResults,
                    errorMessage = supabaseSearchError,
                    favoriteIds = favoriteIds,
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { viewModel.setCurrency(it) },
                    rates = currencyRates,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onSearchTriggered = { viewModel.searchSupabaseDirectly() },
                    onProductSelected = viewModel::selectProduct
                )

                1 -> HomeScreen(
                    searchQuery = searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    onSubmitSearch = viewModel::submitSearch,
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::setCategoryFilter,
                    recentSearches = recentSearches,
                    onClearHistory = viewModel::clearSearchHistory,
                    searchResults = searchResults,
                    favoriteIds = favoriteIds,
                    selectedCurrency = selectedCurrency,
                    rates = currencyRates,
                    onCurrencySelected = { viewModel.setCurrency(it) },
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onProductSelected = viewModel::selectProduct
                )

                2 -> StoresScreen(
                    stores = allStores,
                    selectedCity = selectedCity
                )

                3 -> FavoritesScreen(
                    favoriteProducts = favoriteProducts,
                    selectedCurrency = selectedCurrency,
                    rates = currencyRates,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onProductSelected = viewModel::selectProduct
                )

                4 -> {
                    when (val session = userSession) {
                        is UserSession.SuperAdmin -> {
                            AdminScreen(
                                isImporting = isImporting,
                                onImportCsvUri = viewModel::importCsvFromUri,
                                onImportSampleCsv = viewModel::importSampleCsvText,
                                products = allProducts,
                                stores = allStores,
                                currencyRates = currencyRates,
                                onUpdateRates = viewModel::updateRates,
                                onAddProduct = viewModel::addOrUpdateProduct,
                                onDeleteProduct = viewModel::deleteProduct,
                                onAddStore = viewModel::addOrUpdateStore,
                                onDeleteStore = viewModel::deleteStore,
                                onUpdateInventory = viewModel::updateInventoryItem,
                                onResetData = viewModel::resetToDefaultSampleData,
                                isSupabaseSyncing = isSupabaseSyncing,
                                onSyncFromSupabase = viewModel::syncFromSupabase,
                                onPushToSupabase = viewModel::pushToSupabase,
                                onLogoutAdmin = {
                                    viewModel.logout()
                                    currentTab = 0
                                }
                            )
                        }

                        is UserSession.StoreOwner -> {
                            StoreDashboardScreen(
                                store = session.store,
                                inventoryItems = storeInventoryItems,
                                currencyRates = currencyRates,
                                selectedCurrency = selectedCurrency,
                                isImporting = isImporting,
                                onAddProductToStore = { name, cat, active, unit, price, qty, status ->
                                    viewModel.addStoreProductAndInventory(
                                        session.store.id, name, cat, active, unit, price, qty, status
                                    )
                                },
                                onUpdateInventoryItem = { storeId, prodId, price, qty, status ->
                                    viewModel.updateStoreInventoryDetailed(storeId, prodId, price, qty, status)
                                },
                                onImportCsv = { uri, storeId ->
                                    viewModel.importCsvForSpecificStore(uri, storeId)
                                },
                                onUpdateStoreDetails = { updatedStore ->
                                    viewModel.updateStoreInfo(updatedStore)
                                },
                                onLogout = {
                                    viewModel.logout()
                                    currentTab = 0
                                }
                            )
                        }

                        is UserSession.Guest -> {
                            LaunchedEffect(Unit) { currentTab = 0 }
                        }
                    }
                }
            }

            // Product Detail Modal Sheet
            selectedProduct?.let { product ->
                ProductDetailSheet(
                    product = product,
                    inventoryList = selectedProductInventory,
                    selectedCurrency = selectedCurrency,
                    rates = currencyRates,
                    isFavorite = favoriteIds.contains(product.id),
                    onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                    onDismiss = { viewModel.selectProduct(null) }
                )
            }

            // Rates Dialog
            if (showRatesDialog) {
                RatesQuickDialog(
                    currentRates = currencyRates,
                    onDismiss = { showRatesDialog = false },
                    onUpdate = { sar, usd ->
                        viewModel.updateRates(sar, usd)
                        showRatesDialog = false
                    }
                )
            }

            // Unified Authentication Dialog for Stores and Admin
            if (showLoginDialog) {
                UnifiedLoginDialog(
                    onDismiss = { showLoginDialog = false },
                    onLogin = { username, password ->
                        viewModel.login(username, password) { success, msg ->
                            if (success) {
                                currentTab = 4
                                showLoginDialog = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UnifiedLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var usernameText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    val sampleAccounts = listOf(
        "👑 الأدمن العام" to ("admin" to "1234"),
        "🏥 صيدلية الأمل" to ("amal_pharmacy" to "1234"),
        "🏥 صيدلية العافية" to ("afia_pharmacy" to "1234"),
        "🏥 صيدلية عدن" to ("aden_pharmacy" to "1234"),
        "🚗 مركز التيسير" to ("tayseer_auto" to "1234")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "تسجيل الدخول",
                tint = IndigoPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "تسجيل الدخول إلى لوحة التحكم",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "قم بتسجيل الدخول كمدير نظام (الأدمن) أو صاحب صيدلية / متجر لإدارة المخزون والبيانات الخاصة بك.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    label = { Text("اسم المستخدم (Username)") },
                    placeholder = { Text("مثال: amal_pharmacy أو admin") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_username_input")
                )

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("كلمة المرور (Password)") },
                    placeholder = { Text("مثال: 1234") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "اختصار سريع للحسابات التجريبية:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sampleAccounts.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { (label, creds) ->
                                TextButton(
                                    onClick = {
                                        usernameText = creds.first
                                        passwordText = creds.second
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (usernameText.isNotBlank()) {
                        onLogin(usernameText.trim(), passwordText.trim())
                    }
                },
                modifier = Modifier.testTag("login_submit_btn")
            ) {
                Text("تسجيل الدخول")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun RatesQuickDialog(
    currentRates: com.example.util.CurrencyRates,
    onDismiss: () -> Unit,
    onUpdate: (Double, Double) -> Unit
) {
    var sarText by remember { mutableStateOf(currentRates.yerPerSar.toString()) }
    var usdText by remember { mutableStateOf(currentRates.yerPerUsd.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("أسعار تحويل العملات", fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "حدد سعر صرف الريال السعودي والدولار مقابل الريال اليمني للتحويل الفوري:",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = sarText,
                    onValueChange = { sarText = it },
                    label = { Text("1 ريال سعودي = (ر.ي)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = usdText,
                    onValueChange = { usdText = it },
                    label = { Text("1 دولار أمريكي = (ر.ي)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sar = sarText.toDoubleOrNull() ?: 140.0
                    val usd = usdText.toDoubleOrNull() ?: 530.0
                    onUpdate(sar, usd)
                }
            ) {
                Text("تحديث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
