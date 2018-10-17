package com.glodanif.bluetoothchat.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.utils.getLayoutInflater

class SwitchPreference : FrameLayout {

    private var text = ""

    private lateinit var textLabel: TextView
    private lateinit var switch: Switch

    var listener: ((isChecked: Boolean) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int = 0) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchPreference, defStyleAttr, 0)
        text = typedArray.getString(R.styleable.SwitchPreference_preferenceText) ?: ""
        typedArray.recycle()

        @SuppressLint("InflateParams")
        val view = context.getLayoutInflater().inflate(R.layout.item_switch_setting, null)
        textLabel = view.findViewById(R.id.tv_text)
        switch = view.findViewById(R.id.s_switch)

        view.setOnClickListener {
            switch.isChecked = !switch.isChecked
            listener?.invoke(switch.isChecked)
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            listener?.invoke(isChecked)
        }

        setText(text)

        addView(view)
    }

    fun setText(text: String) {
        this.text = text
        textLabel.text = text
    }

    fun setChecked(isChecked: Boolean) {
        switch.isChecked = isChecked
    }
}
