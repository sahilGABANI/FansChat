package base.util.json

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager


abstract class PaginationListener(
    @field:NonNull @param:NonNull private val layoutManager: LinearLayoutManager,
    page_size: Int,
    page_size_initial: Int
) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!ismLoading && !ismLastPage && isLastItemDisplaying(recyclerView)) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                recyclerView.post { loadMoreItems() }
            }
        }
    }

    private fun isLastItemDisplaying(recyclerView: RecyclerView): Boolean {
        if (recyclerView.adapter!!.itemCount != 0) {
            val lastVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.adapter!!
                    .itemCount - 1
            ) return true
        }
        return false
    }

    protected abstract fun loadMoreItems()
    abstract val ismLastPage: Boolean
    abstract val ismLoading: Boolean

    companion object {
        var PAGE_SIZE = 5
        var PAGE_SIZE_INITIAL = 10

        const val PAGE_SIZE_FIVE = 5
        const val PAGE_SIZE_TEN = 10
        const val PAGE_SIZE_FIFTY = 50
    }

    init {
        PAGE_SIZE = page_size
        PAGE_SIZE_INITIAL = page_size_initial
    }
}