package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
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
import com.example.data.model.UserSession
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.RoseAlert

@Composable
fun MemberTab(
    viewModel: McKaysViewModel,
    currentUser: UserSession
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val members by viewModel.members.collectAsState()

    val isAdmin = currentUser.role == "admin"

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialPoints by remember { mutableStateOf("0") }

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
        item {
            Column {
                Text(
                    text = "Data Member Loyalitas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    text = "Kelola pelanggan terdaftar & perolehan poin",
                    fontSize = 11.sp,
                    color = textMuted
                )
            }
        }

        // Add Member Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tambah Member Baru & Poin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nama Member", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Nomor Telepon", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_phone_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = initialPoints,
                    onValueChange = { initialPoints = it },
                    label = { Text("Poin Awal (Opsional)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val pts = initialPoints.toLongOrNull() ?: 0L
                            viewModel.addMember(name, phone, pts)
                            name = ""
                            phone = ""
                            initialPoints = "0"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_member_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Tambah Member", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Members List
        if (members.isEmpty()) {
            item {
                Text(
                    text = "Belum ada member terdaftar.",
                    fontSize = 12.sp,
                    color = textMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(members) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = m.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        )
                        Text(
                            text = m.phone,
                            fontSize = 11.sp,
                            color = textMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BluePrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${m.points} Poin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BluePrimary
                            )
                        }

                        if (isAdmin) {
                            IconButton(
                                onClick = { viewModel.deleteMember(m.id) },
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
}
