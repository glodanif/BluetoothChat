package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView

class ReceivedImagesPresenter(private val address: String?, private val view: ReceivedImagesView, private val model: MessagesStorage) {

    fun loadImages() {

        model.getFilesMessagesByDevice(address) {
            view.displayImages(it)
        }
    }
}
