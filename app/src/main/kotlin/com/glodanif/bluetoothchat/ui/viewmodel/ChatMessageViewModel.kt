package com.glodanif.bluetoothchat.ui.viewmodel

import android.support.annotation.StringRes
import com.glodanif.bluetoothchat.data.entity.MessageType

data class ChatMessageViewModel(
        val uid: Long,
        val date: String,
        val text: String?,
        val own: Boolean,
        val type: MessageType?,
        val isImageAvailable: Boolean,
        @StringRes
        val imageProblemText: Int,
        val imageSize: Size?,
        val imagePath: String?,
        val imageUri: String?
)
