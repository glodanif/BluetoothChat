package com.glodanif.bluetoothchat.ui.view

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.NightMode

interface SettingsView {
    fun displayNotificationSetting(sound: Boolean)
    fun displayDiscoverySetting(classification: Boolean)
    fun displayBgColorSettings(@ColorInt color: Int)
    fun displayNightModeSettings(@NightMode nightMode: Int)
    fun displayColorPicker(@ColorInt color: Int)
    fun displayNightModePicker(@NightMode nightMode: Int)
}
