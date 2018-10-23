package com.glodanif.bluetoothchat.ui.viewmodel

import androidx.annotation.ColorInt

data class UserPreferencesViewModel(
        @ColorInt var color: Int = 0,
        var sound: Boolean = false,
        var classification: Boolean = true
)
