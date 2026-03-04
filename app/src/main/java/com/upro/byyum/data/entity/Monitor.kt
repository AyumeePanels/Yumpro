package com.upro.byyum.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MonitorType(val displayName: String) {
    HTTP("HTTP / Website"),
    KEYWORD("Keyword"),
    PING("Ping"),
    PORT("Port"),
    CRON("Cron / Heartbeat"),
    DNS("DNS"),
    API("API"),
    UDP("UDP")
}

enum class MonitorStatus(val displayName: String) {
    HEALTHY("Healthy"),
    DOWN("Down"),
    ERROR("Error"),
    PENDING("Pending"),
    UNKNOWN("Unknown")
}

@Entity(tableName = "monitors")
data class Monitor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: MonitorType,
    val url: String = "",
    val keyword: String = "",
    val port: Int = 0,
    val interval: Int = 60,
    val status: MonitorStatus = MonitorStatus.UNKNOWN,
    val responseTime: Long = 0,
    val lastChecked: Long = 0,
    val nextCheck: Long = 0,
    val uptimeSince: Long = 0,
    val isActive: Boolean = true,
    val expectedIp: String = "",
    val jsonKey: String = "",
    val jsonValue: String = "",
    val heartbeatInterval: Int = 300,
    val lastHeartbeat: Long = 0,
    val udpPayload: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
