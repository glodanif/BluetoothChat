package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.net.Uri
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.presenter.ScanPresenter
import com.glodanif.bluetoothchat.ui.view.ScanView
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.experimental.Dispatchers
import org.junit.Before
import org.junit.Test

class ScanPresenterUnitTest {

    @RelaxedMockK
    private lateinit var scanner: BluetoothScanner
    @RelaxedMockK
    private lateinit var connector: BluetoothConnector
    @RelaxedMockK
    private lateinit var fileModel: FileManager
    @RelaxedMockK
    private lateinit var view: ScanView
    @RelaxedMockK
    private lateinit var preferences: UserPreferences

    private lateinit var presenter: ScanPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ScanPresenter(view, scanner, connector,
                fileModel, preferences, Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @Test
    fun availability_isAvailable() {
        every { scanner.isBluetoothAvailable() } returns true
        presenter.checkBluetoothAvailability()
        verify { view.showBluetoothScanner() }
    }

    @Test
    fun availability_isNotAvailable() {
        every { scanner.isBluetoothAvailable() } returns false
        presenter.checkBluetoothAvailability()
        verify { view.showBluetoothIsNotAvailableMessage() }
    }

    @Test
    fun enabling_isEnabled_isDiscoverable() {
        val device = mockk<BluetoothDevice>()
        every { device.bluetoothClass.majorDeviceClass } returns BluetoothClass.Device.Major.PHONE
        val paired = listOf(device)
        every { scanner.getBondedDevices() } returns paired
        every { scanner.isBluetoothEnabled() } returns true
        every { scanner.isDiscoverable() } returns true
        presenter.checkBluetoothEnabling()
        verify { view.showPairedDevices(paired) }
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun enabling_isEnabled_isNotDiscoverable() {
        val device = mockk<BluetoothDevice>()
        every { device.bluetoothClass.majorDeviceClass } returns BluetoothClass.Device.Major.PHONE
        val paired = listOf(device)
        every { scanner.getBondedDevices() } returns paired
        every { scanner.isBluetoothEnabled() } returns true
        every { scanner.isDiscoverable() } returns false
        presenter.checkBluetoothEnabling()
        verify { view.showPairedDevices(paired) }
        verify { view.showDiscoverableFinished() }
    }

    @Test
    fun enabling_isDisabled() {
        every { scanner.isBluetoothEnabled() } returns false
        presenter.checkBluetoothEnabling()
        verify { view.showBluetoothEnablingRequest() }
    }

    @Test
    fun enabling_turnOn() {
        every { scanner.isBluetoothEnabled() } returns false
        presenter.turnOnBluetooth()
        verify { view.requestBluetoothEnabling() }
    }

    @Test
    fun enabling_onEnablingFailed() {
        presenter.onBluetoothEnablingFailed()
        verify { view.showBluetoothEnablingFailed() }
    }

    @Test
    fun discovery_onMadeDiscoverable() {
        presenter.onMadeDiscoverable()
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun discovery_makeDiscoverable() {
        every { scanner.isDiscoverable() } returns false
        presenter.makeDiscoverable()
        verify { view.requestMakingDiscoverable() }
    }

    @Test
    fun scanning_cancel() {
        presenter.cancelScanning()
        verify { view.showScanningStopped() }
    }

    @Test
    fun scanning_startAlreadyStarted() {
        every { scanner.isDiscovering() } returns true
        presenter.scanForDevices()
        verify { view.showScanningStopped() }
    }


    @Test
    fun apkSharing_success() {
        val uri = mockk<Uri>()
        coEvery { fileModel.extractApkFile() } returns uri
        presenter.shareApk()
        verify { view.shareApk(uri) }
    }

    @Test
    fun apkSharing_failure() {
        coEvery { fileModel.extractApkFile() } returns null
        presenter.shareApk()
        verify { view.showExtractionApkFailureMessage() }
    }

    @Test
    fun scanning_start() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        presenter.scanForDevices()
        verify { scanner.scanForDevices(ScanPresenter.SCAN_DURATION_SECONDS) }
        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDiscoveryStart(0)
        verify { view.showScanningStarted(0) }
    }

    @Test
    fun scanning_finished() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDiscoveryFinish()
        verify { view.showScanningStopped() }
    }

    @Test
    fun scanning_discoverableStart() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDiscoverableStart()
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun scanning_discoverableFinishStart() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDiscoverableFinish()
        verify { view.showDiscoverableFinished() }
    }

    @Test
    fun scanning_onFoundDevice_noClassification() {

        every { preferences.isClassificationEnabled() } returns false

        val slot = slot<BluetoothScanner.ScanningListener>()

        val device = mockk<BluetoothDevice>()
        every {device.bluetoothClass.majorDeviceClass} returns BluetoothClass.Device.Major.WEARABLE

        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDeviceFind(device)
        verify { view.addFoundDevice(device) }
    }

    @Test
    fun scanning_onFoundDevice_withClassification() {

        every { preferences.isClassificationEnabled() } returns true

        val slot = slot<BluetoothScanner.ScanningListener>()

        val device = mockk<BluetoothDevice>()
        every {device.bluetoothClass.majorDeviceClass} returns BluetoothClass.Device.Major.WEARABLE

        verify { scanner.setScanningListener(capture(slot)) }
        slot.captured.onDeviceFind(device)
        verify(exactly = 0) { view.addFoundDevice(device) }
    }

    @Test
    fun devicePick_connected_deviceAvailable() {
        val device = mockk<BluetoothDevice>()
        every { scanner.getDeviceByAddress("any") } returns device
        every { connector.isConnectionPrepared() } returns true
        presenter.onDevicePicked("any")
        verify { connector.connect(device) }
    }

    @Test
    fun devicePick_connected_deviceNotAvailable() {
        every { scanner.getDeviceByAddress("any") } returns null
        every { connector.isConnectionPrepared() } returns true
        presenter.onDevicePicked("any")
        verify { view.showServiceUnavailable() }
    }

    @Test
    fun devicePick_notConnected_connectionError() {
        every { connector.isConnectionPrepared() } returns false
        presenter.onDevicePicked("any")
        val slot = slot<OnPrepareListener>()
        verify { connector.addOnPrepareListener(capture(slot)) }
        slot.captured.onError()
        verify { view.showServiceUnavailable() }
    }

    @Test
    fun devicePick_notConnected_deviceAvailable() {
        val device = mockk<BluetoothDevice>()
        every { scanner.getDeviceByAddress("any") } returns device
        every { connector.isConnectionPrepared() } returns false
        presenter.onDevicePicked("any")
        val slot = slot<OnPrepareListener>()
        verify { connector.addOnPrepareListener(capture(slot)) }
        slot.captured.onPrepared()
        verify { connector.connect(device) }
    }

    @Test
    fun devicePick_notConnected_deviceNotAvailable() {
        every { scanner.getDeviceByAddress("any") } returns null
        every { connector.isConnectionPrepared() } returns false
        presenter.onDevicePicked("any")
        val slot = slot<OnPrepareListener>()
        verify { connector.addOnPrepareListener(capture(slot)) }
        slot.captured.onPrepared()
        verify { view.showServiceUnavailable() }
    }
}
