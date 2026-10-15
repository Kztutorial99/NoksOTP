package com.example.nokos.data.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Country(
    val id: String,
    val name: String,
    val code: String,
    val prefix: String, // dial_code e.g. +62
    val flagEmoji: String,
    val active: Boolean = true
)

data class ServiceProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val stock: Int,
    @DrawableRes val iconResId: Int,
    val slug: String,
    val platformId: Int,
    val catalogProductId: String = ""
)

enum class OrderStatus {
    WAITING_SMS,
    RECEIVED,
    FINISHED,
    CANCELLED,
    TIMEOUT
}

@Entity(tableName = "nokos_orders")
data class NokosOrderEntity(
    @PrimaryKey
    val id: String,
    val externalId: String = "",
    val serviceName: String,
    val serviceId: String,
    val countryCode: String,
    val countryName: String,
    val prefix: String,
    val phoneNumber: String,
    val operatorName: String = "Telkomsel",
    val status: String, // WAITING_SMS, RECEIVED, FINISHED, CANCELLED, TIMEOUT
    val smsCode: String? = null,
    val smsText: String? = null,
    val price: Long,
    val createdAt: Long,
    val expiresAt: Long,
    val isLiveApi: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Long,
    val method: String, // QRIS, BCA_VA, MANDIRI_VA, BRI_VA, DANA, GOPAY
    val status: String, // SUCCESS, PENDING, EXPIRED
    val refNumber: String,
    val createdAt: Long
)

// API models matching smscode.gg documentation
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: T? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: Any? = null
)

@JsonClass(generateAdapter = true)
data class ApiBalanceData(
    @Json(name = "currency") val currency: String? = "IDR",
    @Json(name = "balance") val balance: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class ApiCountry(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "dial_code") val dialCode: String? = null,
    @Json(name = "prefix") val prefix: String? = null,
    @Json(name = "emoji") val emoji: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiProduct(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "platform_id") val platformId: Any? = null,
    @Json(name = "catalog_product_id") val catalogProductId: String? = null,
    @Json(name = "price") val price: Double? = 0.0,
    @Json(name = "stock") val stock: Int? = 0
)

@JsonClass(generateAdapter = true)
data class ApiOrder(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "number") val number: String? = null,
    @Json(name = "operator") val operator: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "sms_code") val smsCode: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "sms_text") val smsText: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "price") val price: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "service_id") val serviceId: String? = null,
    @Json(name = "country_id") val countryId: String? = null,
    @Json(name = "platform_id") val platformId: Int? = null,
    @Json(name = "catalog_product_id") val catalogProductId: String? = null
)
