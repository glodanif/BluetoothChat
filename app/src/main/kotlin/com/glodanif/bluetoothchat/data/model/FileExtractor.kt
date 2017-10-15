package com.glodanif.bluetoothchat.data.model

import android.net.Uri

interface FileExtractor {
    fun extractFile(onExtracted: (Uri) -> Unit, onFailed: () -> Unit)
}
