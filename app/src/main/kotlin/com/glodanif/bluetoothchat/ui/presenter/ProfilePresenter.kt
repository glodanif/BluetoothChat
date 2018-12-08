package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.ColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.ui.view.ProfileView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePresenter(private val view: ProfileView,
                       private val settings: ProfileManager,
                       private val scanner: BluetoothScanner,
                       private val uiContext: CoroutineDispatcher = Dispatchers.Main,
                       private val bgContext: CoroutineDispatcher = Dispatchers.IO) : BasePresenter(uiContext) {

    @ColorInt
    private var currentColor = 0
    private var currentName: String = ""

    fun saveUser() {

        if (!currentName.isEmpty() && currentName.length <= 25 && !currentName.contains(Contract.DIVIDER)) {

            launch {
                settings.saveUserName(currentName.trim())
                settings.saveUserColor(currentColor)
                launch(uiContext) {
                    view.redirectToConversations()
                }
            }
        } else {
            view.showNotValidNameError(Contract.DIVIDER)
        }
    }

    fun prepareColorPicker() {
        view.showColorPicker(currentColor)
    }

    fun onColorPicked(@ColorInt color: Int) {
        currentColor = color
        view.showUserData(currentName, color)
    }

    fun onNameChanged(name: String) {
        currentName = name.replace("\\s{2,}".toRegex(), " ").trim()
        view.showUserData(currentName, currentColor)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadSavedUser() = launch {

        currentName = withContext(bgContext) { settings.getUserName() }
        currentColor = withContext(bgContext) { settings.getUserColor() }

        view.prefillUsername(currentName)
        view.showUserData(currentName, currentColor)
        view.showDeviceName(scanner.getMyDeviceName())
    }
}
