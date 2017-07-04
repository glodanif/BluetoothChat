package com.glodanif.bluetoothchat.view

import android.support.annotation.ColorInt

interface ProfileView {
    fun displayUserData(name: String, @ColorInt color: Int)
    fun prefillUsername(name: String)
    fun showColorPicker(@ColorInt color: Int)
    fun redirectToConversations()
    fun showNotValidNameError()
}