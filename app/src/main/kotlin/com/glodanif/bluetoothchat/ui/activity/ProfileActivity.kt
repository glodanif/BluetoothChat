package com.glodanif.bluetoothchat.ui.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.ui.util.SimpleTextWatcher
import com.glodanif.bluetoothchat.ui.view.ProfileView
import com.glodanif.bluetoothchat.utils.argument
import com.glodanif.bluetoothchat.utils.bind
import com.glodanif.bluetoothchat.utils.getFirstLetter
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ProfileActivity : SkeletonActivity(), ProfileView {

    private val editMode: Boolean by argument(EXTRA_EDIT_MODE, false)

    private val presenter: ProfilePresenter by inject { parametersOf(this) }

    private val nameField: EditText by bind(R.id.et_name)
    private val nameLabel: TextView by bind(R.id.tv_name)
    private val deviceNameLabel: TextView by bind(R.id.tv_device_name)
    private val avatar: ImageView by bind(R.id.iv_avatar)
    private val colorPicker: View by bind(R.id.v_color)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile, ActivityType.CHILD_ACTIVITY)
        lifecycle.addObserver(presenter)

        supportActionBar?.setDisplayHomeAsUpEnabled(editMode)
        supportActionBar?.setDisplayShowHomeEnabled(editMode)

        if (editMode) {
            title = getString(R.string.profile__profile)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }

        colorPicker.setOnClickListener {
            presenter.prepareColorPicker()
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            presenter.saveUser()
        }

        deviceNameLabel.setOnClickListener {
            val bluetoothSettings = Intent().apply {
                action = Settings.ACTION_BLUETOOTH_SETTINGS
            }
            try {
                startActivity(bluetoothSettings)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@ProfileActivity, getString(R.string.profile__no_bluetooth_settings_activity), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        nameField.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()
        nameField.removeTextChangedListener(textWatcher)
        hideKeyboard()
    }

    override fun showDeviceName(name: String?) {
        deviceNameLabel.text = name ?: getString(R.string.profile__no_name)
    }

    override fun showUserData(name: String, color: Int) {
        nameLabel.text = if (name.isEmpty()) getString(R.string.profile__your_name) else name
        nameLabel.setTextColor(ContextCompat.getColor(this,
                if (name.isEmpty()) R.color.text_light else R.color.text_dark))
        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)
        avatar.setImageDrawable(drawable)
        colorPicker.setBackgroundColor(color)
    }

    override fun showColorPicker(@ColorInt color: Int) {

        ChromaDialog.Builder()
                .initialColor(color)
                .colorMode(ColorMode.RGB)
                .onColorSelected(colorSelectListener)
                .create()
                .show(supportFragmentManager, "ChromaDialog")
    }

    override fun redirectToConversations() {

        if (!editMode) {

            val intent = Intent(this, ConversationsActivity::class.java)
            if (getIntent().action == Intent.ACTION_SEND) {
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT))
            }
            startActivity(intent)
        }
        finish()
    }

    override fun prefillUsername(name: String) {
        nameField.setText(name)
    }

    override fun showNotValidNameError(divider: String) {
        nameField.error = getString(R.string.profile__validation_error, divider)
    }

    private val textWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(text: String) {
            nameField.error = null
            presenter.onNameChanged(text)
        }
    }

    private val colorSelectListener = object : ColorSelectListener {
        override fun onColorSelected(color: Int) {
            presenter.onColorPicked(color)
        }
    }

    companion object {

        private const val EXTRA_EDIT_MODE = "extra.edit_mode"

        fun start(context: Context, editMode: Boolean) {
            val intent = Intent(context, ProfileActivity::class.java)
                    .putExtra(EXTRA_EDIT_MODE, editMode)
            context.startActivity(intent)
        }
    }
}
