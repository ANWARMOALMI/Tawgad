package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "active_ingredient") val activeIngredient: String = "",
    @Json(name = "unit") val unit: String = "قطعة"
)

@JsonClass(generateAdapter = true)
data class SupabaseStoreDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "city") val city: String,
    @Json(name = "address") val address: String,
    @Json(name = "phone") val phone: String = "",
    @Json(name = "whatsapp") val whatsapp: String = "",
    @Json(name = "working_hours") val workingHours: String = "24/7",
    @Json(name = "distance_km") val distanceKm: Double = 1.2,
    @Json(name = "latitude") val latitude: Double = 15.369444,
    @Json(name = "longitude") val longitude: Double = 44.191007
)

@JsonClass(generateAdapter = true)
data class SupabaseInventoryDto(
    @Json(name = "store_id") val storeId: Long,
    @Json(name = "product_id") val productId: Long,
    @Json(name = "price_yer") val priceYer: Double,
    @Json(name = "stock_status") val stockStatus: String = "متوفر",
    @Json(name = "stock_quantity") val stockQuantity: Int = 10,
    @Json(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
