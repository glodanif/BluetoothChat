package com.glodanif.bluetoothchat.data.model

import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.ui.util.NotificationSettings

interface UserPreferences {
    fun isSoundEnabled(): Boolean
    fun isVibrationEnabled(): Boolean
    @ColorInt
    fun getChatBackgroundColor(): Int
    fun getSettings(): NotificationSettings
    fun saveChatBgColor(@ColorInt color: Int)
    fun saveNewSoundPreference(enabled: Boolean)
    fun saveNewVibrationPreference(enabled: Boolean)
}
