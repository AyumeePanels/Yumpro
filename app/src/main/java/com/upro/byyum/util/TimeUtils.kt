package com.upro.byyum.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val fullFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault())

    fun formatFull(timestamp: Long): String = if (timestamp == 0L) "Never" else fullFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = if (timestamp == 0L) "Never" else timeFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = if (timestamp == 0L) "Never" else dateTimeFormat.format(Date(timestamp))

    fun formatDuration(startTimestamp: Long): String {
        if (startTimestamp == 0L) return "N/A"
        val diff = System.currentTimeMillis() - startTimestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    fun formatRelative(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        return when {
            seconds < 60 -> "${seconds}s ago"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86400}d ago"
        }
    }
}
