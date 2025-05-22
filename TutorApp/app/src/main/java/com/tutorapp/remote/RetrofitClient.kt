package com.tutorapp.remote

import android.util.Log // Import Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Import TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.7:8000/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Keep a reference to the OkHttpClient instance to access its connectionPool
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Optionally, configure timeouts here if you were also facing SocketTimeoutExceptions
        // These are OkHttp's defaults (10 seconds). Adjust if needed.
        .connectTimeout(20, TimeUnit.SECONDS) // Example: 20 seconds for connection
        .readTimeout(20, TimeUnit.SECONDS)    // Example: 20 seconds for reading data
        .writeTimeout(20, TimeUnit.SECONDS)   // Example: 20 seconds for writing data
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use the configured OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Evicts all idle connections from OkHttp's connection pool.
     * Call this when the network is confirmed to be lost to prevent attempts
     * to reuse stale connections when the network recovers.
     */
    fun evictAllConnections() {
        okHttpClient.connectionPool.evictAll()
        Log.d("RetrofitClient", "Evicted all connections from OkHttp's connection pool.")
    }
}
