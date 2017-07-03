package com.glodanif.bluetoothchat.presenter

import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.view.ProfileView

class ProfilePresenter(private val view: ProfileView, private val settings: SettingsManager) {

    fun loadUser() {

    }

    fun prepareColorPicker() {
        view.showColorPicker(settings.getUserColor())
    }

    fun onColorPicked(@ColorInt color: Int) {
        settings.saveUserColor(color)
        val name = if (settings.getUserName() == null)  "Your Name" else settings.getUserName()
        view.displayUserData(name!!, color)
    }

    fun onStart() {
        val name = if (settings.getUserName() == null)  "Your Name" else settings.getUserName()
        view.displayUserData(name!!, settings.getUserColor())
    }
}