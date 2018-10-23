package com.glodanif.bluetoothchat.ui.view

import androidx.annotation.ColorInt
import com.glodanif.bluetoothchat.ui.viewmodel.UserPreferencesViewModel

interface SettingsView {
    fun displaySetting(preferences: UserPreferencesViewModel)
    fun displayColorPicker(@ColorInt color: Int)
}
