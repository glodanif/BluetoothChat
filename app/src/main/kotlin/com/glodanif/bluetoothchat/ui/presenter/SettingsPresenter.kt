package com.glodanif.bluetoothchat.ui.presenter

import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.ui.view.SettingsView

class SettingsPresenter(private val view: SettingsView, private val preferences: UserPreferences) {

    fun loadPreferences() {
        view.displayNotificationSetting(preferences.isSoundEnabled())
        view.displayAppearanceSettings(preferences.getChatBackgroundColor())
        view.displayDiscoverySetting(preferences.isClassificationEnabled())
    }

    fun prepareColorPicker() {
        view.displayColorPicker(preferences.getChatBackgroundColor())
    }

    fun onNewColorPicked(@ColorInt color: Int) {
        preferences.saveChatBgColor(color)
        view.displayAppearanceSettings(color)
    }

    fun onNewSoundPreference(enabled: Boolean) {
        preferences.saveNewSoundPreference(enabled)
    }

    fun onNewClassificationPreference(enabled: Boolean) {
        preferences.saveNewClassificationPreference(enabled)
    }
}
