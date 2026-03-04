package com.upro.byyum.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.upro.byyum.databinding.FragmentSettingsBinding
import com.upro.byyum.service.MonitoringService
import com.upro.byyum.service.TelegramReportService
import com.upro.byyum.util.PrefsManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = PrefsManager(requireContext())

        binding.switchService.isChecked = prefs.serviceEnabled
        binding.switchNotifications.isChecked = prefs.notificationsEnabled

        binding.switchService.setOnCheckedChangeListener { _, checked ->
            prefs.serviceEnabled = checked
            if (checked) {
                MonitoringService.start(requireContext())
                TelegramReportService.start(requireContext())
            } else {
                MonitoringService.stop(requireContext())
            }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.notificationsEnabled = checked
        }

        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }

        binding.tvVersion.text = "YumPRO v1.0.0\nDeveloped with ❤️"
        binding.tvServiceStatus.text = if (MonitoringService.isRunning) "🟢 Service Running" else "🔴 Service Stopped"
        binding.tvServiceStatus.setTextColor(
            if (MonitoringService.isRunning) 0xFF00FF88.toInt() else 0xFFFF4444.toInt()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
