package com.glodanif.bluetoothchat.di

import android.content.Context
import com.glodanif.bluetoothchat.di.component.*
import com.glodanif.bluetoothchat.di.module.*
import com.glodanif.bluetoothchat.ui.activity.*
import java.io.File

class ComponentsManager {

    companion object {

        private lateinit var appComponent: ApplicationComponent

        fun initialize(context: Context) {
            appComponent = DaggerApplicationComponent.builder()
                    .applicationModule(ApplicationModule(context))
                    .build()
        }

        fun injectConversations(activity: ConversationsActivity) {
            DaggerConversationsComponent.builder()
                    .applicationComponent(appComponent)
                    .conversationsModule(ConversationsModule(activity))
                    .build()
                    .inject(activity)
        }

        fun injectChat(activity: ChatActivity, address: String) {
            DaggerChatComponent.builder()
                    .applicationComponent(appComponent)
                    .chatModule(ChatModule(address, activity))
                    .build()
                    .inject(activity)
        }

        fun injectProfile(activity: ProfileActivity) {
            DaggerProfileComponent.builder()
                    .applicationComponent(appComponent)
                    .profileModule(ProfileModule(activity))
                    .build()
                    .inject(activity)
        }

        fun injectReceivedImages(activity: ReceivedImagesActivity, address: String?) {
            DaggerReceivedImagesComponent.builder()
                    .applicationComponent(appComponent)
                    .receivedImagesModule(ReceivedImagesModule(address, activity))
                    .build()
                    .inject(activity)
        }

        fun injectImagePreview(activity: ImagePreviewActivity, messageId: Long, image: File) {
            DaggerImagePreviewComponent.builder()
                    .applicationComponent(appComponent)
                    .imagePreviewModule(ImagePreviewModule(messageId, image, activity))
                    .build()
                    .inject(activity)
        }

        fun injectContactChooser(activity: ContactChooserActivity) {
            DaggerContactChooserComponent.builder()
                    .applicationComponent(appComponent)
                    .contactChooserModule(ContactChooserModule(activity))
                    .build()
                    .inject(activity)
        }
    }
}
