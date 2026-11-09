package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import java.text.NumberFormat
import java.util.*

@Composable
fun SalesModal(
    viewModel: McKaysViewModel,
    branch: String,
    onDismiss: () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val masterPrices by viewModel.masterPrices.collectAsState()
    val members by viewModel.members.collectAsState()

    var saleType by remember { mutableStateOf("Kombinasi (Bibit + Botol)") }
    var itemQuery by remember { mutableStateOf("") }
    var takaranMl by remember { mutableStateOf("0") }
    var bottleSize by remember { mutableStateOf("30ml") }
    var jumlahBotol by remember { mutableStateOf("0") }
    var priceInput by remember { mutableStateOf("0") }
    var cashPaid by remember { mutableStateOf("0") }
    var selectedMember by remember { mutableStateOf("Umum") }

    var isQuickAddMemberOpen by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Auto-calculate price on changes
    LaunchedEffect(saleType, itemQuery, takaranMl, jumlahBotol, masterPrices) {
        val ml = takaranMl.toDoubleOrNull() ?: 0.0
        val botol = jumlahBotol.toLongOrNull() ?: 0L
        val pricing = masterPrices.find { it.name.equals(itemQuery.trim(), ignoreCase = true) }
            ?: masterPrices.firstOrNull()
            ?: com.example.data.model.MasterPrice(price_ml = 4000.0, price_botol = 6000.0)

        val calc = when {
            saleType.contains("Kombinasi") -> (pricing.price_ml * ml) + (pricing.price_botol * botol)
            saleType.contains("Bibit") -> pricing.price_ml * ml
            saleType.contains("Botol") -> pricing.price_botol * botol.toDouble()
            else -> 0.0
        }
        if (calc > 0) {
            priceInput = calc.toLong().toString()
        }
    }

    val surfaceColor = if (darkMode) Color(0xFF111827) else Color.White
    val borderColor = if (darkMode) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val textPrimary = if (darkMode) Color.White else Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(top = 40.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(surfaceColor)
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Billing Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BlueDark, Color(0xFF312E81))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL TAGIHAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFBFDBFE),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormatter.format(priceInput.toDoubleOrNull() ?: 0.0),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                        }
                    }
                }

                // Tipe Penjualan Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tipe Penjualan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMuted)
                    var expandedType by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedType = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(saleType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            listOf("Kombinasi (Bibit + Botol)", "Bibit Saja", "Botol Saja").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontSize = 12.sp) },
                                    onClick = {
                                        saleType = opt
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Form Item Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nama Item / Parfum", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textMuted)
                        OutlinedTextField(
                            value = itemQuery,
                            onValueChange = { itemQuery = it },
                            placeholder = { Text("cth: VC Scandal", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = takaranMl,
                            onValueChange = { takaranMl = it },
                            label = { Text("Bibit (ml)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Bottle Size selector
                        var expandedSize by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedSize = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(bottleSize, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(expanded = expandedSize, onDismissRequest = { expandedSize = false }) {
                                listOf("10ml", "15ml", "20ml", "30ml", "50ml", "100ml").forEach { sz ->
                                    DropdownMenuItem(
                                        text = { Text(sz, fontSize = 11.sp) },
                                        onClick = {
                                            bottleSize = sz
                                            expandedSize = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = jumlahBotol,
                            onValueChange = { jumlahBotol = it },
                            label = { Text("Jumlah Botol", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Price Field (Editable)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nominal Harga (Dapat Diubah Manual)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("price_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = borderColor
                        )
                    )
                }

                // Member Selection & Quick Add Member
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pelanggan Member", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMuted)
                        Text(
                            text = "+ Tambah Member",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier.clickable { isQuickAddMemberOpen = true }
                        )
                    }

                    var expandedMember by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedMember = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(selectedMember, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = expandedMember, onDismissRequest = { expandedMember = false }) {
                            DropdownMenuItem(
                                text = { Text("Umum (Tanpa Poin)", fontSize = 12.sp) },
                                onClick = {
                                    selectedMember = "Umum"
                                    expandedMember = false
                                }
                            )
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.points} Poin)", fontSize = 12.sp) },
                                    onClick = {
                                        selectedMember = m.name
                                        expandedMember = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Numeric Keypad for Cash Payment
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "UANG TUNAI / BAYAR (RP)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary,
                        letterSpacing = 0.5.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(BluePrimary.copy(alpha = 0.1f))
                            .border(2.dp, BluePrimary, RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            Text(
                                text = NumberFormat.getNumberInstance(Locale("id", "ID")).format(cashPaid.toLongOrNull() ?: 0L),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = BluePrimary
                            )
                        }
                    }

                    // Keypad grid
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("00", "0", "DEL", "C")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { k ->
                                Button(
                                    onClick = {
                                        when (k) {
                                            "C" -> cashPaid = "0"
                                            "DEL" -> cashPaid = if (cashPaid.length > 1) cashPaid.dropLast(1) else "0"
                                            else -> cashPaid = if (cashPaid == "0") k else cashPaid + k
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                        contentColor = textPrimary
                                    )
                                ) {
                                    Text(k, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // Process Transaction Button
                Button(
                    onClick = {
                        val ml = takaranMl.toDoubleOrNull() ?: 0.0
                        val botol = jumlahBotol.toLongOrNull() ?: 0L
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        val success = viewModel.processSale(
                            branch = branch,
                            saleType = saleType,
                            itemQuery = itemQuery,
                            mlVal = ml,
                            bottleSize = bottleSize,
                            botolVal = botol,
                            priceVal = price,
                            member = selectedMember
                        )
                        if (success) {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_sale_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text(
                        text = "Proses & Selesaikan Transaksi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Quick Add Member Dialog
    if (isQuickAddMemberOpen) {
        var quickName by remember { mutableStateOf("") }
        var quickPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isQuickAddMemberOpen = false },
            title = { Text("Tambah Member Cepat", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quickName,
                        onValueChange = { quickName = it },
                        placeholder = { Text("Nama Member") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quickPhone,
                        onValueChange = { quickPhone = it },
                        placeholder = { Text("Nomor Telepon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickName.isNotBlank()) {
                            viewModel.addMember(quickName, quickPhone, 10L)
                            selectedMember = quickName
                            isQuickAddMemberOpen = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { isQuickAddMemberOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
