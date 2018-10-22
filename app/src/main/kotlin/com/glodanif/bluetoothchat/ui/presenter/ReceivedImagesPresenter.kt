package com.glodanif.bluetoothchat.ui.presenter

import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.domain.interactor.GetReceivedImagesInteractor
import com.glodanif.bluetoothchat.ui.router.ReceivedImagesRouter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView

class ReceivedImagesPresenter(private val address: String,
                              private val view: ReceivedImagesView,
                              private val router: ReceivedImagesRouter,
                              private val getReceivedImagesInteractor: GetReceivedImagesInteractor
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadImages() {

        getReceivedImagesInteractor.execute(address,
                onResult = { messages ->
                    if (messages.isNotEmpty()) {
                        view.displayImages(messages)
                    } else {
                        view.showNoImages()
                    }
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        getReceivedImagesInteractor.cancel()
    }

    fun onImageClick(view: ImageView, message: MessageFile) {
        router.openImage(view, message)
    }
}
