package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.NightMode
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.ui.view.SettingsView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsPresenter(private val view: SettingsView,
                        private val preferences: UserPreferences,
                        private val uiContext: CoroutineDispatcher = Dispatchers.Main,
                        private val bgContext: CoroutineDispatcher = Dispatchers.IO) : BasePresenter(uiContext) {

    fun loadPreferences() = launch {

        val color = withContext(bgContext) { preferences.getChatBackgroundColor() }
        val nightMode = withContext(bgContext) { preferences.getNightMode() }
        val sound = withContext(bgContext) { preferences.isSoundEnabled() }
        val classification = withContext(bgContext) { preferences.isClassificationEnabled() }

        view.displayBgColorSettings(color)
        view.displayNightModeSettings(nightMode)
        view.displayNotificationSetting(sound)
        view.displayDiscoverySetting(classification)
    }

    fun prepareColorPicker() {
        view.displayColorPicker(preferences.getChatBackgroundColor())
    }

    fun prepareNightModePicker() {
        view.displayNightModePicker(preferences.getNightMode())
    }

    fun onNewColorPicked(@ColorInt color: Int) = launch(bgContext) {
        preferences.saveChatBgColor(color)
        launch(uiContext) {
            view.displayColorPicker(color)
        }
    }

    fun onNewNightModePreference(@NightMode nightMode: Int) = launch(bgContext) {
        preferences.saveNightMode(nightMode)
        launch(uiContext) {
            view.displayNightModeSettings(nightMode)
        }
    }

    fun onNewSoundPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewSoundPreference(enabled)
    }

    fun onNewClassificationPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewClassificationPreference(enabled)
    }
}
