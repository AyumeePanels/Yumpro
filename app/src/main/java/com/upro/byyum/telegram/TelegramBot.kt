package com.upro.byyum.telegram

import android.content.Context
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.monitor.CheckResult
import com.upro.byyum.util.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TelegramBot(private val context: Context) {

    private val prefs = PrefsManager(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    suspend fun sendMessage(message: String): Boolean = withContext(Dispatchers.IO) {
        val token = prefs.telegramToken
        val chatId = prefs.telegramChatId
        if (token.isBlank() || chatId.isBlank()) return@withContext false
        return@withContext try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", message)
                put("parse_mode", "HTML")
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.telegram.org/bot$token/sendMessage")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful.also { response.close() }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendImmediate(monitor: Monitor, result: CheckResult) {
        val emoji = when (result.status) {
            MonitorStatus.DOWN -> "🔴"
            MonitorStatus.ERROR -> "🟡"
            MonitorStatus.HEALTHY -> "🟢"
            else -> "⚪"
        }
        val message = """
$emoji <b>ALERT - ${result.status.displayName.uppercase()}</b>
━━━━━━━━━━━━━━━━━━━━
📌 <b>Monitor:</b> ${monitor.name}
🌐 <b>URL/Host:</b> ${monitor.url}
📊 <b>Status:</b> ${result.status.displayName}
⚡ <b>Response:</b> ${if (result.responseTime > 0) "${result.responseTime}ms" else "N/A"}
💬 <b>Message:</b> ${result.message}
🕐 <b>Time:</b> ${dateFormat.format(Date())}
━━━━━━━━━━━━━━━━━━━━
<i>YumPRO Monitoring</i>
        """.trimIndent()
        sendMessage(message)
    }

    suspend fun sendSummaryReport(monitors: List<Monitor>) {
        val healthy = monitors.count { it.status == MonitorStatus.HEALTHY }
        val down = monitors.count { it.status == MonitorStatus.DOWN }
        val error = monitors.count { it.status == MonitorStatus.ERROR }
        val statusLines = monitors.joinToString("\n") { m ->
            val e = when (m.status) {
                MonitorStatus.HEALTHY -> "🟢"
                MonitorStatus.DOWN -> "🔴"
                MonitorStatus.ERROR -> "🟡"
                else -> "⚪"
            }
            "$e ${m.name} - ${m.responseTime}ms"
        }
        val message = """
📊 <b>YumPRO - Summary Report</b>
━━━━━━━━━━━━━━━━━━━━
🟢 Healthy: $healthy  🔴 Down: $down  🟡 Error: $error
📋 Total: ${monitors.size} monitors
━━━━━━━━━━━━━━━━━━━━
<b>Monitor Status:</b>
$statusLines
━━━━━━━━━━━━━━━━━━━━
🕐 <b>Report Time:</b> ${dateFormat.format(Date())}
<i>Auto report every 10 minutes</i>
        """.trimIndent()
        sendMessage(message)
    }

    suspend fun testConnection(): Boolean {
        val token = prefs.telegramToken
        val chatId = prefs.telegramChatId
        if (token.isBlank() || chatId.isBlank()) return false
        return sendMessage("✅ <b>YumPRO</b>\nTelegram connection test successful!\n🕐 ${dateFormat.format(Date())}")
    }
}
