package com.glodanif.bluetoothchat.presenter

import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.view.ProfileView

class ProfilePresenter(private val view: ProfileView, private val settings: SettingsManager) {

    @ColorInt
    private var currentColor = settings.getUserColor()
    private var currentName = settings.getUserName()

    fun saveUser() {
        if (!currentName.isEmpty() && currentName.length <= 25 && !currentName.contains("#")) {
            settings.saveUserName(currentName)
            settings.saveUserColor(currentColor)
            view.redirectToConversations()
        } else {
            view.showNotValidNameError()
        }
    }

    fun prepareColorPicker() {
        view.showColorPicker(currentColor)
    }

    fun onColorPicked(@ColorInt color: Int) {
        currentColor = color
        view.displayUserData(currentName, color)
    }

    fun onNameChanged(name: String) {
        currentName = name.replace("\\s{2,}".toRegex(), " ")
        view.displayUserData(currentName, currentColor)
    }

    fun onStart() {
        view.displayUserData(currentName, currentColor)
    }
}
