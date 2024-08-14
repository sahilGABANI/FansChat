package base.util

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import base.R

class SlideAnimationUtil {
    companion object {
        /**
         * Animates a view so that it slides in from the left of it's container.
         *
         * @param context
         * @param view
         */
        fun slideInFromLeft(context: Context, view: View, callback: (isStart: Boolean) -> Unit) {
            return runSimpleAnimation(context, view, R.anim.slide_from_left, callback)
        }

        /**
         * Animates a view so that it slides from its current position, out of view to the left.
         *
         * @param context
         * @param view
         */
        fun slideOutToLeft(context: Context, view: View, callback: (isStart: Boolean) -> Unit) {
            runSimpleAnimation(context, view, R.anim.slide_to_left, callback)
        }

        /**
         * Animates a view so that it slides in the from the right of it's container.
         *
         * @param context
         * @param view
         */
        fun slideInFromRight(context: Context, view: View, callback: (isStart: Boolean) -> Unit) {
            runSimpleAnimation(context, view, R.anim.slide_from_right, callback)
        }

        /**
         * Animates a view so that it slides from its current position, out of view to the right.
         *
         * @param context
         * @param view
         */
        fun slideOutToRight(context: Context, view: View, callback: (isStart: Boolean) -> Unit) {
            runSimpleAnimation(context, view, R.anim.slide_to_right, callback)
        }

        /**
         * Runs a simple animation on a View with no extra parameters.
         *
         * @param context
         * @param view
         * @param animationId
         */
        private fun runSimpleAnimation(
            context: Context,
            view: View,
            animationId: Int,
            callback: (isStart: Boolean) -> Unit
        ) {
            val anim = AnimationUtils.loadAnimation(
                context, animationId
            )
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    callback.invoke(true)
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback.invoke(false)
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
//            anim.interpolator = BounceInterpolator()
//            anim.fillAfter = true
            view.startAnimation(
                anim
            )
        }
    }
}