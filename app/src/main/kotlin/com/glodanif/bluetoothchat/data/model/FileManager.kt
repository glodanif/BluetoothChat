package com.glodanif.bluetoothchat.data.model

import android.net.Uri
import java.io.File

interface FileManager {
    suspend fun extractApkFile(): Uri?
}
