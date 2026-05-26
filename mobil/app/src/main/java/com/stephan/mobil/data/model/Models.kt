package com.stephan.mobil.data.model

import com.google.gson.annotations.SerializedName

// --- Auth Models ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String
)

// --- Banking Models ---
data class BalanceResponse(
    val balance: Double,
    val currency: String
)

data class Transaction(
    val id: Int,
    val amount: Double,
    val type: String, // "credit" ou "debit"
    val category: String,
    val description: String,
    val date: String
) {
    // Helper pour savoir si c'est un crédit (vert) ou un débit (rouge)
    val isCredit: Boolean
        get() = type == "credit"
}

data class TransactionListResponse(
    val transactions: List<Transaction>
)
