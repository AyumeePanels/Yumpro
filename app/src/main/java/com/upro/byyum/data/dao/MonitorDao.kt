package com.upro.byyum.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorStatus

@Dao
interface MonitorDao {

    @Query("SELECT * FROM monitors ORDER BY createdAt DESC")
    fun getAllMonitors(): LiveData<List<Monitor>>

    @Query("SELECT * FROM monitors ORDER BY createdAt DESC")
    suspend fun getAllMonitorsList(): List<Monitor>

    @Query("SELECT * FROM monitors WHERE id = :id")
    suspend fun getMonitorById(id: Long): Monitor?

    @Query("SELECT * FROM monitors WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveMonitors(): List<Monitor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitor(monitor: Monitor): Long

    @Update
    suspend fun updateMonitor(monitor: Monitor)

    @Delete
    suspend fun deleteMonitor(monitor: Monitor)

    @Query("UPDATE monitors SET status = :status, responseTime = :responseTime, lastChecked = :lastChecked, nextCheck = :nextCheck WHERE id = :id")
    suspend fun updateMonitorStatus(id: Long, status: MonitorStatus, responseTime: Long, lastChecked: Long, nextCheck: Long)

    @Query("UPDATE monitors SET uptimeSince = :uptimeSince WHERE id = :id")
    suspend fun updateUptimeSince(id: Long, uptimeSince: Long)

    @Query("UPDATE monitors SET lastHeartbeat = :timestamp WHERE id = :id")
    suspend fun updateHeartbeat(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM monitors WHERE status = 'HEALTHY'")
    fun getHealthyCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM monitors WHERE status = 'DOWN'")
    fun getDownCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM monitors WHERE status = 'ERROR'")
    fun getErrorCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM monitors")
    fun getTotalCount(): LiveData<Int>
}
