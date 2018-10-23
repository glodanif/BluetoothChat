package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.domain.interactor.GetUserPreferencesInteractor
import com.glodanif.bluetoothchat.domain.interactor.SaveUserPreferencesInteractor
import com.glodanif.bluetoothchat.ui.view.SettingsView
import com.glodanif.bluetoothchat.ui.viewmodel.UserPreferencesViewModel
import com.glodanif.bluetoothchat.ui.viewmodel.converter.PreferencesConverter

class SettingsPresenter(private val view: SettingsView,
                        private val getUserPreferencesInteractor: GetUserPreferencesInteractor,
                        private val saveUserPreferencesInteractor: SaveUserPreferencesInteractor,
                        private val converter: PreferencesConverter
) : LifecycleObserver {

    private lateinit var preferencesViewModel: UserPreferencesViewModel

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadPreferences() {

        getUserPreferencesInteractor.execute(Unit,
                onResult = { preferences ->
                    preferencesViewModel = converter.transform(preferences)
                    view.displaySetting(preferencesViewModel)
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        getUserPreferencesInteractor.cancel()
        saveUserPreferencesInteractor.cancel()
    }

    fun prepareColorPicker() {
        view.displayColorPicker(preferencesViewModel.color)
    }

    fun onNewColorPicked(@ColorInt color: Int) {
        preferencesViewModel.color = color
        val preferences = converter.transform(preferencesViewModel)
        saveUserPreferencesInteractor.execute(preferences,
                onResult = {
                    view.displaySetting(preferencesViewModel)
                })
    }

    fun onNewSoundPreference(enabled: Boolean) {
        preferencesViewModel.sound = enabled
        val preferences = converter.transform(preferencesViewModel)
        saveUserPreferencesInteractor.execute(preferences)
    }

    fun onNewClassificationPreference(enabled: Boolean) {
        preferencesViewModel.classification = enabled
        val preferences = converter.transform(preferencesViewModel)
        saveUserPreferencesInteractor.execute(preferences)
    }
}
