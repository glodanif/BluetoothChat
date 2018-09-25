package com.glodanif.bluetoothchat.data.model

import androidx.annotation.ColorInt

interface UserPreferences {
    fun isSoundEnabled(): Boolean
    fun isClassificationEnabled(): Boolean
    @ColorInt
    fun getChatBackgroundColor(): Int
    fun saveChatBgColor(@ColorInt color: Int)
    fun saveNewSoundPreference(enabled: Boolean)
    fun saveNewClassificationPreference(enabled: Boolean)
}
