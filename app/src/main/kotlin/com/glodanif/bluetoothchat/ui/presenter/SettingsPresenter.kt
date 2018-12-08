package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
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
        val sound = withContext(bgContext) { preferences.isSoundEnabled() }
        val classification = withContext(bgContext) { preferences.isClassificationEnabled() }

        view.displayAppearanceSettings(color)
        view.displayNotificationSetting(sound)
        view.displayDiscoverySetting(classification)
    }

    fun prepareColorPicker() {
        view.displayColorPicker(preferences.getChatBackgroundColor())
    }

    fun onNewColorPicked(@ColorInt color: Int) = launch(bgContext) {
        preferences.saveChatBgColor(color)
        launch(uiContext) {
            view.displayAppearanceSettings(color)
        }
    }

    fun onNewSoundPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewSoundPreference(enabled)
    }

    fun onNewClassificationPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewClassificationPreference(enabled)
    }
}
