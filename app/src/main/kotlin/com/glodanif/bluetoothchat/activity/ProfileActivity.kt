package com.glodanif.bluetoothchat.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.text.Editable
import android.text.TextWatcher
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

class ProfileActivity : AppCompatActivity(), ProfileView {

    private lateinit var presenter: ProfilePresenter
    private lateinit var settings: SettingsManager

    private lateinit var nameField: EditText
    private lateinit var nameLabel: TextView
    private lateinit var avatar: ImageView
    private lateinit var colorPicker: View

    private var initMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)

        settings = SettingsManagerImpl(this)
        presenter = ProfilePresenter(this, settings)

        colorPicker = findViewById(R.id.v_color)
        nameField = findViewById(R.id.et_name) as EditText
        nameLabel = findViewById(R.id.tv_name) as TextView
        avatar = findViewById(R.id.iv_avatar) as ImageView

        presenter.init()

        nameField.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                presenter.onNameChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        colorPicker.setOnClickListener {
            presenter.prepareColorPicker()
        }

        findViewById(R.id.btn_save).setOnClickListener {
            presenter.saveUser()
        }

        if (initMode) {
            presenter.dispatch()
        }
    }

    override fun displayUserData(name: String, color: Int) {
        val symbol = if (name.isEmpty()) "?" else name[0].toString()
        nameLabel.text = if (name.isEmpty()) "Your name" else name
        val drawable = TextDrawable.builder().buildRound(symbol, color)
        avatar.setImageDrawable(drawable)
        colorPicker.setBackgroundColor(color)
    }

    private val colorSelectListener = object : ColorSelectListener {
        override fun onColorSelected(color: Int) {
            presenter.onColorPicked(color)
        }
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
        ConversationsActivity.start(this)
        finish()
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
