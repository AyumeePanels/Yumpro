package com.upro.byyum.ui.telegram

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.upro.byyum.databinding.FragmentTelegramBinding
import com.upro.byyum.util.PrefsManager

class TelegramFragment : Fragment() {
    private var _binding: FragmentTelegramBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTelegramBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfigureTelegram.setOnClickListener {
            startActivity(Intent(requireContext(), TelegramSettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PrefsManager(requireContext())
        val configured = prefs.telegramToken.isNotBlank() && prefs.telegramChatId.isNotBlank()
        binding.tvStatus.text = if (configured) "✅ Telegram Bot Connected" else "⚠️ Not configured"
        binding.tvStatus.setTextColor(
            if (configured) 0xFF00FF88.toInt() else 0xFFFFB800.toInt()
        )
        if (configured) {
            binding.tvTokenPreview.text = "Token: ****${prefs.telegramToken.takeLast(6)}"
            binding.tvChatPreview.text = "Chat ID: ${prefs.telegramChatId}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
