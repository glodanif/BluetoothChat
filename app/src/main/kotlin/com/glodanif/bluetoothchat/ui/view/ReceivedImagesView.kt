package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.data.entity.ChatMessage

interface ReceivedImagesView {
    fun displayImages(imageMessages: List<ChatMessage>)
    fun showNoImages()
}
