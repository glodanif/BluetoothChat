package com.glodanif.bluetoothchat.data.model

import android.net.Uri
import java.io.File
import java.io.InputStream

interface FilesManager {
    fun saveFile(stream: InputStream, name: String, size: Long)
    fun getFileByName(name: String)
}
