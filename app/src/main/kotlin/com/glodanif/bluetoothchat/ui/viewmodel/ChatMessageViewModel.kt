package com.glodanif.bluetoothchat.ui.viewmodel

import androidx.annotation.StringRes
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.utils.Size

data class ChatMessageViewModel(
        val uid: Long,
        val dayOfYear: String,
        val time: String,
        val text: String?,
        val own: Boolean,
        var seen: Boolean,
        var delivered: Boolean,
        val type: PayloadType?,
        val isImageAvailable: Boolean,
        @StringRes
        val imageProblemText: Int,
        val imageSize: Size,
        val imagePath: String?,
        val imageUri: String?
)
