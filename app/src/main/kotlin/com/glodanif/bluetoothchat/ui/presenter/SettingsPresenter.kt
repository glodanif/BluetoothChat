package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.ui.util.ThemeHolder
import com.glodanif.bluetoothchat.ui.view.SettingsView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsPresenter(private val view: SettingsView,
                        private val preferences: UserPreferences,
                        private val themeHolder: ThemeHolder,
                        private val uiContext: CoroutineDispatcher = Dispatchers.Main,
                        private val bgContext: CoroutineDispatcher = Dispatchers.IO) : BasePresenter(uiContext) {

    @NightMode
    private var initialNightMode: Int = AppCompatDelegate.MODE_NIGHT_NO
    @NightMode
    private var changedNightMode: Int = AppCompatDelegate.MODE_NIGHT_NO

    fun loadPreferences() = launch {

        val color = withContext(bgContext) { preferences.getChatBackgroundColor() }
        val nightMode = withContext(bgContext) { preferences.getNightMode() }
        val sound = withContext(bgContext) { preferences.isSoundEnabled() }
        val classification = withContext(bgContext) { preferences.isClassificationEnabled() }

        initialNightMode = nightMode
        changedNightMode = nightMode

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
            themeHolder.setNightMode(nightMode)
            changedNightMode = nightMode
        }
    }

    fun onNewSoundPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewSoundPreference(enabled)
    }

    fun onNewClassificationPreference(enabled: Boolean) = launch(bgContext) {
        preferences.saveNewClassificationPreference(enabled)
    }

    fun isNightModeChanged() = initialNightMode != changedNightMode
}
