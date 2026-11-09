package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseAlert

@Composable
fun OrderCartModal(
    viewModel: McKaysViewModel,
    activeBranch: String,
    onDismiss: () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val orderCart by viewModel.orderCart.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var itemMl by remember { mutableStateOf("") }
    var bottleSize by remember { mutableStateOf("30ml") }
    var itemBotol by remember { mutableStateOf("") }

    val bottleSizes = listOf("10ml", "15ml", "20ml", "30ml", "50ml", "100ml")

    val surfaceColor = if (darkMode) Color(0xFF111827) else Color.White
    val borderColor = if (darkMode) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val textPrimary = if (darkMode) Color.White else Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(top = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(surfaceColor)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keranjang & Order Stok ke Admin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Add to Cart Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tambah Item ke Keranjang Order",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        placeholder = { Text("Nama Item / Parfum (cth: VC Scandal)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemMl,
                            onValueChange = { itemMl = it },
                            label = { Text("Bibit (ml)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

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
                                bottleSizes.forEach { sz ->
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
                            value = itemBotol,
                            onValueChange = { itemBotol = it },
                            label = { Text("Jumlah Botol", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                val ml = itemMl.toDoubleOrNull() ?: 0.0
                                val botol = itemBotol.toLongOrNull() ?: 0L
                                viewModel.addToOrderCart(itemName, ml, bottleSize, botol)
                                itemName = ""
                                itemMl = ""
                                itemBotol = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("+ Masukkan ke Keranjang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Cart List
                Text(
                    text = "Daftar Item di Keranjang (${orderCart.size}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted
                )

                if (orderCart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keranjang order masih kosong.",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orderCart) { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = c.item,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Bibit: ${c.ml.toInt()}ml  |  Botol (${c.bottleSize}): ${c.botol}pcs",
                                        fontSize = 10.sp,
                                        color = textMuted
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeFromOrderCart(c.id) },
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

                    // Send All Batch Order Button
                    Button(
                        onClick = {
                            viewModel.sendBatchOrder(activeBranch)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("send_batch_order_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Semua Order ke Admin",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
