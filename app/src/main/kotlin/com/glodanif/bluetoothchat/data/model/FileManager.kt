package com.glodanif.bluetoothchat.data.model

import android.net.Uri
import java.io.File

interface FileManager {
    fun extractApkFile(onExtracted: (Uri) -> Unit, onFailed: () -> Unit)
    fun saveFileToDownloads(file: File, onSaved: () -> Unit)
}
