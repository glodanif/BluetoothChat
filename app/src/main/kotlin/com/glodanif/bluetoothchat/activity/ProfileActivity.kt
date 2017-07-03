package com.glodanif.bluetoothchat.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.ColorInt
import android.text.Editable
import android.text.TextWatcher
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

class ProfileActivity : AppCompatActivity(), ProfileView {

    private lateinit var presenter: ProfilePresenter
    private val settings = SettingsManagerImpl(this)

    private lateinit var nameField: EditText
    private lateinit var nameLabel: TextView
    private lateinit var avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, settings)

        nameField.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                nameLabel.text = if (s.isNullOrEmpty()) "Your Name" else s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        findViewById(R.id.v_color).setOnClickListener {
            presenter.prepareColorPicker()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun displayUserData(name: String, color: Int) {
        nameLabel.text = name
        val drawable = TextDrawable.builder().buildRect(name[0].toString(), color)
        avatar.setImageDrawable(drawable)
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
}
