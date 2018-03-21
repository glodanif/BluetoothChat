package com.glodanif.bluetoothchat.ui.util

import android.animation.Animator

abstract class EmptyAnimatorListener : Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator?) {
    }

    override fun onAnimationRepeat(p0: Animator?) {
    }

    override fun onAnimationCancel(p0: Animator?) {
    }
}
