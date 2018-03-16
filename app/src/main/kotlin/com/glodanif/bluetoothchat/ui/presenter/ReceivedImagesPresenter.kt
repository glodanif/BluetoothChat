package com.glodanif.bluetoothchat.ui.presenter

import android.os.Handler
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ReceivedImagesPresenter(private val address: String?, private val view: ReceivedImagesView) {

    @Inject
    lateinit var model: MessagesStorage

    private val handler = Handler()

    init {
        ComponentsManager.getDataSourceComponent().inject(this)
    }

    fun loadImages() {

        launch {

            val messages = model.getFilesMessagesByDevice(address)

            handler.post {
                if (messages.isNotEmpty()) {
                    view.displayImages(messages)
                } else {
                    view.showNoImages()
                }
            }
        }
    }
}
