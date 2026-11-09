package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserSession(
    val username: String = "",
    val role: String = "staff", // "admin" or "staff"
    val branch: String = "Pusat",
    val profilePic: String? = null
)

@IgnoreExtraProperties
data class InventoryItem(
    @DocumentId val id: String = "",
    val branch: String = "",
    val name: String = "",
    val stock_ml: Double = 0.0,
    val stock_botol: Long = 0L
)

@IgnoreExtraProperties
data class MasterPrice(
    @DocumentId val id: String = "",
    val name: String = "",
    val price_ml: Double = 0.0,
    val price_botol: Double = 0.0
)

@IgnoreExtraProperties
data class StaffAccount(
    @DocumentId val id: String = "",
    val username: String = "",
    val password: String = "",
    val branch: String = ""
)

@IgnoreExtraProperties
data class StockOrder(
    @DocumentId val id: String = "",
    val branch: String = "",
    val item: String = "",
    val ml: Double = 0.0,
    val bottleSize: String = "30ml",
    val botol: Long = 0L,
    val status: String = "Menunggu Persetujuan" // "Menunggu Persetujuan", "Disetujui", "Ditolak"
)

@IgnoreExtraProperties
data class TransactionRecord(
    @DocumentId val id: String = "",
    val branch: String = "",
    val type: String = "Kombinasi (Bibit + Botol)",
    val item: String = "",
    val detail: String = "",
    val price: Double = 0.0,
    val member: String = "Umum",
    val pointsEarned: Long = 0L,
    val time: String = "",
    val date: String = ""
)

@IgnoreExtraProperties
data class MemberRecord(
    @DocumentId val id: String = "",
    val name: String = "",
    val phone: String = "-",
    val points: Long = 0L
)

@IgnoreExtraProperties
data class AppSettings(
    @DocumentId val id: String = "global",
    val appLogo: String? = null,
    val googleDriveSyncAccount: String = "budayaxwel@gmail.com"
)

data class OrderCartItem(
    val id: String,
    val item: String,
    val ml: Double,
    val bottleSize: String,
    val botol: Long
)
