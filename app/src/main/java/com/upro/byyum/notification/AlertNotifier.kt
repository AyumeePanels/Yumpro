package com.upro.byyum.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.upro.byyum.R
import com.upro.byyum.UproApplication.Companion.CHANNEL_ALERTS
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.monitor.CheckResult
import com.upro.byyum.ui.MainActivity

class AlertNotifier(private val context: Context) {

    private val nm = NotificationManagerCompat.from(context)
    private var notifId = 2000

    fun sendAlert(monitor: Monitor, result: CheckResult) {
        val title = when (result.status) {
            MonitorStatus.DOWN -> "🔴 DOWN: ${monitor.name}"
            MonitorStatus.ERROR -> "🟡 ERROR: ${monitor.name}"
            MonitorStatus.HEALTHY -> "🟢 HEALTHY: ${monitor.name}"
            else -> return
        }

        val pendingIntent = PendingIntent.getActivity(
            context, monitor.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText("${monitor.url} - ${result.message}")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            nm.notify(notifId++, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
