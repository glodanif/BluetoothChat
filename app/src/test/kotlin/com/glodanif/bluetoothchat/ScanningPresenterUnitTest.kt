package com.glodanif.bluetoothchat

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
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
        Mockito.verify(view)?.showBluetoothScanner()
    }

    @Test
    fun availability_isNotAvailable() {
        Mockito.`when`(model.isBluetoothAvailable()).thenReturn(false)
        presenter.checkBluetoothAvailability()
        Mockito.verify(view)?.showBluetoothIsNotAvailableMessage()
    }

    @Test
    fun enabling_isEnabled_isDiscoverable() {
        Mockito.`when`(model.isBluetoothEnabled()).thenReturn(true)
        Mockito.`when`(model.isDiscoverable()).thenReturn(true)
        presenter.checkBluetoothEnabling()
        Mockito.verify(view)?.showPairedDevices(ArrayList<BluetoothDevice>())
        Mockito.verify(view)?.showDiscoverableProcess()
    }

    @Test
    fun enabling_isEnabled_isNotDiscoverable() {
        Mockito.`when`(model.isBluetoothEnabled()).thenReturn(true)
        Mockito.`when`(model.isDiscoverable()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        Mockito.verify(view)?.showPairedDevices(ArrayList<BluetoothDevice>())
        Mockito.verify(view)?.showDiscoverableFinished()
    }

    @Test
    fun enabling_isDisabled() {
        Mockito.`when`(model.isBluetoothEnabled()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        Mockito.verify(view)?.showBluetoothEnablingRequest()
    }
}