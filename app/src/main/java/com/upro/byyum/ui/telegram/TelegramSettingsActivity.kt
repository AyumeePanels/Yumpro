package com.upro.byyum.ui.telegram

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upro.byyum.databinding.ActivityTelegramSettingsBinding
import com.upro.byyum.telegram.TelegramBot
import com.upro.byyum.util.PrefsManager
import kotlinx.coroutines.launch

class TelegramSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelegramSettingsBinding
    private lateinit var prefs: PrefsManager
    private lateinit var bot: TelegramBot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelegramSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        bot = TelegramBot(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Telegram Bot"
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadSavedData()
        setupButtons()
    }

    private fun loadSavedData() {
        binding.etBotToken.setText(prefs.telegramToken)
        binding.etChatId.setText(prefs.telegramChatId)
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val token = binding.etBotToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()
            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Please fill in Bot Token and Chat ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.telegramToken = token
            prefs.telegramChatId = chatId
            Toast.makeText(this, "✅ Saved!", Toast.LENGTH_SHORT).show()
        }

        binding.btnTest.setOnClickListener {
            val token = binding.etBotToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()
            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Please save your Bot Token and Chat ID first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.telegramToken = token
            prefs.telegramChatId = chatId

            binding.btnTest.isEnabled = false
            binding.progressTest.visibility = View.VISIBLE
            lifecycleScope.launch {
                val success = bot.testConnection()
                runOnUiThread {
                    binding.btnTest.isEnabled = true
                    binding.progressTest.visibility = View.GONE
                    if (success) {
                        binding.tvTestResult.text = "✅ Test message sent successfully!"
                        binding.tvTestResult.setTextColor(0xFF00FF88.toInt())
                    } else {
                        binding.tvTestResult.text = "❌ Failed. Check your Bot Token and Chat ID"
                        binding.tvTestResult.setTextColor(0xFFFF4444.toInt())
                    }
                    binding.tvTestResult.visibility = View.VISIBLE
                }
            }
        }
    }
}
