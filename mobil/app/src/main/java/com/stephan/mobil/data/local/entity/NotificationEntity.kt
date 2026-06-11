package com.stephan.mobil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stephan.mobil.data.model.AppNotification

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val read: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String?
) {
    fun toModel() = AppNotification(id, title, body, read, createdAt)
}

fun AppNotification.toEntity() = NotificationEntity(id, title, body, read, createdAt)
