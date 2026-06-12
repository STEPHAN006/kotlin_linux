package com.stephan.mobil.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.stephan.mobil.data.local.dao.AccountDao
import com.stephan.mobil.data.local.dao.NotificationDao
import com.stephan.mobil.data.local.dao.TransactionDao
import com.stephan.mobil.data.local.entity.AccountEntity
import com.stephan.mobil.data.local.entity.NotificationEntity
import com.stephan.mobil.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, AccountEntity::class, NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scpay.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
