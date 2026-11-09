package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSession
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.RoseAlert
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeTab(
    viewModel: McKaysViewModel,
    currentUser: UserSession,
    onOpenSalesModal: () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val inventory by viewModel.inventory.collectAsState()

    val isAdmin = currentUser.role == "admin"
    val activeBranch = currentUser.branch

    val context = LocalContext.current
    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(sdfDate.format(Date())) }

    // Filter transactions for selected date
    val dateTx = remember(transactions, selectedDate, isAdmin, activeBranch) {
        transactions.filter { tx ->
            tx.date == selectedDate && (isAdmin || tx.branch.equals(activeBranch, ignoreCase = true))
        }
    }

    val totalOmzet = remember(dateTx) {
        dateTx.sumOf { it.price }
    }

    val branchItems = remember(inventory, isAdmin, activeBranch) {
        inventory.filter { item ->
            if (isAdmin) item.branch.equals("Pusat", ignoreCase = true)
            else item.branch.equals(activeBranch, ignoreCase = true)
        }
    }

    val lowStockItems = remember(branchItems) {
        branchItems.filter { it.stock_ml < 10.0 || it.stock_botol < 10L }.take(4)
    }

    val bestSellerNames = remember {
        listOf("VC Scandal", "PL Romantichwish", "Pink Shiffon", "ES Cherry", "Air ZR Nude")
    }

    val activeBestSellers = remember(branchItems) {
        branchItems.filter { item ->
            bestSellerNames.any { bs -> item.name.contains(bs, ignoreCase = true) }
        }
    }

    val maxOmzetChart = remember(dateTx) {
        (dateTx.maxOfOrNull { it.price } ?: 500000.0).coerceAtLeast(100000.0)
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val surfaceColor = if (darkMode) Color(0xFF111827) else Color.White
    val borderColor = if (darkMode) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val textPrimary = if (darkMode) Color.White else Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- OMSET CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BlueDark, Color(0xFF312E81))
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(
                        text = "TOTAL OMSET (${if (isAdmin) "PUSAT" else activeBranch.uppercase()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFBFDBFE),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(totalOmzet),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Date Picker trigger button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    selectedDate = formatted
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color(0xFFBFDBFE),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // --- BUKA KASIR ACTION BUTTON ---
        Button(
            onClick = onOpenSalesModal,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("open_cashier_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Buka Kasir Penjualan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        // --- VISUAL ANALYTICS DASHBOARD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceColor)
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Grafik Performa Penjualan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BluePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Real-Time Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
            }

            if (dateTx.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Belum ada transaksi pada tanggal ini untuk divisualisasikan.",
                        fontSize = 11.sp,
                        color = textMuted,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                dateTx.take(5).forEach { tx ->
                    val percentage = (tx.price / maxOmzetChart).toFloat().coerceIn(0.1f, 1f)
                    val animatedWidth by animateFloatAsState(targetValue = percentage, label = "bar")

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tx.item,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = currencyFormatter.format(tx.price),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BluePrimary
                            )
                        }

                        // Progress Bar Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (darkMode) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedWidth)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(BluePrimary, Color(0xFF6366F1))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // --- BEST SELLER SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "BEST SELLER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = textMuted,
                letterSpacing = 1.sp
            )

            if (activeBestSellers.isEmpty()) {
                Text(
                    text = "Belum ada item best seller di stok toko.",
                    fontSize = 11.sp,
                    color = textMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeBestSellers.take(2).forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(surfaceColor)
                                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.stock_ml.toInt()}ml",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                                Text(
                                    text = "${item.stock_botol}pcs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- STOK MENIPIS SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RoseAlert,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "STOK MENIPIS (< 10)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = RoseAlert,
                    letterSpacing = 1.sp
                )
            }

            if (lowStockItems.isEmpty()) {
                Text(
                    text = "Semua stok produk dalam kondisi aman.",
                    fontSize = 11.sp,
                    color = textMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                lowStockItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (item.stock_ml < 10.0) "${item.stock_ml.toInt()} ml" else "${item.stock_botol} pcs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = RoseAlert
                        )
                    }
                }
            }
        }
    }
}
