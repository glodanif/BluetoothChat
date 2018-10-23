package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.UserPreferences

class UserPreferencesStorageImpl(private val context: Context) : UserPreferencesStorage {

    private val version = 1

    private val keyPreferencesName = "userPreferences"

    private val keyVersion = "storage_version"
    private val keyNotificationSound = "notifications_sound"
    private val keyAppearanceChatBgColor = "notifications_chat_bg_color"
    private val keyDiscoveryClassification = "notifications_classification"

    private val defaultChatBackgroundColor =
            ContextCompat.getColor(context, R.color.background_chat_default)

    private val sharedPreferences
            by lazy { context.getSharedPreferences(keyPreferencesName, Context.MODE_PRIVATE) }

    init {
        migrate()
    }

    override fun getPreferences(): UserPreferences {
        val color = sharedPreferences.getInt(keyAppearanceChatBgColor, defaultChatBackgroundColor)
        val sound = sharedPreferences.getBoolean(keyNotificationSound, false)
        val classification = sharedPreferences.getBoolean(keyDiscoveryClassification, true)
        return UserPreferences(color, sound, classification)
    }

    override fun savePreferences(preferences: UserPreferences) {
        sharedPreferences.edit()
                .putInt(keyAppearanceChatBgColor, preferences.color)
                .putBoolean(keyNotificationSound, preferences.sound)
                .putBoolean(keyDiscoveryClassification, preferences.classification)
                .apply()
    }

    private fun migrate() {

        val lastVersion = sharedPreferences.getInt(keyVersion, 0)

        if (lastVersion == version) {
            return
        }

        if (lastVersion < 1) {

            val oldPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            sharedPreferences.edit()
                    .putBoolean(keyNotificationSound, oldPreferences.getBoolean("notifications_sound", false))
                    .apply()

            oldPreferences.edit()
                    .remove("notifications_sound")
                    .remove("notifications_vibration")
                    .apply()
        }

        sharedPreferences.edit().putInt(keyVersion, version).apply()
    }
}
