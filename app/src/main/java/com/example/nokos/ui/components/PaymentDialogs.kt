package com.example.nokos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nokos.data.model.Country
import com.example.nokos.data.model.ServiceProduct
import com.example.nokos.data.model.TransactionEntity
import com.example.nokos.data.repository.NokosRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
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
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpBottomSheet(
    onDismiss: () -> Unit,
    onSubmitTopUp: (amount: Long, method: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nominalOptions = listOf(10000L, 25000L, 50000L, 100000L, 250000L, 500000L)
    var selectedNominal by remember { mutableLongStateOf(50000L) }
    var customNominalText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("QRIS") }

    val methods = listOf(
        Pair("QRIS", "QRIS Real-Time (Semua E-Wallet & M-Banking)"),
        Pair("BCA_VA", "BCA Virtual Account (Otomatis)"),
        Pair("MANDIRI_VA", "Mandiri Virtual Account"),
        Pair("BRI_VA", "BRI Virtual Account (BRIVA)"),
        Pair("DANA", "DANA Direct"),
        Pair("GOPAY", "GoPay Direct")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Slate100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Top Up Saldo Otomatis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Verifikasi instan 24/7 tanpa perlu kirim bukti transfer",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nominal Selection
            Text(
                text = "PILIH NOMINAL SALDO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Grid of nominals
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                nominalOptions.chunked(3).forEach { rowNominals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowNominals.forEach { nominal ->
                            val isSelected = selectedNominal == nominal && customNominalText.isBlank()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Slate850,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanGlow else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedNominal = nominal
                                        customNominalText = ""
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = NokosRepository.formatCurrency(nominal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanGlow else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom amount input
            OutlinedTextField(
                value = customNominalText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    customNominalText = filtered
                    val amount = filtered.toLongOrNull()
                    if (amount != null && amount > 0) {
                        selectedNominal = amount
                    }
                },
                label = { Text("Atau masukkan nominal lain (min. Rp 5.000)") },
                prefix = { Text("Rp ", color = Slate400, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Methods
            Text(
                text = "PILIH METODE PEMBAYARAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { (code, label) ->
                    val isSelected = selectedMethod == code
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Slate850,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanGlow else Slate700
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = code }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (code) {
                                            "QRIS" -> CyanPrimary
                                            "BCA_VA" -> Color(0xFF005EAA)
                                            "MANDIRI_VA" -> Color(0xFF003087)
                                            "BRI_VA" -> Color(0xFF00529C)
                                            "DANA" -> Color(0xFF118EEA)
                                            else -> Color(0xFF00AA13)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (code == "QRIS") Icons.Default.QrCode2 else Icons.Default.AccountBalance,
                                    contentDescription = code,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Otomatis & Real-Time (Biaya: Rp 0)",
                                    fontSize = 10.sp,
                                    color = EmeraldSuccess
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (isSelected) CyanGlow else Slate600,
                                        CircleShape
                                    )
                                    .background(if (isSelected) CyanGlow else Color.Transparent)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    val finalAmount = customNominalText.toLongOrNull() ?: selectedNominal
                    if (finalAmount >= 5000L) {
                        onSubmitTopUp(finalAmount, selectedMethod)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_topup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lanjutkan Bayar ${NokosRepository.formatCurrency(customNominalText.toLongOrNull() ?: selectedNominal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun RealtimePaymentDialog(
    transaction: TransactionEntity,
    onSimulateSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(900) } // 15 mins
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "payPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Ref ID & Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PEMBAYARAN OTOMATIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanGlow,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = transaction.refNumber,
                            fontSize = 11.sp,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AmberWarning.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = AmberWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timerStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QRIS Canvas or VA Display
                if (transaction.method == "QRIS") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // QR Graphic Generator via Canvas
                            Canvas(modifier = Modifier.size(150.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val cellSize = canvasWidth / 15f

                                // Draw simulated QR blocks & corner finder patterns
                                for (row in 0..14) {
                                    for (col in 0..14) {
                                        val isFinderTopLeft = row < 5 && col < 5
                                        val isFinderTopRight = row < 5 && col > 9
                                        val isFinderBottomLeft = row > 9 && col < 5
                                        val isCenterLogo = row in 6..8 && col in 6..8

                                        val shouldFill = when {
                                            isFinderTopLeft -> row == 0 || row == 4 || col == 0 || col == 4 || (row in 2..2 && col in 2..2)
                                            isFinderTopRight -> row == 0 || row == 4 || col == 10 || col == 14 || (row in 2..2 && col in 12..12)
                                            isFinderBottomLeft -> row == 10 || row == 14 || col == 0 || col == 4 || (row in 12..12 && col in 2..2)
                                            isCenterLogo -> false
                                            else -> ((row * 7 + col * 13 + row * col) % 3 == 0)
                                        }

                                        if (shouldFill) {
                                            drawRect(
                                                color = Color.Black,
                                                topLeft = Offset(col * cellSize, row * cellSize),
                                                size = Size(cellSize - 1f, cellSize - 1f)
                                            )
                                        }
                                    }
                                }

                                // Center QRIS pill
                                drawCircle(
                                    color = Color(0xFF0284C7),
                                    radius = cellSize * 1.5f,
                                    center = Offset(canvasWidth / 2, canvasHeight / 2)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "QRIS STANDAR INDONESIA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    // Virtual Account Number Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Slate850,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NOMOR VIRTUAL ACCOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val vaNumber = "88019" + transaction.refNumber.filter { it.isDigit() }.take(8)
                            Text(
                                text = vaNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanGlow,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onCopy("Nomor Virtual Account", vaNumber) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy VA",
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Nomor VA", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nominal display with unique code
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate850,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PEMBAYARAN",
                                fontSize = 10.sp,
                                color = Slate400,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = NokosRepository.formatCurrency(transaction.amount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { onCopy("Nominal Pembayaran", transaction.amount.toString()) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy nominal",
                                tint = CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .clip(CircleShape)
                            .background(AmberWarning)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Menunggu transfer masuk dari Anda...",
                        fontSize = 11.sp,
                        color = Slate300
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Auto-simulation button (Simulasikan Pembayaran Berhasil)
                Button(
                    onClick = onSimulateSuccess,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("simulate_payment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Slate950,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulasikan Bayar Berhasil (Instant Callback)",
                        color = Slate950,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("Tutup Jendela", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmBottomSheet(
    service: ServiceProduct,
    country: Country,
    currentBalance: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBalanceSufficient = currentBalance >= service.price
    val remainingBalance = currentBalance - service.price

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Slate100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Konfirmasi Sewa Nomor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Slate850,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Layanan", fontSize = 13.sp, color = Slate400)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = service.iconResId),
                                contentDescription = service.name,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(service.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Negara", fontSize = 13.sp, color = Slate400)
                        Text("${country.flagEmoji} ${country.name} (${country.prefix})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Masa Aktif", fontSize = 13.sp, color = Slate400)
                        Text("20 Menit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biaya Sewa", fontSize = 13.sp, color = Slate400)
                        Text(NokosRepository.formatCurrency(service.price), fontSize = 15.sp, fontWeight = FontWeight.Black, color = CyanGlow)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.HorizontalDivider(color = Slate700)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sisa Saldo Anda", fontSize = 13.sp, color = Slate400)
                        Text(
                            text = if (isBalanceSufficient) NokosRepository.formatCurrency(remainingBalance) else "Saldo Kurang!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBalanceSufficient) EmeraldSuccess else RoseError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Guarantee Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Garansi 100% Refund otomatis jika SMS tidak sampai dalam masa aktif.",
                    fontSize = 11.sp,
                    color = EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = isBalanceSufficient,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    disabledContainerColor = Slate800
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_order_button")
            ) {
                Text(
                    text = if (isBalanceSufficient) "Konfirmasi & Beli Sekarang" else "Saldo Tidak Mencukupi (Top Up Dulu)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
