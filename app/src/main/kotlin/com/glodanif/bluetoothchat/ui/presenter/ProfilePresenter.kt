package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.domain.InvalidProfileNameException
import com.glodanif.bluetoothchat.domain.interactor.NoInput
import com.glodanif.bluetoothchat.domain.entity.Profile
import com.glodanif.bluetoothchat.domain.interactor.GetMyDeviceNameInteractor
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
                       private val getMyDeviceNameInteractor: GetMyDeviceNameInteractor) : LifecycleObserver {

    private var currentName = ""
    private var currentColor = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {

        getProfileInteractor.execute(NoInput,
                onResult = { profile ->
                    currentName = profile.name
                    currentColor = profile.color
                    view.prefillUsername(currentName)
                    view.showUserData(converter.transform(profile))
                }
        )

        getMyDeviceNameInteractor.execute(NoInput,
                onResult = { view.showDeviceName(it) }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        getProfileInteractor.cancel()
        saveProfileInteractor.cancel()
        getMyDeviceNameInteractor.cancel()
    }

    fun saveUser() {

        val profile = Profile(currentName, currentColor)
        saveProfileInteractor.execute(profile,
                onResult = {
                    if (setupMode) {
                        router.redirectToConversations()
                    } else {
                        router.close()
                    }
                },
                onError = { error ->
                    if (error is InvalidProfileNameException) {
                        view.showNotValidNameError(error.forbiddenSymbol)
                    }
                }
        )
    }

    fun prepareColorPicker() {
        view.showColorPicker(currentColor)
    }

    fun onColorPicked(@ColorInt color: Int) {
        currentColor = color
        view.showUserData(converter.transform(currentName, currentColor))
    }

    fun onNameChanged(name: String) {
        currentName = name.replace("\\s{2,}".toRegex(), " ").trim()
        view.showUserData(converter.transform(currentName, currentColor))
    }

    fun openBluetoothSettings() {
        router.openBluetoothSettings()
    }

    fun onBluetoothSettingsNotAvailable() {
        view.showBluetoothSettingsActivityNotFound()
    }
}
