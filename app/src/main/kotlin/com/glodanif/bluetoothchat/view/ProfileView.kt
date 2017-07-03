package com.glodanif.bluetoothchat.view

import android.support.annotation.ColorInt

interface ProfileView {
    fun displayUserData(name: String, @ColorInt color: Int)
    fun showColorPicker(@ColorInt color: Int)
}