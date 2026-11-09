package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItem
import com.example.data.model.MasterPrice
import com.example.data.model.UserSession
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseAlert

@Composable
fun StockTab(
    viewModel: McKaysViewModel,
    currentUser: UserSession
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val masterPrices by viewModel.masterPrices.collectAsState()
    val orderCart by viewModel.orderCart.collectAsState()

    val isAdmin = currentUser.role == "admin"
    val activeBranch = currentUser.branch

    var searchTerm by remember { mutableStateOf("") }
    var isOrderModalOpen by remember { mutableStateOf(false) }

    // Admin branch inspector selection
    var selectedBranchView by remember { mutableStateOf("Bandung") }

    // Price drafts for admin
    var priceDrafts by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }

    LaunchedEffect(masterPrices) {
        val drafts = mutableMapOf<String, Pair<String, String>>()
        masterPrices.forEach { p ->
            drafts[p.name] = Pair(p.price_ml.toInt().toString(), p.price_botol.toInt().toString())
        }
        priceDrafts = drafts
    }

    val bottleSizes = listOf("10ml", "15ml", "20ml", "30ml", "50ml", "100ml")

    val currentBranchItems = remember(inventory, isAdmin, activeBranch, searchTerm) {
        val branchFilter = if (isAdmin) "Pusat" else activeBranch
        inventory
            .filter { it.branch.equals(branchFilter, ignoreCase = true) }
            .filter { it.name.contains(searchTerm, ignoreCase = true) }
            .sortedBy { it.name }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAdmin) "Kelola Stok Cabang" else "Katalog & Stok ($activeBranch)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    Text(
                        text = "Pemisahan Stok Bibit & Botol (A-Z)",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }

                if (!isAdmin) {
                    Button(
                        onClick = { isOrderModalOpen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("cart_order_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Keranjang (${orderCart.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- SEARCH BAR ---
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Cari produk...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = borderColor
                )
            )
        }

        // --- ADMIN ONLY: KELOLA HARGA MASTER ---
        if (isAdmin) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KELOLA HARGA MASTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BluePrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Konfirmasi perubahan agar tersinkron ke staff",
                                fontSize = 10.sp,
                                color = textMuted
                            )
                        }

                        Button(
                            onClick = {
                                priceDrafts.forEach { (name, draft) ->
                                    val ml = draft.first.toDoubleOrNull() ?: 0.0
                                    val botol = draft.second.toDoubleOrNull() ?: 0.0
                                    viewModel.saveMasterPrice(name, ml, botol)
                                }
                                viewModel.showToast("Semua harga master berhasil dikonfirmasi & disinkronkan!")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Konfirmasi Semua", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    masterPrices.forEach { mp ->
                        val draft = priceDrafts[mp.name] ?: Pair(mp.price_ml.toInt().toString(), mp.price_botol.toInt().toString())
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mp.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary
                                )

                                Button(
                                    onClick = {
                                        val ml = draft.first.toDoubleOrNull() ?: 0.0
                                        val botol = draft.second.toDoubleOrNull() ?: 0.0
                                        viewModel.saveMasterPrice(mp.name, ml, botol)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Konfirmasi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draft.first,
                                    onValueChange = { newMl ->
                                        priceDrafts = priceDrafts + (mp.name to Pair(newMl, draft.second))
                                    },
                                    label = { Text("Harga/ml", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = draft.second,
                                    onValueChange = { newBotol ->
                                        priceDrafts = priceDrafts + (mp.name to Pair(draft.first, newBotol))
                                    },
                                    label = { Text("Harga Botol", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- ADMIN ONLY: LIHAT & HAPUS STOK CABANG STAFF ---
            item {
                val otherBranches = remember(inventory) {
                    inventory.map { it.branch }.distinct().filter { !it.equals("Pusat", ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIHAT & HAPUS STOK CABANG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5CF6),
                            letterSpacing = 0.5.sp
                        )

                        // EXPORT BUTTONS: ONLY DISPLAYED TO ADMIN!
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    val branchItems = inventory.filter { it.branch == selectedBranchView }
                                    val summary = StringBuilder("LAPORAN STOK CABANG $selectedBranchView\n\n")
                                    branchItems.forEachIndexed { i, itm ->
                                        summary.append("${i + 1}. ${itm.name} - Bibit: ${itm.stock_ml}ml, Botol: ${itm.stock_botol}pcs\n")
                                    }
                                    viewModel.exportReportPDF("Stok_Cabang_$selectedBranchView", summary.toString())
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.exportBranchStockCSV(selectedBranchView)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Excel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Branch selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val branchesToShow = if (otherBranches.isEmpty()) listOf("Bandung", "Surabaya") else otherBranches
                        branchesToShow.forEach { br ->
                            val isSelected = selectedBranchView.equals(br, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BluePrimary else if (darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    .clickable { selectedBranchView = br }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Cabang $br",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else textMuted
                                )
                            }
                        }
                    }

                    val branchStockList = inventory.filter { it.branch.equals(selectedBranchView, ignoreCase = true) }
                    if (branchStockList.isEmpty()) {
                        Text(
                            text = "Cabang $selectedBranchView kosong stok.",
                            fontSize = 11.sp,
                            color = textMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        branchStockList.forEach { itm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = itm.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "${itm.stock_ml.toInt()}ml | ${itm.stock_botol}pcs",
                                        fontSize = 10.sp,
                                        color = textMuted
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteBranchItem(itm.id, selectedBranchView)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = RoseAlert,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- KATALOG BIBIT (ml) ---
        item {
            Text(
                text = "KATALOG STOK BIBIT (ML)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = BluePrimary,
                letterSpacing = 1.sp
            )
        }

        if (currentBranchItems.isEmpty()) {
            item {
                Text(
                    text = "Belum ada stok produk.",
                    fontSize = 12.sp,
                    color = textMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(currentBranchItems) { item ->
                val priceInfo = masterPrices.find { it.name.equals(item.name, ignoreCase = true) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        )
                        Text(
                            text = "Sisa Stok Bibit: ${item.stock_ml.toInt()} ml",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = "Harga: Rp ${priceInfo?.price_ml?.toInt() ?: 4000}/ml",
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    }

                    if (isAdmin) {
                        IconButton(
                            onClick = {
                                viewModel.deleteBranchItem(item.id, item.branch)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoseAlert.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = RoseAlert,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- KATALOG BOTOL (Pcs per Ukuran) ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "KATALOG STOK BOTOL (PCS PER UKURAN)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8B5CF6),
                letterSpacing = 1.sp
            )
        }

        item {
            val totalBotol = currentBranchItems.sumOf { it.stock_botol }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (chunk in bottleSizes.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chunk.forEach { size ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(surfaceColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Botol Ukuran",
                                    fontSize = 10.sp,
                                    color = textMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = size,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF8B5CF6)
                                )
                                Text(
                                    text = "Sisa: ${totalBotol / bottleSizes.size} pcs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ORDER MODAL / CART FOR STAFF ---
    if (isOrderModalOpen) {
        OrderCartModal(
            viewModel = viewModel,
            activeBranch = activeBranch,
            onDismiss = { isOrderModalOpen = false }
        )
    }
}
