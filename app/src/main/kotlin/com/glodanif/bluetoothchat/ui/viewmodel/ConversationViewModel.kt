package com.glodanif.bluetoothchat.ui.viewmodel

import android.graphics.Color
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.extension.getFirstLetter

data class ConversationViewModel(
        val address: String,
        val name: String,
        val color: Int,
        val displayName: String,
        val lastMessage: String?,
        val lastActivity: String?,
        val notSeen: Int
) {

    fun getColoredAvatar() = textDrawable(color)

    fun getGrayedAvatar() = textDrawable(Color.LTGRAY)

    private fun textDrawable(color: Int): TextDrawable =
            TextDrawable.builder().buildRound(name.getFirstLetter(), color)
}
