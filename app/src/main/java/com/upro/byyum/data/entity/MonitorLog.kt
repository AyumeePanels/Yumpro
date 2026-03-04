package com.upro.byyum.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monitor_logs",
    foreignKeys = [ForeignKey(
        entity = Monitor::class,
        parentColumns = ["id"],
        childColumns = ["monitorId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("monitorId")]
)
data class MonitorLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monitorId: Long,
    val status: MonitorStatus,
    val responseTime: Long = 0,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
