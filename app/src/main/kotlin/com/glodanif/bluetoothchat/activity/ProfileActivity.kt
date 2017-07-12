package com.glodanif.bluetoothchat.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.model.SettingsManagerImpl
import com.glodanif.bluetoothchat.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.view.ProfileView
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.util.SimpleTextWatcher

class ProfileActivity : AppCompatActivity(), ProfileView {

    private lateinit var presenter: ProfilePresenter
    private lateinit var settings: SettingsManager

    private lateinit var nameField: EditText
    private lateinit var nameLabel: TextView
    private lateinit var avatar: ImageView
    private lateinit var colorPicker: View

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        editMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)

        val toolbar = findViewById(R.id.tb_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(editMode)
        supportActionBar?.setDisplayShowHomeEnabled(editMode)
        if (editMode) {
            title = "Profile"
        }

        settings = SettingsManagerImpl(this)
        presenter = ProfilePresenter(this, settings)

        colorPicker = findViewById(R.id.v_color)
        nameField = findViewById(R.id.et_name) as EditText
        nameLabel = findViewById(R.id.tv_name) as TextView
        avatar = findViewById(R.id.iv_avatar) as ImageView

        colorPicker.setOnClickListener {
            presenter.prepareColorPicker()
        }

        findViewById(R.id.btn_save).setOnClickListener {
            presenter.saveUser()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.loadSavedUser()
        nameField.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()
        nameField.removeTextChangedListener(textWatcher)
    }

    override fun showUserData(name: String, color: Int) {
        val symbol = if (name.isEmpty()) "?" else name[0].toString().toUpperCase()
        nameLabel.text = if (name.isEmpty()) "Your name" else name
        nameLabel.setTextColor(resources.getColor(
                if (name.isEmpty()) R.color.text_light else R.color.text_dark))
        val drawable = TextDrawable.builder().buildRound(symbol, color)
        avatar.setImageDrawable(drawable)
        colorPicker.setBackgroundColor(color)
    }

    override fun showColorPicker(@ColorInt color: Int) {
        ChromaDialog.Builder()
                .initialColor(color)
                .colorMode(ColorMode.RGB)
                .onColorSelected(colorSelectListener)
                .create()
                .show(supportFragmentManager, "ChromaDialog");
    }

    override fun redirectToConversations() {
        if (!editMode) {
            ConversationsActivity.start(this)
        }
        finish()
    }

    override fun prefillUsername(name: String) {
        nameField.setText(name)
    }

    override fun showNotValidNameError() {
        nameField.error = "Your name cannot be empty or longer than 25 characters, also, \'#\' symbol is not allowed"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return onOptionsItemSelected(item)
        }
    }

    companion object {

        val EXTRA_EDIT_MODE = "extra.edit_mode"

        fun start(context: Context, editMode: Boolean) {
            val intent = Intent(context, ProfileActivity::class.java)
                    .putExtra(EXTRA_EDIT_MODE, editMode)
            context.startActivity(intent)
        }
    }
}
