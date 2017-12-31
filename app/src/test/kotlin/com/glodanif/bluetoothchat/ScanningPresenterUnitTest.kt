package com.glodanif.bluetoothchat

import android.bluetooth.BluetoothDevice
import android.net.Uri
import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.BluetoothScanner.ScanningListener
import com.glodanif.bluetoothchat.data.model.FileManager
import com.glodanif.bluetoothchat.ui.presenter.ScanPresenter
import com.glodanif.bluetoothchat.ui.view.ScanView
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ScanningPresenterUnitTest {

    @JvmField
    @Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var scannerModel: BluetoothScanner
    @Mock
    private lateinit var connectionModel: BluetoothConnector
    @Mock
    private lateinit var fileModel: FileManager
    @Mock
    private lateinit var view: ScanView
    @Mock
    private lateinit var listener: ScanningListener

    private val scanCaptor: KArgumentCaptor<ScanningListener> = argumentCaptor()
    private val extractedCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()
    private val failedCaptor: KArgumentCaptor<() -> Unit> = argumentCaptor()

    lateinit var presenter: ScanPresenter

    @Before
    fun setup() {
        presenter = ScanPresenter(view, scannerModel, connectionModel, fileModel)
    }

    @Test
    fun availability_isAvailable() {
        `when`(scannerModel.isBluetoothAvailable()).thenReturn(true)
        presenter.checkBluetoothAvailability()
        verify(view).showBluetoothScanner()
    }

    @Test
    fun availability_isNotAvailable() {
        `when`(scannerModel.isBluetoothAvailable()).thenReturn(false)
        presenter.checkBluetoothAvailability()
        verify(view).showBluetoothIsNotAvailableMessage()
    }

    @Test
    fun enabling_isEnabled_isDiscoverable() {
        `when`(scannerModel.isBluetoothEnabled()).thenReturn(true)
        `when`(scannerModel.isDiscoverable()).thenReturn(true)
        presenter.checkBluetoothEnabling()
        verify(view).showPairedDevices(ArrayList<BluetoothDevice>())
        verify(view).showDiscoverableProcess()
    }

    @Test
    fun enabling_isEnabled_isNotDiscoverable() {
        `when`(scannerModel.isBluetoothEnabled()).thenReturn(true)
        `when`(scannerModel.isDiscoverable()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        verify(view).showPairedDevices(ArrayList<BluetoothDevice>())
        verify(view).showDiscoverableFinished()
    }

    @Test
    fun enabling_isDisabled() {
        `when`(scannerModel.isBluetoothEnabled()).thenReturn(false)
        presenter.checkBluetoothEnabling()
        verify(view).showBluetoothEnablingRequest()
    }

    @Test
    fun enabling_turnOn() {
        `when`(scannerModel.isBluetoothEnabled()).thenReturn(false)
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
        `when`(scannerModel.isDiscoverable()).thenReturn(false)
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
        verify(scannerModel).scanForDevices(30)
        verify(scannerModel).setScanningListener(scanCaptor.capture())
        val scanningListener = scanCaptor.firstValue
        scanningListener.onDiscoveryStart(0)
        verify(view).showScanningStarted(0)
    }

    @Test
    fun scanning_finished() {
        verify(scannerModel).setScanningListener(scanCaptor.capture())
        val scanningListener = scanCaptor.firstValue
        scanningListener.onDiscoveryFinish()
        verify(view).showScanningStopped()
    }

    @Test
    fun scanning_discoverableStart() {
        verify(scannerModel).setScanningListener(scanCaptor.capture())
        val scanningListener = scanCaptor.firstValue
        scanningListener.onDiscoverableStart()
        verify(view)?.showDiscoverableProcess()
    }

    @Test
    fun scanning_discoverableFinishStart() {
        verify(scannerModel).setScanningListener(scanCaptor.capture())
        val scanningListener = scanCaptor.firstValue
        scanningListener.onDiscoverableFinish()
        verify(view).showDiscoverableFinished()
    }

    @Test
    fun scanning_onFoundDevice() {
        val device = mock(BluetoothDevice::class.java)
        verify(scannerModel).setScanningListener(scanCaptor.capture())
        val scanningListener = scanCaptor.firstValue
        scanningListener.onDeviceFind(device)
        verify(view).addFoundDevice(device)
    }

    @Test
    fun scanning_startAlreadyStarted() {
        `when`(scannerModel.isDiscovering()).thenReturn(true)
        presenter.scanForDevices()
        verify(view).showScanningStopped()
    }

    /*@Test
    fun apkSharing_success() {

        verify(fileModel).extractApkFile(extractedCaptor.capture(), failedCaptor.capture())
        extractedCaptor.firstValue.invoke(Uri.EMPTY)
        //verify(view).shareApk(Uri.EMPTY)
    }*/

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}