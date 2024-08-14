package base.ui.fragment.details.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.data.api.users.model.FansChatUserDetails
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SearchTagUserAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterItems = listOf<AdapterItem>()

    var listOfSearchTagUserInfo: List<FansChatUserDetails>? = null
        set(listOfSearchTag) {
            field = listOfSearchTag
            updateAdapterItems()
        }

    private val searchTagUserClicksSubject: PublishSubject<FansChatUserDetails> = PublishSubject.create()
    val searchTagUserClicks: Observable<FansChatUserDetails> = searchTagUserClicksSubject.hide()

    @Synchronized
    private fun updateAdapterItems() {
        val adapterItems = mutableListOf<AdapterItem>()
        listOfSearchTagUserInfo?.forEach { adapterItems.add(AdapterItem.SearchTagUserViewItem(it)) }
        this.adapterItems = adapterItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.SearchTagUserViewItemType.ordinal -> {
                SearchTagUserAdapterViewHolder(SearchTagUserView(context).apply {
                    searchTagUserClicks.subscribe {
                        searchTagUserClicksSubject.onNext(it)
                    }
                })
            }
            else -> throw IllegalArgumentException("Unsupported ViewType")
        }
    }

    override fun getItemCount(): Int {
        return adapterItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return adapterItems[position].type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapterItem = adapterItems.getOrNull(position) ?: return
        when (adapterItem) {
            is AdapterItem.SearchTagUserViewItem -> {
                (holder.itemView as SearchTagUserView).bind(adapterItem.searchTagUserInfo)
            }
        }
    }

    private class SearchTagUserAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view)

    sealed class AdapterItem(val type: Int) {
        data class SearchTagUserViewItem(val searchTagUserInfo: FansChatUserDetails) :
            AdapterItem(ViewType.SearchTagUserViewItemType.ordinal)
    }

    private enum class ViewType {
        SearchTagUserViewItemType
    }


}