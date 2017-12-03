package com.glodanif.bluetoothchat.data.model

interface OnFileListener {
    fun onFileSendingStarted(fileAddress: String?, fileSize: Long)
    fun onFileSendingProgress(sentBytes: Long, totalBytes: Long)
    fun onFileSendingFinished()
    fun onFileSendingCanceled()
    fun onFileSendingFailed()
    fun onFileReceivingStarted(fileSize: Long)
    fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long)
    fun onFileReceivingFinished()
    fun onFileReceivingCanceled()
    fun onFileReceivingFailed()
}
