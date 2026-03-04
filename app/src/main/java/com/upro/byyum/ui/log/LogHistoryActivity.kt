package com.upro.byyum.ui.log

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.upro.byyum.UproApplication
import com.upro.byyum.data.entity.MonitorLog
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.databinding.ActivityLogHistoryBinding
import com.upro.byyum.databinding.ItemLogBinding
import com.upro.byyum.util.TimeUtils

class LogHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogHistoryBinding
    private val repo by lazy { (application as UproApplication).repository }
    private var monitorId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        monitorId = intent.getLongExtra("monitor_id", -1L)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (monitorId != -1L) "Log History" else "All Logs"
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = LogAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        if (monitorId != -1L) {
            repo.getLogsForMonitor(monitorId).observe(this) { adapter.submitList(it) }
        } else {
            repo.allLogs.observe(this) { adapter.submitList(it) }
        }
    }
}

class LogAdapter : ListAdapter<MonitorLog, LogAdapter.LogVH>(object : DiffUtil.ItemCallback<MonitorLog>() {
    override fun areItemsTheSame(o: MonitorLog, n: MonitorLog) = o.id == n.id
    override fun areContentsTheSame(o: MonitorLog, n: MonitorLog) = o == n
}) {
    inner class LogVH(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LogVH(ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LogVH, position: Int) {
        val log = getItem(position)
        holder.binding.tvTimestamp.text = TimeUtils.formatFull(log.timestamp)
        holder.binding.tvMessage.text = log.message
        holder.binding.tvResponseTime.text = if (log.responseTime > 0) "${log.responseTime}ms" else "--"
        val color = when (log.status) {
            MonitorStatus.HEALTHY -> Color.parseColor("#00FF88")
            MonitorStatus.DOWN -> Color.parseColor("#FF4444")
            MonitorStatus.ERROR -> Color.parseColor("#FFB800")
            else -> Color.parseColor("#888888")
        }
        holder.binding.tvStatus.text = log.status.displayName
        holder.binding.tvStatus.setTextColor(color)
        holder.binding.statusBar.setBackgroundColor(color)
    }
}
