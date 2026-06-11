package com.stephan.mobil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stephan.mobil.data.model.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Int,
    val amount: Double,
    val type: String,
    val category: String?,
    val description: String?,
    val reference: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?
) {
    fun toModel() = Transaction(id, amount, type, category, description, reference, createdAt)
}

fun Transaction.toEntity() = TransactionEntity(id, amount, type, category, description, reference, createdAt)
