package com.glodanif.bluetoothchat.ui.router

import android.widget.ImageView
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel

interface ChatRouter {
    fun openImage(view: ImageView, message: ChatMessageViewModel)
}