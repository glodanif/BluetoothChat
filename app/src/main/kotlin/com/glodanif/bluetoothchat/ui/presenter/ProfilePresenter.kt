package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.domain.interactor.NoInput
import com.glodanif.bluetoothchat.domain.entity.Profile
import com.glodanif.bluetoothchat.domain.interactor.GetProfileInteractor
import com.glodanif.bluetoothchat.domain.interactor.SaveProfileInteractor
import com.glodanif.bluetoothchat.ui.router.ProfileRouter
import com.glodanif.bluetoothchat.ui.view.ProfileView
import com.glodanif.bluetoothchat.ui.viewmodel.ProfileConverter
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch

class ProfilePresenter(private val setupMode: Boolean,
                       private val view: ProfileView,
                       private val router: ProfileRouter,
                       private val converter: ProfileConverter,
                       private val getProfileInteractor: GetProfileInteractor,
                       private val saveProfileInteractor: SaveProfileInteractor,
                       private val scanner: BluetoothScanner) : LifecycleObserver {

    private var currentName = ""
    private var currentColor = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {

        getProfileInteractor.execute(NoInput,
                onResult = {
                    currentName = it.name
                    currentColor = it.color
                    view.prefillUsername(currentName)
                    view.showUserData(converter.transform(it))
                }
        )

        view.showDeviceName(scanner.getMyDeviceName())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        getProfileInteractor.cancel()
        saveProfileInteractor.cancel()
    }

    fun saveUser() {

        if (!currentName.isEmpty() && currentName.length <= 25 && !currentName.contains(Contract.DIVIDER)) {

            val profile = Profile(currentName, currentColor)
            saveProfileInteractor.execute(profile, onResult = {
                afterSave()
            })
        } else {
            view.showNotValidNameError(Contract.DIVIDER)
        }
    }

    private fun afterSave() {

        if (setupMode) {
            router.redirectToConversations()
        } else {
            router.close()
        }
    }

    fun prepareColorPicker() {
        view.showColorPicker(currentColor)
    }

    fun onColorPicked(@ColorInt color: Int) {
        currentColor = color
        val profile = Profile(currentName, currentColor)
        view.showUserData(converter.transform(profile))
    }

    fun onNameChanged(name: String) {
        currentName = name.replace("\\s{2,}".toRegex(), " ").trim()
        val profile = Profile(currentName, currentColor)
        view.showUserData(converter.transform(profile))
    }

    fun openBluetoothSettings() {
        router.openBluetoothSettings()
    }

    fun onBluetoothSettingsNotAvailable() {
        view.showBluetoothSettingsActivityNotFound()
    }
}
