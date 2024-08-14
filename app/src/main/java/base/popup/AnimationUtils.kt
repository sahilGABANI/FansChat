package base.popup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.PathInterpolator

object AnimationUtils {

    /**
     * Fade Animation
     *
     * @param view      View to be animated
     * @param fromAlpha initial alpha
     * @param toAlpha   final alpha
     * @param duration  animation duration in milliseconds
     * @return Animator Object
     */
    fun fade(view: View, fromAlpha: Float, toAlpha: Float, duration: Int): Animator {
        val animator = ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha)
        animator.duration = duration.toLong()
        return animator
    }

    /**
     * Circular Reveal Animation
     *
     * @param view        View to be animated
     * @param cx          x coordinate of the center of the circle
     * @param cy          y coordinate of the center of the circle
     * @param startRadius initial circle radius
     * @param finalRadius final circle radius
     * @param duration    animation duration in milliseconds
     * @return Animator Object
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun reveal(view: View, cx: Int, cy: Int, startRadius: Int, finalRadius: Int, duration: Int): Animator {
        val animator =
            ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius.toFloat(), finalRadius.toFloat())
        animator.duration = duration.toLong()
        return animator
    }

    /**
     * Animator to animate Y scale of the view. X scale is constant
     *
     * @param view      View to be animated
     * @param pivotX    x coordinate of the pivot
     * @param pivotY    y coordinate of the pivot
     * @param fromScale initial scale
     * @param toScale   final scale
     * @param duration  animation duration in milliseconds
     * @return Animator Object
     */
    fun scaleY(view: View, pivotX: Int, pivotY: Int, fromScale: Float, toScale: Float, duration: Int): Animator {
        view.pivotX = pivotX.toFloat()
        view.pivotY = pivotY.toFloat()
        val animator: Animator = ObjectAnimator.ofFloat(view, "scaleY", fromScale, toScale)
        animator.duration = duration.toLong()
        return animator
    }

    /**
     * Animator to animate X scale of the view. Y scale is constant
     *
     * @param view      View to be animated
     * @param pivotX    x coordinate of the pivot
     * @param pivotY    y coordinate of the pivot
     * @param fromScale initial scale
     * @param toScale   final scale
     * @param duration  animation duration in milliseconds
     * @return Animator Object
     */
    fun scaleX(view: View, pivotX: Int, pivotY: Int, fromScale: Float, toScale: Float, duration: Int): Animator {
        view.pivotX = pivotX.toFloat()
        view.pivotY = pivotY.toFloat()
        val animator: Animator = ObjectAnimator.ofFloat(view, "scaleX", fromScale, toScale)
        animator.duration = duration.toLong()
        return animator
    }

    fun bounceAnimation(view: View) {
        val from = 1.0f
        val to = 1.3f

        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, from, to)
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, from, to)
        val translationZ: ObjectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, from, to)

        val set1 = AnimatorSet()
        set1.playTogether(scaleX, scaleY, translationZ)
        set1.duration = 100
        set1.interpolator = AccelerateInterpolator()

        val scaleXBack: ObjectAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, to, from)
        val scaleYBack: ObjectAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, to, from)
        val translationZBack: ObjectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, to, from)

        val path = Path()
        path.moveTo(0.0f, 0.0f)
        path.lineTo(0.5f, 1.3f)
        path.lineTo(0.75f, 0.8f)
        path.lineTo(1.0f, 1.0f)
        val pathInterpolator = PathInterpolator(path)

        val set2 = AnimatorSet()
        set2.playTogether(scaleXBack, scaleYBack, translationZBack)
        set2.duration = 300
        set2.interpolator = pathInterpolator

        val set = AnimatorSet()
        set.playSequentially(set1, set2)

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.isClickable = true
            }

            override fun onAnimationStart(animation: Animator?) {
                view.isClickable = false
            }
        })
        set.start()
    }
}