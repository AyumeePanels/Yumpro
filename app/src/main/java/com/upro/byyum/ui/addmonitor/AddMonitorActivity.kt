package com.upro.byyum.ui.addmonitor

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upro.byyum.UproApplication
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorType
import com.upro.byyum.databinding.ActivityAddMonitorBinding
import kotlinx.coroutines.launch

class AddMonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMonitorBinding
    private val repo by lazy { (application as UproApplication).repository }

    private val intervals = listOf(5, 10, 30, 60, 120, 300, 600)
    private val intervalLabels = listOf("5 sec", "10 sec", "30 sec", "1 min", "2 min", "5 min", "10 min")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupTypeSpinner()
        setupIntervalSpinner()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTypeSpinner() {
        val types = MonitorType.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        binding.spinnerType.adapter = adapter
        binding.spinnerType.setSelection(0)
        binding.spinnerType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                updateFieldVisibility(MonitorType.values()[pos])
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupIntervalSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intervalLabels)
        binding.spinnerInterval.adapter = adapter
        binding.spinnerInterval.setSelection(3)
    }

    private fun updateFieldVisibility(type: MonitorType) {
        binding.layoutUrl.visibility = View.VISIBLE
        binding.layoutKeyword.visibility = if (type == MonitorType.KEYWORD) View.VISIBLE else View.GONE
        binding.layoutPort.visibility = if (type == MonitorType.PORT || type == MonitorType.UDP) View.VISIBLE else View.GONE
        binding.layoutExpectedIp.visibility = if (type == MonitorType.DNS) View.VISIBLE else View.GONE
        binding.layoutJsonKey.visibility = if (type == MonitorType.API) View.VISIBLE else View.GONE
        binding.layoutJsonValue.visibility = if (type == MonitorType.API) View.VISIBLE else View.GONE
        binding.layoutHeartbeatInterval.visibility = if (type == MonitorType.CRON) View.VISIBLE else View.GONE
        binding.layoutUdpPayload.visibility = if (type == MonitorType.UDP) View.VISIBLE else View.GONE

        when (type) {
            MonitorType.PING -> binding.tilUrl.hint = "IP Address or Domain"
            MonitorType.PORT -> binding.tilUrl.hint = "Host / IP Address"
            MonitorType.DNS -> binding.tilUrl.hint = "Domain to Resolve"
            MonitorType.CRON -> binding.layoutUrl.visibility = View.GONE
            MonitorType.UDP -> binding.tilUrl.hint = "Host / IP Address"
            else -> binding.tilUrl.hint = "URL (https://example.com)"
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) { binding.etName.error = "Name is required"; return@setOnClickListener }

            val type = MonitorType.values()[binding.spinnerType.selectedItemPosition]
            val url = binding.etUrl.text.toString().trim()
            val intervalSeconds = intervals[binding.spinnerInterval.selectedItemPosition]

            if (type != MonitorType.CRON && url.isEmpty()) { binding.etUrl.error = "URL / Host is required"; return@setOnClickListener }

            val monitor = Monitor(
                name = name,
                type = type,
                url = url,
                keyword = binding.etKeyword.text.toString().trim(),
                port = binding.etPort.text.toString().toIntOrNull() ?: 80,
                interval = intervalSeconds,
                expectedIp = binding.etExpectedIp.text.toString().trim(),
                jsonKey = binding.etJsonKey.text.toString().trim(),
                jsonValue = binding.etJsonValue.text.toString().trim(),
                heartbeatInterval = binding.etHeartbeatInterval.text.toString().toIntOrNull() ?: 300,
                udpPayload = binding.etUdpPayload.text.toString().trim()
            )

            lifecycleScope.launch {
                repo.insert(monitor)
                runOnUiThread {
                    Toast.makeText(this@AddMonitorActivity, "Monitor added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
