package com.upro.byyum.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.databinding.ItemMonitorBinding
import com.upro.byyum.util.TimeUtils

class MonitorAdapter(private val onClick: (Monitor) -> Unit) :
    ListAdapter<Monitor, MonitorAdapter.MonitorViewHolder>(MonitorDiff()) {

    inner class MonitorViewHolder(private val binding: ItemMonitorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(monitor: Monitor) {
            binding.tvName.text = monitor.name
            binding.tvUrl.text = monitor.url.ifBlank { "Port: ${monitor.port}" }
            binding.tvType.text = monitor.type.displayName
            binding.tvResponseTime.text = if (monitor.responseTime > 0) "${monitor.responseTime}ms" else "--"
            binding.tvLastChecked.text = TimeUtils.formatRelative(monitor.lastChecked)

            val (statusText, statusColor, indicatorColor) = when (monitor.status) {
                MonitorStatus.HEALTHY -> Triple("HEALTHY", Color.parseColor("#00FF88"), Color.parseColor("#00FF88"))
                MonitorStatus.DOWN -> Triple("DOWN", Color.parseColor("#FF4444"), Color.parseColor("#FF4444"))
                MonitorStatus.ERROR -> Triple("ERROR", Color.parseColor("#FFB800"), Color.parseColor("#FFB800"))
                MonitorStatus.PENDING -> Triple("PENDING", Color.parseColor("#888888"), Color.parseColor("#888888"))
                MonitorStatus.UNKNOWN -> Triple("UNKNOWN", Color.parseColor("#555555"), Color.parseColor("#555555"))
            }

            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(statusColor)
            binding.statusIndicator.setColorFilter(indicatorColor)
            binding.root.setOnClickListener { onClick(monitor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonitorViewHolder {
        val binding = ItemMonitorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonitorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MonitorDiff : DiffUtil.ItemCallback<Monitor>() {
    override fun areItemsTheSame(o: Monitor, n: Monitor) = o.id == n.id
    override fun areContentsTheSame(o: Monitor, n: Monitor) = o == n
}
