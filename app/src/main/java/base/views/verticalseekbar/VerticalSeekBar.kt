package base.views.verticalseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.view.ViewCompat
import base.R
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class VerticalSeekBar : AppCompatSeekBar {
    companion object {
        val ROTATION_ANGLE_CW_90 = 90
        val ROTATION_ANGLE_CW_270 = 270
    }

    var mIsDragging = false
    var mMethodSetProgressFromUser: Method? = null
    var mRotationAngle = ROTATION_ANGLE_CW_90

    constructor(context: Context) : super(context) {
        initialize(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context, attrs, defStyle, 0)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar, defStyleAttr, defStyleRes)
            val rotationAngle = a.getInteger(R.styleable.VerticalSeekBar_seekBarRotation, 0)
            if (isValidRotationAngle(rotationAngle)) {
                mRotationAngle = rotationAngle
            }
            a.recycle()
        }
    }

    override fun setThumb(thumb: Drawable?) {
        super.setThumb(thumb)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (useViewRotation()) {
            onTouchEventUseViewRotation(event)
        } else {
            onTouchEventTraditionalRotation(event)
        }
    }

    private fun onTouchEventTraditionalRotation(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag(true)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> if (mIsDragging) {
                trackTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    attemptClaimDrag(false)
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    private fun onTouchEventUseViewRotation(event: MotionEvent): Boolean {
        val handled: Boolean = super.onTouchEvent(event)
        if (handled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> attemptClaimDrag(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> attemptClaimDrag(false)
            }
        }
        return handled
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val paddingLeft: Int = super.getPaddingLeft()
        val paddingRight: Int = super.getPaddingRight()
        val height: Int = height
        val available = height - paddingLeft - paddingRight
        val y = event.y.toInt()
        val scale: Float
        var value = 0f
        when (mRotationAngle) {
            ROTATION_ANGLE_CW_90 -> value = (y - paddingLeft).toFloat()
            ROTATION_ANGLE_CW_270 -> value = (height - paddingLeft - y).toFloat()
        }
        scale = if (value < 0 || available == 0) {
            0.0f
        } else if (value > available) {
            1.0f
        } else {
            value / available.toFloat()
        }
        val max: Int = max
        val progress = scale * max
        _setProgressFromUser(progress.toInt(), true)
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag(active: Boolean) {
        this.parent?.requestDisallowInterceptTouchEvent(active)
    }

    /**
     * This is called when the user has started touching this widget.
     */
    private fun onStartTrackingTouch() {
        mIsDragging = true
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    fun onStopTrackingTouch() {
        mIsDragging = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isEnabled) {
            val handled: Boolean
            var direction = 0
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    direction = if (mRotationAngle == ROTATION_ANGLE_CW_90) 1 else -1
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    direction = if (mRotationAngle == ROTATION_ANGLE_CW_270) 1 else -1
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT ->                     // move view focus to previous/next view
                    return false
                else -> handled = false
            }
            if (handled) {
                val keyProgressIncrement: Int = keyProgressIncrement
                var progress: Int = progress
                progress += direction * keyProgressIncrement
                if (progress in 0..max) {
                    _setProgressFromUser(progress, true)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        if (!useViewRotation()) {
            refreshThumb()
        }
    }

    @Synchronized
    private fun _setProgressFromUser(progress: Int, fromUser: Boolean) {
        if (mMethodSetProgressFromUser == null) {
            try {
                val m: Method = ProgressBar::class.java.getDeclaredMethod("setProgress",
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType)
                m.isAccessible = true
                mMethodSetProgressFromUser = m
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        if (mMethodSetProgressFromUser != null) {
            try {
                mMethodSetProgressFromUser!!.invoke(this, progress, fromUser)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            super.setProgress(progress)
        }
        refreshThumb()
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (useViewRotation()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            val lp: ViewGroup.LayoutParams? = layoutParams
            if (isInEditMode && lp != null && lp.height >= 0) {
                setMeasuredDimension(super.getMeasuredHeight(), lp.height)
            } else {
                setMeasuredDimension(super.getMeasuredHeight(), super.getMeasuredWidth())
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            super.onSizeChanged(w, h, oldw, oldh)
        } else {
            super.onSizeChanged(h, w, oldh, oldw)
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        if (!useViewRotation()) {
            when (mRotationAngle) {
                ROTATION_ANGLE_CW_90 -> {
                    canvas.rotate(90f)
                    canvas.translate(0f, -super.getWidth().toFloat())
                }
                ROTATION_ANGLE_CW_270 -> {
                    canvas.rotate(-90f)
                    canvas.translate(-super.getHeight().toFloat(), 0f)
                }
            }
        }
        super.onDraw(canvas)
    }

    fun getRotationAngle(): Int {
        return mRotationAngle
    }

    // refresh thumb position
    private fun refreshThumb() {
        onSizeChanged(super.getWidth(), super.getHeight(), 0, 0)
    }

    /*package*/
    fun useViewRotation(): Boolean {
        return !isInEditMode
    }

    private fun getWrapper(): VerticalSeekBarWrapper? {
        val parent: ViewParent = parent
        return if (parent is VerticalSeekBarWrapper) {
            parent
        } else {
            null
        }
    }

    private fun isValidRotationAngle(angle: Int): Boolean {
        return angle == ROTATION_ANGLE_CW_90 || angle == ROTATION_ANGLE_CW_270
    }
}
