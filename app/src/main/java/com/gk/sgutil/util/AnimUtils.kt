package com.gk.sgutil.util

import android.view.animation.RotateAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator


/**
 *
 */
class AnimUtils {
    companion object {

        fun createRotateAnimation(from: Float, to: Float, duration: Long) : RotateAnimation {
            val anim = RotateAnimation(from, to,
                    Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f)
            anim.interpolator = DecelerateInterpolator()
            anim.duration = duration
            anim.fillAfter = true
            return anim
        }
    }
}
