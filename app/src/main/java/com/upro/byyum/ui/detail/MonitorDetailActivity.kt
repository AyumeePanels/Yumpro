package com.upro.byyum.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.upro.byyum.R
import com.upro.byyum.UproApplication
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.databinding.ActivityMonitorDetailBinding
import com.upro.byyum.ui.log.LogHistoryActivity
import com.upro.byyum.util.TimeUtils
import kotlinx.coroutines.launch

class MonitorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorDetailBinding
    private val repo by lazy { (application as UproApplication).repository }
    private var monitorId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        monitorId = intent.getLongExtra("monitor_id", -1L)
        if (monitorId == -1L) { finish(); return }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadMonitorData()
        setupChart()
        setupLogButton()
    }

    private fun loadMonitorData() {
        lifecycleScope.launch {
            val monitor = repo.getById(monitorId) ?: return@launch
            runOnUiThread {
                supportActionBar?.title = monitor.name
                binding.tvUrl.text = monitor.url.ifBlank { "Port: ${monitor.port}" }
                binding.tvType.text = monitor.type.displayName
                binding.tvInterval.text = "${monitor.interval}s"
                binding.tvLastChecked.text = TimeUtils.formatFull(monitor.lastChecked)
                binding.tvNextCheck.text = TimeUtils.formatFull(monitor.nextCheck)
                binding.tvUptime.text = TimeUtils.formatDuration(monitor.uptimeSince)
                binding.tvResponseTime.text = if (monitor.responseTime > 0) "${monitor.responseTime}ms" else "--"

                val (statusText, color) = when (monitor.status) {
                    MonitorStatus.HEALTHY -> "HEALTHY" to Color.parseColor("#00FF88")
                    MonitorStatus.DOWN -> "DOWN" to Color.parseColor("#FF4444")
                    MonitorStatus.ERROR -> "ERROR" to Color.parseColor("#FFB800")
                    else -> "UNKNOWN" to Color.parseColor("#888888")
                }
                binding.tvStatus.text = statusText
                binding.tvStatus.setTextColor(color)
                binding.statusDot.setColorFilter(color)
            }
        }
    }

    private fun setupChart() {
        lifecycleScope.launch {
            val logs = repo.getRecentLogs(monitorId)
            val entries = logs.reversed().mapIndexed { i, log ->
                Entry(i.toFloat(), log.responseTime.toFloat())
            }
            runOnUiThread {
                val dataSet = LineDataSet(entries, "Response Time (ms)").apply {
                    color = Color.parseColor("#00FF88")
                    setCircleColor(Color.parseColor("#00FF88"))
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    fillColor = Color.parseColor("#1A00FF88")
                    setDrawFilled(true)
                }
                with(binding.chart) {
                    data = LineData(dataSet)
                    setBackgroundColor(Color.parseColor("#0D1B2A"))
                    description.isEnabled = false
                    legend.textColor = Color.WHITE
                    xAxis.textColor = Color.WHITE
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    axisLeft.textColor = Color.WHITE
                    axisLeft.gridColor = Color.parseColor("#1AFFFFFF")
                    axisRight.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    animateX(500)
                    invalidate()
                }
            }
        }
    }

    private fun setupLogButton() {
        binding.btnViewLogs.setOnClickListener {
            val intent = Intent(this, LogHistoryActivity::class.java)
            intent.putExtra("monitor_id", monitorId)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("Delete Monitor")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            val monitor = repo.getById(monitorId)
                            monitor?.let { repo.delete(it) }
                            runOnUiThread { finish() }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMonitorData()
    }
}
