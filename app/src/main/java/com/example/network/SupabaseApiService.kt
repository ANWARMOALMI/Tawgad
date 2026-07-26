package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApiService {

    @GET("rest/v1/products")
    suspend fun getProducts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseProductDto>>

    @GET("rest/v1/products")
    suspend fun queryProducts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*",
        @Query("or") orCondition: String? = null,
        @Query("category") categoryFilter: String? = null,
        @Query("order") order: String = "id.desc"
    ): Response<List<SupabaseProductDto>>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("rest/v1/products")
    suspend fun upsertProducts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body products: List<SupabaseProductDto>
    ): Response<List<SupabaseProductDto>>

    @GET("rest/v1/stores")
    suspend fun getStores(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseStoreDto>>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("rest/v1/stores")
    suspend fun upsertStores(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body stores: List<SupabaseStoreDto>
    ): Response<List<SupabaseStoreDto>>

    @GET("rest/v1/store_inventory")
    suspend fun getInventory(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseInventoryDto>>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("rest/v1/store_inventory")
    suspend fun upsertInventory(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body inventory: List<SupabaseInventoryDto>
    ): Response<List<SupabaseInventoryDto>>
}
