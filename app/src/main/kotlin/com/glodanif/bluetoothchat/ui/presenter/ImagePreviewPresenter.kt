package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.FileManager
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.extension.getReadableFileSize
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import java.io.File

class ImagePreviewPresenter(private val message: ChatMessage, private val view: ImagePreviewView, private val storageModel: MessagesStorage) {

    private val file = File(message.filePath)

    fun loadData() {
        view.showFileInfo(file.name, file.length().getReadableFileSize())
        view.displayImage("file://${file.absolutePath}")
    }

    fun removeFile() {

        file.delete()

        message.fileInfo = null
        message.filePath = null
        storageModel.updateMessage(message)
    }
}
