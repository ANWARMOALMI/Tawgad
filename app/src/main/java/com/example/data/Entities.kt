package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // e.g., "صيدلية", "قطع غيار سيارات", "إلكترونيات", "أدوات ومعدات"
    val city: String,     // e.g., "صنعاء", "عدن", "تعز", "المكلا", "إب"
    val address: String,  // e.g., "شارع الزبيري - مقابل مستشفى الثورة"
    val phone: String,    // e.g., "771234567"
    val whatsapp: String, // e.g., "967771234567"
    val workingHours: String = "24/7",
    val distanceKm: Double = 1.2, // Simulated proximity
    val latitude: Double = 15.369444,
    val longitude: Double = 44.191007,
    val username: String = "",
    val password: String = "1234"
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,              // e.g., "بنادول إكسترا 500 ملغ", "فلتر زيت تويوتا كامري"
    val category: String,          // e.g., "أدوية وصيدليات", "قطع غيار سيارات", "إلكترونيات"
    val description: String = "",  // e.g., "مسكن للآلام ومخفض للحرارة"
    val activeIngredient: String = "", // e.g., "Paracetamol + Caffeine" or Part Number "15601-YZZT1"
    val unit: String = "قطعة"      // e.g., "علبة", "شريط", "طقم", "قطعة"
)

@Entity(
    tableName = "store_inventory",
    primaryKeys = ["storeId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId"), Index("productId")]
)
data class StoreInventoryEntity(
    val storeId: Long,
    val productId: Long,
    val priceYer: Double,         // Price in Yemeni Rial (Base Currency)
    val stockStatus: String = "متوفر", // "متوفر", "كمية محدودة", "غير متوفر"
    val stockQuantity: Int = 10,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
