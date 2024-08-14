package base.popup


import android.animation.Animator
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import timber.log.Timber
import kotlin.math.max

@SuppressLint("ViewConstructor")
class Tooltip(builder: Builder) : ViewGroup(builder.context) {

    private val MIN_INT_VALUE = -2147483648

    private var debug = false

    private var contentView: View? = null
    private var anchorView: View? = null

    private val anchorLocation = IntArray(2)
    private val holderLocation = IntArray(2)

    @Position
    private var position = 0

    private var isCancelable = true
    private var autoAdjust = true

    private var padding = 0

    private var builderListener: Listener? = null
    private var listener: Listener? = null

    private var tip: Tip? = null
    private var tipPaint: Paint? = null
    private var tipPath: Path? = null
    private var showTip = false

    private val anchorPoint = Point()
    private val tooltipSize = IntArray(2)

    companion object {
        const val NO_AUTO_CANCEL = 0
        const val TAG = "Tooltip"
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
    }

    @IntDef(LEFT, TOP, RIGHT, BOTTOM)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Position

    private var animation: TooltipAnimation? = null
    private var animate = false
    private var hasAnimatedIn = false

    // To avoid multiple click dismiss error (in animation)
    private var isDismissed = false
    private var isDismissAnimationInProgress = false

    // Coordinator anchored view BS
    // Coordinator anchored view BS
    /**
     * If we have made a call to [.doLayout] or not
     */
    private var hasDrawn = false

    /**
     * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
     * about its position in the window. So we need to wait for a preDraw event and then
     * draw tooltip and layout its contents.
     */
    private var checkForPreDraw = false

    /**
     * If the view is attached to window or not
     */
    private var isAttached = false

    init {
        init(builder)
    }

    private fun init(builder: Builder) {
        contentView = builder.contentView
        anchorView = builder.anchorView
        builderListener = builder.myListener
        autoAdjust = builder.autoAdjust
        position = builder.position
        padding = builder.padding
        checkForPreDraw = builder.checkForPreDraw
        debug = builder.debug

        // Cancelable
        isCancelable = builder.cancelable

        // Animation
        animation = builder.animation
        animate = animation != null && animation!!.getType() != TooltipAnimation.NONE
        tipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tipPaint!!.style = Paint.Style.FILL_AND_STROKE

        // Tip
        tip = builder.tip
        showTip = tip != null
        if (tip != null) {
            tipPaint!!.color = tip!!.color
            if (tip!!.tipRadius > 0) {
                tipPaint!!.strokeJoin = Paint.Join.ROUND
                tipPaint!!.strokeCap = Paint.Cap.ROUND
                tipPaint!!.strokeWidth = tip!!.tipRadius.toFloat()
            }
        }
        tipPaint!!.color = if (tip == null) -0x1 else tip!!.color
        if (debug) {
            Timber.tag(TAG).d("show tip: $showTip")
        }
        listener = builder.listener
        tipPath = Path()
        var params = contentView!!.layoutParams
        if (params == null) {
            params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        rootView.setOnClickListener {
            if (isCancelable) {
                dismiss(animate)
            }
        }
        this.addView(contentView, params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val child = getChildAt(0)
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
        if (debug) {
            Timber.tag(TAG).i("child measured width: %s", child.measuredWidth)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (debug) {
            Timber.tag(TAG).i("l: $l, t: $t, r: $r, b: $b")
        }
        if (checkForPreDraw && !hasDrawn) {
            anchorView!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    anchorView!!.viewTreeObserver.removeOnPreDrawListener(this)
                    anchorView!!.getLocationInWindow(anchorLocation)
//                    Timber.tag(TAG).i("%s%s", "onPreDraw: " + anchorLocation[0] + ", ", anchorLocation[1])
                    hasDrawn = true
                    doLayout(changed, l, t, r, b)
                    return true
                }
            })
            return
        }
        hasDrawn = true
        doLayout(changed, l, t, r, b)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
    }

