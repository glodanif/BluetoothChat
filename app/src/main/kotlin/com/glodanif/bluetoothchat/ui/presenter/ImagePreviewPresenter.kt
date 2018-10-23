package com.glodanif.bluetoothchat.ui.presenter

import androidx.lifecycle.LifecycleObserver
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import com.glodanif.bluetoothchat.utils.toReadableFileSize
import kotlinx.coroutines.experimental.launch
import java.io.File

class ImagePreviewPresenter(private val messageId: Long,
                            private val image: File,
                            private val view: ImagePreviewView,
                            private val storage: MessagesStorage
): LifecycleObserver {

    fun loadImage() {
        view.showFileInfo(image.name, image.length().toReadableFileSize())
        view.displayImage("file://${image.absolutePath}")
    }

    fun removeFile() {

        launch {
            image.delete()
            storage.removeFileInfo(messageId)
        }

        view.close()
    }
}
