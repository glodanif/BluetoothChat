package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.util.Log
import java.io.*

class FilesManagerImpl(private val context: Context) : FilesManager {

    val TAG = "TAG14"

    override fun saveFile(stream: InputStream, name: String, size: Long) {

        val file = File(context.filesDir, name)

        val bis = BufferedInputStream(stream)
        val bos = BufferedOutputStream(FileOutputStream(file))

        try {

            var bytesRead: Long = 0
            var len = 0
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var timeOut = 0
            val maxTimeOut = 16

            while (bytesRead < size) {
                Log.w(TAG, "BEFORE AVAILABLE " + bytesRead)
                while (bis.available() == 0 && timeOut < maxTimeOut) {
                    timeOut++
                    Thread.sleep(250)
                }

                val remainingSize = size - bytesRead
                val byteCount = Math.min(remainingSize, bufSize.toLong()).toInt()
                Log.w(TAG, "BEFORE READ " + "currentSize : "
                        + bytesRead + " byteCount " + byteCount)

                len = bis.read(buffer, 0, byteCount)

                Log.w(TAG, "AFTER READ " + "Len " + len)
                if (len > 0) {
                    timeOut = 0
                    Log.w(TAG, "BEFORE WRITE " + bytesRead)
                    bos.write(buffer, 0, len)
                    bytesRead += len.toLong()
                    Log.w(TAG, "AFTER WRITE " + bytesRead)
                }
            }
            bos.flush()

        } catch (e: Exception) {
            Log.e(TAG, "Receiving problem")
            throw e
        } finally {
            try {
                Log.i(TAG, "FILE CLOSE")
                bos.close()
            } catch (e: IOException) {
            }
        }
    }

    override fun getFileByName(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
