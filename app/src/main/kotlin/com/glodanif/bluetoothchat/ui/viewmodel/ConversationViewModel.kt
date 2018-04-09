package com.glodanif.bluetoothchat.ui.viewmodel

import android.graphics.Color
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.utils.getFirstLetter
import java.util.*

data class ConversationViewModel(
        val address: String,
        val deviceName: String,
        val displayName: String,
        val fullName: String,
        val color: Int,
        val lastMessage: String?,
        val lastActivity: Date?,
        val lastActivityText: String?,
        val notSeen: Int
) {

    fun getColoredAvatar() = textDrawable(color)

    fun getGrayedAvatar() = textDrawable(Color.LTGRAY)

    private fun textDrawable(color: Int): TextDrawable =
            TextDrawable.builder().buildRound(displayName.getFirstLetter(), color)
}
