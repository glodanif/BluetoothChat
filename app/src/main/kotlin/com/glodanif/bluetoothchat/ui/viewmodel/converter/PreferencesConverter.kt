package com.glodanif.bluetoothchat.ui.viewmodel.converter

import com.glodanif.bluetoothchat.data.entity.UserPreferences
import com.glodanif.bluetoothchat.ui.viewmodel.UserPreferencesViewModel

class PreferencesConverter {

    fun transform(preferences: UserPreferences): UserPreferencesViewModel {
        return UserPreferencesViewModel(preferences.color, preferences.sound, preferences.classification)
    }

    fun transform(preferences: UserPreferencesViewModel): UserPreferences {
        return UserPreferences(preferences.color, preferences.sound, preferences.classification)
    }
}
