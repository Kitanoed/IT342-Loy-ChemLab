package com.example.chemlab.features.auth.data.remote

import com.example.chemlab.features.auth.data.remote.AuthService
import com.example.chemlab.features.inventory.data.remote.InventoryService
import com.example.chemlab.features.requests.data.remote.RequestsService
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // For Android emulator

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun getAuthService(): AuthService {
        return retrofit.create(AuthService::class.java)
    }
    /**
     * Creates an authenticated OkHttp client with Bearer token in Authorization header.
     */
    private fun getAuthenticatedClient(accessToken: String): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getAuthenticatedRetrofit(accessToken: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(accessToken))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getInventoryService(accessToken: String): InventoryService {
        return getAuthenticatedRetrofit(accessToken)
            .create(InventoryService::class.java)
    }

    fun getRequestsService(accessToken: String): RequestsService {
        return getAuthenticatedRetrofit(accessToken)
            .create(RequestsService::class.java)
    }
}

