package com.glodanif.bluetoothchat.view

import android.support.annotation.ColorInt

interface ProfileView {
    fun showUserData(name: String, @ColorInt color: Int)
    fun showColorPicker(@ColorInt color: Int)
    fun showNotValidNameError()
    fun prefillUsername(name: String)
    fun addSearchShortcut()
    fun redirectToConversations()
}