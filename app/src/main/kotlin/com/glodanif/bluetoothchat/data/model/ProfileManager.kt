package com.glodanif.bluetoothchat.data.model

import android.support.annotation.ColorInt

interface ProfileManager {
    fun saveUserName(name: String)
    fun saveUserColor(@ColorInt color: Int)
    fun getUserName(): String
    @ColorInt
    fun getUserColor(): Int
}