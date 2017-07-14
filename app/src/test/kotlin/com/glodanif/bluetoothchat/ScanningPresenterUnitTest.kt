package com.glodanif.bluetoothchat

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.model.BluetoothScanner.ScanningListener
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.createinstance.createInstance
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ScanningPresenterUnitTest {

    @JvmField
    @Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var model: BluetoothScanner
    @Mock
    private lateinit var view: ScanView
    @Mock
    private lateinit var listener: ScanningListener

    private val captor: KArgumentCaptor<ScanningListener> = argumentCaptor<ScanningListener>()

    lateinit var presenter: ScanPresenter

    @Before
    fun setup() {
        presenter = ScanPresenter(view, model)
    }

    @Test
    fun availability_isAvailable() {
        `when`(model.isBluetoothAvailable()).thenReturn(true)
        presenter.checkBluetoothAvailability()
        verify(view).showBluetoothScanner()
    }

    @Test
    fun availability_isNotAvailable() {
        `when`(model.isBluetoothAvailable()).thenReturn(false)
        presenter.checkBluetoothAvailability()
        verify(view).showBluetoothIsNotAvailableMessage()
    }

    @Test
    fun enabling_isEnabled_isDiscoverable() {
        `when`(model.isBluetoothEnabled()).thenReturn(true)
        `when`(model.isDiscoverable()).thenReturn(true)
        presenter.checkBluetoothEnabling()
        verify(view).showPairedDevices(ArrayList<BluetoothDevice>())
        verify(view).showDiscoverableProcess()
    }

    @Test
    fun enabling_isEnabled_isNotDiscoverable() {
        `when`(model.isBluetoothEnabled()).thenReturn(true)
        `when`(model.isDiscoverable()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        verify(view).showPairedDevices(ArrayList<BluetoothDevice>())
        verify(view).showDiscoverableFinished()
    }

    @Test
    fun enabling_isDisabled() {
        `when`(model.isBluetoothEnabled()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        verify(view).showBluetoothEnablingRequest()
    }

    @Test
    fun enabling_turnOn() {
        `when`(model.isBluetoothEnabled()).thenReturn(false)
        presenter.turnOnBluetooth()
        verify(view).requestBluetoothEnabling()
    }

    @Test
    fun enabling_onEnablingFailed() {
        presenter.onBluetoothEnablingFailed()
        verify(view).showBluetoothEnablingFailed()
    }

    @Test
    fun discovery_onMadeDiscoverable() {
        presenter.onMadeDiscoverable()
        verify(view).showDiscoverableProcess()
    }

    @Test
    fun discovery_makeDiscoverable() {
        `when`(model.isDiscoverable()).thenReturn(false)
        presenter.makeDiscoverable()
        verify(view).requestMakingDiscoverable()
    }

    @Test
    fun scanning_cancel() {
        presenter.cancelScanning()
        verify(view).showScanningStopped()
    }

    @Test
    fun scanning_start() {
        presenter.scanForDevices()
        verify(model).scanForDevices(30)
        verify(model).setScanningListener(captor.capture())
        val scanningListener = captor.firstValue
        scanningListener.onDiscoveryStart(0)
        verify(view).showScanningStarted(0)
    }

    @Test
    fun scanning_finished() {
        verify(model).setScanningListener(captor.capture())
        val scanningListener = captor.firstValue
        scanningListener.onDiscoveryFinish()
        verify(view).showScanningStopped()
    }

    @Test
    fun scanning_discoverableStart() {
        verify(model).setScanningListener(captor.capture())
        val scanningListener = captor.firstValue
        scanningListener.onDiscoverableStart()
        verify(view)?.showDiscoverableProcess()
    }

    @Test
    fun scanning_discoverableFinishStart() {
        verify(model).setScanningListener(captor.capture())
        val scanningListener = captor.firstValue
        scanningListener.onDiscoverableFinish()
        verify(view).showDiscoverableFinished()
    }

    @Test
    fun scanning_onFoundDevice() {
        val device = mock(BluetoothDevice::class.java)
        verify(model).setScanningListener(captor.capture())
        val scanningListener = captor.firstValue
        scanningListener.onDeviceFind(device)
        verify(view).addFoundDevice(device)
    }

    @Test
    fun scanning_startAlreadyStarted() {
        `when`(model.isDiscovering()).thenReturn(true)
        presenter.scanForDevices()
        verify(view).showScanningStopped()
    }
}