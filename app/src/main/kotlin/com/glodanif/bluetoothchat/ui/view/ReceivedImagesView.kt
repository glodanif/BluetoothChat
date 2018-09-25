package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.data.entity.MessageFile

interface ReceivedImagesView {
    fun displayImages(imageMessages: List<MessageFile>)
    fun showNoImages()
}
