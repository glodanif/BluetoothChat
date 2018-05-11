package com.glodanif.bluetoothchat.ui.view

import android.support.annotation.ColorInt

interface SettingsView {
    fun displayNotificationSetting(sound: Boolean, vibration: Boolean)
    fun displayAppearanceSettings(@ColorInt color: Int)
    fun displayColorPicker(@ColorInt color: Int)
}
