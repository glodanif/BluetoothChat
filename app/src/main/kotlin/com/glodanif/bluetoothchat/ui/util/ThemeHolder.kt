package com.glodanif.bluetoothchat.ui.util

import androidx.appcompat.app.AppCompatDelegate.*

interface ThemeHolder {
    fun setNightMode(@NightMode nightMode: Int)
    @NightMode
    fun getNightMode(): Int
}
