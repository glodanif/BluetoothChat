package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.graphics.Color

class SettingsManagerImpl(context: Context) : SettingsManager {

    private val KEY_PREFERENCES = "key.settings"

    private val KEY_USER_NAME = "key.user_name"
    private val KEY_USER_COLOR = "key.user_color"

    private val preferences
            by lazy { context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE) }

    override fun saveUserName(name: String) {
        preferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun saveUserColor(color: Int) {
        preferences.edit().putInt(KEY_USER_COLOR, color).apply()
    }

    override fun getUserName(): String {
        return preferences.getString(KEY_USER_NAME, "")
    }

    override fun getUserColor() =
            preferences.getInt(KEY_USER_COLOR, Color.parseColor("#008756"))
}
