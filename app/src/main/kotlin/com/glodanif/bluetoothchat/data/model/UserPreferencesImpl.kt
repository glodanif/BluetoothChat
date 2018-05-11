package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import com.glodanif.bluetoothchat.ui.util.NotificationSettings

class UserPreferencesImpl(private val context: Context) : UserPreferences {

    private val version = 1

    private val keyPreferencesName = "userPreferences"

    private val keyVersion = "storage_version"
    private val keyNotificationSound = "notifications_sound"
    private val keyNotificationVibration = "notifications_vibration"
    private val keyAppearanceChatBgColor = "notifications_chat_bg_color"

    private val preferences
            by lazy { context.getSharedPreferences(keyPreferencesName, Context.MODE_PRIVATE) }

    init {
        migrate()
    }

    override fun isSoundEnabled() =
            preferences.getBoolean(keyNotificationSound, false)

    override fun isVibrationEnabled() =
            preferences.getBoolean(keyNotificationVibration, false)

    override fun getChatBackgroundColor() =
            preferences.getInt(keyAppearanceChatBgColor, Color.parseColor("#ADE9C5"))

    override fun getSettings() = NotificationSettings().apply {
        soundEnabled = isSoundEnabled()
        vibrationEnabled = isVibrationEnabled()
    }

    override fun saveChatBgColor(color: Int) {
        preferences.edit()
                .putInt(keyAppearanceChatBgColor, color)
                .apply()
    }

    override fun saveNewSoundPreference(enabled: Boolean) {
        preferences.edit()
                .putBoolean(keyNotificationSound, enabled)
                .apply()
    }

    override fun saveNewVibrationPreference(enabled: Boolean) {
        preferences.edit()
                .putBoolean(keyNotificationVibration, enabled)
                .apply()
    }

    private fun migrate() {

        val lastVersion = preferences.getInt(keyVersion, 0)

        if (lastVersion == version) {
            return
        }

        if (lastVersion < 1) {

            val oldPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            preferences.edit()
                    .putBoolean(keyNotificationSound, oldPreferences.getBoolean("notifications_sound", false))
                    .putBoolean(keyNotificationVibration, oldPreferences.getBoolean("notifications_vibration", false))
                    .apply()

            oldPreferences.edit()
                    .remove("notifications_sound")
                    .remove("notifications_vibration")
                    .apply()
        }

        preferences.edit().putInt(keyVersion, version).apply()
    }
}
