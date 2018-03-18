package com.glodanif.bluetoothchat.di

import android.content.Context
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.di.component.*
import com.glodanif.bluetoothchat.di.module.*
import com.glodanif.bluetoothchat.ui.activity.*

class ComponentsManager {

    companion object {

        private lateinit var dsComponent: ApplicationComponent

        fun getDataSourceComponent(): ApplicationComponent = dsComponent

        fun initialize(context: Context) {
            dsComponent = DaggerApplicationComponent.builder()
                    .applicationModule(ApplicationModule(context))
                    .build()
        }

        fun injectConversations(activity: ConversationsActivity) {
            DaggerConversationsComponent.builder().applicationComponent(dsComponent)
                    .conversationsModule(ConversationsModule(activity))
                    .build()
                    .inject(activity)
        }

        fun injectChat(activity: ChatActivity, address: String) {
            DaggerChatComponent.builder().applicationComponent(dsComponent)
                    .chatModule(ChatModule(address, activity))
                    .build()
                    .inject(activity)
        }

        fun injectProfile(activity: ProfileActivity) {
            DaggerProfileComponent.builder().applicationComponent(dsComponent)
                    .profileModule(ProfileModule(activity))
                    .build()
                    .inject(activity)
        }

        fun injectReceivedImages(activity: ReceivedImagesActivity, address: String?) {
            DaggerReceivedImagesComponent.builder().applicationComponent(dsComponent)
                    .receivedImagesModule(ReceivedImagesModule(address, activity))
                    .build()
                    .inject(activity)
        }

        fun injectImagePreview(activity: ImagePreviewActivity, message: ChatMessage) {
            DaggerImagePreviewComponent.builder().applicationComponent(dsComponent)
                    .imagePreviewModule(ImagePreviewModule(message, activity))
                    .build()
                    .inject(activity)
        }

        fun injectContactChooser(activity: ContactChooserActivity) {
            DaggerContactChooserComponent.builder().applicationComponent(dsComponent)
                    .contactChooserModule(ContactChooserModule(activity))
                    .build()
                    .inject(activity)
        }
    }
}
