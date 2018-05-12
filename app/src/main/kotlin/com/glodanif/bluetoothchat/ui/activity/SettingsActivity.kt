package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.presenter.SettingsPresenter
import com.glodanif.bluetoothchat.ui.view.SettingsView
import com.glodanif.bluetoothchat.ui.widget.SwitchPreference
import com.glodanif.bluetoothchat.utils.bind
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import javax.inject.Inject

class SettingsActivity : SkeletonActivity(), SettingsView {

    @Inject
    internal lateinit var presenter: SettingsPresenter

    private val colorPreview: View by bind(R.id.v_color)
    private val notificationsHeader: TextView by bind(R.id.tv_notifications_header)
    private val soundPreference: SwitchPreference by bind(R.id.sp_sound)
    private val classificationPreference: SwitchPreference by bind(R.id.sp_class_filter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings, ActivityType.CHILD_ACTIVITY)
        ComponentsManager.injectSettings(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationsHeader.visibility = View.GONE
            soundPreference.visibility = View.GONE
        } else {
            soundPreference.listener = { presenter.onNewSoundPreference(it) }
        }

        classificationPreference.listener = { presenter.onNewClassificationPreference(it) }

        findViewById<RelativeLayout>(R.id.rl_chat_bg_color_button).setOnClickListener {
            presenter.prepareColorPicker()
        }

        presenter.loadPreferences()
    }

    override fun displayNotificationSetting(sound: Boolean) {
        soundPreference.setChecked(sound)
    }

    override fun displayAppearanceSettings(@ColorInt color: Int) {
        colorPreview.setBackgroundColor(color)
    }

    override fun displayDiscoverySetting(classification: Boolean) {
        classificationPreference.setChecked(classification)
    }

    override fun displayColorPicker(@ColorInt color: Int) {

        ChromaDialog.Builder()
                .initialColor(color)
                .colorMode(ColorMode.RGB)
                .onColorSelected(colorSelectListener)
                .create()
                .show(supportFragmentManager, "ChromaDialog")
    }

    private val colorSelectListener = object : ColorSelectListener {
        override fun onColorSelected(color: Int) {
            presenter.onNewColorPicked(color)
        }
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}
