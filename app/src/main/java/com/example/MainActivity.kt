package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nokos.data.model.TransactionEntity
import com.example.nokos.ui.components.CountryPickerBottomSheet
import com.example.nokos.ui.components.RealtimePaymentDialog
import com.example.nokos.ui.components.SaaSTopBar
import com.example.nokos.ui.components.TopUpBottomSheet
import com.example.nokos.ui.screens.HistoryScreen
import com.example.nokos.ui.screens.HomeScreen
import com.example.nokos.ui.screens.LiveOtpScreen
import com.example.nokos.ui.screens.OrderScreen
import com.example.nokos.ui.screens.ProfileScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.nokos.ui.viewmodel.NokosViewModel
import com.example.nokos.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    ORDER("Beli", Icons.Filled.AddCircle, Icons.Filled.AddCircle),
    LIVE("Live OTP", Icons.Filled.Sensors, Icons.Outlined.Sensors),
    HISTORY("Riwayat", Icons.Filled.History, Icons.Outlined.History),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: NokosViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var showTopUpSheet by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var activePaymentTx by remember { mutableStateOf<TransactionEntity?>(null) }

    val balance by viewModel.balance.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val countries = viewModel.countries
    val selectedCountry by viewModel.selectedCountry.collectAsState()

    val isLive = !isDemoMode && apiKey.isNotBlank()

    // Handle back button: return to HOME if in sub-tab
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        currentTab = ScreenTab.HOME
    }

    // Collect UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.OtpReceived -> {
                    snackbarHostState.showSnackbar("SMS Masuk! OTP ${event.serviceName}: ${event.otp}")
                }
                is UiEvent.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip = ClipData.newPlainText(event.label, event.text)
                    clipboard?.setPrimaryClip(clip)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SaaSTopBar(
                balance = balance,
                isLiveApi = isLive,
                onTopUpClick = { showTopUpSheet = true },
                onApiStatusClick = { currentTab = ScreenTab.PROFILE }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                NavigationBar(
                    containerColor = Slate900,
                    contentColor = Slate400,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .testTag("main_bottom_nav")
                ) {
                    ScreenTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                if (tab == ScreenTab.LIVE && activeOrders.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = EmeraldSuccess,
                                                contentColor = Slate950
                                            ) {
                                                Text(
                                                    text = "${activeOrders.size}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            tint = if (isSelected) CyanGlow else Slate400
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) CyanGlow else Slate400
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanGlow else Slate400
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanGlow,
                                selectedTextColor = CyanGlow,
                                indicatorColor = Slate800,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { targetTab ->
                when (targetTab) {
                    ScreenTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToOrder = { currentTab = ScreenTab.ORDER },
                        onNavigateToLiveOtp = { currentTab = ScreenTab.LIVE },
                        onOpenTopUp = { showTopUpSheet = true },
                        onOpenCountryPicker = { showCountryPicker = true }
                    )
                    ScreenTab.ORDER -> OrderScreen(
                        viewModel = viewModel,
                        onNavigateToLiveOtp = { currentTab = ScreenTab.LIVE },
                        onOpenCountryPicker = { showCountryPicker = true }
                    )
                    ScreenTab.LIVE -> LiveOtpScreen(
                        viewModel = viewModel,
                        onNavigateToOrder = { currentTab = ScreenTab.ORDER }
                    )
                    ScreenTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onOpenTopUp = { showTopUpSheet = true }
                    )
                }
            }
        }
    }
}

    // Top up bottom sheet
    if (showTopUpSheet) {
        TopUpBottomSheet(
            onDismiss = { showTopUpSheet = false },
            onSubmitTopUp = { amount, method ->
                viewModel.createTopUp(amount, method) { transaction ->
                    activePaymentTx = transaction
                }
            }
        )
    }

    // Country picker modal
    if (showCountryPicker) {
        CountryPickerBottomSheet(
            countries = countries,
            selectedCountry = selectedCountry,
            onSelectCountry = { country ->
                viewModel.setSelectedCountry(country)
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    // Realtime payment dialog with QRIS generator & simulation
    activePaymentTx?.let { tx ->
        RealtimePaymentDialog(
            transaction = tx,
            onSimulateSuccess = {
                viewModel.confirmPayment(tx) {
                    activePaymentTx = null
                }
            },
            onDismiss = { activePaymentTx = null },
            onCopy = { label, text ->
                viewModel.copyToClipboard(label, text)
            }
        )
    }
}
