package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.util.NotificationSettings

interface Preferences {
    fun isSoundEnabled(): Boolean
    fun isVibrationEnabled(): Boolean
    fun getSettings(): NotificationSettings
}
