package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FavoriteEntity
import com.example.data.InventoryWithStoreDetails
import com.example.data.ProductEntity
import com.example.data.ProductSearchResult
import com.example.data.SearchHistoryEntity
import com.example.data.StoreEntity
import com.example.data.StoreInventoryEntity
import com.example.util.CsvExcelImporter
import com.example.util.Currency
import com.example.util.CurrencyRates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

sealed class UserSession {
    object Guest : UserSession()
    object SuperAdmin : UserSession()
    data class StoreOwner(val store: StoreEntity) : UserSession()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val storeDao = db.storeDao()
    private val inventoryDao = db.inventoryDao()
    private val favoriteDao = db.favoriteDao()
    private val searchHistoryDao = db.searchHistoryDao()
    private val supabaseRepo = com.example.data.SupabaseSyncRepository(db)

    // User Session State
    private val _userSession = MutableStateFlow<UserSession>(UserSession.Guest)
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _isSupabaseSyncing = MutableStateFlow(false)
    val isSupabaseSyncing: StateFlow<Boolean> = _isSupabaseSyncing.asStateFlow()

    private val _supabaseSearchResults = MutableStateFlow<List<com.example.network.SupabaseProductDto>>(emptyList())
    val supabaseSearchResults: StateFlow<List<com.example.network.SupabaseProductDto>> = _supabaseSearchResults.asStateFlow()

    private val _isSearchingSupabase = MutableStateFlow(false)
    val isSearchingSupabase: StateFlow<Boolean> = _isSearchingSupabase.asStateFlow()

    private val _supabaseSearchError = MutableStateFlow<String?>(null)
    val supabaseSearchError: StateFlow<String?> = _supabaseSearchError.asStateFlow()

    init {
        // Ensure sample data is populated if empty
        viewModelScope.launch {
            AppDatabase.populateDatabase(db)
        }
    }

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")
    val selectedCity = MutableStateFlow("الكل")

    // --- Currency & Rates ---
    val selectedCurrency = MutableStateFlow(Currency.YER)
    val currencyRates = MutableStateFlow(CurrencyRates())

    // --- Selected Product Sheet State ---
    val selectedProduct = MutableStateFlow<ProductEntity?>(null)