    private fun doLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = getChildAt(0)
        anchorView!!.getLocationInWindow(anchorLocation)
        getLocationInWindow(holderLocation)
        val dx = anchorLocation[0] - holderLocation[0]
        val dy = anchorLocation[1] - holderLocation[1]
        val w = child.measuredWidth
        val h = child.measuredHeight
        var left = dx
        var top = dy
        if (debug) {
            Timber.tag(TAG).d("anchor location: ${anchorLocation[0]} ,  ${anchorLocation[1]}")
            Timber.tag(TAG).d("holder location:  ${holderLocation[0]} , ${holderLocation[1]}")
            Timber.tag(TAG).d("child w: $w h: $h")
            Timber.tag(TAG).d("left: $left, top: $top")
        }
        tipPath!!.reset()
        var px = MIN_INT_VALUE
        var py = MIN_INT_VALUE
        when (position) {
            LEFT -> {

                // to left of anchor view
                // align with horizontal axis
                val diff = (anchorView!!.height - h) / 2
                // We should pad right side
                left -= w + padding + if (showTip) tip!!.height else 0
                // Top and bottom padding is not required
                top += diff
                if (showTip) {
                    px = left + w + tip!!.height
                    py = top + h / 2
                    tipPath!!.moveTo(px.toFloat(), py.toFloat())
                    tipPath!!.lineTo((px - tip!!.height).toFloat(), (py + tip!!.width / 2).toFloat())
                    tipPath!!.lineTo((px - tip!!.height).toFloat(), (py - tip!!.width / 2).toFloat())
                    tipPath!!.lineTo(px.toFloat(), py.toFloat())
                }
            }
            RIGHT -> {

                // to right of anchor view
                // align with horizontal axis
                val diff = (anchorView!!.height - h) / 2
                // We should pad left side
                left += anchorView!!.width + padding + if (showTip) tip!!.height else 0
                // Top and bottom padding is not required
                top += diff
                if (showTip) {
                    px = left - tip!!.height
                    py = top + h / 2
                    tipPath!!.moveTo(px.toFloat(), py.toFloat())
                    tipPath!!.lineTo((px + tip!!.height).toFloat(), (py + tip!!.width / 2).toFloat())
                    tipPath!!.lineTo((px + tip!!.height).toFloat(), (py - tip!!.width / 2).toFloat())
                    tipPath!!.lineTo(px.toFloat(), py.toFloat())
                }
            }
            TOP -> {

                // to top of anchor view
                // align with vertical axis
                val diff = (anchorView!!.width - w) / 2

                // Left and Right padding are not required.
                left += diff

                // We should only pad bottom
                top -= h + padding + if (showTip) tip!!.height else 0
                if (showTip) {
                    px = left + w / 2
                    py = top + h + tip!!.height
                    tipPath!!.moveTo(px.toFloat(), py.toFloat())
                    tipPath!!.lineTo((px - tip!!.width / 2).toFloat(), (py - tip!!.height).toFloat())
                    tipPath!!.lineTo((px + tip!!.width / 2).toFloat(), (py - tip!!.height).toFloat())
                    tipPath!!.lineTo(px.toFloat(), py.toFloat())
                }
            }
            BOTTOM -> {

                // to top of anchor view
                // align with vertical axis
                val diff = (anchorView!!.width - w) / 2

                // Left and Right padding are not required.
                left += diff

                // We should only pad top
                top += anchorView!!.height + padding + if (showTip) tip!!.height else 0
                if (debug) {
                    Timber.tag(TAG).d("tip top: $top")
                }
                if (showTip) {
                    px = left + w / 2
                    py = top - tip!!.height
                    tipPath!!.moveTo(px.toFloat(), py.toFloat())
                    tipPath!!.lineTo((px - tip!!.width / 2).toFloat(), (py + tip!!.height).toFloat())
                    tipPath!!.lineTo((px + tip!!.width / 2).toFloat(), (py + tip!!.height).toFloat())
                    tipPath!!.lineTo(px.toFloat(), py.toFloat())
                }
            }
        }
        if (autoAdjust) {
            when (position) {
                TOP, BOTTOM -> if (left + w > r) {
                    // View is going out on the right side
                    // Add padding to the right
                    left = r - w - padding
                } else if (left < l) {
                    // View is going out on the left side
                    // Add padding to the left
                    left = l + padding
                }
                LEFT, RIGHT -> if (top + h > b) {
                    // View is going out on the bottom side
                    // Add padding to bottom
                    top = b - h - padding
                } else if (top < t) {
                    // View is going out on the top side
                    // Add padding to top
                    top = t + padding
                }
            }
        }
        if (debug) {
            Timber.tag(TAG)
                .i("child layout: left: $left top: $top right: ${left + child.measuredWidth} bottom: ${(top + child.measuredHeight)}")
            Timber.tag(TAG).i("px: $px, py: $py")
        }

        // Tip was not drawn. We need to set anchor point for animation
        if (px == MIN_INT_VALUE || py == MIN_INT_VALUE) {
            if (debug) {
                Timber.tag(TAG).d("Tip was not drawn")
            }
            when (position) {
                TOP -> {
                    px = left + child.measuredWidth / 2
                    py = top + child.measuredHeight
                }
                BOTTOM -> {
                    px = left + child.measuredWidth / 2
                    py = top
                }
                LEFT -> {
                    px = left + child.measuredWidth
                    py = top + child.measuredHeight
                }
                RIGHT -> {
                    px = left
                    py = top + child.measuredHeight / 2
                }
            }
        }

