package com.example.data

import com.example.network.SupabaseClient
import com.example.network.SupabaseInventoryDto
import com.example.network.SupabaseProductDto
import com.example.network.SupabaseStoreDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseSyncRepository(private val db: AppDatabase) {

    suspend fun syncFromSupabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = SupabaseClient.supabaseKey
            val token = SupabaseClient.bearerToken
            val service = SupabaseClient.apiService

            // 1. Fetch products
            val productsResponse = service.getProducts(key, token)
            var productsCount = 0
            if (productsResponse.isSuccessful) {
                productsResponse.body()?.let { dtos ->
                    if (dtos.isNotEmpty()) {
                        val entities = dtos.map { dto ->
                            ProductEntity(
                                id = dto.id ?: 0,
                                name = dto.name,
                                category = dto.category,
                                description = dto.description,
                                activeIngredient = dto.activeIngredient,
                                unit = dto.unit
                            )
                        }
                        db.productDao().insertProducts(entities)
                        productsCount = entities.size
                    }
                }
            } else {
                return@withContext Result.failure(
                    Exception("فشل الاتصال بـ Supabase (${productsResponse.code()}): ${productsResponse.errorBody()?.string()}")
                )
            }

            // 2. Fetch stores
            val storesResponse = service.getStores(key, token)
            var storesCount = 0
            if (storesResponse.isSuccessful) {
                storesResponse.body()?.let { dtos ->
                    if (dtos.isNotEmpty()) {
                        val entities = dtos.map { dto ->
                            StoreEntity(
                                id = dto.id ?: 0,
                                name = dto.name,
                                category = dto.category,
                                city = dto.city,
                                address = dto.address,
                                phone = dto.phone,
                                whatsapp = dto.whatsapp,
                                workingHours = dto.workingHours,
                                distanceKm = dto.distanceKm,
                                latitude = dto.latitude,
                                longitude = dto.longitude
                            )
                        }
                        db.storeDao().insertStores(entities)
                        storesCount = entities.size
                    }
                }
            }

            // 3. Fetch inventory
            val inventoryResponse = service.getInventory(key, token)
            if (inventoryResponse.isSuccessful) {
                inventoryResponse.body()?.let { dtos ->
                    if (dtos.isNotEmpty()) {
                        val entities = dtos.map { dto ->
                            StoreInventoryEntity(
                                storeId = dto.storeId,
                                productId = dto.productId,
                                priceYer = dto.priceYer,
                                stockStatus = dto.stockStatus,
                                stockQuantity = dto.stockQuantity,
                                lastUpdated = dto.lastUpdated
                            )
                        }
                        db.inventoryDao().insertInventories(entities)
                    }
                }
            }

            Result.success("تم التزامن بنجاح من Supabase ($productsCount منتج، $storesCount متجر)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushToSupabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = SupabaseClient.supabaseKey
            val token = SupabaseClient.bearerToken
            val service = SupabaseClient.apiService

            // Push products
            val products = db.productDao().getAllProductsList()
            if (products.isNotEmpty()) {
                val dtos = products.map {
                    SupabaseProductDto(
                        id = if (it.id > 0) it.id else null,
                        name = it.name,
                        category = it.category,
                        description = it.description,
                        activeIngredient = it.activeIngredient,
                        unit = it.unit
                    )
                }
                val res = service.upsertProducts(key, token, dtos)
                if (!res.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("فشل رفع المنتجات (${res.code()}): ${res.errorBody()?.string()}")
                    )
                }
            }

            // Push stores
            val stores = db.storeDao().getAllStoresList()
            if (stores.isNotEmpty()) {
                val dtos = stores.map {
                    SupabaseStoreDto(
                        id = if (it.id > 0) it.id else null,
                        name = it.name,
                        category = it.category,
                        city = it.city,
                        address = it.address,
                        phone = it.phone,
                        whatsapp = it.whatsapp,
                        workingHours = it.workingHours,
                        distanceKm = it.distanceKm,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
                val res = service.upsertStores(key, token, dtos)
                if (!res.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("فشل رفع المتاجر (${res.code()}): ${res.errorBody()?.string()}")
                    )
                }
            }

            Result.success("تم رفع جميع البيانات إلى Supabase بنجاح!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProductsInSupabase(
        query: String,
        category: String?
    ): Result<List<SupabaseProductDto>> = withContext(Dispatchers.IO) {
        try {
            val key = SupabaseClient.supabaseKey
            val token = SupabaseClient.bearerToken
            val service = SupabaseClient.apiService

            val trimmedQuery = query.trim()
            val orCond = if (trimmedQuery.isNotEmpty()) {
                "(name.ilike.*$trimmedQuery*,description.ilike.*$trimmedQuery*,active_ingredient.ilike.*$trimmedQuery*)"
            } else null

            val catFilter = if (!category.isNullOrBlank() && category != "الكل") {
                "eq.$category"
            } else null

            val res = service.queryProducts(
                apiKey = key,
                bearerToken = token,
                orCondition = orCond,
                categoryFilter = catFilter
            )

            if (res.isSuccessful) {
                val products = res.body() ?: emptyList()
                // Cache fetched products locally so detail sheet works smoothly
                if (products.isNotEmpty()) {
                    val entities = products.map { dto ->
                        ProductEntity(
                            id = dto.id ?: 0,
                            name = dto.name,
                            category = dto.category,
                            description = dto.description,
                            activeIngredient = dto.activeIngredient,
                            unit = dto.unit
                        )
                    }
                    db.productDao().insertProducts(entities)
                }
                Result.success(products)
            } else {
                Result.failure(Exception("خطأ في الاستعلام من Supabase (${res.code()}): ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
