package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.adapter.ImagesAdapter
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import com.glodanif.bluetoothchat.utils.bind
import javax.inject.Inject

class AboutActivity : SkeletonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about, ActivityType.CHILD_ACTIVITY)


    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}
