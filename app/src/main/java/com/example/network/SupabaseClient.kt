package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    val supabaseUrl: String by lazy {
        val url = try {
            BuildConfig.SUPABASE_URL
        } catch (e: Exception) {
            "https://kmaoujggvnbhmnwzbcbv.supabase.co"
        }
        if (url.endsWith("/")) url else "$url/"
    }

    val supabaseKey: String by lazy {
        try {
            BuildConfig.SUPABASE_ANON_KEY
        } catch (e: Exception) {
            "sb_publishable_pNbALl19UYPnqIyfYElX-A_fljCqpmK"
        }
    }

    val bearerToken: String by lazy {
        "Bearer $supabaseKey"
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val apiService: SupabaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApiService::class.java)
    }
}
