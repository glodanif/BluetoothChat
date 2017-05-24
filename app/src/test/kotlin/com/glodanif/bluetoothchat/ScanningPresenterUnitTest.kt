package com.glodanif.bluetoothchat

import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.MockitoAnnotations

class ScanningPresenterUnitTest {

    @Mock
    lateinit var model: BluetoothScanner
    @Mock
    lateinit var view: ScanView

    lateinit var presenter: ScanPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = ScanPresenter(view, model)
    }

    @Test
    fun availability_isAvailable() {
        Mockito.`when`(model.isBluetoothAvailable()).thenReturn(true)
        presenter.checkBluetoothAvailability()
        Mockito.verify(view, atLeastOnce())?.showBluetoothFunctionality()
    }

    @Test
    fun availability_isNotAvailable() {
        Mockito.`when`(model.isBluetoothAvailable()).thenReturn(false)
        presenter.checkBluetoothAvailability()
        Mockito.verify(view, atLeastOnce())?.showBluetoothIsNotAvailableMessage()
    }
}