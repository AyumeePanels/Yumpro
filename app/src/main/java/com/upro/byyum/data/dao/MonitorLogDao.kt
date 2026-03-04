package com.upro.byyum.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.upro.byyum.data.entity.MonitorLog

@Dao
interface MonitorLogDao {

    @Query("SELECT * FROM monitor_logs WHERE monitorId = :monitorId ORDER BY timestamp DESC LIMIT 100")
    fun getLogsForMonitor(monitorId: Long): LiveData<List<MonitorLog>>

    @Query("SELECT * FROM monitor_logs WHERE monitorId = :monitorId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLogsForMonitorList(monitorId: Long, limit: Int = 50): List<MonitorLog>

    @Query("SELECT * FROM monitor_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): LiveData<List<MonitorLog>>

    @Query("SELECT * FROM monitor_logs ORDER BY timestamp DESC LIMIT 500")
    suspend fun getAllLogsList(): List<MonitorLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MonitorLog)

    @Query("DELETE FROM monitor_logs WHERE monitorId = :monitorId")
    suspend fun deleteLogsForMonitor(monitorId: Long)

    @Query("DELETE FROM monitor_logs WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)

    @Query("SELECT * FROM monitor_logs WHERE monitorId = :monitorId ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentLogs(monitorId: Long): List<MonitorLog>
}
