package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSession
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryTab(
    viewModel: McKaysViewModel,
    currentUser: UserSession
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val isAdmin = currentUser.role == "admin"
    val activeBranch = currentUser.branch

    val context = LocalContext.current
    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val sdfYear = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }

    var filterType by remember { mutableStateOf("daily") } // "daily", "monthly", "yearly"
    var selectedDate by remember { mutableStateOf(sdfDate.format(Date())) }
    var selectedMonth by remember { mutableStateOf(sdfMonth.format(Date())) }
    var selectedYear by remember { mutableStateOf(sdfYear.format(Date())) }

    val filteredTx = remember(transactions, filterType, selectedDate, selectedMonth, selectedYear, isAdmin, activeBranch) {
        transactions.filter { tx ->
            val branchMatches = isAdmin || tx.branch.equals(activeBranch, ignoreCase = true)
            if (!branchMatches) return@filter false

            when (filterType) {
                "daily" -> tx.date == selectedDate
                "monthly" -> tx.date.startsWith(selectedMonth)
                "yearly" -> tx.date.startsWith(selectedYear)
                else -> true
            }
        }
    }

    val totalOmzet = remember(filteredTx) {
        filteredTx.sumOf { it.price }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER ---
        item {
            Column {
                Text(
                    text = "Riwayat Transaksi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    text = "Filter berdasarkan Kalender Masehi",
                    fontSize = 11.sp,
                    color = textMuted
                )
            }
        }

        // --- FILTER CARD ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceColor)
                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter Type Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (darkMode) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("daily" to "Harian", "monthly" to "Bulanan", "yearly" to "Tahunan").forEach { (type, label) ->
                        val isSelected = filterType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BluePrimary else Color.Transparent)
                                .clickable { filterType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else textMuted
                            )
                        }
                    }
                }

                // Filter Picker
                when (filterType) {
                    "daily" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (darkMode) Color(0xFF1F2937) else Color(0xFFF8FAFC))
                                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tanggal: $selectedDate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    "monthly" -> {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { selectedMonth = it },
                            label = { Text("Bulan (YYYY-MM)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    "yearly" -> {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            label = { Text("Tahun (YYYY)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // Total Omset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Omset:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted
                    )
                    Text(
                        text = currencyFormatter.format(totalOmzet),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )
                }

                // CRITICAL REQUIREMENT: EXPORT BUTTONS ARE ONLY DISPLAYED TO ADMIN!
                // "untuk tampilan eksport data penjualan dan data stock toko hanya ditampilkan di akun admin dan diakun pengguna staff cabang tidak diperlihatkan"
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val currentFilterVal = when (filterType) {
                                    "daily" -> selectedDate
                                    "monthly" -> selectedMonth
                                    else -> selectedYear
                                }
                                viewModel.exportTransactionsCSV(filterType, currentFilterVal)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CSV (Excel)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val currentFilterVal = when (filterType) {
                                    "daily" -> selectedDate
                                    "monthly" -> selectedMonth
                                    else -> selectedYear
                                }
                                val summary = StringBuilder("LAPORAN PENJUALAN ($filterType - $currentFilterVal)\n")
                                summary.append("Total Omset: ${currencyFormatter.format(totalOmzet)}\n")
                                summary.append("Jumlah Transaksi: ${filteredTx.size}\n\n")
                                filteredTx.forEachIndexed { i, tx ->
                                    summary.append("${i + 1}. [${tx.time}] ${tx.branch} - ${tx.item} : Rp ${tx.price.toLong()} (${tx.member})\n")
                                }
                                viewModel.exportReportPDF("Laporan_Penjualan_$currentFilterVal", summary.toString())
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF (Cetak)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- TRANSACTIONS LIST ---
        if (filteredTx.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada riwayat transaksi pada filter ini.",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                }
            }
        } else {
            items(filteredTx) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = tx.item,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        )
                        Text(
                            text = "${tx.date} • ${tx.time} • ${tx.branch} • ${tx.member}",
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    }

                    Text(
                        text = currencyFormatter.format(tx.price),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldSuccess
                    )
                }
            }
        }
    }
}
