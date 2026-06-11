package com.stephan.mobil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stephan.mobil.data.model.Account

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "account_number") val accountNumber: String,
    val balance: Double,
    @ColumnInfo(name = "formatted_balance") val formattedBalance: String,
    val currency: String,
    val status: String,
    val type: String
) {
    fun toModel() = Account(id, accountNumber, balance, formattedBalance, currency, status, type)
}

fun Account.toEntity() = AccountEntity(id, accountNumber, balance, formattedBalance, currency, status, type)
