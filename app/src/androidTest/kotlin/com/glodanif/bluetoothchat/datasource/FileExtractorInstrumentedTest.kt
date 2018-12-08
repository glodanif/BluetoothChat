package com.glodanif.bluetoothchat.datasource

import android.os.Environment
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.model.FileManager
import com.glodanif.bluetoothchat.data.model.FileManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileExtractorInstrumentedTest {

    private lateinit var extractor: FileManager

    private lateinit var apkFile: File
    private lateinit var zipFile: File

    @Before
    fun prepare() {

        val context = InstrumentationRegistry.getTargetContext()
        extractor = FileManagerImpl(context)

        val directory = context.externalCacheDir
                ?: File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name))
        apkFile = File(directory, "BluetoothChat.apk")
        zipFile = File(directory, "BluetoothChat.zip")

        apkFile.delete()
        zipFile.delete()
    }

    @After
    fun release() {
        apkFile.delete()
        zipFile.delete()
    }

    @Test
    fun apkExtraction() = runBlocking {
        val uri = extractor.extractApkFile()
        assertNotNull(uri)
        assertTrue(uri?.lastPathSegment == "BluetoothChat.zip")
        assertTrue(apkFile.exists())
        assertTrue(zipFile.exists())
        assertTrue(apkFile.length() > 2_097_152)
        assertTrue(zipFile.length() > 2_097_152)
    }
}