        // Set anchor point
        anchorPoint[px] = py

        // Get Tooltip content size
        tooltipSize[0] = child.measuredWidth
        tooltipSize[1] = child.measuredHeight
        child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
        if (animate && !hasAnimatedIn) {
            hasAnimatedIn = false
            animateIn(animation!!)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (debug) {
            Timber.tag(TAG).i("canvas w: ${canvas.width}, h: ${canvas.height}")
        }
        if (showTip && hasDrawn) {
            canvas.drawPath(tipPath!!, tipPaint!!)
        }
    }

    fun isCancelable(): Boolean {
        return isCancelable
    }


    /**
     * Dismiss and remove Tooltip from the view.
     * No animation is performed.
     */
    fun dismiss() {

        // Dismissing or already dismissed
        if (isDismissed) {
            return
        }
        isDismissed = true
        removeView(contentView)
        val parent = parent as ViewGroup
        parent.removeView(this)
        builderListener!!.onDismissed()
        if (listener != null) {
            listener!!.onDismissed()
        }
    }

    /**
     * Dismiss and remove Tooltip from the view.
     *
     * @param animate Animation is performed if true
     */
    fun dismiss(animate: Boolean) {

        // Dismissing or already dismissed
        if (isDismissed) {
            return
        }
        if (!isAttached) {
            if (debug) {
                Timber.tag(TAG).e("view is detached. Not animating")
            }
            return
        }
        if (!animate || animation == null) {
            dismiss()
            return
        }
        animateOut(animation!!)
    }

    private fun getAnchorPoint(): Point {
        return anchorPoint
    }

    private fun getTooltipSize(): IntArray {
        return tooltipSize
    }

    private fun animateIn(animation: TooltipAnimation) {
        if (!isAttached) {
            if (debug) {
                Timber.tag(TAG).e("View is not attached. Not animating the tooltip")
            }
            return
        }
        val point = getAnchorPoint()
        val size = getTooltipSize()
        if (debug) {
            Timber.tag(TAG).d("anchor point: ${point.x}, ${point.y}")
            Timber.tag(TAG).d("size: ${size[0]}, ${size[1]}")
        }
        val animator = getAnimator(animation, point, size, true)
        if (animator != null) {
            animator.start()
            animation.hideContentWhenAnimatingIn(animator, contentView!!)
        }
    }

    private fun getAnimator(animation: TooltipAnimation, point: Point, size: IntArray, animateIn: Boolean): Animator? {
        var startAlpha = 0f
        var endAlpha = 1f
        var startScale = 0f
        var endScale = 1f
        var startRadius = 0
        var finalRadius = max(size[0], size[1])
        if (!animateIn) {
            startAlpha = 1f
            endAlpha = 0f
            startScale = 1f
            endScale = 0f
            startRadius = finalRadius
            finalRadius = 0
        }
        return when (animation.getType()) {
            TooltipAnimation.FADE -> AnimationUtils.fade(this, startAlpha, endAlpha, animation.getDuration())
            TooltipAnimation.REVEAL -> AnimationUtils.reveal(this,
                point.x,
                point.y,
                startRadius,
                finalRadius,
                animation.getDuration())
            TooltipAnimation.SCALE -> getScaleAnimator(animation, size, startScale, endScale)
            TooltipAnimation.SCALE_AND_FADE -> {
                val scaleAnimator = getScaleAnimator(animation, size, startScale, endScale)
                val fadeAnimator = AnimationUtils.fade(this, startAlpha, endAlpha, animation.getDuration())
                if (scaleAnimator == null) {
                    return fadeAnimator
                }
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(scaleAnimator, fadeAnimator)
                animatorSet
            }
            TooltipAnimation.NONE -> null
            else -> null
        }
    }

    private fun animateOut(animation: TooltipAnimation) {
        if (isDismissAnimationInProgress) {
            return
        }
        val point = getAnchorPoint()
        val size = getTooltipSize()

        val animator = getAnimator(animation, point, size, false)
        if (animator == null) {
            dismiss()
            return
        }
        animator.start()
        isDismissAnimationInProgress = true
        animation.hideContentWhenAnimatingOut(contentView!!)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                dismiss()
            }

