package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.McKaysRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class McKaysViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = McKaysRepository(application)
    private val prefs = application.getSharedPreferences("mckays_prefs", Context.MODE_PRIVATE)

    val inventory: StateFlow<List<InventoryItem>> = repository.observeInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterPrices: StateFlow<List<MasterPrice>> = repository.observeMasterPrices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffAccounts: StateFlow<List<StaffAccount>> = repository.observeStaffAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockOrders: StateFlow<List<StockOrder>> = repository.observeStockOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionRecord>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<MemberRecord>> = repository.observeMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _orderCart = MutableStateFlow<List<OrderCartItem>>(emptyList())
    val orderCart: StateFlow<List<OrderCartItem>> = _orderCart.asStateFlow()

    init {
        // Restore session from prefs if exists
        val savedUsername = prefs.getString("user_username", null)
        val savedRole = prefs.getString("user_role", null)
        val savedBranch = prefs.getString("user_branch", null)
        val savedPic = prefs.getString("user_pic", null)

        if (savedUsername != null && savedRole != null && savedBranch != null) {
            _currentUser.value = UserSession(savedUsername, savedRole, savedBranch, savedPic)
        }

        val savedDarkMode = prefs.getBoolean("dark_mode", false)
        _darkMode.value = savedDarkMode

        // Seed initial data in Firestore if empty
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setDarkMode(dark: Boolean) {
        _darkMode.value = dark
        prefs.edit().putBoolean("dark_mode", dark).apply()
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            val cleanUser = username.trim()
            val cleanPass = pass.trim()
            if (cleanUser == "mckays" && cleanPass == "bismillah22") {
                val adminUser = UserSession(
                    username = "mckays",
                    role = "admin",
                    branch = "Pusat",
                    profilePic = prefs.getString("user_pic_mckays", null)
                )
                _currentUser.value = adminUser
                saveUserSession(adminUser)
                showToast("Login berhasil sebagai Admin Pusat!")
                return@launch
            }

            var found = staffAccounts.value.find {
                it.username.equals(cleanUser, ignoreCase = true) && it.password == cleanPass
            }

            if (found == null) {
                try {
                    found = repository.getStaffAccount(cleanUser, cleanPass)
                } catch (e: Exception) {
                    Log.e("McKaysViewModel", "Error fetching staff account", e)
                }
            }

            if (found != null) {
                val staffUser = UserSession(
                    username = found.username,
                    role = "staff",
                    branch = found.branch,
                    profilePic = prefs.getString("user_pic_${found.username}", null)
                )
                _currentUser.value = staffUser
                saveUserSession(staffUser)
                showToast("Login berhasil sebagai Staff (${found.branch})!")
                return@launch
            }

            showToast("ID atau Password salah!")
        }
    }

    private fun saveUserSession(user: UserSession) {
        prefs.edit()
            .putString("user_username", user.username)
            .putString("user_role", user.role)
            .putString("user_branch", user.branch)
            .putString("user_pic", user.profilePic)
            .apply()
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit()
            .remove("user_username")
            .remove("user_role")
            .remove("user_branch")
            .remove("user_pic")
            .apply()
        showToast("Logout berhasil.")
    }

    fun updateProfilePicture(picUriOrBase64: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(profilePic = picUriOrBase64)
        _currentUser.value = updated
        prefs.edit()
            .putString("user_pic", picUriOrBase64)
            .putString("user_pic_${current.username}", picUriOrBase64)
            .apply()
        showToast("Foto profil diperbarui!")
    }

    fun updateAppLogo(logoUriOrBase64: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.updateAppSettings(currentSettings.copy(appLogo = logoUriOrBase64))
            showToast("Logo aplikasi berhasil diperbarui & disinkronkan!")
        }
    }

    // --- Order Cart Actions ---

    fun addToOrderCart(item: String, ml: Double, bottleSize: String, botol: Long) {
        val newItem = OrderCartItem(
            id = UUID.randomUUID().toString(),
            item = item.trim(),
            ml = ml,
            bottleSize = bottleSize,
            botol = botol
        )
        _orderCart.value = _orderCart.value + newItem
        showToast("Item ditambahkan ke keranjang order.")
    }

    fun removeFromOrderCart(id: String) {
        _orderCart.value = _orderCart.value.filter { it.id != id }
    }

    fun sendBatchOrder(branch: String) {
        val cart = _orderCart.value
        if (cart.isEmpty()) return

        viewModelScope.launch {
            for (c in cart) {
                val order = StockOrder(
                    id = "ORD-${(100..999).random()}",
                    branch = branch,
                    item = c.item,
                    ml = c.ml,
                    bottleSize = c.bottleSize,
                    botol = c.botol,
                    status = "Menunggu Persetujuan"
                )
                repository.createStockOrder(order)
            }
            _orderCart.value = emptyList()
            showToast("Semua order berhasil dikirim ke Admin Pusat!")
        }
    }

    // --- Admin Actions ---

    fun saveMasterPrice(name: String, priceMl: Double, priceBotol: Double) {
        viewModelScope.launch {
            repository.saveMasterPrice(MasterPrice(id = name, name = name, price_ml = priceMl, price_botol = priceBotol))
            showToast("Harga master $name berhasil disimpan!")
        }
    }

    fun deleteBranchItem(id: String, branch: String) {
        viewModelScope.launch {
            repository.deleteInventoryItem(id)
            showToast("Item berhasil dihapus dari $branch.")
        }
    }

    fun approveOrder(order: StockOrder, approve: Boolean) {
        viewModelScope.launch {
            repository.approveStockOrder(order, approve, inventory.value)
            showToast(if (approve) "Order disetujui & stok masuk cabang!" else "Order ditolak.")
        }
    }

    fun approveAllOrders() {
        val pending = stockOrders.value.filter { it.status == "Menunggu Persetujuan" }
        if (pending.isEmpty()) return

        viewModelScope.launch {
            repository.approveAllPendingOrders(pending, inventory.value)
            showToast("Semua order pending berhasil disetujui!")
        }
    }

    fun addStaffAccount(username: String, pass: String, branch: String) {
        viewModelScope.launch {
            val newStaff = StaffAccount(
                id = UUID.randomUUID().toString(),
                username = username.trim(),
                password = pass.trim(),
                branch = branch.trim()
            )
            repository.saveStaffAccount(newStaff)
            showToast("Cabang $branch & Staff $username berhasil ditambahkan!")
        }
    }

    fun updateStaffPassword(staffId: String, newPass: String) {
        viewModelScope.launch {
            repository.updateStaffPassword(staffId, newPass.trim())
            showToast("Password staff berhasil direset!")
        }
    }

    fun resetStaffPassword(staffId: String, newPass: String) {
        updateStaffPassword(staffId, newPass)
    }

    fun deleteStaffAccount(staffId: String) {
        viewModelScope.launch {
            repository.deleteStaffAccount(staffId)
            showToast("Akun staff berhasil dihapus.")
        }
    }

    fun bulkUploadInventory(branch: String, text: String) {
        viewModelScope.launch {
            repository.bulkUploadInventory(branch, text, inventory.value)
            showToast("Berhasil upload & akumulasi stok cabang $branch!")
        }
    }

    fun resetAllMemberPoints() {
        viewModelScope.launch {
            repository.resetAllMemberPoints()
            showToast("Semua poin member dinolkan.")
        }
    }

    // --- Cashier / Sales Action ---

    fun processSale(
        branch: String,
        saleType: String,
        itemQuery: String,
        mlVal: Double,
        bottleSize: String,
        botolVal: Long,
        priceVal: Double,
        member: String
    ): Boolean {
        val matchedItem = inventory.value.find {
            it.branch == branch && it.name.equals(itemQuery.trim(), ignoreCase = true)
        }

        if (matchedItem != null) {
            if (matchedItem.stock_ml < mlVal) {
                showToast("Stok bibit tidak mencukupi (${matchedItem.stock_ml}ml tersedia)")
                return false
            }
            if (matchedItem.stock_botol < botolVal) {
                showToast("Stok botol tidak mencukupi (${matchedItem.stock_botol}pcs tersedia)")
                return false
            }
        }

        viewModelScope.launch {
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val now = Date()

            val newTx = TransactionRecord(
                id = "TRX-${(1000..9999).random()}",
                branch = branch,
                type = saleType,
                item = if (itemQuery.isNotBlank()) "$itemQuery (${mlVal.toInt()}ml)" else "Penjualan",
                detail = "$saleType | ${mlVal.toInt()}ml | ${botolVal}pcs ($bottleSize)",
                price = priceVal,
                member = member,
                pointsEarned = (priceVal / 10000).toLong(),
                time = sdfTime.format(now),
                date = sdfDate.format(now)
            )

            repository.recordTransaction(newTx, matchedItem, mlVal, botolVal)
            showToast("Transaksi berhasil disimpan!")
        }
        return true
    }

    fun addMember(name: String, phone: String, points: Long) {
        viewModelScope.launch {
            val mem = MemberRecord(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                phone = phone.trim().ifBlank { "-" },
                points = points
            )
            repository.saveMember(mem)
            showToast("Member baru berhasil ditambahkan!")
        }
    }

    fun deleteMember(id: String) {
        viewModelScope.launch {
            repository.deleteMember(id)
            showToast("Member berhasil dihapus.")
        }
    }

    // --- Export Functions (Admin Exclusively) ---

    fun exportTransactionsCSV(filterType: String, dateFilter: String): String {
        val txs = transactions.value.filter {
            when (filterType) {
                "daily" -> it.date == dateFilter
                "monthly" -> it.date.startsWith(dateFilter)
                "yearly" -> it.date.startsWith(dateFilter)
                else -> true
            }
        }

        val sb = StringBuilder()
        sb.append("No,Tanggal,ID Transaksi,Cabang,Tipe,Produk,Harga,Member\n")
        txs.forEachIndexed { idx, tx ->
            sb.append("${idx + 1},${tx.date},${tx.id},${tx.branch},\"${tx.type}\",\"${tx.item}\",${tx.price.toLong()},\"${tx.member}\"\n")
        }
        val content = sb.toString()
        saveAndShareFile("Laporan_Penjualan_${filterType}_$dateFilter.csv", content, "text/csv")
        showToast("Laporan CSV berhasil dibuat & dibagikan!")
        return content
    }

    fun exportBranchStockCSV(branchName: String): String {
        val items = inventory.value.filter { it.branch == branchName }
        val sb = StringBuilder()
        sb.append("No,Nama Produk,Stok Bibit (ml),Stok Botol (pcs)\n")
        items.forEachIndexed { idx, item ->
            sb.append("${idx + 1},\"${item.name}\",${item.stock_ml},${item.stock_botol}\n")
        }
        val content = sb.toString()
        saveAndShareFile("Stok_Cabang_$branchName.csv", content, "text/csv")
        showToast("Export CSV Stok Cabang $branchName berhasil!")
        return content
    }

    fun exportReportPDF(title: String, summaryText: String) {
        saveAndShareFile("$title.txt", summaryText, "text/plain")
        showToast("Laporan $title siap dicetak/diekspor!")
    }

    private fun saveAndShareFile(filename: String, content: String, mimeType: String) {
        try {
            val app = getApplication<Application>()
            val cachePath = File(app.cacheDir, "exports")
            cachePath.mkdirs()
            val file = File(cachePath, filename)
            FileWriter(file).use { it.write(content) }

            val contentUri: Uri = FileProvider.getUriForFile(
                app,
                "${app.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(Intent.createChooser(shareIntent, "Ekspor $filename").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e("McKaysViewModel", "Share file error", e)
        }
    }
}
