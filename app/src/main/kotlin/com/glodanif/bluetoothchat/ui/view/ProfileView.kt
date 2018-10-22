package com.glodanif.bluetoothchat.ui.view

import androidx.annotation.ColorInt
import com.glodanif.bluetoothchat.ui.viewmodel.ProfileViewModel

interface ProfileView {
    fun showUserData(profile: ProfileViewModel)
    fun showColorPicker(@ColorInt color: Int)
    fun showDeviceName(name: String?)
    fun showNotValidNameError(divider: String)
    fun prefillUsername(name: String)
    fun redirectToConversations()
    fun showBluetoothSettingsActivityNotFound()
}
