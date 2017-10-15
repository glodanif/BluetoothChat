package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.preference.PreferenceManager
import com.glodanif.bluetoothchat.ui.util.NotificationSettings

class UserPreferences(context: Context): Preferences {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun isSoundEnabled(): Boolean {
        return preferences.getBoolean("notifications_sound", false)
    }

    override fun isVibrationEnabled(): Boolean {
        return preferences.getBoolean("notifications_vibration", false)
    }

    override fun getSettings(): NotificationSettings {
        val settings = NotificationSettings()
        settings.soundEnabled = isSoundEnabled()
        settings.vibrationEnabled = isVibrationEnabled()
        return settings
    }
}
