package com.glodanif.bluetoothchat.data.model

import android.net.Uri

interface FileManager {
    suspend fun extractApkFile(): Uri?
}
