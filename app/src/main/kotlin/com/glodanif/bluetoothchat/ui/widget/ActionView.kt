package com.glodanif.bluetoothchat.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.glodanif.bluetoothchat.R

class ActionView: FrameLayout {

    @SuppressLint("InflateParams")
    private var container: View =
            LayoutInflater.from(context).inflate(R.layout.view_action, null)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setActions(text: String, firstAction: Action?, secondAction: Action?) {

        removeAllViews()

        val textLabel = container.findViewById<TextView>(R.id.tv_text)
        val firstActionButton = container.findViewById<Button>(R.id.btn_first_action)
        val secondActionButton = container.findViewById<Button>(R.id.btn_second_action)

        textLabel.text = text

        if (firstAction != null) {
            firstActionButton.visibility = View.VISIBLE
            firstActionButton.text = firstAction.text
            firstActionButton.setOnClickListener { firstAction.action.invoke() }
        } else {
            firstActionButton.visibility = View.GONE
        }

        if (secondAction != null) {
            secondActionButton.visibility = View.VISIBLE
            secondActionButton.text = secondAction.text
            secondActionButton.setOnClickListener { secondAction.action.invoke() }
        } else {
            secondActionButton.visibility = View.GONE
        }

        addView(container)
    }

    class Action(var text: String, var action: () -> Unit)
}
