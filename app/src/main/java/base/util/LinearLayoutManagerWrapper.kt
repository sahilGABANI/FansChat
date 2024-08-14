package base.util

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class LinearLayoutManagerWrapper(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {


    //    override fun supportsPredictiveItemAnimations(): Boolean {
//        return false
//    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Timber.tag("LinearLayout").e("Inconsistency detected")
        }
    }
}
