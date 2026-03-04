package com.upro.byyum.data

import androidx.lifecycle.LiveData
import com.upro.byyum.data.dao.MonitorDao
import com.upro.byyum.data.dao.MonitorLogDao
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorLog
import com.upro.byyum.data.entity.MonitorStatus

class MonitorRepository(
    private val monitorDao: MonitorDao,
    private val logDao: MonitorLogDao
) {
    val allMonitors: LiveData<List<Monitor>> = monitorDao.getAllMonitors()
    val allLogs: LiveData<List<MonitorLog>> = logDao.getAllLogs()
    val healthyCount: LiveData<Int> = monitorDao.getHealthyCount()
    val downCount: LiveData<Int> = monitorDao.getDownCount()
    val errorCount: LiveData<Int> = monitorDao.getErrorCount()
    val totalCount: LiveData<Int> = monitorDao.getTotalCount()

    suspend fun insert(monitor: Monitor): Long = monitorDao.insertMonitor(monitor)
    suspend fun update(monitor: Monitor) = monitorDao.updateMonitor(monitor)
    suspend fun delete(monitor: Monitor) = monitorDao.deleteMonitor(monitor)
    suspend fun getById(id: Long) = monitorDao.getMonitorById(id)
    suspend fun getActiveMonitors() = monitorDao.getActiveMonitors()
    suspend fun getAllList() = monitorDao.getAllMonitorsList()

    suspend fun updateStatus(id: Long, status: MonitorStatus, responseTime: Long) {
        val now = System.currentTimeMillis()
        val monitor = monitorDao.getMonitorById(id) ?: return
        val nextCheck = now + (monitor.interval * 1000L)
        monitorDao.updateMonitorStatus(id, status, responseTime, now, nextCheck)
        if (status == MonitorStatus.HEALTHY && monitor.status != MonitorStatus.HEALTHY) {
            monitorDao.updateUptimeSince(id, now)
        }
    }

    suspend fun insertLog(log: MonitorLog) = logDao.insertLog(log)
    fun getLogsForMonitor(monitorId: Long) = logDao.getLogsForMonitor(monitorId)
    suspend fun getRecentLogs(monitorId: Long) = logDao.getRecentLogs(monitorId)
    suspend fun getAllLogsList() = logDao.getAllLogsList()
    suspend fun updateHeartbeat(id: Long) = monitorDao.updateHeartbeat(id, System.currentTimeMillis())

    suspend fun cleanOldLogs() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        logDao.deleteOldLogs(thirtyDaysAgo)
    }
}
