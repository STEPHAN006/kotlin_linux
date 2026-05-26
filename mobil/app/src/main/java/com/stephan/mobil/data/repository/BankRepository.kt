package com.stephan.mobil.data.repository

import com.stephan.mobil.data.api.ApiService
import com.stephan.mobil.data.model.BalanceResponse
import com.stephan.mobil.data.model.Transaction
import kotlinx.coroutines.delay

class BankRepository(private val apiService: ApiService) {

    // Variables pour gérer le mode Mock
    var useMockData = true

    // ==========================================
    // MOCK DATA
    // ==========================================
    private val mockBalance = BalanceResponse(balance = 1250000.0, currency = "MGA")
    
    private val mockTransactions = listOf(
        Transaction(1, 150000.0, "debit", "courses", "Shoprite", "2026-05-20"),
        Transaction(2, 4500000.0, "credit", "salaire", "Salaire Mai", "2026-05-21"),
        Transaction(3, 50000.0, "debit", "transport", "Essence", "2026-05-22"),
        Transaction(4, 20000.0, "credit", "remboursement", "Ami", "2026-05-25")
    )

    // ==========================================
    // OPERATIONS
    // ==========================================
    
    suspend fun getBalance(): Result<BalanceResponse> {
        return if (useMockData) {
            delay(500) // Simuler latence réseau
            Result.success(mockBalance)
        } else {
            try {
                val response = apiService.getBalance()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Erreur API : ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTransactions(): Result<List<Transaction>> {
        return if (useMockData) {
            delay(500) // Simuler latence réseau
            Result.success(mockTransactions)
        } else {
            try {
                val response = apiService.getTransactions()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.transactions)
                } else {
                    Result.failure(Exception("Erreur API : ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
