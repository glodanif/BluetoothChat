package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import kotlin.coroutines.experimental.CoroutineContext

class ReceivedImagesPresenter(private val address: String,
                              private val view: ReceivedImagesView,
                              private val model: MessagesStorage,
                              private val uiContext: CoroutineContext = UI,
                              private val bgContext: CoroutineContext = CommonPool) {

    fun loadImages() = launch(uiContext) {
        val messages = withContext(bgContext) { model.getFileMessagesByDevice(address) }
        if (messages.isNotEmpty()) {
            view.displayImages(messages)
        } else {
            view.showNoImages()
        }
    }
}
