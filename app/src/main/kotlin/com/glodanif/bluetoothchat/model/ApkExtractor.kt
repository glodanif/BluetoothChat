package com.glodanif.bluetoothchat.model

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import com.glodanif.bluetoothchat.BuildConfig
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.thread

class ApkExtractor(private val context: Context) : FileExtractor {

    private val handler = Handler()

    override fun extractFile(onExtracted: (Uri) -> Unit, onFailed: () -> Unit) {

        val application = context.packageManager
                .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SHARED_LIBRARY_FILES)
        val directory = context.externalCacheDir

        if (application != null) {

            val file = File(application.applicationInfo.publicSourceDir)

            thread {
                val newFile = copyAndZip(file, directory, "BluetoothChat")
                handler.post {
                    val uri = Uri.fromFile(newFile)
                    onExtracted.invoke(uri)
                }
            }

        } else {
            onFailed.invoke()
        }
    }

    private fun copyAndZip(file: File, targetDirectory: File, newName: String): File {

        val bufferSize = 2048

        val copiedFile = File(targetDirectory, "$newName.apk")
        val zipFile = File(targetDirectory, "$newName.zip")

        copiedFile.deleteOnExit()
        copiedFile.createNewFile()
        zipFile.deleteOnExit()

        FileInputStream(file).use {
            val sourceChannel = it.channel
            FileOutputStream(copiedFile).use {
                val destinationChannel = it.channel
                destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
            }
        }

        FileInputStream(copiedFile).use {
            val origin: BufferedInputStream = BufferedInputStream(it, bufferSize)
            origin.use {
                FileOutputStream(zipFile).use {
                    ZipOutputStream(BufferedOutputStream(it)).use {

                        val data = ByteArray(bufferSize)
                        val entry = ZipEntry(copiedFile.name)
                        it.putNextEntry(entry)

                        var count = origin.read(data, 0, bufferSize)
                        while (count != -1) {
                            it.write(data, 0, count)
                            count = origin.read(data, 0, bufferSize)
                        }
                    }
                }
            }
        }

        return zipFile
    }
}