    // --- Admin & Toast Message State ---
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Reactive Data Streams ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<ProductSearchResult>> = combine(
        searchQuery,
        selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        productDao.searchProducts(query, category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentSearches: StateFlow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteProducts: StateFlow<List<ProductSearchResult>> = favoriteDao.getFavoriteProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<Long>> = favoriteDao.getAllFavoriteIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProductInventory: StateFlow<List<InventoryWithStoreDetails>> = combine(
        selectedProduct,
        selectedCity
    ) { product, city ->
        Pair(product, city)
    }.flatMapLatest { (product, city) ->
        if (product != null) {
            inventoryDao.getProductInventoryInStores(product.id, city)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStores: StateFlow<List<StoreEntity>> = combine(
        selectedCity,
        selectedCategory
    ) { city, _ ->
        city
    }.flatMapLatest { city ->
        if (city == "الكل") storeDao.getAllStores() else storeDao.getStoresByCity(city)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<ProductEntity>> = productDao.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val storeInventoryItems: StateFlow<List<com.example.data.StoreInventoryItemDetail>> = userSession
        .flatMapLatest { session ->
            if (session is UserSession.StoreOwner) {
                inventoryDao.getStoreInventoryDetailed(session.store.id)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Authentication Actions ---

    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        val cleanUsername = username.trim()
        val cleanPassword = password.trim()

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
            onResult(false, "يرجى كتابة اسم المستخدم وكلمة المرور")
            return
        }

        // 1. Check SuperAdmin Credentials
        if ((cleanUsername.equals("admin", ignoreCase = true) || cleanUsername.equals("ادمن", ignoreCase = true)) &&
            (cleanPassword == "1234" || cleanPassword == "admin")) {
            _userSession.value = UserSession.SuperAdmin
            _toastMessage.value = "مرحباً بك! تم الدخول كـ مدير النظام العام 👑"
            onResult(true, "نجح الدخول كأدمن")
            return
        }

        // 2. Check Store/Pharmacy Credentials
        viewModelScope.launch {
            val store = storeDao.getStoreByCredentials(cleanUsername, cleanPassword)
            if (store != null) {
                _userSession.value = UserSession.StoreOwner(store)
                _toastMessage.value = "أهلاً وسهلاً! تم الدخول لـ ${store.name} 🏥"
                onResult(true, "نجح الدخول للمتجر")
            } else {
                onResult(false, "اسم المستخدم أو كلمة المرور غير صحيحة")
            }
        }
    }

    fun logout() {
        _userSession.value = UserSession.Guest
        _toastMessage.value = "تم تسجيل الخروج"
    }

    // --- Actions ---

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun submitSearch(query: String) {
        if (query.trim().isNotEmpty()) {
            searchQuery.value = query
            viewModelScope.launch {
                searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
        }
    }

    fun setCategoryFilter(category: String) {
        selectedCategory.value = category
    }

    fun setCityFilter(city: String) {
        selectedCity.value = city
    }

    fun setCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    fun updateRates(yerPerSar: Double, yerPerUsd: Double) {
        currencyRates.value = CurrencyRates(yerPerSar = yerPerSar, yerPerUsd = yerPerUsd)
        _toastMessage.value = "تم تحديث أسعار الصرف بنجاح"
    }

    fun selectProduct(product: ProductEntity?) {
        selectedProduct.value = product
    }

    fun toggleFavorite(productId: Long) {
        viewModelScope.launch {
            if (favoriteDao.isFavorite(productId)) {
                favoriteDao.removeFavorite(productId)
                _toastMessage.value = "تمت الإزالة من المفضلة"
            } else {
                favoriteDao.addFavorite(FavoriteEntity(productId = productId))
                _toastMessage.value = "تمت الإضافة للمفضلة"
            }
        }
    }

    // --- Admin Actions ---

    fun addOrUpdateProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.insertProduct(product)
            _toastMessage.value = "تم حفظ المنتج بنجاح"
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            productDao.deleteProduct(productId)
            if (selectedProduct.value?.id == productId) {
                selectedProduct.value = null
            }
            _toastMessage.value = "تم حذف المنتج"
        }
    }

    fun addOrUpdateStore(store: StoreEntity) {
        viewModelScope.launch {
            storeDao.insertStore(store)
            _toastMessage.value = "تم حفظ الصيدلية/المحل بنجاح"
        }
    }

    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            storeDao.deleteStore(storeId)
            _toastMessage.value = "تم حذف الصيدلية/المحل"
        }
    }

    fun updateInventoryItem(storeId: Long, productId: Long, priceYer: Double, stockStatus: String) {
        viewModelScope.launch {
            inventoryDao.insertInventory(
                StoreInventoryEntity(
                    storeId = storeId,
                    productId = productId,
                    priceYer = priceYer,
                    stockStatus = stockStatus
                )
            )
            _toastMessage.value = "تم تحديث السعر والتوفر"
        }
    }

    // --- Specific Store Dashboard Functions ---

    fun updateStoreInventoryDetailed(storeId: Long, productId: Long, priceYer: Double, stockQuantity: Int, stockStatus: String) {
        viewModelScope.launch {
            inventoryDao.insertInventory(
                StoreInventoryEntity(
                    storeId = storeId,
                    productId = productId,
                    priceYer = priceYer,
                    stockQuantity = stockQuantity,
                    stockStatus = stockStatus
                )
            )
            _toastMessage.value = "تم تحديث كمية وسعر الاصنوف بمتجرك بنجاح"
        }
    }

    fun addStoreProductAndInventory(
        storeId: Long,
        name: String,
        category: String,
        activeIngredient: String,
        unit: String,
        priceYer: Double,
        stockQuantity: Int,
        stockStatus: String
    ) {
        viewModelScope.launch {
            var product = productDao.getProductByName(name.trim())
            val productId = product?.id ?: productDao.insertProduct(
                ProductEntity(
                    name = name.trim(),
                    category = category,
                    activeIngredient = activeIngredient,
                    unit = unit.ifBlank { "قطعة" }
                )
            )
            inventoryDao.insertInventory(
                StoreInventoryEntity(
                    storeId = storeId,
                    productId = productId,
                    priceYer = priceYer,
                    stockQuantity = stockQuantity,
                    stockStatus = stockStatus
                )
            )
            _toastMessage.value = "تم إضافة الصنف والكمية إلى متجرك الخاص بنجاح 📦"
        }
    }

    fun importCsvForSpecificStore(uri: Uri, storeId: Long) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val importedItems = CsvExcelImporter.parseCsvStream(inputStream)
                    var count = 0
                    for (item in importedItems) {
                        var product = productDao.getProductByName(item.productName.trim())
                        val productId = product?.id ?: productDao.insertProduct(
                            ProductEntity(
                                name = item.productName.trim(),
                                category = item.category,
                                description = item.description,
                                activeIngredient = item.activeIngredient,
                                unit = item.unit
                            )
                        )
                        inventoryDao.insertInventory(
                            StoreInventoryEntity(
                                storeId = storeId,
                                productId = productId,
                                priceYer = item.priceYer,
                                stockQuantity = 15,
                                stockStatus = item.stockStatus
                            )
                        )
                        count++
                    }
                    _toastMessage.value = "تم استيراد $count صنف بنجاح إلى مخزون متجرك فقط 📊"
                } else {
                    _toastMessage.value = "فشل في قراءة الملف"
                }
            } catch (e: Exception) {
                _toastMessage.value = "خطأ الاستيراد: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun updateStoreInfo(store: StoreEntity) {
        viewModelScope.launch {
            storeDao.insertStore(store)
            val current = _userSession.value
            if (current is UserSession.StoreOwner && current.store.id == store.id) {
                _userSession.value = UserSession.StoreOwner(store)
            }
            _toastMessage.value = "تم تحديث بيانات التواصل الخاصة بمتجرك بنجاح 📞"
        }
    }

