package com.glodanif.bluetoothchat.ui.view

import androidx.annotation.ColorInt

interface SettingsView {
    fun displayNotificationSetting(sound: Boolean)
    fun displayDiscoverySetting(classification: Boolean)
    fun displayAppearanceSettings(@ColorInt color: Int)
    fun displayColorPicker(@ColorInt color: Int)
}
