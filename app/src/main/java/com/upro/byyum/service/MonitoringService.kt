package com.upro.byyum.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.upro.byyum.R
import com.upro.byyum.UproApplication
import com.upro.byyum.UproApplication.Companion.CHANNEL_MONITORING_SERVICE
import com.upro.byyum.data.entity.MonitorLog
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.data.entity.MonitorType
import com.upro.byyum.monitor.MonitorChecker
import com.upro.byyum.notification.AlertNotifier
import com.upro.byyum.telegram.TelegramBot
import com.upro.byyum.ui.MainActivity
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class MonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val monitorJobs = ConcurrentHashMap<Long, Job>()
    private lateinit var repo: com.upro.byyum.data.MonitorRepository
    private lateinit var alertNotifier: AlertNotifier
    private lateinit var telegramBot: TelegramBot
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1001
        var isRunning = false

        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply { action = ACTION_START }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as UproApplication
        repo = app.repository
        alertNotifier = AlertNotifier(this)
        telegramBot = TelegramBot(this)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YumPRO::MonitoringWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification("Monitoring active..."))
                isRunning = true
                serviceScope.launch { refreshMonitors() }
            }
        }
        return START_STICKY
    }

    private suspend fun refreshMonitors() {
        val monitors = repo.getActiveMonitors()
        val activeIds = monitors.map { it.id }.toSet()
        monitorJobs.keys.filter { it !in activeIds }.forEach { id ->
            monitorJobs[id]?.cancel()
            monitorJobs.remove(id)
        }
        monitors.forEach { monitor ->
            if (!monitorJobs.containsKey(monitor.id) || monitorJobs[monitor.id]?.isActive == false) {
                startMonitorJob(monitor)
            }
        }
        updateNotification("Monitoring ${monitors.size} service(s)")
    }

    private fun startMonitorJob(monitor: com.upro.byyum.data.entity.Monitor) {
        val job = serviceScope.launch {
            while (isActive) {
                try {
                    val freshMonitor = repo.getById(monitor.id) ?: break
                    if (!freshMonitor.isActive) break
                    val result = withContext(Dispatchers.IO) {
                        when (freshMonitor.type) {
                            MonitorType.HTTP -> MonitorChecker.checkHttp(freshMonitor.url)
                            MonitorType.KEYWORD -> MonitorChecker.checkKeyword(freshMonitor.url, freshMonitor.keyword)
                            MonitorType.PING -> MonitorChecker.checkPing(freshMonitor.url)
                            MonitorType.PORT -> MonitorChecker.checkPort(freshMonitor.url, freshMonitor.port)
                            MonitorType.DNS -> MonitorChecker.checkDns(freshMonitor.url, freshMonitor.expectedIp)
                            MonitorType.API -> MonitorChecker.checkApi(freshMonitor.url, freshMonitor.jsonKey, freshMonitor.jsonValue)
                            MonitorType.UDP -> MonitorChecker.checkUdp(freshMonitor.url, freshMonitor.port, freshMonitor.udpPayload)
                            MonitorType.CRON -> MonitorChecker.checkHeartbeat(freshMonitor.lastHeartbeat, freshMonitor.heartbeatInterval)
                        }
                    }
                    val previousStatus = freshMonitor.status
                    repo.updateStatus(freshMonitor.id, result.status, result.responseTime)
                    repo.insertLog(MonitorLog(monitorId = freshMonitor.id, status = result.status, responseTime = result.responseTime, message = result.message))
                    if (previousStatus != result.status && result.status != MonitorStatus.UNKNOWN) {
                        alertNotifier.sendAlert(freshMonitor, result)
                        if (result.status == MonitorStatus.DOWN || result.status == MonitorStatus.ERROR) {
                            telegramBot.sendImmediate(freshMonitor, result)
                        }
                    }
                    delay(freshMonitor.interval * 1000L)
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    delay(30_000L)
                }
            }
        }
        monitorJobs[monitor.id] = job
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_MONITORING_SERVICE)
            .setContentTitle("YumPRO")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        monitorJobs.values.forEach { it.cancel() }
        monitorJobs.clear()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
