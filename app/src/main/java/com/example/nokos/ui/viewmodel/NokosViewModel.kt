package com.example.nokos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nokos.data.local.NokosDatabase
import com.example.nokos.data.model.Country
import com.example.nokos.data.model.NokosOrderEntity
import com.example.nokos.data.model.ServiceProduct
import com.example.nokos.data.model.TransactionEntity
import com.example.nokos.data.repository.ApiConnectionStatus
import com.example.nokos.data.repository.NokosRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class OtpReceived(val serviceName: String, val otp: String) : UiEvent()
    data class CopyToClipboard(val label: String, val text: String) : UiEvent()
}

class NokosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NokosDatabase.getDatabase(application)
    private val repository = NokosRepository(application, database.nokosDao())

    val balance: StateFlow<Long> = repository.balance
    val apiKey: StateFlow<String> = repository.apiKey
    val isDemoMode: StateFlow<Boolean> = repository.isDemoMode
    val autoCopyOtp: StateFlow<Boolean> = repository.autoCopyOtp
    val vibrationEnabled: StateFlow<Boolean> = repository.vibrationEnabled
    val connectionStatus: StateFlow<ApiConnectionStatus> = repository.connectionStatus

    val countries: List<Country> = repository.defaultCountries
    private val _selectedCountry = MutableStateFlow(countries.first())
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isOrdering = MutableStateFlow(false)
    val isOrdering: StateFlow<Boolean> = _isOrdering.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    // Room orders flows
    val allOrders: StateFlow<List<NokosOrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<NokosOrderEntity>> = repository.getActiveOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered services
    val filteredServices: StateFlow<List<ServiceProduct>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        val list = repository.defaultServices
        list.filter { service ->
            val matchesCategory = (category == "Semua" || service.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() || service.name.contains(query, ignoreCase = true) || service.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.defaultServices)

    fun setSelectedCountry(country: Country) {
        _selectedCountry.value = country
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setApiKey(key: String) {
        repository.setApiKey(key)
    }

    fun setDemoMode(isDemo: Boolean) {
        repository.setDemoMode(isDemo)
    }

    fun setAutoCopyOtp(enabled: Boolean) {
        repository.setAutoCopyOtp(enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
    }

    fun testApiConnection() {
        repository.testApiConnection()
    }

    fun placeOrder(service: ServiceProduct, onSuccess: (NokosOrderEntity) -> Unit) {
        viewModelScope.launch {
            _isOrdering.value = true
            val result = repository.placeOrder(service, _selectedCountry.value)
            _isOrdering.value = false
            result.onSuccess { order ->
                _uiEvents.emit(UiEvent.ShowToast("Berhasil sewa nomor ${order.serviceName}: ${order.phoneNumber}"))
                onSuccess(order)
            }.onFailure { err ->
                _uiEvents.emit(UiEvent.ShowToast(err.message ?: "Gagal memesan nomor"))
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId)
            result.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Sewa nomor berhasil dibatalkan dan saldo telah direfund."))
            }.onFailure { err ->
                _uiEvents.emit(UiEvent.ShowToast(err.message ?: "Gagal membatalkan pesanan"))
            }
        }
    }

    fun finishOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.finishOrder(orderId)
            result.onSuccess {
                _uiEvents.emit(UiEvent.ShowToast("Verifikasi selesai."))
            }
        }
    }

    fun createTopUp(amount: Long, method: String, onCreated: (TransactionEntity) -> Unit) {
        viewModelScope.launch {
            val tx = repository.createTopUp(amount, method)
            onCreated(tx)
        }
    }

    fun confirmPayment(transaction: TransactionEntity, onConfirmed: () -> Unit) {
        viewModelScope.launch {
            repository.executePaymentSuccess(transaction)
            _uiEvents.emit(UiEvent.ShowToast("Top up ${NokosRepository.formatCurrency(transaction.amount)} berhasil! Saldo telah bertambah."))
            onConfirmed()
        }
    }

    fun copyToClipboard(label: String, text: String) {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.CopyToClipboard(label, text))
            _uiEvents.emit(UiEvent.ShowToast("$label berhasil disalin!"))
        }
    }
}
