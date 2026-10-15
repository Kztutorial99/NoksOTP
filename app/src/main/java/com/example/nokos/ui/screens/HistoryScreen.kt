package com.example.nokos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nokos.data.model.NokosOrderEntity
import com.example.nokos.data.model.OrderStatus
import com.example.nokos.data.repository.NokosRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.nokos.ui.viewmodel.NokosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: NokosViewModel,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderForDetail by remember { mutableStateOf<NokosOrderEntity?>(null) }

    val filterOptions = listOf("Semua", "Sukses (OTP)", "Menunggu", "Dibatalkan")

    val filteredOrders = allOrders.filter { order ->
        val matchesFilter = when (selectedFilter) {
            "Sukses (OTP)" -> order.status == OrderStatus.RECEIVED.name || order.status == OrderStatus.FINISHED.name
            "Menunggu" -> order.status == OrderStatus.WAITING_SMS.name
            "Dibatalkan" -> order.status == OrderStatus.CANCELLED.name || order.status == OrderStatus.TIMEOUT.name
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
                order.serviceName.contains(searchQuery, ignoreCase = true) ||
                order.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                order.id.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Header
        item {
            Column {
                Text(
                    text = "Riwayat Sewa & Verifikasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Arsip lengkap seluruh nomor virtual dan pesan SMS OTP",
                    fontSize = 12.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nomor atau layanan...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate400
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("history_search_input")
                )
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Slate900,
                            selectedContainerColor = CyanGlow.copy(alpha = 0.2f),
                            labelColor = Slate400,
                            selectedLabelColor = CyanGlow
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Slate800,
                            selectedBorderColor = CyanGlow
                        )
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No history",
                            tint = Slate600,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tidak Ada Riwayat",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pesanan yang Anda buat akan tersimpan otomatis di sini.",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredOrders) { order ->
                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(order.createdAt))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOrderForDetail = order }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = order.serviceName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${order.countryName}",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = order.phoneNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanGlow,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // Status Badge
                            val badgeBg: Color
                            val badgeText: String
                            val badgeColor: Color
                            when (order.status) {
                                OrderStatus.RECEIVED.name, OrderStatus.FINISHED.name -> {
                                    badgeBg = EmeraldSuccess.copy(alpha = 0.15f)
                                    badgeText = "SUKSES"
                                    badgeColor = EmeraldSuccess
                                }
                                OrderStatus.WAITING_SMS.name -> {
                                    badgeBg = AmberWarning.copy(alpha = 0.15f)
                                    badgeText = "MENUNGGU"
                                    badgeColor = AmberWarning
                                }
                                else -> {
                                    badgeBg = RoseError.copy(alpha = 0.15f)
                                    badgeText = "BATAL"
                                    badgeColor = RoseError
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (!order.smsCode.isNullOrBlank()) {
                                Text(
                                    text = "OTP: ${order.smsCode}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = NokosRepository.formatCurrency(order.price),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Order Modal Dialog
    selectedOrderForDetail?.let { order ->
        val dateStr = SimpleDateFormat("dd MMMM yyyy - HH:mm:ss", Locale("id", "ID")).format(Date(order.createdAt))
        Dialog(onDismissRequest = { selectedOrderForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rincian Transaksi Nokos",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { selectedOrderForDetail = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate850,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order ID", fontSize = 12.sp, color = Slate400)
                                Text(order.id, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Layanan", fontSize = 12.sp, color = Slate400)
                                Text(order.serviceName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nomor Telepon", fontSize = 12.sp, color = Slate400)
                                Text(order.phoneNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanGlow)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Negara", fontSize = 12.sp, color = Slate400)
                                Text("${order.countryName} (${order.prefix})", fontSize = 12.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Waktu Order", fontSize = 12.sp, color = Slate400)
                                Text(dateStr, fontSize = 11.sp, color = Slate400)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biaya", fontSize = 12.sp, color = Slate400)
                                Text(NokosRepository.formatCurrency(order.price), fontSize = 13.sp, fontWeight = FontWeight.Black, color = CyanGlow)
                            }
                        }
                    }

                    if (!order.smsCode.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("KODE OTP TERVERIFIKASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                Text(order.smsCode, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                                if (!order.smsText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(order.smsText, fontSize = 11.sp, color = Slate300)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.copyToClipboard("Nomor & Info Order", "${order.serviceName}: ${order.phoneNumber} (OTP: ${order.smsCode ?: "N/A"})")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Info Order", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