            override fun onAnimationCancel(animation: Animator) {
                dismiss()
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun getScaleAnimator(animation: TooltipAnimation,
        size: IntArray,
        startScale: Float,
        endScale: Float): Animator? {
        return when (position) {
            BOTTOM -> AnimationUtils.scaleY(contentView!!,
                size[0] / 2,
                0,
                startScale,
                endScale,
                animation.getDuration())
            TOP -> AnimationUtils.scaleY(contentView!!,
                size[0] / 2,
                size[1],
                startScale,
                endScale,
                animation.getDuration())
            RIGHT -> AnimationUtils.scaleX(contentView!!, 0, size[1] / 2, startScale, endScale, animation.getDuration())
            LEFT -> AnimationUtils.scaleX(contentView!!,
                size[0],
                size[1] / 2,
                startScale,
                endScale,
                animation.getDuration())
            else -> null
        }
    }

    /**
     * Builder class for [Tooltip]. Builder has the responsibility of creating the Tooltip
     * and adding/displaying it in the [.rootView].
     */
    class Builder(val context: Context) {
        /**
         * ViewGroup where Tooltip is added
         */
        private var rootView: ViewGroup? = null

        /**
         * Content of the Tooltip
         */
        var contentView: View? = null

        /**
         * Anchor of the Tooltip. This is where the tooltip is anchored.
         */
        var anchorView: View? = null

        /**
         * Position of the Tooltip relative to the anchor. Default position is [.TOP].
         * Other positions are - [.BOTTOM], [.RIGHT], [.LEFT]
         */
        @Position
        var position: Int = TOP

        /**
         * Whether the tooltip should be dismissed or not if clicked outside
         */
        var cancelable = true

        /**
         * Automatically adjust tooltip layout if it's going out of screen.
         * Scenario: If tooltip is anchored with position [.TOP], it will try to position itself
         * within the bounds of the view in right and left direction. It will not try to adjust itself in top
         * bottom direction.
         */
        var autoAdjust = true

        /**
         * Tip of the tooltip.
         */
        var tip: Tip? = null

        /**
         * Margin from the anchor and screen boundaries
         */
        var padding = 0

        /**
         * If you want the tooltip to dismiss automatically after a certain amount of time,
         * set it in milliseconds. Values <= 0 are considered invalid and auto dismiss is turned off.
         */
        private var autoCancelTime: Int = NO_AUTO_CANCEL

        /**
         * Tooltip instance
         */
        private var tooltip: Tooltip? = null
        private val handler: Handler = Handler()
        private val autoCancelRunnable: Runnable

        /**
         * Dismiss Listener for Builder
         */
        val myListener: Listener

        /**
         * Dismiss Listener for User
         */
        var listener: Listener? = null
        var animation: TooltipAnimation? = null
        private var animate = false

        /**
         * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
         * about its position in the window. So we need to wait for a preDraw event and then
         * draw tooltip.
         */
        var checkForPreDraw = false

        /**
         * Show logs
         */
        var debug = false

        /**
         * set tooltip's content view
         *
         * @param view Content of the tooltip
         * @return Builder
         */
        fun content(view: View): Builder {
            contentView = view
            return this
        }

        /**
         * set tooltip's anchor with position [.TOP]
         *
         * @param view Anchor view
         * @return Builder
         */
        fun anchor(view: View): Builder {
            anchorView = view
            return this
        }

        /**
         * Set tooltip's anchor with tooltip's relative position
         *
         * @param view     Anchor view
         * @param position position of tooltip relative to the anchor. [.TOP], [.RIGHT],
         * [.BOTTOM], [.LEFT]
         * @return Builder
         */
        fun anchor(view: View, @Position position: Int): Builder {
            anchorView = view
            this.position = position
            return this
        }

        /**
         * Add Tooltip in this view
         *
         * @param viewGroup [ViewGroup] root view (parent view) for the tooltip
         * @return Builder
         */
        fun into(viewGroup: ViewGroup): Builder {
            rootView = viewGroup
            return this
        }

        /**
         * Whether the tooltip should be dismissed or not if clicked outside. Default it true
         *
         * @param cancelable boolean
         * @return Builder
         */
        fun cancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        /**
         * Automatically adjust tooltip layout if it's going out of screen.
         * Scenario: If tooltip is anchored with position [.TOP], it will try to position itself
         * within the bounds of the view in right and left direction. It will not try to adjust itself in top
         * bottom direction.
         *
         * @param autoAdjust boolean
         * @return Builder
         */
        fun autoAdjust(autoAdjust: Boolean): Builder {
            this.autoAdjust = autoAdjust
            return this
        }

        /**
         * Margin from the anchor and screen boundaries
         *
         * @param padding - margin from the screen edge (in pixels).
         * @return Builder
         */
        fun withPadding(padding: Int): Builder {
            this.padding = padding
            return this
        }

        /**
         * Attach dismiss listener
         *
         * @param listener dismiss listener
         * @return Builder
         */
        fun withListener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        /**
         * Show Tip. If null, it doesn't show the tip.
         *
         * @param tip [Tip]
         * @return Builder
         */
        fun withTip(tip: Tip): Builder {
            this.tip = tip
            return this
        }

        /**
         * If you want the tooltip to dismiss automatically after a certain amount of time,
         * set it in milliseconds. Values &lt;= 0 are considered invalid and auto dismiss is turned off.
         *
         *
         * Default is 0.
         *
         * @param timeInMilli dismiss time
         * @return Builder
         */
        fun autoCancel(timeInMilli: Int): Builder {
            autoCancelTime = timeInMilli
            return this
        }

        /**
         * Set show and dismiss animation for the tooltip
         *
         * @param animation [TooltipAnimation] to be performed while showing and dismissing
         * @return Builder
         */
        fun animate(animation: TooltipAnimation): Builder {
            this.animation = animation
            animate = true
            return this
        }

        /**
         * If the anchor is anchored to some view in CoordinatorLayout, we get incorrect data
         * about its position in the window. So we need to wait for a preDraw event and then
         * draw tooltip.
         *
         * @param check - boolean
         * @return Builder
         */
        fun checkForPreDraw(check: Boolean): Builder {
            checkForPreDraw = check
            return this
        }

        /**
         * Show logs
         *
         * @param debug boolean
         * @return Builder
         */
        fun debug(debug: Boolean): Builder {
            this.debug = debug
            return this
        }

        /**
         * Create a new instance of Tooltip. This method will throw [NullPointerException]
         * if [.anchorView] or [.rootView] or [.contentView] is not assigned.
         *
         * @return [Tooltip]
         */
        fun build(): Tooltip {
            if (anchorView == null) {
                throw NullPointerException("anchor view is null")
            }
            if (rootView == null) {
                throw NullPointerException("Root view is null")
            }
            if (contentView == null) {
                throw NullPointerException("content view is null")
            }
            tooltip = Tooltip(this)
            return tooltip!!
        }

        /**
         * Creates a new instance of Tooltip by calling [.build] and adds tooltip to [.rootView].
         * <br></br><br></br>
         * Tooltip is added to the rootView with MATCH_PARENT for width and height constraints. [.contentView]
         * is drawn based on its LayoutParams. If it does not contain any LayoutParams, new LayoutParams are generated
         * with WRAP_CONTENT for width and height and added to the Tooltip view.
         *
         * @return Generated [Tooltip]
         */
        fun show(): Tooltip? {
            tooltip = build()
            val anchorLocation = IntArray(2)
            anchorView!!.getLocationInWindow(anchorLocation)
            if (debug) {
                Timber.tag(TAG).d("anchor location before adding: ${anchorLocation[0]}, ${anchorLocation[1]}")
            }
            rootView!!.addView(tooltip, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            anchorView!!.getLocationInWindow(anchorLocation)
            if (debug) {
                Timber.tag(TAG).i("anchor location after adding: ${anchorLocation[0]} ,  ${anchorLocation[1]}")
            }
            if (autoCancelTime > NO_AUTO_CANCEL) {
                handler.postDelayed(autoCancelRunnable, autoCancelTime.toLong())
            }
            return tooltip
        }

        init {
            autoCancelRunnable = Runnable {
                if (tooltip != null) {
                    tooltip!!.dismiss(animate)
                }
            }
            myListener = object : Listener {
                override fun onDismissed() {
                    handler.removeCallbacks(autoCancelRunnable)
                }
            }
        }
    }

    /**
     * Tip of the tooltip. Tip is drawn separately to accommodate custom views.
     * It has three properties. [.width], [.height], and [.color].
     * <br></br><br></br>
     * Tip is drawn as an isosceles triangle. The length of the base
     * is defined by width and perpendicular length between top vertex and base is defined
     * by height.
     */
    class Tip @JvmOverloads constructor(
        /**
         * length of the base of isosceles triangle
         */
        val width: Int,
        /**
         * length of the perpendicular from top vertex to the base
         */
        val height: Int,
        /**
         * color of the tip.
         */
        @field:ColorInt @get:ColorInt val color: Int,
        /**
         * Corner radius of the tip in px
         */
        val tipRadius: Int = DEFAULT_TIP_RADIUS) {

        companion object {
            private const val DEFAULT_TIP_RADIUS = 0
        }
    }

    /**
     * Tooltip dismiss listener. [.onDismissed] is called when tooltip is dismissed.
     */
    interface Listener {
        fun onDismissed()
    }
}