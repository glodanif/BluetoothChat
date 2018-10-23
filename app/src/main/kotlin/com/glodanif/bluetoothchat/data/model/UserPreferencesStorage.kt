package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.UserPreferences

interface UserPreferencesStorage {
    fun getPreferences(): UserPreferences
    fun savePreferences(preferences: UserPreferences)
}
