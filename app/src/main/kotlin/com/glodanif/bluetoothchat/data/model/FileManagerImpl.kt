package com.glodanif.bluetoothchat.data.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.support.annotation.RequiresPermission
import com.glodanif.bluetoothchat.BuildConfig
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.thread
import android.support.v4.content.FileProvider
import org.apache.commons.io.FilenameUtils

class FileManagerImpl(private val context: Context) : FileManager {

    private val handler = Handler()

    override fun extractApkFile(onExtracted: (Uri) -> Unit, onFailed: () -> Unit) {

        val application = context.packageManager
                .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SHARED_LIBRARY_FILES)
        val directory = context.externalCacheDir

        if (application != null) {

            val file = File(application.applicationInfo.publicSourceDir)

            thread {

                try {

                    val newFile = copyAndZip(file, directory, "BluetoothChat")

                    handler.post {

                        val archiveUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            FileProvider.getUriForFile(context,
                                    context.applicationContext.packageName + ".provider", newFile)
                        } else {
                            Uri.fromFile(newFile)
                        }

                        onExtracted.invoke(archiveUri)
                    }
                } catch (e: IOException) {
                    onFailed.invoke()
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

            val origin = BufferedInputStream(it, bufferSize)

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

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun saveFileToDownloads(file: File, onSaved: () -> Unit) {

        val downloads = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val copiedFile = File(downloads, getUniqueFileName(downloads.absolutePath, file.name))

        copiedFile.createNewFile()

        FileInputStream(file).use {
            val sourceChannel = it.channel
            FileOutputStream(copiedFile).use {
                val destinationChannel = it.channel
                destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
            }
        }

        onSaved.invoke()
    }

    private fun getUniqueFileName(directory: String, name: String): String =

            if (File(directory, name).exists()) {
                val fileName = FilenameUtils.removeExtension(name)
                val extension = FilenameUtils.getExtension(name)
                getUniqueFileName(directory, "$fileName (1).$extension")
            } else {
                name
            }
}
