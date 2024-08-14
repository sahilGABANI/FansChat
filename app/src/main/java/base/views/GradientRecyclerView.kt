package base.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import base.R

class GradientRecyclerView : RecyclerView {
    private var backgroundDrawables: Drawable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        backgroundDrawables = ContextCompat.getDrawable(context, R.drawable.bg_wall)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (layoutManager == null || backgroundDrawables == null) {
            return
        }

        val top = -computeVerticalScrollOffset()
        val bottom = computeVerticalScrollRange() + top

        backgroundDrawables?.setBounds(0, top, width, bottom)
        backgroundDrawables?.draw(canvas)
    }
}
