package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.experimental.EmptyCoroutineContext

class ReceivedImagesPresenterUnitTest {

    @RelaxedMockK
    private lateinit var storage: MessagesStorage
    @RelaxedMockK
    private lateinit var view: ReceivedImagesView

    private lateinit var presenter: ReceivedImagesPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ReceivedImagesPresenter(null, view, storage,
                EmptyCoroutineContext, EmptyCoroutineContext)
    }

    @Test
    fun loading_empty() {
        coEvery { storage.getFileMessagesByDevice(null) } returns ArrayList()
        presenter.loadImages()
        verify { view.showNoImages() }
    }

    @Test
    fun loading_notEmpty() {
        val list = arrayListOf<MessageFile>(mockk())
        coEvery { storage.getFileMessagesByDevice(null) } returns list
        presenter.loadImages()
        verify { view.displayImages(list) }
    }
}
