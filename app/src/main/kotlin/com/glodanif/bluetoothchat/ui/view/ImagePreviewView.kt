package com.glodanif.bluetoothchat.ui.view

interface ImagePreviewView {
    fun showFileInfo(name: String, readableSize: String)
    fun displayImage(fileUrl: String)
    fun close()
}
