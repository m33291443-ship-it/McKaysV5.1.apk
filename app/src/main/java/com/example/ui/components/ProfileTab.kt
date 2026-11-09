package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserSession
import com.example.ui.McKaysViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseAlert
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileTab(
    viewModel: McKaysViewModel,
    currentUser: UserSession
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val staffAccounts by viewModel.staffAccounts.collectAsState()
    val stockOrders by viewModel.stockOrders.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val isAdmin = currentUser.role == "admin"

    var newStaffUser by remember { mutableStateOf("") }
    var newStaffPass by remember { mutableStateOf("") }
    var newStaffBranch by remember { mutableStateOf("") }

    var editingStaffId by remember { mutableStateOf<String?>(null) }
    var editStaffNewPass by remember { mutableStateOf("") }
    var visiblePasswords by remember { mutableStateOf(setOf<String>()) }

    var bulkBranch by remember { mutableStateOf("Pusat") }
    var bulkText by remember { mutableStateOf("") }

    // Photo picker for profile picture
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfilePicture(uri.toString())
        }
    }

    // Photo picker for app logo (Admin)
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateAppLogo(uri.toString())
        }
    }

    val surfaceColor = if (darkMode) Color(0xFF111827) else Color.White
    val borderColor = if (darkMode) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val textPrimary = if (darkMode) Color.White else Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    val branches = remember(inventory) {
        val list = inventory.map { it.branch }.distinct().toMutableList()
        if (!list.contains("Pusat")) list.add(0, "Pusat")
        list
    }

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
                Text(
                    text = "Pengaturan Akun",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )

                IconButton(
                    onClick = { viewModel.setDarkMode(!darkMode) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (darkMode) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (darkMode) Color(0xFFFBBF24) else Color(0xFF475569)
                    )
                }
            }
        }

        // --- USER PROFILE CARD ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceColor)
                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with click to pick
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(alpha = 0.15f))
                        .border(2.dp, BluePrimary, CircleShape)
                        .clickable { profilePhotoPicker.launch("image/*") }
                        .testTag("profile_avatar_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser.profilePic.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser.profilePic,
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = currentUser.username.take(2).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = BluePrimary
                        )
                    }

                    // Camera overlay icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ganti Foto",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentUser.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    Text(
                        text = if (isAdmin) "Admin Pusat" else "Staff Cabang: ${currentUser.branch}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    // Note: "POS Multi-Branch v1.0" is removed as requested by the prompt!
                }

                // Cloud backup info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sinkron Google Drive: budayaxwel@gmail.com",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted
                    )
                }
            }
        }

        // --- ADMIN CONTROLS ---
        if (isAdmin) {
            item {
                Text(
                    text = "KONTROL MASTER & MANAJEMEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = textMuted,
                    letterSpacing = 1.sp
                )
            }

            // 1. Ubah Logo Aplikasi
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ubah Logo Aplikasi (Sinkron Semua Akun)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!settings.appLogo.isNullOrBlank()) {
                                AsyncImage(
                                    model = settings.appLogo,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "M",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { logoPicker.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Logo Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Upload List Produk Massal (Akumulasi Stok)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Upload List Produk Massal (Akumulasi Stok)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )

                    // Branch dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        branches.forEach { br ->
                            val isSelected = bulkBranch.equals(br, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BluePrimary else if (darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    .clickable { bulkBranch = br }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = br,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else textMuted
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = { Text("Format: NamaProduk, ml, botol\ncth: VC Scandal, 10, 20", fontSize = 11.sp) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            if (bulkText.isNotBlank()) {
                                viewModel.bulkUploadInventory(bulkBranch, bulkText)
                                bulkText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("Upload & Akumulasi Stok Cabang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Persetujuan Request Barang Cabang
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Persetujuan Request Barang Cabang",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = BluePrimary,
                            letterSpacing = 0.5.sp
                        )

                        if (stockOrders.any { it.status == "Menunggu Persetujuan" }) {
                            Button(
                                onClick = { viewModel.approveAllOrders() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Terima Semua", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (stockOrders.isEmpty()) {
                        Text(
                            text = "Belum ada order masuk.",
                            fontSize = 11.sp,
                            color = textMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        stockOrders.forEach { ord ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (darkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BluePrimary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ord.branch.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = BluePrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ord.item,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when (ord.status) {
                                                    "Disetujui" -> EmeraldSuccess.copy(alpha = 0.15f)
                                                    "Ditolak" -> RoseAlert.copy(alpha = 0.15f)
                                                    else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = ord.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (ord.status) {
                                                "Disetujui" -> EmeraldSuccess
                                                "Ditolak" -> RoseAlert
                                                else -> Color(0xFFF59E0B)
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = "Bibit: ${ord.ml.toInt()} ml  •  Botol (${ord.bottleSize}): ${ord.botol} pcs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMuted
                                )

                                if (ord.status == "Menunggu Persetujuan") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approveOrder(ord, true) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                        ) {
                                            Text("Setuju", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.approveOrder(ord, false) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RoseAlert)
                                        ) {
                                            Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Kelola Toko Cabang & Staff
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kelola Toko Cabang & Staff",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )

                    OutlinedTextField(
                        value = newStaffUser,
                        onValueChange = { newStaffUser = it },
                        placeholder = { Text("Username Staff", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newStaffPass,
                        onValueChange = { newStaffPass = it },
                        placeholder = { Text("Password", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newStaffBranch,
                        onValueChange = { newStaffBranch = it },
                        placeholder = { Text("Nama Cabang (cth: Medan)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (newStaffUser.isNotBlank() && newStaffPass.isNotBlank() && newStaffBranch.isNotBlank()) {
                                viewModel.addStaffAccount(newStaffUser, newStaffPass, newStaffBranch)
                                newStaffUser = ""
                                newStaffPass = ""
                                newStaffBranch = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Tambah Cabang & Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daftar Akun Staff & Manajemen:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted
                    )

                    staffAccounts.forEach { staff ->
                        val isPassVisible = visiblePasswords.contains(staff.id)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BluePrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = staff.username,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "Cabang: ${staff.branch}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Akses Aktif",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }

                            // Password Display Row with Visibility Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (darkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Password:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textMuted
                                    )
                                    Text(
                                        text = if (isPassVisible) staff.password else "••••••••",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        visiblePasswords = if (isPassVisible) {
                                            visiblePasswords - staff.id
                                        } else {
                                            visiblePasswords + staff.id
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Lihat Password",
                                        tint = textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        editingStaffId = if (editingStaffId == staff.id) null else staff.id
                                        editStaffNewPass = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockReset,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (editingStaffId == staff.id) "Batal" else "Reset Password",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                TextButton(
                                    onClick = { viewModel.deleteStaffAccount(staff.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = RoseAlert,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hapus",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoseAlert
                                    )
                                }
                            }

                            if (editingStaffId == staff.id) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editStaffNewPass,
                                        onValueChange = { editStaffNewPass = it },
                                        placeholder = { Text("Password baru untuk ${staff.username}...", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (editStaffNewPass.isNotBlank()) {
                                                viewModel.resetStaffPassword(staff.id, editStaffNewPass)
                                                editingStaffId = null
                                                editStaffNewPass = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                    ) {
                                        Text("Simpan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Export Laporan Bulanan (PDF & Excel) -> Admin only
            item {
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Export Laporan Bulanan ($currentMonth)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportReportPDF("Laporan_Bulanan_$currentMonth", "Ringkasan Laporan Bulanan $currentMonth")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.exportTransactionsCSV("monthly", currentMonth)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("Export Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Reset Points
            item {
                Button(
                    onClick = { viewModel.resetAllMemberPoints() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "Nol-kan Poin Seluruh Member",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseAlert
                    )
                }
            }
        }

        // --- LOGOUT BUTTON ---
        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseAlert)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT DARI APLIKASI", fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }

        // --- FOOTER ---
        item {
            Text(
                text = "McKays",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}