    fun importCsvFromUri(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val importedItems = CsvExcelImporter.parseCsvStream(inputStream)
                    processImportedItems(importedItems)
                } else {
                    _toastMessage.value = "فشل في قراءة الملف المحدد"
                }
            } catch (e: Exception) {
                _toastMessage.value = "خطأ أثناء استيراد الملف: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun importSampleCsvText() {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val stream = CsvExcelImporter.SAMPLE_CSV_CONTENT.byteInputStream(Charsets.UTF_8)
                val items = CsvExcelImporter.parseCsvStream(stream)
                processImportedItems(items)
            } catch (e: Exception) {
                _toastMessage.value = "خطأ أثناء الاستيراد: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    private suspend fun processImportedItems(items: List<com.example.util.ImportedInventoryItem>) {
        if (items.isEmpty()) {
            _toastMessage.value = "الملف لا يحتوي على بيانات صالحة"
            return
        }

        var productCount = 0
        var storeCount = 0
        var inventoryCount = 0

        for (item in items) {
            // Find or insert Product
            var product = productDao.getProductByName(item.productName)
            val productId = if (product != null) {
                product.id
            } else {
                val newId = productDao.insertProduct(
                    ProductEntity(
                        name = item.productName,
                        category = item.category,
                        description = item.description,
                        activeIngredient = item.activeIngredient,
                        unit = item.unit
                    )
                )
                productCount++
                newId
            }

            // Find or create Store
            val storesInCity = storeDao.getAllStores() // check existing
            val existingStore = storeDao.getStoresByCity(item.city)
                .map { list -> list.firstOrNull { it.name == item.storeName } }
                .stateIn(viewModelScope).value

            val storeId = if (existingStore != null) {
                existingStore.id
            } else {
                val newStoreId = storeDao.insertStore(
                    StoreEntity(
                        name = item.storeName,
                        category = item.storeCategory,
                        city = item.city,
                        address = item.address,
                        phone = item.phone,
                        whatsapp = item.phone,
                        workingHours = "8:00 ص - 10:00 م"
                    )
                )
                storeCount++
                newStoreId
            }

            // Insert/Update Inventory
            inventoryDao.insertInventory(
                StoreInventoryEntity(
                    storeId = storeId,
                    productId = productId,
                    priceYer = item.priceYer,
                    stockStatus = item.stockStatus
                )
            )
            inventoryCount++
        }

        _toastMessage.value = "تم استيراد $inventoryCount منتج بنجاح لـ $storeCount محلات وصيدليات"
    }

    fun resetToDefaultSampleData() {
        viewModelScope.launch {
            AppDatabase.populateDatabase(db)
            _toastMessage.value = "تمت استعادة البيانات الافتراضية"
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch {
            _isSupabaseSyncing.value = true
            val result = supabaseRepo.syncFromSupabase()
            _isSupabaseSyncing.value = false
            result.onSuccess { msg ->
                _toastMessage.value = msg
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "فشل التزامن مع Supabase"
            }
        }
    }

    fun pushToSupabase() {
        viewModelScope.launch {
            _isSupabaseSyncing.value = true
            val result = supabaseRepo.pushToSupabase()
            _isSupabaseSyncing.value = false
            result.onSuccess { msg ->
                _toastMessage.value = msg
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "فشل الرفع إلى Supabase"
            }
        }
    }

    fun searchSupabaseDirectly(query: String = searchQuery.value, category: String = selectedCategory.value) {
        viewModelScope.launch {
            _isSearchingSupabase.value = true
            _supabaseSearchError.value = null
            val result = supabaseRepo.searchProductsInSupabase(query, category)
            _isSearchingSupabase.value = false
            result.onSuccess { products ->
                _supabaseSearchResults.value = products
            }.onFailure { err ->
                _supabaseSearchError.value = err.localizedMessage ?: "فشل البحث في Supabase"
            }
        }
    }
}

