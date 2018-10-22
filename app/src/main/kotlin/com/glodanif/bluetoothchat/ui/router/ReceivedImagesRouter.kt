package com.glodanif.bluetoothchat.ui.router

import android.widget.ImageView
import com.glodanif.bluetoothchat.data.entity.MessageFile

interface ReceivedImagesRouter {
    fun openImage(view: ImageView, message: MessageFile)
}
