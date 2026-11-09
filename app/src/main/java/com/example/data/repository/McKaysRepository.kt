package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class McKaysRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth = Firebase.auth

    // --- SNAPSHOT LISTENER FLOWS ---

    fun observeInventory(): Flow<List<InventoryItem>> = callbackFlow {
        val listener = db.collection("inventory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing inventory", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(InventoryItem::class.java) }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeMasterPrices(): Flow<List<MasterPrice>> = callbackFlow {
        val listener = db.collection("master_prices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing master prices", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val prices = snapshot.documents.mapNotNull { it.toObject(MasterPrice::class.java) }
                    trySend(prices)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeStaffAccounts(): Flow<List<StaffAccount>> = callbackFlow {
        val listener = db.collection("staff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing staff", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val staff = snapshot.documents.mapNotNull { it.toObject(StaffAccount::class.java) }
                    trySend(staff)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeStockOrders(): Flow<List<StockOrder>> = callbackFlow {
        val listener = db.collection("stock_orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing orders", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { it.toObject(StockOrder::class.java) }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeTransactions(): Flow<List<TransactionRecord>> = callbackFlow {
        val listener = db.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing transactions", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val txs = snapshot.documents.mapNotNull { it.toObject(TransactionRecord::class.java) }
                    trySend(txs)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeMembers(): Flow<List<MemberRecord>> = callbackFlow {
        val listener = db.collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing members", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val members = snapshot.documents.mapNotNull { it.toObject(MemberRecord::class.java) }
                    trySend(members)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeSettings(): Flow<AppSettings> = callbackFlow {
        val listener = db.collection("settings").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("McKaysRepo", "Error observing settings", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val settings = snapshot.toObject(AppSettings::class.java) ?: AppSettings()
                    trySend(settings)
                } else {
                    trySend(AppSettings())
                }
            }
        awaitClose { listener.remove() }
    }

    // --- SEED INITIAL DATA IF EMPTY ---
    suspend fun seedInitialDataIfEmpty() {
        try {
            val pricesSnap = db.collection("master_prices").limit(1).get().await()
            if (pricesSnap.isEmpty) {
                val defaultPrices = listOf(
                    MasterPrice(id = "VC Scandal", name = "VC Scandal", price_ml = 4000.0, price_botol = 6000.0),
                    MasterPrice(id = "PL Romantichwish", name = "PL Romantichwish", price_ml = 3500.0, price_botol = 5000.0),
                    MasterPrice(id = "Pink Shiffon", name = "Pink Shiffon", price_ml = 3000.0, price_botol = 5000.0),
                    MasterPrice(id = "ES Cherry", name = "ES Cherry", price_ml = 4500.0, price_botol = 6000.0),
                    MasterPrice(id = "Air ZR Nude", name = "Air ZR Nude", price_ml = 3500.0, price_botol = 5000.0)
                )
                for (p in defaultPrices) {
                    db.collection("master_prices").document(p.id).set(p).await()
                }
            }

            val staffSnap = db.collection("staff").limit(1).get().await()
            if (staffSnap.isEmpty) {
                val defaultStaff = listOf(
                    StaffAccount(id = "staff-1", username = "budi", password = "123", branch = "Bandung"),
                    StaffAccount(id = "staff-2", username = "surabaya", password = "123", branch = "Surabaya")
                )
                for (s in defaultStaff) {
                    db.collection("staff").document(s.id).set(s).await()
                }
            }

            val invSnap = db.collection("inventory").limit(1).get().await()
            if (invSnap.isEmpty) {
                val defaultItems = listOf(
                    InventoryItem(id = "pusat-1", branch = "Pusat", name = "VC Scandal", stock_ml = 150.0, stock_botol = 50),
                    InventoryItem(id = "pusat-2", branch = "Pusat", name = "PL Romantichwish", stock_ml = 100.0, stock_botol = 30),
                    InventoryItem(id = "pusat-3", branch = "Pusat", name = "Pink Shiffon", stock_ml = 80.0, stock_botol = 40),
                    InventoryItem(id = "pusat-4", branch = "Pusat", name = "ES Cherry", stock_ml = 9.0, stock_botol = 5),
                    InventoryItem(id = "bdg-1", branch = "Bandung", name = "VC Scandal", stock_ml = 50.0, stock_botol = 20),
                    InventoryItem(id = "bdg-2", branch = "Bandung", name = "Air ZR Nude", stock_ml = 8.0, stock_botol = 2),
                    InventoryItem(id = "sby-1", branch = "Surabaya", name = "Pink Shiffon", stock_ml = 45.0, stock_botol = 15)
                )
                for (item in defaultItems) {
                    db.collection("inventory").document(item.id).set(item).await()
                }
            }

            val memSnap = db.collection("members").limit(1).get().await()
            if (memSnap.isEmpty) {
                val defaultMembers = listOf(
                    MemberRecord(id = "mem-1", name = "Siti Rahma", phone = "081234567890", points = 45),
                    MemberRecord(id = "mem-2", name = "Ahmad Fauzi", phone = "089876543210", points = 20)
                )
                for (m in defaultMembers) {
                    db.collection("members").document(m.id).set(m).await()
                }
            }

            val ordersSnap = db.collection("stock_orders").limit(1).get().await()
            if (ordersSnap.isEmpty) {
                val defaultOrders = listOf(
                    StockOrder(id = "ORD-01", branch = "Bandung", item = "VC Scandal", ml = 100.0, bottleSize = "30ml", botol = 20, status = "Menunggu Persetujuan"),
                    StockOrder(id = "ORD-02", branch = "Surabaya", item = "Pink Shiffon", ml = 50.0, bottleSize = "50ml", botol = 10, status = "Menunggu Persetujuan")
                )
                for (o in defaultOrders) {
                    db.collection("stock_orders").document(o.id).set(o).await()
                }
            }

            val setSnap = db.collection("settings").document("global").get().await()
            if (!setSnap.exists()) {
                db.collection("settings").document("global").set(
                    AppSettings(id = "global", googleDriveSyncAccount = "budayaxwel@gmail.com")
                ).await()
            }
        } catch (e: Exception) {
            Log.w("McKaysRepo", "Seeding error or already seeded: ${e.message}")
        }
    }

    // --- WRITE ACTIONS ---

    suspend fun saveInventoryItem(item: InventoryItem) {
        val docId = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id
        db.collection("inventory").document(docId).set(item.copy(id = docId)).await()
    }

    suspend fun deleteInventoryItem(id: String) {
        db.collection("inventory").document(id).delete().await()
    }

    suspend fun saveMasterPrice(price: MasterPrice) {
        val docId = if (price.id.isBlank()) price.name else price.id
        db.collection("master_prices").document(docId).set(price.copy(id = docId)).await()
    }

    suspend fun getStaffAccount(username: String, pass: String): StaffAccount? {
        val allStaff = db.collection("staff").get().await()
        return allStaff.documents.mapNotNull { it.toObject(StaffAccount::class.java) }
            .find { it.username.equals(username.trim(), ignoreCase = true) && it.password == pass.trim() }
    }

    suspend fun saveStaffAccount(staff: StaffAccount) {
        val docId = if (staff.id.isBlank()) UUID.randomUUID().toString() else staff.id
        db.collection("staff").document(docId).set(staff.copy(id = docId)).await()
    }

    suspend fun deleteStaffAccount(id: String) {
        db.collection("staff").document(id).delete().await()
    }

    suspend fun updateStaffPassword(id: String, newPass: String) {
        db.collection("staff").document(id).update("password", newPass).await()
    }

    suspend fun createStockOrder(order: StockOrder) {
        val docId = if (order.id.isBlank()) "ORD-${(100..999).random()}" else order.id
        db.collection("stock_orders").document(docId).set(order.copy(id = docId)).await()
    }

    suspend fun approveStockOrder(order: StockOrder, approve: Boolean, allInventory: List<InventoryItem>) {
        val newStatus = if (approve) "Disetujui" else "Ditolak"
        db.collection("stock_orders").document(order.id).update("status", newStatus).await()

        if (approve) {
            val existing = allInventory.find { it.branch == order.branch && it.name.equals(order.item, ignoreCase = true) }
            if (existing != null) {
                val updated = existing.copy(
                    stock_ml = existing.stock_ml + order.ml,
                    stock_botol = existing.stock_botol + order.botol
                )
                saveInventoryItem(updated)
            } else {
                val newItem = InventoryItem(
                    id = UUID.randomUUID().toString(),
                    branch = order.branch,
                    name = order.item,
                    stock_ml = order.ml,
                    stock_botol = order.botol
                )
                saveInventoryItem(newItem)
            }
        }
    }

    suspend fun approveAllPendingOrders(pendingOrders: List<StockOrder>, allInventory: List<InventoryItem>) {
        for (ord in pendingOrders) {
            approveStockOrder(ord, true, allInventory)
        }
    }

    suspend fun recordTransaction(
        trx: TransactionRecord,
        deductItem: InventoryItem?,
        mlDeducted: Double,
        botolDeducted: Long
    ) {
        val docId = if (trx.id.isBlank()) "TRX-${(1000..9999).random()}" else trx.id
        db.collection("transactions").document(docId).set(trx.copy(id = docId)).await()

        if (deductItem != null) {
            val newMl = (deductItem.stock_ml - mlDeducted).coerceAtLeast(0.0)
            val newBotol = (deductItem.stock_botol - botolDeducted).coerceAtLeast(0)
            saveInventoryItem(deductItem.copy(stock_ml = newMl, stock_botol = newBotol))
        }

        // Also update member loyalty points if member is registered
        if (trx.member != "Umum" && trx.pointsEarned > 0) {
            val memberDoc = db.collection("members").whereEqualTo("name", trx.member).limit(1).get().await()
            if (!memberDoc.isEmpty) {
                val mem = memberDoc.documents.first().toObject(MemberRecord::class.java)
                if (mem != null) {
                    db.collection("members").document(mem.id).update("points", mem.points + trx.pointsEarned).await()
                }
            }
        }
    }

    suspend fun saveMember(member: MemberRecord) {
        val docId = if (member.id.isBlank()) UUID.randomUUID().toString() else member.id
        db.collection("members").document(docId).set(member.copy(id = docId)).await()
    }

    suspend fun deleteMember(id: String) {
        db.collection("members").document(id).delete().await()
    }

    suspend fun resetAllMemberPoints() {
        val allMembers = db.collection("members").get().await()
        for (doc in allMembers.documents) {
            db.collection("members").document(doc.id).update("points", 0).await()
        }
    }

    suspend fun bulkUploadInventory(branch: String, text: String, currentInv: List<InventoryItem>) {
        val lines = text.split("\n")
        for (line in lines) {
            val parts = line.split(",")
            if (parts.isNotEmpty()) {
                val name = parts[0].trim()
                if (name.isNotBlank()) {
                    val ml = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                    val botol = parts.getOrNull(2)?.trim()?.toLongOrNull() ?: 0L

                    val existing = currentInv.find { it.branch == branch && it.name.equals(name, ignoreCase = true) }
                    if (existing != null) {
                        saveInventoryItem(
                            existing.copy(
                                stock_ml = existing.stock_ml + ml,
                                stock_botol = existing.stock_botol + botol
                            )
                        )
                    } else {
                        saveInventoryItem(
                            InventoryItem(
                                id = UUID.randomUUID().toString(),
                                branch = branch,
                                name = name,
                                stock_ml = ml,
                                stock_botol = botol
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun updateAppSettings(settings: AppSettings) {
        db.collection("settings").document("global").set(settings, SetOptions.merge()).await()
    }
}
