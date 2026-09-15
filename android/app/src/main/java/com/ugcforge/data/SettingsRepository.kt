package com.ugcforge.data

import android.content.Context

enum class PrivacyMode(val label: String) {
    LOCAL("Local only"),
    REMOTE("Remote backend"),
    ASK("Ask each time"),
}

const val DEFAULT_BACKEND_URL = "http://10.0.0.2:8080"

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("ugcforge_settings", Context.MODE_PRIVATE)

    var backendUrl: String
        get() = prefs.getString("backend_url", DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL
        set(value) = prefs.edit().putString("backend_url", value).apply()

    var privacyMode: PrivacyMode
        get() = PrivacyMode.valueOf(prefs.getString("privacy_mode", PrivacyMode.LOCAL.name)!!)
        set(value) = prefs.edit().putString("privacy_mode", value.name).apply()

    var useLocalEngine: Boolean
        get() = prefs.getBoolean("use_local_engine", true)
        set(value) = prefs.edit().putBoolean("use_local_engine", value).apply()
}