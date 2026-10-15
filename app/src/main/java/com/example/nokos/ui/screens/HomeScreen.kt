package com.example.nokos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nokos.data.model.Country
import com.example.nokos.data.model.OrderStatus
import com.example.nokos.data.model.ServiceProduct
import com.example.nokos.data.repository.NokosRepository
import com.example.nokos.ui.components.ActiveOrderCard
import com.example.nokos.ui.components.MetricStatCard
import com.example.nokos.ui.components.OrderConfirmBottomSheet
import com.example.nokos.ui.components.ServiceItemCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletAccent
import com.example.nokos.ui.viewmodel.NokosViewModel

@Composable
fun HomeScreen(
    viewModel: NokosViewModel,
    onNavigateToOrder: () -> Unit,
    onNavigateToLiveOtp: () -> Unit,
    onOpenTopUp: () -> Unit,
    onOpenCountryPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.balance.collectAsState()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val services by viewModel.filteredServices.collectAsState()

    var selectedServiceForOrder by remember { mutableStateOf<ServiceProduct?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SaaS Hero Balance Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Slate900,
                                    Slate850,
                                    CyanPrimary.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = CyanGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SALDO DOMPET ANDA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Quick Country Tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Slate800,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                modifier = Modifier.clickable { onOpenCountryPicker() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedCountry.flagEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedCountry.code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = NokosRepository.formatCurrency(balance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenTopUp,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("home_topup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Top up",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Isi Saldo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = onNavigateToOrder,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("home_order_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = "Beli",
                                    tint = CyanGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Beli Nokos",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Nokos Aktif",
                    value = "${activeOrders.size}",
                    subtitle = "Menunggu SMS",
                    icon = Icons.Default.PhoneAndroid,
                    iconTint = CyanGlow,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "Verifikasi Sukses",
                    value = "${allOrders.count { it.status == OrderStatus.RECEIVED.name || it.status == OrderStatus.FINISHED.name }}",
                    subtitle = "Tingkat 99.8%",
                    icon = Icons.Default.CheckCircle,
                    iconTint = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "Respon Rata-rata",
                    value = "8s",
                    subtitle = "Real-time API",
                    icon = Icons.Default.Speed,
                    iconTint = VioletAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Orders Live Card (if any)
        if (activeOrders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SEWA NOMOR BERJALAN (${activeOrders.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Lihat Semua →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanGlow,
                        modifier = Modifier.clickable { onNavigateToLiveOtp() }
                    )
                }
            }

            items(activeOrders.take(2)) { order ->
                ActiveOrderCard(
                    order = order,
                    onCopyNumber = { phone -> viewModel.copyToClipboard("Nomor Telepon", phone) },
                    onCopyOtp = { otp -> viewModel.copyToClipboard("Kode OTP", otp) },
                    onCancelOrder = { id -> viewModel.cancelOrder(id) },
                    onFinishOrder = { id -> viewModel.finishOrder(id) }
                )
            }
        }

        // Popular Services Quick Order
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAYANAN NOKOS TERPOPULER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Katalog Lengkap →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanGlow,
                    modifier = Modifier.clickable { onNavigateToOrder() }
                )
            }
        }

        // List top popular services
        items(services.take(4)) { service ->
            ServiceItemCard(
                service = service,
                countryCode = selectedCountry.code,
                onOrderClick = { selectedServiceForOrder = service }
            )
        }

        // SaaS Features Guarantee Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keunggulan Nokos SMS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Verifikasi nomor 100% otomatis real-time tanpa penundaan.\n" +
                                "• Otomatis refund saldo jika SMS OTP tidak masuk dalam 20 menit.\n" +
                                "• Terkoneksi dengan REST API smscode.gg atau mode Demo Sandbox interaktif.",
                        fontSize = 11.sp,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Order Confirmation Sheet
    selectedServiceForOrder?.let { service ->
        OrderConfirmBottomSheet(
            service = service,
            country = selectedCountry,
            currentBalance = balance,
            onConfirm = {
                viewModel.placeOrder(service) {
                    onNavigateToLiveOtp()
                }
            },
            onDismiss = { selectedServiceForOrder = null }
        )
    }
}
