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
    val phone: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val role: String? = null,
    @SerializedName("kyc_status") val kycStatus: String = "none"
)

data class KycStatusResponse(
    val status: String,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null
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
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("balance_after") val balanceAfter: Double? = null,
    @SerializedName("date_human") val dateHuman: String? = null
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

data class CardDetails(
    @SerializedName("card_number") val cardNumber: String,
    val cvv: String,
    @SerializedName("expiry_date") val expiryDate: String
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

data class QrScanResult(
    val payload: String,
    @SerializedName("recipient_name")   val recipientName: String,
    @SerializedName("account_masked")   val accountMasked: String,
    @SerializedName("suggested_amount") val suggestedAmount: Double?,
    val currency: String = "MGA"
)

// Support Tickets
data class SupportTicket(
    val id: Int,
    val subject: String,
    val status: String,
    val priority: String,
    val category: String,
    @SerializedName("message_count") val messageCount: Int = 0,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SupportMessage(
    val id: Int,
    val message: String,
    @SerializedName("is_from_agent") val isFromAgent: Boolean,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    val attachments: List<SupportAttachment> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null
)

data class SupportAttachment(
    val id: Int,
    @SerializedName("original_name") val originalName: String,
    val url: String,
    @SerializedName("mime_type") val mimeType: String,
    val size: Long
)

data class SupportTicketDetail(
    val id: Int,
    val subject: String,
    val status: String,
    val priority: String,
    val category: String,
    val messages: List<SupportMessage> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateTicketRequest(
    val subject: String,
    val message: String,
    val category: String = "general",
    val priority: String = "medium"
)

// ─── CoinGecko market data ────────────────────────────────────────────────────

data class SparklineData(val price: List<Double> = emptyList())

data class CoinMarketData(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerializedName("current_price") val currentPrice: Double = 0.0,
    @SerializedName("price_change_percentage_24h") val change24h: Double? = null,
    @SerializedName("market_cap") val marketCap: Long? = null,
    @SerializedName("total_volume") val volume: Long? = null,
    @SerializedName("sparkline_in_7d") val sparkline: SparklineData? = null
)

data class CoinChartData(
    val prices: List<List<Double>> = emptyList()
)

// ─── Backend crypto wallet / trade ───────────────────────────────────────────

data class CryptoWallet(
    val symbol: String,
    val name: String,
    @SerializedName("coin_id") val coinId: String,
    val address: String,
    val balance: Double = 0.0
)

data class CryptoBuyRequest(
    val symbol: String,
    @SerializedName("amount_mga") val amountMga: Double,
    @SerializedName("price_usd") val priceUsd: Double,
    @SerializedName("mga_per_usd") val mgaPerUsd: Double
)

data class CryptoSellRequest(
    val symbol: String,
    @SerializedName("crypto_amount") val cryptoAmount: Double,
    @SerializedName("price_usd") val priceUsd: Double,
    @SerializedName("mga_per_usd") val mgaPerUsd: Double
)

data class CryptoSendRequest(
    val symbol: String,
    @SerializedName("crypto_amount") val cryptoAmount: Double,
    @SerializedName("to_address") val toAddress: String,
    @SerializedName("price_usd") val priceUsd: Double,
    @SerializedName("mga_per_usd") val mgaPerUsd: Double
)

data class CryptoSwapRequest(
    @SerializedName("from_symbol")    val fromSymbol: String,
    @SerializedName("to_symbol")      val toSymbol: String,
    @SerializedName("from_amount")    val fromAmount: Double,
    @SerializedName("from_price_usd") val fromPriceUsd: Double,
    @SerializedName("to_price_usd")   val toPriceUsd: Double,
    @SerializedName("mga_per_usd")    val mgaPerUsd: Double
)

data class CryptoSwapResult(
    @SerializedName("from_symbol") val fromSymbol: String? = null,
    @SerializedName("to_symbol")   val toSymbol: String? = null,
    @SerializedName("from_amount") val fromAmount: Double? = null,
    @SerializedName("to_amount")   val toAmount: Double? = null,
    @SerializedName("tx_hash")     val txHash: String? = null
)

data class CryptoTradeResult(
    val symbol: String? = null,
    @SerializedName("crypto_amount") val cryptoAmount: Double? = null,
    @SerializedName("total_mga") val totalMga: Double? = null,
    @SerializedName("tx_hash") val txHash: String? = null
)

data class ExchangeRateResponse(
    val result: String = "",
    val rates: Map<String, Double> = emptyMap()
)

data class AppNotification(
    val id: Int,
    val title: String,
    val body: String,
    val read: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class PendingCardPayment(
    val reference: String,
    val merchant: String,
    val product: String,
    val amount: Double,
    @SerializedName("card_masked") val cardMasked: String?,
    @SerializedName("expires_at") val expiresAt: String?
)

data class CryptoTxn(
    val id: Int,
    val type: String,
    val symbol: String,
    val amount: Double,
    @SerializedName("price_usd") val priceUsd: Double,
    @SerializedName("total_mga") val totalMga: Double,
    @SerializedName("to_address") val toAddress: String? = null,
    @SerializedName("tx_hash") val txHash: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ScheduledWithdrawal(
    val id: Int,
    val amount: Double,
    val note: String? = null,
    @SerializedName("frequency_days") val frequencyDays: Int,
    @SerializedName("next_run_at") val nextRunAt: String? = null,
    @SerializedName("last_run_at") val lastRunAt: String? = null,
    @SerializedName("run_count") val runCount: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
    val beneficiary: ScheduledWithdrawalBeneficiary? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ScheduledWithdrawalBeneficiary(
    val id: Int,
    val name: String,
    @SerializedName("bank_name") val bankName: String,
    val channel: String
)

data class ScheduledWithdrawalRequest(
    @SerializedName("sender_account_id") val senderAccountId: Int,
    @SerializedName("beneficiary_id") val beneficiaryId: Int,
    val amount: Double,
    val note: String?,
    @SerializedName("frequency_days") val frequencyDays: Int
)

data class DepositRequest(
    @SerializedName("account_id") val accountId: Int,
    val amount: Double,
    val method: String,
    val phone: String?
)

data class DepositResult(
    val reference: String,
    val amount: Double,
    val method: String,
    val status: String, // "pending" | "completed" | "cancelled"
    @SerializedName("new_balance") val newBalance: Double? = null
)
