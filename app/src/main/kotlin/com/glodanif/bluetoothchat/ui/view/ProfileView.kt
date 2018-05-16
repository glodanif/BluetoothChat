package com.glodanif.bluetoothchat.ui.view

import android.support.annotation.ColorInt

interface ProfileView {
    fun showUserData(name: String, @ColorInt color: Int)
    fun showColorPicker(@ColorInt color: Int)
    fun showDeviceName(name: String?)
    fun showNotValidNameError()
    fun prefillUsername(name: String)
    fun redirectToConversations()
}
