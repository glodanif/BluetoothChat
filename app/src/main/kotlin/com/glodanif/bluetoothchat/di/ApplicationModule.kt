package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.service.connection.ConnectionController
import com.glodanif.bluetoothchat.ui.presenter.*
import org.koin.dsl.module.module

val applicationModule = module {

    factory { params ->
        ChatPresenter(params[0], params[1], get(), get(), get(), get(), get(), get())
    }

    factory { params ->
        ContactChooserPresenter(params[0], get(), get())
    }

    factory { params ->
        ConversationsPresenter(params[0], get(), get(), get(), get(), get())
    }

    factory { params ->
        ImagePreviewPresenter(params[0], params[1], params[2], get())
    }

    factory { params ->
        ProfilePresenter(params[0], params[1], params[1], get(), get(), get(), get(), get())
    }

    factory { params ->
        ReceivedImagesPresenter(params[0], params[1], get())
    }

    factory { params ->
        ScanPresenter(params[0], get(), get(), get(), get())
    }

    factory { params ->
        SettingsPresenter(params[0], get())
    }

    factory { params ->
        ConnectionController(params[0], params[1], get(), get(), get(), get(), get(), get())
    }
}
