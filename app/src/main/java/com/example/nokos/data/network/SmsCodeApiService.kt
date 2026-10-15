package com.example.nokos.data.network

import com.example.nokos.data.model.ApiBalanceData
import com.example.nokos.data.model.ApiCountry
import com.example.nokos.data.model.ApiEnvelope
import com.example.nokos.data.model.ApiOrder
import com.example.nokos.data.model.ApiProduct
import com.example.nokos.data.model.CreateOrderRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SmsCodeApiService {

    @GET("v1/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String
    ): Response<ApiEnvelope<ApiBalanceData>>

    @GET("v1/catalog/countries")
    suspend fun getCountries(
        @Header("Authorization") token: String
    ): Response<ApiEnvelope<List<ApiCountry>>>

    @GET("v1/catalog/products")
    suspend fun getProducts(
        @Header("Authorization") token: String,
        @Query("country_id") countryId: String? = null
    ): Response<ApiEnvelope<List<ApiProduct>>>

    @POST("v1/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<ApiEnvelope<ApiOrder>>

    @GET("v1/orders/{id}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<ApiEnvelope<ApiOrder>>

    @POST("v1/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<ApiEnvelope<ApiOrder>>

    @POST("v1/orders/{id}/finish")
    suspend fun finishOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<ApiEnvelope<ApiOrder>>

    @GET("v1/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String
    ): Response<ApiEnvelope<List<ApiOrder>>>

    companion object {
        private const val BASE_URL = "https://api.smscode.gg/"

        fun create(): SmsCodeApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(SmsCodeApiService::class.java)
        }
    }
}
