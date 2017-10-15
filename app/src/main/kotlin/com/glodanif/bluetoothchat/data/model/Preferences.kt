package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.ui.util.NotificationSettings

interface Preferences {
    fun isSoundEnabled(): Boolean
    fun isVibrationEnabled(): Boolean
    fun getSettings(): NotificationSettings
}
