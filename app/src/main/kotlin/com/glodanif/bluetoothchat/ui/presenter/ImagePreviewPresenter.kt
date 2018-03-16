package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.extension.toReadableFileSize
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import kotlinx.coroutines.experimental.launch
import java.io.File
import javax.inject.Inject

class ImagePreviewPresenter(private val message: ChatMessage, private val view: ImagePreviewView) {

    @Inject
    lateinit var storageModel: MessagesStorage

    init {
        ComponentsManager.getDataSourceComponent().inject(this)
    }

    private val file = File(message.filePath)

    fun loadData() {
        view.showFileInfo(file.name, file.length().toReadableFileSize())
        view.displayImage("file://${file.absolutePath}")
    }

    fun removeFile() {

        file.delete()

        message.fileInfo = null
        message.filePath = null

        launch {
            storageModel.updateMessage(message)
        }
    }
}
