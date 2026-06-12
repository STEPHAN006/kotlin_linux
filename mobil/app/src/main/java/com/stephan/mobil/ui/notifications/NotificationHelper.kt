package com.stephan.mobil.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stephan.mobil.MainActivity
import com.stephan.mobil.R

object NotificationHelper {

    private const val CHANNEL_TRANSACTIONS = "scpay_transactions"
    private const val CHANNEL_SECURITY     = "scpay_security"
    private var notifId = 1000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_TRANSACTIONS,
                    "Transactions SCpay",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Alertes pour chaque transaction et virement" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SECURITY,
                    "Sécurité SCpay",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Alertes de sécurité et fraude" }
            )
        }
    }

    fun notifyTransfer(context: Context, amount: String, status: String) {
        val title = if (status == "completed") "Virement effectué" else "Virement en attente"
        val body  = if (status == "completed")
            "Votre virement de $amount MGA a été effectué avec succès."
        else
            "Un code OTP est requis pour valider votre virement de $amount MGA."
        show(context, CHANNEL_TRANSACTIONS, title, body)
    }

    fun notifyTransactionReceived(context: Context, amount: String) {
        show(
            context,
            CHANNEL_TRANSACTIONS,
            "Paiement reçu",
            "Vous avez reçu $amount MGA sur votre compte SCpay."
        )
    }

    fun notifyQrPayment(context: Context, amount: String) {
        show(
            context,
            CHANNEL_TRANSACTIONS,
            "Paiement QR confirmé",
            "Paiement de $amount MGA effectué par QR Code."
        )
    }

    fun notifyFraudAlert(context: Context, reason: String) {
        show(
            context,
            CHANNEL_SECURITY,
            "Alerte sécurité SCpay",
            reason
        )
    }

    private fun show(context: Context, channel: String, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(notifId++, notification)
    }
}
