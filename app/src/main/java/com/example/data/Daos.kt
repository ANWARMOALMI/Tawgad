package com.example.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class InventoryWithStoreDetails(
    @Embedded val inventory: StoreInventoryEntity,
    @Embedded(prefix = "store_") val store: StoreEntity
)

data class ProductSearchResult(
    @Embedded val product: ProductEntity,
    val availableStoreCount: Int,
    val minPriceYer: Double?,
    val maxPriceYer: Double?
)

data class StoreInventoryItemDetail(
    val storeId: Long,
    val productId: Long,
    val priceYer: Double,
    val stockStatus: String,
    val stockQuantity: Int,
    val lastUpdated: Long,
    val productName: String,
    val productCategory: String,
    val productDescription: String,
    val productActiveIngredient: String,
    val productUnit: String
)

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY name ASC")
    fun getAllStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores")
    suspend fun getAllStoresList(): List<StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    fun getStoreByIdFlow(id: Long): Flow<StoreEntity?>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreById(id: Long): StoreEntity?

    @Query("SELECT * FROM stores WHERE LOWER(username) = LOWER(:username) AND password = :password LIMIT 1")
    suspend fun getStoreByCredentials(username: String, password: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE city = :city ORDER BY name ASC")
    fun getStoresByCity(city: String): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteStore(id: Long)

    @Query("SELECT COUNT(*) FROM stores")
    suspend fun getStoreCount(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("""
        SELECT p.*, 
               COUNT(inv.storeId) as availableStoreCount,
               MIN(inv.priceYer) as minPriceYer,
               MAX(inv.priceYer) as maxPriceYer
        FROM products p
        LEFT JOIN store_inventory inv ON p.id = inv.productId AND inv.stockStatus != 'غير متوفر'
        WHERE (:query = '' OR p.name LIKE '%' || :query || '%' OR p.activeIngredient LIKE '%' || :query || '%' OR p.description LIKE '%' || :query || '%')
          AND (:category = 'الكل' OR p.category = :category)
        GROUP BY p.id
        ORDER BY availableStoreCount DESC, p.name ASC
    """)
    fun searchProducts(query: String, category: String): Flow<List<ProductSearchResult>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

@Dao
interface InventoryDao {
    @Transaction
    @Query("""
        SELECT inv.*, 
               s.id AS store_id, 
               s.name AS store_name, 
               s.category AS store_category, 
               s.city AS store_city, 
               s.address AS store_address, 
               s.phone AS store_phone, 
               s.whatsapp AS store_whatsapp, 
               s.workingHours AS store_workingHours, 
               s.distanceKm AS store_distanceKm,
               s.latitude AS store_latitude,
               s.longitude AS store_longitude,
               s.username AS store_username,
               s.password AS store_password
        FROM store_inventory inv
        INNER JOIN stores s ON inv.storeId = s.id
        WHERE inv.productId = :productId
          AND (:city = 'الكل' OR s.city = :city)
        ORDER BY inv.priceYer ASC
    """)
    fun getProductInventoryInStores(productId: Long, city: String = "الكل"): Flow<List<InventoryWithStoreDetails>>

    @Transaction
    @Query("""
        SELECT inv.storeId AS storeId, 
               inv.productId AS productId, 
               inv.priceYer AS priceYer, 
               inv.stockStatus AS stockStatus, 
               inv.stockQuantity AS stockQuantity, 
               inv.lastUpdated AS lastUpdated,
               p.name AS productName,
               p.category AS productCategory,
               p.description AS productDescription,
               p.activeIngredient AS productActiveIngredient,
               p.unit AS productUnit
        FROM store_inventory inv
        INNER JOIN products p ON inv.productId = p.id
        WHERE inv.storeId = :storeId
        ORDER BY p.name ASC
    """)
    fun getStoreInventoryDetailed(storeId: Long): Flow<List<StoreInventoryItemDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: StoreInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventories(inventories: List<StoreInventoryEntity>)

    @Query("DELETE FROM store_inventory WHERE storeId = :storeId AND productId = :productId")
    suspend fun deleteInventory(storeId: Long, productId: Long)

    @Query("SELECT COUNT(*) FROM store_inventory")
    suspend fun getInventoryCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Query("""
        SELECT p.*, 
               COUNT(inv.storeId) as availableStoreCount,
               MIN(inv.priceYer) as minPriceYer,
               MAX(inv.priceYer) as maxPriceYer
        FROM products p
        INNER JOIN favorites f ON p.id = f.productId
        LEFT JOIN store_inventory inv ON p.id = inv.productId AND inv.stockStatus != 'غير متوفر'
        GROUP BY p.id
        ORDER BY f.addedAt DESC
    """)
    fun getFavoriteProducts(): Flow<List<ProductSearchResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun removeFavorite(productId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :productId)")
    suspend fun isFavorite(productId: Long): Boolean
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}
