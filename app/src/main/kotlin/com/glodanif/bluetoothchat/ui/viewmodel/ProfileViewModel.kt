package com.glodanif.bluetoothchat.ui.viewmodel

import androidx.annotation.ColorInt
import com.amulyakhare.textdrawable.TextDrawable

data class ProfileViewModel(
        val nameLabelText: String,
        @ColorInt
        val nameLabelColor: Int,
        val avatar: TextDrawable,
        @ColorInt
        val color: Int
)
