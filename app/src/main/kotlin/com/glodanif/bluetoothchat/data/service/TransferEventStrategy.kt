package com.glodanif.bluetoothchat.data.service

import com.glodanif.bluetoothchat.utils.isNumber

class TransferEventStrategy : DataTransferThread.EventsStrategy {

    private val regex = Regex("\\d+#\\d+#\\d+#*")

    override fun isMessage(message: String?) =
            message != null && regex.containsMatchIn(message)

    override fun isFileStart(message: String?): DataTransferThread.FileInfo? {

        if (message != null && message.contains("6#0#0#")) {
            val info = message.replace("6#0#0#", "")
            return if (info.isEmpty()) {
                null
            } else {
                val size = info.substringAfter("#").substringBefore("#")
                if (size.isNumber()) {
                    DataTransferThread.FileInfo(
                            info.substringBefore("#"),
                            size.toLong()
                    )
                } else {
                    null
                }
            }
        }

        return null
    }

    override fun isFileCanceled(message: String?): DataTransferThread.CancelInfo? =

            if (message != null && (message.contains("8#0#0#") || message.contains("8#0#1#"))) {
                val byPartner = message
                        .substringAfter("8#0#")
                        .replace("8#0#", "")
                        .substringBefore("#")
                DataTransferThread.CancelInfo(byPartner == "1")
            } else {
                null
            }

    override fun isFileFinish(message: String?) =
            message != null && message.contains("7#0#0#")
}
