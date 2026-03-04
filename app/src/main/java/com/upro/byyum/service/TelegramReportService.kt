package com.upro.byyum.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.upro.byyum.UproApplication
import com.upro.byyum.telegram.TelegramBot
import kotlinx.coroutines.*

class TelegramReportService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        fun start(context: Context) {
            context.startService(Intent(context, TelegramReportService::class.java))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as UproApplication
        val repo = app.repository
        val bot = TelegramBot(this)

        scope.launch {
            while (isActive) {
                delay(10 * 60 * 1000L)
                try {
                    val monitors = repo.getAllList()
                    if (monitors.isNotEmpty()) {
                        bot.sendSummaryReport(monitors)
                    }
                } catch (e: Exception) {
                    // Ignore errors, keep running
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
