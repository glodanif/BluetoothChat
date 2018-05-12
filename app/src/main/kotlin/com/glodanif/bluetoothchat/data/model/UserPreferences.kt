package com.glodanif.bluetoothchat.data.model

import android.support.annotation.ColorInt

interface UserPreferences {
    fun isSoundEnabled(): Boolean
    @ColorInt
    fun getChatBackgroundColor(): Int
    fun saveChatBgColor(@ColorInt color: Int)
    fun saveNewSoundPreference(enabled: Boolean)
}
