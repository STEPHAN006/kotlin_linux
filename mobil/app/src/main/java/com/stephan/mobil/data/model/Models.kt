package com.stephan.mobil.data.model

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

data class AuthData(
    val token: String,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null
)

data class BalanceResponse(
    val accounts: List<Account> = emptyList(),
    @SerializedName("total_balance") val totalBalance: Double = 0.0,
    @SerializedName("formatted_total_balance") val formattedTotalBalance: String = "",
    val currency: String = "MGA"
)

data class Account(
    val id: Int,
    @SerializedName("account_number") val accountNumber: String,
    val balance: Double,
    @SerializedName("formatted_balance") val formattedBalance: String,
    val currency: String,
    val status: String,
    val type: String
)

data class Transaction(
    val id: Int,
    val amount: Double,
    val type: String,
    val category: String? = null,
    val description: String? = null,
    val reference: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val isCredit: Boolean get() = type == "credit"
}

data class Beneficiary(
    val id: Int,
    val name: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("account_number_masked") val accountNumberMasked: String,
    val phone: String?,
    val channel: String,
    @SerializedName("is_verified") val isVerified: Boolean
)

data class BeneficiaryRequest(
    val name: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("account_number") val accountNumber: String,
    val phone: String?,
    val channel: String
)

data class Card(
    val id: Int,
    @SerializedName("card_number_masked") val cardNumberMasked: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("is_blocked") val isBlocked: Boolean,
    val type: String,
    @SerializedName("daily_limit") val dailyLimit: Double
)

data class CreateCardRequest(
    @SerializedName("account_id") val accountId: Int,
    @SerializedName("daily_limit") val dailyLimit: Double
)

data class TransferRequest(
    @SerializedName("sender_account_id") val senderAccountId: Int,
    @SerializedName("receiver_account_id") val receiverAccountId: Int,
    val amount: Double,
    val note: String?,
    val channel: String = "internal"
)

data class TransferData(
    @SerializedName("otp_required") val otpRequired: Boolean,
    val transfer: Transfer
)

data class Transfer(
    val id: Int,
    val amount: Double,
    val status: String,
    val reference: String,
    @SerializedName("otp_verified") val otpVerified: Boolean,
    val note: String? = null,
    val channel: String
)

data class VerifyOtpRequest(val reference: String, val otp: String)
data class QrGenerateRequest(@SerializedName("account_id") val accountId: Int, val amount: Double?)
data class QrScanRequest(val payload: String)
data class QrPayRequest(
    @SerializedName("sender_account_id") val senderAccountId: Int,
    val payload: String,
    val amount: Double
)
data class QrData(val payload: String, val display: Map<String, Any>?)
