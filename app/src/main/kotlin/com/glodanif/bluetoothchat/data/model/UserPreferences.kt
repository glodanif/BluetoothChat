package com.glodanif.bluetoothchat.data.model

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.NightMode

interface UserPreferences {
    fun isSoundEnabled(): Boolean
    fun isClassificationEnabled(): Boolean
    @ColorInt
    fun getChatBackgroundColor(): Int
    @NightMode
    fun getNightMode(): Int
    fun saveChatBgColor(@ColorInt color: Int)
    fun saveNewSoundPreference(enabled: Boolean)
    fun saveNewClassificationPreference(enabled: Boolean)
    fun saveNightMode(@NightMode mode: Int)
}
