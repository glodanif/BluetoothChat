package com.glodanif.bluetoothchat.data.service

import com.crashlytics.android.Crashlytics
import com.glodanif.bluetoothchat.utils.isNumber
import io.fabric.sdk.android.Fabric

class TransferEventStrategy : DataTransferThread.EventsStrategy {

    private val generalMessageRegex = Regex("\\d+#\\d+#\\d+#*")
    private val fileStartRegex = Regex("6#\\d+#0#*")

    override fun isMessage(message: String?) = message != null && generalMessageRegex.containsMatchIn(message)

    override fun isFileStart(message: String?): DataTransferThread.FileInfo? =

            if (message != null && fileStartRegex.containsMatchIn(message)) {

                val messageBody = "6#" + message.substringAfter("6#")

                val info = fileStartRegex.replace(messageBody, "")
                val uid = try {
                    messageBody.substring(2).substringBefore("#").toLong()
                } catch (e: NumberFormatException) {

                    //TODO: remove if crash doesn't appear anymore
                    val exception = NumberFormatException(e.message + " | " + fileStartRegex.find(messageBody)?.value)

                    if (Fabric.isInitialized()) {
                        Crashlytics.getInstance().core.logException(exception)
                    }

                    throw exception
                }

                if (info.isEmpty()) {
                    null
                } else {
                    val size = info.substringAfter("#").substringBefore("#")
                    if (size.isNumber()) {
                        DataTransferThread.FileInfo(
                                uid,
                                info.substringBefore("#"),
                                size.toLong()
                        )
                    } else {
                        null
                    }
                }
            } else {
                null
            }

    override fun isFileCanceled(message: String?) =

            if (message != null && (message.contains("8#0#0#") || message.contains("8#0#1#"))) {
                val byPartner = message
                        .substringAfter("8#0#")
                        .replace("8#0#", "")
                        .substringBefore("#")
                DataTransferThread.CancelInfo(byPartner == "1")
            } else {
                null
            }

    override fun isFileFinish(message: String?) = message != null && message.contains("7#0#0#")

    override fun getCancellationMessage(byPartner: Boolean) = "8#0#${if (byPartner) "1" else "0"}#"
}
