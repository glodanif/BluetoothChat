package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.utils.toReadableFileSize
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import kotlin.coroutines.experimental.CoroutineContext

class ImagePreviewPresenter(private val messageId: Long,
                            private val image: File,
                            private val view: ImagePreviewView,
                            private val storage: MessagesStorage,
                            private val uiContext: CoroutineContext = UI,
                            private val bgContext: CoroutineContext = CommonPool) {

    fun loadImage() {
        view.showFileInfo(image.name, image.length().toReadableFileSize())
        view.displayImage("file://${image.absolutePath}")
    }

    fun removeFile() = launch(uiContext) {

        launch(bgContext) {
            image.delete()
            storage.removeFileInfo(messageId)
        }

        view.close()
    }
}
