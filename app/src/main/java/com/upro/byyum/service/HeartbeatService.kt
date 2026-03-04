package com.upro.byyum.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.upro.byyum.UproApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HeartbeatService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val EXTRA_MONITOR_ID = "monitor_id"

        fun ping(context: Context, monitorId: Long) {
            val intent = Intent(context, HeartbeatService::class.java).apply {
                putExtra(EXTRA_MONITOR_ID, monitorId)
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val monitorId = intent?.getLongExtra(EXTRA_MONITOR_ID, -1L) ?: -1L
        if (monitorId != -1L) {
            val repo = (application as UproApplication).repository
            scope.launch {
                repo.updateHeartbeat(monitorId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
