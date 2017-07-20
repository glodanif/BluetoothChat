package com.glodanif.bluetoothchat.model

import android.net.Uri

interface FileExtractor {
    fun extractFile(onExtracted: (Uri) -> Unit, onFailed: () -> Unit)
}
