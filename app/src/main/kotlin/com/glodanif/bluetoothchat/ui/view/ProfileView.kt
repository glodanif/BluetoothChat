package com.glodanif.bluetoothchat.ui.view

import androidx.annotation.ColorInt

interface ProfileView {
    fun showUserData(name: String, @ColorInt color: Int)
    fun showColorPicker(@ColorInt color: Int)
    fun showDeviceName(name: String?)
    fun showNotValidNameError(divider: String)
    fun prefillUsername(name: String)
    fun redirectToConversations()
}
