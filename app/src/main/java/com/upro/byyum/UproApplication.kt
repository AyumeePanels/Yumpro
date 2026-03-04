package com.upro.byyum

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.upro.byyum.data.AppDatabase
import com.upro.byyum.data.MonitorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class UproApplication : Application(), Configuration.Provider {

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MonitorRepository(database.monitorDao(), database.monitorLogDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                CHANNEL_MONITORING_SERVICE, "Monitoring Service", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "YumPRO Background Monitoring"; setShowBadge(false) }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS, "Monitor Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts when a monitor goes DOWN or has ERROR"; enableVibration(true) }

            val reportChannel = NotificationChannel(
                CHANNEL_REPORTS, "Telegram Reports", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Telegram report status" }

            nm.createNotificationChannels(listOf(serviceChannel, alertChannel, reportChannel))
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        const val CHANNEL_MONITORING_SERVICE = "yumpro_monitoring_service"
        const val CHANNEL_ALERTS = "yumpro_alerts"
        const val CHANNEL_REPORTS = "yumpro_reports"
    }
}
