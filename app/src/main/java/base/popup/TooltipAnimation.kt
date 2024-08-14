package base.popup

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef

class TooltipAnimation {

    companion object {
        const val NONE = 0
        const val FADE = 1
        const val REVEAL = 2
        const val SCALE = 3
        const val SCALE_AND_FADE = 4
        private val DEFAULT_TYPE = FADE
        private val DEFAULT_DURATION = 400 // ms
    }

    /**
     * Types of Animations available:
     * <br></br>
     *
     *  * [.NONE] : No Animation
     *  * [.FADE] : Fade in and Fade Out
     *  * [.REVEAL] : Circular Reveal. Center point would be Tip. If tip is not present,
     * center point would be where it's being anchored. This is supported for API 21 and above.
     *  * [.SCALE] : Scale animation based on position of the tooltip
     *  * [.SCALE_AND_FADE] : Scale and Fade animation
     *
     */
    @IntDef(NONE, FADE, REVEAL, SCALE, SCALE_AND_FADE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Type

    @Type
    private var type = 0


    private var duration = 0
    private var hideContentWhenAnimating = false

    /**
     * Create a new Animation object for [Tooltip]
     *
     * @param type     [Type]
     * @param duration animation duration in milliseconds
     */
    constructor(type: Int, duration: Int) : this(type, duration, false)

    /**
     * Create a new Animation object for [Tooltip]
     *
     * @param type                     [Type]
     * @param duration                 animation duration in milliseconds
     * @param hideContentWhenAnimating hide content when animating
     */
    constructor(@Type type: Int, duration: Int, hideContentWhenAnimating: Boolean) {
        this.type = type
        this.duration = duration
        this.hideContentWhenAnimating = hideContentWhenAnimating
    }

    /**
     * Create a new Animation object for [Tooltip], with duration [.DEFAULT_DURATION]
     *
     * @param type [Type]
     */
    constructor(@Type type: Int) : this(type, DEFAULT_DURATION)

    @Type
    fun getType(): Int {
        return type
    }

    fun getDuration(): Int {
        return duration
    }

    fun hideContentWhenAnimatingIn(animator: Animator, contentView: View) {
        if (hideContentWhenAnimating && contentView is ViewGroup) {
            hideAllChildren(contentView)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    showAllChildren(contentView)
                    animator.removeListener(this)
                }

                override fun onAnimationCancel(animation: Animator) {
                    showAllChildren(contentView)
                    animator.removeListener(this)
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    fun hideContentWhenAnimatingOut(contentView: View) {
        if (hideContentWhenAnimating && contentView is ViewGroup) {
            hideAllChildren(contentView)
        }
    }

    private fun hideAllChildren(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            view.getChildAt(i).visibility = View.INVISIBLE
        }
    }

    private fun showAllChildren(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            view.getChildAt(i).visibility = View.VISIBLE
        }
    }
}