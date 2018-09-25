package com.glodanif.bluetoothchat.ui.util

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.widget.GoDownButton

class ScrollAwareBehavior(val context: Context) : CoordinatorLayout.Behavior<GoDownButton>() {

    private var childView: GoDownButton? = null

    private var isAnimationRunning = false
    private var isOpened = false

    private var scrollRange = 0

    var onHideListener: (() -> Unit)? = null

    private val openAnimation: Animation by lazy {

        AnimationUtils.loadAnimation(context, R.anim.switcher_scale_in).apply {

            setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
                    isAnimationRunning = true
                    childView?.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {
                    isAnimationRunning = false
                    isOpened = true
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
    }

    private val closeAnimation: Animation by lazy {

        AnimationUtils.loadAnimation(context, R.anim.switcher_scale_out).apply {

            setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
                    isAnimationRunning = true
                }

                override fun onAnimationEnd(animation: Animation) {
                    isAnimationRunning = false
                    isOpened = false
                    childView?.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
    }

    fun isChildShown() = isOpened

    fun hideChild() {
        childView?.startAnimation(closeAnimation)
        scrollRange = 0
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: GoDownButton, layoutDirection: Int): Boolean {
        childView = child
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: GoDownButton, directTargetChild: View,
                                     target: View, nestedScrollAxes: Int, @ViewCompat.NestedScrollType type: Int) = true

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: GoDownButton, target: View,
                                dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @ViewCompat.NestedScrollType type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)

        scrollRange += -dyConsumed
        val showLimit = coordinatorLayout.height * 1.5

        if (!isAnimationRunning) {

            if (scrollRange > showLimit && !isOpened) {
                childView?.startAnimation(openAnimation)
            } else if (scrollRange < showLimit && isOpened) {
                childView?.startAnimation(closeAnimation)
                onHideListener?.invoke()
            }
        }
    }
}
