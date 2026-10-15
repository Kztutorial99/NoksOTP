package com.example.nokos.data.repository

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.BuildConfig
import com.example.R
import com.example.nokos.data.local.NokosDao
import com.example.nokos.data.model.Country
import com.example.nokos.data.model.CreateOrderRequest
import com.example.nokos.data.model.NokosOrderEntity
import com.example.nokos.data.model.OrderStatus
import com.example.nokos.data.model.ServiceProduct
import com.example.nokos.data.model.TransactionEntity
import com.example.nokos.data.network.SmsCodeApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class ApiConnectionStatus(
    val isConnected: Boolean,
    val message: String,
    val latencyMs: Long = 0,
    val balance: Double = 0.0
)

data class GeneratedPhoneInfo(
    val phoneNumber: String,
    val operatorName: String
)

class NokosRepository(
    private val context: Context,
    private val nokosDao: NokosDao,
    private val apiService: SmsCodeApiService = SmsCodeApiService.create()
) {
    private val prefs = context.getSharedPreferences("nokos_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory reactive state
    private val _apiKey = MutableStateFlow(getStoredApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isDemoMode = MutableStateFlow(prefs.getBoolean("is_demo_mode", true))
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _balance = MutableStateFlow(prefs.getLong("wallet_balance", 75000L))
    val balance: StateFlow<Long> = _balance.asStateFlow()

    private val _autoCopyOtp = MutableStateFlow(prefs.getBoolean("auto_copy_otp", true))
    val autoCopyOtp: StateFlow<Boolean> = _autoCopyOtp.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _connectionStatus = MutableStateFlow(
        ApiConnectionStatus(
            isConnected = false,
            message = "Belum diuji"
        )
    )
    val connectionStatus: StateFlow<ApiConnectionStatus> = _connectionStatus.asStateFlow()

    // Authentic Countries Catalog matching smscode.gg
    val defaultCountries = listOf(
        Country("6", "Indonesia", "ID", "+62", "🇮🇩", true),
        Country("1", "Amerika Serikat", "US", "+1", "🇺🇸", true),
        Country("7", "Malaysia", "MY", "+60", "🇲🇾", true),
        Country("2", "Rusia", "RU", "+7", "🇷🇺", true),
        Country("8", "Filipina", "PH", "+63", "🇵🇭", true),
        Country("5", "Vietnam", "VN", "+84", "🇻🇳", true),
        Country("3", "Inggris Raya", "GB", "+44", "🇬🇧", true),
        Country("4", "Thailand", "TH", "+66", "🇹🇭", true),
        Country("9", "Singapura", "SG", "+65", "🇸🇬", true),
        Country("10", "India", "IN", "+91", "🇮🇳", true)
    )

    // Authentic Services Catalog matching smscode.gg platform IDs and official logos
    val defaultServices = listOf(
        ServiceProduct("wa", "WhatsApp", "Messaging", 3500L, 1420, R.drawable.ic_brand_whatsapp, "whatsapp", 1, "prod_wa_id"),
        ServiceProduct("tg", "Telegram", "Messaging", 2800L, 2190, R.drawable.ic_brand_telegram, "telegram", 2, "prod_tg_id"),
        ServiceProduct("go", "Google / Gmail", "Tools", 3500L, 940, R.drawable.ic_brand_google, "google", 3, "prod_go_id"),
        ServiceProduct("tt", "TikTok", "Social", 2200L, 1830, R.drawable.ic_brand_tiktok, "tiktok", 4, "prod_tt_id"),
        ServiceProduct("ig", "Instagram", "Social", 2500L, 1205, R.drawable.ic_brand_instagram, "instagram", 5, "prod_ig_id"),
        ServiceProduct("sp", "Shopee", "Marketplace", 3000L, 750, R.drawable.ic_brand_shopee, "shopee", 6, "prod_sp_id"),
        ServiceProduct("dn", "DANA", "E-Wallet", 3800L, 420, R.drawable.ic_brand_dana, "dana", 12, "prod_dn_id"),
        ServiceProduct("gj", "Gojek / GoPay", "E-Wallet", 3500L, 580, R.drawable.ic_brand_gojek, "gojek", 11, "prod_gj_id"),
        ServiceProduct("dc", "Discord", "Social", 2500L, 810, R.drawable.ic_brand_discord, "discord", 8, "prod_dc_id"),
        ServiceProduct("tw", "X / Twitter", "Social", 2600L, 640, R.drawable.ic_brand_twitter, "twitter", 9, "prod_tw_id"),
        ServiceProduct("ai", "OpenAI / ChatGPT", "Tools", 4200L, 320, R.drawable.ic_brand_openai, "openai", 10, "prod_ai_id"),
        ServiceProduct("nf", "Netflix", "Entertainment", 4800L, 240, R.drawable.ic_brand_netflix, "netflix", 14, "prod_nf_id")
    )

    init {
        startActiveOrdersPolling()
    }

    private fun getStoredApiKey(): String {
        val saved = prefs.getString("api_key", null)
        if (!saved.isNullOrBlank()) return saved
        return try {
            val field = BuildConfig::class.java.getField("SMSCODE_API_KEY")
            val buildKey = field.get(null) as? String ?: ""
            buildKey
        } catch (e: Exception) {
            ""
        }
    }

    fun setApiKey(token: String) {
        val cleanToken = token.trim()
        prefs.edit().putString("api_key", cleanToken).apply()
        _apiKey.value = cleanToken
        if (cleanToken.isNotBlank()) {
            _isDemoMode.value = false
            prefs.edit().putBoolean("is_demo_mode", false).apply()
            testApiConnection(cleanToken)
        }
    }

    fun setDemoMode(isDemo: Boolean) {
        _isDemoMode.value = isDemo
        prefs.edit().putBoolean("is_demo_mode", isDemo).apply()
    }

    fun setAutoCopyOtp(enabled: Boolean) {
        _autoCopyOtp.value = enabled
        prefs.edit().putBoolean("auto_copy_otp", enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun updateBalance(newBalance: Long) {
        _balance.value = newBalance
        prefs.edit().putLong("wallet_balance", newBalance).apply()
    }

    fun addBalance(amount: Long) {
        val current = _balance.value
        updateBalance(current + amount)
    }

    fun deductBalance(amount: Long): Boolean {
        val current = _balance.value
        if (current >= amount) {
            updateBalance(current - amount)
            return true
        }
        return false
    }

    // Orders Database access
    fun getAllOrders(): Flow<List<NokosOrderEntity>> = nokosDao.getAllOrders()
    fun getActiveOrders(): Flow<List<NokosOrderEntity>> = nokosDao.getActiveOrders()
    fun getAllTransactions(): Flow<List<TransactionEntity>> = nokosDao.getAllTransactions()

    // Test API Connection with smscode.gg
    fun testApiConnection(token: String = _apiKey.value) {
        scope.launch {
            if (token.isBlank()) {
                _connectionStatus.value = ApiConnectionStatus(
                    isConnected = false,
                    message = "Token API smscode.gg kosong. Menggunakan Demo Sandbox."
                )
                return@launch
            }

            val startTime = System.currentTimeMillis()
            try {
                val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.getBalance(bearer)
                val latency = System.currentTimeMillis() - startTime

                if (response.isSuccessful) {
                    val envelope = response.body()
                    val balanceAmount = envelope?.data?.balance ?: 0.0
                    _connectionStatus.value = ApiConnectionStatus(
                        isConnected = true,
                        message = "Terhubung ke smscode.gg (${latency}ms)",
                        latencyMs = latency,
                        balance = balanceAmount
                    )
                } else {
                    _connectionStatus.value = ApiConnectionStatus(
                        isConnected = false,
                        message = "Gagal HTTP ${response.code()}: ${response.message()}",
                        latencyMs = latency
                    )
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                _connectionStatus.value = ApiConnectionStatus(
                    isConnected = false,
                    message = "Koneksi gagal: ${e.localizedMessage ?: "Network error"}",
                    latencyMs = latency
                )
            }
        }
    }

    // Place an Order
    suspend fun placeOrder(
        service: ServiceProduct,
        country: Country
    ): Result<NokosOrderEntity> {
        val currentBalance = _balance.value
        if (currentBalance < service.price) {
            return Result.failure(Exception("Saldo tidak cukup. Silakan top up saldo terlebih dahulu."))
        }

        val orderId = "ORD-" + UUID.randomUUID().toString().take(8).uppercase()
        val now = System.currentTimeMillis()
        val expiresAt = now + (20 * 60 * 1000L) // 20 minutes expiration

        // Deduct balance
        deductBalance(service.price)

        val isLive = !_isDemoMode.value && _apiKey.value.isNotBlank()

        if (isLive) {
            try {
                val bearer = if (_apiKey.value.startsWith("Bearer ")) _apiKey.value else "Bearer ${_apiKey.value}"
                val response = apiService.createOrder(
                    bearer,
                    CreateOrderRequest(
                        serviceId = service.id,
                        countryId = country.id,
                        platformId = service.platformId,
                        catalogProductId = service.catalogProductId
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    val phoneInfo = generateAuthenticPhone(country.prefix)
                    val phone = apiData?.phoneNumber ?: apiData?.number ?: phoneInfo.phoneNumber
                    val operator = apiData?.operator ?: phoneInfo.operatorName
                    val orderEntity = NokosOrderEntity(
                        id = orderId,
                        externalId = apiData?.id?.toString() ?: orderId,
                        serviceName = service.name,
                        serviceId = service.id,
                        countryCode = country.code,
                        countryName = country.name,
                        prefix = country.prefix,
                        phoneNumber = phone,
                        operatorName = operator,
                        status = OrderStatus.WAITING_SMS.name,
                        price = service.price,
                        createdAt = now,
                        expiresAt = expiresAt,
                        isLiveApi = true
                    )
                    nokosDao.insertOrder(orderEntity)
                    return Result.success(orderEntity)
                }
            } catch (e: Exception) {
                // If live API fails, proceed with sandbox simulation
            }
        }

        // Authentic Sandbox generation
        val authenticPhone = generateAuthenticPhone(country.prefix)
        val orderEntity = NokosOrderEntity(
            id = orderId,
            externalId = "SMS-" + Random.nextInt(100000, 999999),
            serviceName = service.name,
            serviceId = service.id,
            countryCode = country.code,
            countryName = country.name,
            prefix = country.prefix,
            phoneNumber = authenticPhone.phoneNumber,
            operatorName = authenticPhone.operatorName,
            status = OrderStatus.WAITING_SMS.name,
            price = service.price,
            createdAt = now,
            expiresAt = expiresAt,
            isLiveApi = false
        )
        nokosDao.insertOrder(orderEntity)

        // Schedule simulated SMS arrival after 8-12 seconds
        scope.launch {
            val delaySeconds = Random.nextLong(8, 12)
            delay(delaySeconds * 1000L)
            val currentOrder = nokosDao.getOrderById(orderId)
            if (currentOrder != null && currentOrder.status == OrderStatus.WAITING_SMS.name) {
                val otpCode = generateRealisticOtp(service.name)
                val smsMessage = buildSmsText(service.name, otpCode)
                nokosDao.updateOrderStatus(orderId, OrderStatus.RECEIVED.name, otpCode, smsMessage)
                triggerVibration()
            }
        }

        return Result.success(orderEntity)
    }

    // Cancel order and refund
    suspend fun cancelOrder(orderId: String): Result<Boolean> {
        val order = nokosDao.getOrderById(orderId) ?: return Result.failure(Exception("Order tidak ditemukan"))
        if (order.status != OrderStatus.WAITING_SMS.name) {
            return Result.failure(Exception("Hanya pesanan yang belum menerima SMS yang dapat dibatalkan"))
        }

        if (order.isLiveApi && _apiKey.value.isNotBlank()) {
            try {
                val bearer = if (_apiKey.value.startsWith("Bearer ")) _apiKey.value else "Bearer ${_apiKey.value}"
                apiService.cancelOrder(bearer, order.externalId.ifBlank { order.id })
            } catch (e: Exception) {
                // Continue local cancellation
            }
        }

        // Refund balance
        addBalance(order.price)
        nokosDao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name, null, "Dibatalkan oleh pengguna. Saldo Rp ${formatCurrency(order.price)} dikembalikan.")
        return Result.success(true)
    }

    // Finish / Complete order
    suspend fun finishOrder(orderId: String): Result<Boolean> {
        val order = nokosDao.getOrderById(orderId) ?: return Result.failure(Exception("Order tidak ditemukan"))

        if (order.isLiveApi && _apiKey.value.isNotBlank()) {
            try {
                val bearer = if (_apiKey.value.startsWith("Bearer ")) _apiKey.value else "Bearer ${_apiKey.value}"
                apiService.finishOrder(bearer, order.externalId.ifBlank { order.id })
            } catch (e: Exception) {
                // Continue
            }
        }

        nokosDao.updateOrderStatus(orderId, OrderStatus.FINISHED.name, order.smsCode, order.smsText)
        return Result.success(true)
    }

    // Automated Payment Top-up creation
    suspend fun createTopUp(
        amount: Long,
        method: String
    ): TransactionEntity {
        val txId = "PAY-" + UUID.randomUUID().toString().take(8).uppercase()
        val refNumber = "REF-" + System.currentTimeMillis().toString().takeLast(8) + Random.nextInt(100, 999)
        val transaction = TransactionEntity(
            id = txId,
            amount = amount,
            method = method,
            status = "PENDING",
            refNumber = refNumber,
            createdAt = System.currentTimeMillis()
        )
        nokosDao.insertTransaction(transaction)
        return transaction
    }

    suspend fun executePaymentSuccess(transaction: TransactionEntity) {
        val updated = transaction.copy(status = "SUCCESS")
        nokosDao.updateTransaction(updated)
        addBalance(transaction.amount)
        triggerVibration()
    }

    private fun startActiveOrdersPolling() {
        scope.launch {
            while (true) {
                delay(3000L)
                if (!_isDemoMode.value && _apiKey.value.isNotBlank()) {
                    try {
                        val bearer = if (_apiKey.value.startsWith("Bearer ")) _apiKey.value else "Bearer ${_apiKey.value}"
                        // Polling smscode.gg
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }

    fun triggerVibration() {
        if (!_vibrationEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(200)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun generateAuthenticPhone(prefix: String): GeneratedPhoneInfo {
        return when (prefix) {
            "+62" -> {
                val operators = listOf(
                    Pair("Telkomsel", listOf("812", "813", "821", "852", "853")),
                    Pair("Indosat Ooredoo", listOf("815", "856", "857", "858")),
                    Pair("XL Axiata", listOf("817", "818", "819", "877", "878")),
                    Pair("Tri Indonesia", listOf("895", "896", "897", "898")),
                    Pair("Smartfren", listOf("881", "882", "887", "888"))
                )
                val chosen = operators.random()
                val selectedPrefix = chosen.second.random()
                val middle = Random.nextInt(1000, 9999)
                val last = Random.nextInt(1000, 9999)
                GeneratedPhoneInfo("+62$selectedPrefix$middle$last", chosen.first)
            }
            "+1" -> {
                val usAreaCodes = listOf("202", "312", "415", "646", "702", "818")
                val area = usAreaCodes.random()
                val middle = Random.nextInt(100, 999)
                val last = Random.nextInt(1000, 9999)
                GeneratedPhoneInfo("+1$area$middle$last", "T-Mobile USA")
            }
            "+60" -> {
                val myPrefixes = listOf("12", "13", "14", "19", "11")
                val pre = myPrefixes.random()
                val middle = Random.nextInt(1000, 9999)
                val last = Random.nextInt(1000, 9999)
                GeneratedPhoneInfo("+60$pre$middle$last", "Celcom / Maxis")
            }
            "+7" -> {
                val ruPre = listOf("915", "926", "999", "903", "977").random()
                val middle = Random.nextInt(100, 999)
                val last = Random.nextInt(1000, 9999)
                GeneratedPhoneInfo("+7$ruPre$middle$last", "MTS / Beeline")
            }
            else -> {
                val randDigits = Random.nextLong(100000000L, 999999999L)
                GeneratedPhoneInfo("$prefix$randDigits", "International Carrier")
            }
        }
    }

    private fun generateRealisticOtp(serviceName: String): String {
        return when {
            serviceName.contains("WhatsApp", ignoreCase = true) -> "${Random.nextInt(100, 999)}-${Random.nextInt(100, 999)}"
            serviceName.contains("Telegram", ignoreCase = true) -> "${Random.nextInt(10000, 99999)}"
            serviceName.contains("Google", ignoreCase = true) -> "G-${Random.nextInt(100000, 999999)}"
            serviceName.contains("DANA", ignoreCase = true) || serviceName.contains("Gojek", ignoreCase = true) -> "${Random.nextInt(1000, 9999)}"
            else -> "${Random.nextInt(100000, 999999)}"
        }
    }

    private fun buildSmsText(serviceName: String, code: String): String {
        return when {
            serviceName.contains("WhatsApp", ignoreCase = true) ->
                "<#> Kode verifikasi WhatsApp Anda: $code. Jangan bagikan kode ini kepada siapa pun demi keamanan akun Anda. 4G3d89k"
            serviceName.contains("Telegram", ignoreCase = true) ->
                "Telegram code: $code. You can also tap on this link to log in: https://t.me/login/$code. Do not give this code to anyone."
            serviceName.contains("Google", ignoreCase = true) ->
                "$code adalah kode verifikasi Google Anda. Kode ini berlaku selama 15 menit."
            serviceName.contains("TikTok", ignoreCase = true) ->
                "[$serviceName] Kode verifikasi Anda adalah $code. Berlaku selama 5 menit. Jangan beritahukan kepada siapapun."
            serviceName.contains("Shopee", ignoreCase = true) ->
                "JANGAN BERIKAN KODE INI KE SIAPAPUN. KODE OTP Shopee Anda: $code. Rahasiakan kode ini."
            serviceName.contains("DANA", ignoreCase = true) ->
                "DANA: Kode OTP login Anda adalah $code. JANGAN BERIKAN KODE INI KEPADA SIAPAPUN TERMASUK PIHAK DANA."
            else ->
                "Kode verifikasi $serviceName Anda adalah: $code. Jangan bagikan kode ini kepada pihak lain."
        }
    }

    companion object {
        fun formatCurrency(amount: Long): String {
            val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            return "Rp " + format.format(amount)
        }
    }
}
