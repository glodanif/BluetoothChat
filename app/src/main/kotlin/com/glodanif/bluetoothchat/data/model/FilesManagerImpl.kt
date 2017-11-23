package com.glodanif.bluetoothchat.data.model

import android.content.Context
import java.io.*

class FilesManagerImpl(private val context: Context) : FilesManager {

    override fun saveFile(stream: InputStream, name: String) {

        val file = File(context.filesDir, name)

        try {

            FileOutputStream(file).use({

                if (!file.exists()) {
                    file.createNewFile()
                }

                val bufferSize = 1024
                val buffer = CharArray(bufferSize)
                val inReader = InputStreamReader(stream, "UTF-8")
                while (true) {
                    val rsz = inReader.read(buffer, 0, buffer.size)
                    if (rsz < 0)
                        break
                    it.write(rsz)
                }

                it.flush()
                it.close()
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getFileByName(name: String) {
    }
}
