package base.util

import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {
    private var isRTL = false

    init {
        isRTL = TextUtils.getLayoutDirectionFromLocale(
            Locale
                .getDefault()
        ) == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        if (includeEdge) {
            if (isRTL) {
                outRect.right = spacing - column * spacing / spanCount
                outRect.left = (column + 1) * spacing / spanCount
            } else {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
            }
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            if (isRTL) {
                outRect.right = column * spacing / spanCount
                outRect.left = spacing - (column + 1) * spacing / spanCount
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
            }
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}