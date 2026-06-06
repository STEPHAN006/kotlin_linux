package com.stephan.mobil.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.stephan.mobil.MainActivity
import com.stephan.mobil.R
import com.stephan.mobil.data.api.ApiClient
import com.stephan.mobil.data.api.ApiService
import com.stephan.mobil.security.SecurityUtil
import java.util.concurrent.TimeUnit

class PushNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID        = "scpay_transactions"
        const val CHANNEL_NAME      = "SCpay — Transactions"
        const val CHANNEL_DESC      = "Alertes immédiates pour chaque transaction"
        const val WORK_NAME         = "scpay_notification_poll"
        private const val PREFS     = "scpay_notif_prefs"
        private const val KEY_LAST  = "last_notif_id"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                    enableLights(true)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        /** Lance le worker périodique dès le login, toutes les 15 min (minimum WorkManager). */
        fun schedule(context: Context) {
            createChannel(context)
            val request = PeriodicWorkRequestBuilder<PushNotificationWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** Arrête le worker au logout. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /** Poll immédiat (one-shot) appelé après chaque action sensible. */
        fun pollNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<PushNotificationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        val token = SecurityUtil.getAuthToken(context) ?: return Result.success()

        return try {
            val api = ApiClient.getClient(context).create(ApiService::class.java)
            val response = api.getNotifications()

            if (!response.isSuccessful) return Result.success()

            val notifications = response.body()?.data ?: return Result.success()
            if (notifications.isEmpty()) return Result.success()

            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val lastSeenId = prefs.getInt(KEY_LAST, 0)

            // Filtrer les nouvelles notifs non lues et plus récentes
            val newNotifs = notifications
                .filter { !it.read && it.id > lastSeenId }
                .sortedBy { it.id }

            if (newNotifs.isNotEmpty()) {
                // Mettre à jour l'ID du dernier vu
                prefs.edit().putInt(KEY_LAST, newNotifs.maxOf { it.id }).apply()

                // Afficher une notification Android pour chacune
                newNotifs.forEach { notif ->
                    showAndroidNotification(notif.id, notif.title, notif.body)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showAndroidNotification(id: Int, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_notifications", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS non accordée
        }
    }
}
