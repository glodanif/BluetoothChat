package com.glodanif.bluetoothchat.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.glodanif.bluetoothchat.R

class ActionView : FrameLayout {

    @SuppressLint("InflateParams")
    private var container: View =
            LayoutInflater.from(context).inflate(R.layout.view_action, null)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setActions(textMessage: String, firstAction: Action?, secondAction: Action?) {

        removeAllViews()

        container.findViewById<TextView>(R.id.tv_text).text = Html.fromHtml(textMessage)

        container.findViewById<Button>(R.id.btn_first_action).let {

            if (firstAction != null) {
                it.visibility = View.VISIBLE
                it.text = firstAction.text
                it.setOnClickListener { firstAction.action.invoke() }
            } else {
                it.visibility = View.GONE
            }
        }

        container.findViewById<Button>(R.id.btn_second_action).let {

            if (secondAction != null) {
                it.visibility = View.VISIBLE
                it.text = secondAction.text
                it.setOnClickListener { secondAction.action.invoke() }
            } else {
                it.visibility = View.GONE
            }
        }

        addView(container)
    }

    fun setActionsAndShow(textMessage: String, firstAction: Action?, secondAction: Action?) {
        setActions(textMessage, firstAction, secondAction)
        visibility = View.VISIBLE
    }

    class Action(var text: String, var action: () -> Unit)
}
