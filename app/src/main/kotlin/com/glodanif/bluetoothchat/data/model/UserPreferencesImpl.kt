package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.glodanif.bluetoothchat.R

class UserPreferencesImpl(private val context: Context) : UserPreferences {

    private val version = 1

    private val keyPreferencesName = "userPreferences"

    private val keyVersion = "storage_version"
    private val keyNotificationSound = "notifications_sound"
    private val keyAppearanceChatBgColor = "notifications_chat_bg_color"
    private val keyDiscoveryClassification = "notifications_classification"

    private val defaultChatBackgroundColor =
            ContextCompat.getColor(context, R.color.background_chat_default)

    private val preferences
            by lazy { context.getSharedPreferences(keyPreferencesName, Context.MODE_PRIVATE) }

    init {
        migrate()
    }

    override fun isSoundEnabled() =
            preferences.getBoolean(keyNotificationSound, false)

    override fun isClassificationEnabled() =
            preferences.getBoolean(keyDiscoveryClassification, true)

    override fun getChatBackgroundColor() =
            preferences.getInt(keyAppearanceChatBgColor, defaultChatBackgroundColor)

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

    override fun saveNewClassificationPreference(enabled: Boolean) {
        preferences.edit()
                .putBoolean(keyDiscoveryClassification, enabled)
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
                    .apply()

            oldPreferences.edit()
                    .remove("notifications_sound")
                    .remove("notifications_vibration")
                    .apply()
        }

        preferences.edit().putInt(keyVersion, version).apply()
    }
}
