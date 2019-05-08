package com.glodanif.bluetoothchat.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.*
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.presenter.SettingsPresenter
import com.glodanif.bluetoothchat.ui.util.ThemeHolder
import com.glodanif.bluetoothchat.ui.view.SettingsView
import com.glodanif.bluetoothchat.ui.widget.SwitchPreference
import com.glodanif.bluetoothchat.utils.bind
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class SettingsActivity : SkeletonActivity(), SettingsView {

    private val presenter: SettingsPresenter by inject {
        parametersOf(this, application as ThemeHolder)
    }

    private val colorPreview: View by bind(R.id.v_color)
    private val notificationsHeader: TextView by bind(R.id.tv_notifications_header)
    private val nightModeSubtitle: TextView by bind(R.id.tv_night_mode)
    private val soundPreference: SwitchPreference by bind(R.id.sp_sound)
    private val classificationPreference: SwitchPreference by bind(R.id.sp_class_filter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings, ActivityType.CHILD_ACTIVITY)

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

        findViewById<LinearLayout>(R.id.ll_night_mode_button).setOnClickListener {
            presenter.prepareNightModePicker()
        }

        presenter.loadPreferences()
    }

    override fun displayNotificationSetting(sound: Boolean) {
        soundPreference.setChecked(sound)
    }

    override fun displayBgColorSettings(@ColorInt color: Int) {
        colorPreview.setBackgroundColor(color)
    }

    override fun displayNightModeSettings(@NightMode nightMode: Int) {
        val modeLabelText = when (nightMode) {
            MODE_NIGHT_YES -> R.string.settings__night_mode_on
            MODE_NIGHT_NO -> R.string.settings__night_mode_off
            MODE_NIGHT_FOLLOW_SYSTEM -> R.string.settings__night_mode_system
            else -> R.string.settings__night_mode_off
        }
        nightModeSubtitle.setText(modeLabelText)
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

    override fun displayNightModePicker(nightMode: Int) {

        val items = arrayOf<CharSequence>(
                getString(R.string.settings__night_mode_on), getString(R.string.settings__night_mode_off), getString(R.string.settings__night_mode_system)
        )

        val modes = arrayOf(MODE_NIGHT_YES, MODE_NIGHT_NO, MODE_NIGHT_FOLLOW_SYSTEM)

       AlertDialog.Builder(this).apply {
            setSingleChoiceItems(items, modes.indexOf(nightMode)) { dialog, which ->
                presenter.onNewNightModePreference(modes[which])
                dialog.dismiss()
            }
            setNegativeButton(R.string.general__cancel, null)
            setTitle(R.string.settings__night_mode)
        }.show()
    }

    private val colorSelectListener = object : ColorSelectListener {
        override fun onColorSelected(color: Int) {
            presenter.onNewColorPicked(color)
        }
    }

    override fun onBackPressed() {
        if (presenter.isNightModeChanged()) {
            ConversationsActivity.start(this, clearTop = true)
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}
