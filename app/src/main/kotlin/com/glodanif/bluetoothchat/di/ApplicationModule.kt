package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.data.service.connection.ConnectionController
import com.glodanif.bluetoothchat.di.Params.ADDRESS
import com.glodanif.bluetoothchat.di.Params.CHAT_VIEW
import com.glodanif.bluetoothchat.di.Params.CONNECTION_SUBJECT
import com.glodanif.bluetoothchat.di.Params.CONTACT_CHOOSER_VIEW
import com.glodanif.bluetoothchat.di.Params.CONVERSATIONS_VIEW
import com.glodanif.bluetoothchat.di.Params.FILE
import com.glodanif.bluetoothchat.di.Params.IMAGE_PREVIEW_VIEW
import com.glodanif.bluetoothchat.di.Params.MESSAGE_ID
import com.glodanif.bluetoothchat.di.Params.PROFILE_VIEW
import com.glodanif.bluetoothchat.di.Params.RECEIVED_IMAGES_VIEW
import com.glodanif.bluetoothchat.di.Params.SCAN_VIEW
import com.glodanif.bluetoothchat.di.Params.SETTINGS_VIEW
import com.glodanif.bluetoothchat.ui.presenter.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val applicationModule = applicationContext {

    factory { params ->
        ChatPresenter(params[ADDRESS], params[CHAT_VIEW], get(), get(), get(), get(), get(), get())
    }

    factory { params ->
        ContactChooserPresenter(params[CONTACT_CHOOSER_VIEW], get(), get())
    }

    factory { params ->
        ConversationsPresenter(params[CONVERSATIONS_VIEW], get(), get(), get(), get(), get())
    }

    factory { params ->
        ImagePreviewPresenter(params[MESSAGE_ID], params[FILE], params[IMAGE_PREVIEW_VIEW], get())
    }

    factory { params ->
        ProfilePresenter(params[PROFILE_VIEW], get(), get())
    }

    factory { params ->
        ReceivedImagesPresenter(params[ADDRESS], params[RECEIVED_IMAGES_VIEW], get())
    }

    factory { params ->
        ScanPresenter(params[SCAN_VIEW], get(), get(), get(), get())
    }

    factory { params ->
        SettingsPresenter(params[SETTINGS_VIEW], get())
    }

    factory { params ->
        ConnectionController(androidApplication() as ChatApplication, params[CONNECTION_SUBJECT], get(), get(), get(), get(), get(), get())
    }
}

object Params {
    const val CHAT_VIEW = "chatView"
    const val CONTACT_CHOOSER_VIEW = "contactChooserView"
    const val CONVERSATIONS_VIEW = "conversationsView"
    const val IMAGE_PREVIEW_VIEW = "imagePreviewView"
    const val PROFILE_VIEW = "profileView"
    const val RECEIVED_IMAGES_VIEW = "receivedImagesView"
    const val SCAN_VIEW = "scanView"
    const val SETTINGS_VIEW = "settingsView"
    const val CONNECTION_SUBJECT = "connectionSubject"
    const val ADDRESS = "address"
    const val MESSAGE_ID = "messageId"
    const val FILE = "file"
}
