package com.upro.byyum.util

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("yumpro_prefs", Context.MODE_PRIVATE)

    var telegramToken: String
        get() = prefs.getString("tg_token", "") ?: ""
        set(v) = prefs.edit().putString("tg_token", v).apply()

    var telegramChatId: String
        get() = prefs.getString("tg_chat_id", "") ?: ""
        set(v) = prefs.edit().putString("tg_chat_id", v).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(v) = prefs.edit().putBoolean("dark_mode", v).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean("service_enabled", true)
        set(v) = prefs.edit().putBoolean("service_enabled", v).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(v) = prefs.edit().putBoolean("notifications_enabled", v).apply()
}
