package base.ui.fragment.other.friends.requests.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.data.api.friendrequest.model.FriendRequest
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class FriendRequestAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterItems = listOf<AdapterItem>()

    private val acceptRequestViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val acceptRequestViewClicks: Observable<FriendRequest> = acceptRequestViewClicksSubject.hide()

    private val declineRequestViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val declineRequestViewClicks: Observable<FriendRequest> = declineRequestViewClicksSubject.hide()

    private val openProfileViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val openProfileViewClicks: Observable<FriendRequest> = openProfileViewClicksSubject.hide()

    var listOfFriendRequest: List<FriendRequest>? = null
        set(listOfFriendRequest) {
            field = listOfFriendRequest

            updateAdapterItems()
        }

    @Synchronized
    private fun updateAdapterItems() {
        val adapterItems = mutableListOf<AdapterItem>()
        listOfFriendRequest?.forEach { adapterItems.add(AdapterItem.FriendRequestViewItem(it)) }

        this.adapterItems = adapterItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.FriendRequestViewItemType.ordinal -> {
                FriendRequestViewHolder(FriendRequestView(context).apply {
                    acceptRequestViewClicks.subscribe {
                        acceptRequestViewClicksSubject.onNext(it)
                    }

                    declineRequestViewClicks.subscribe {
                        declineRequestViewClicksSubject.onNext(it)
                    }

                    openProfileViewClicks.subscribe {
                        openProfileViewClicksSubject.onNext(it)
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
            is AdapterItem.FriendRequestViewItem -> {
                (holder.itemView as FriendRequestView).bind(adapterItem.friendRequest)
            }
        }
    }

    class FriendRequestViewHolder(view: View) : RecyclerView.ViewHolder(view)

    sealed class AdapterItem(val type: Int) {
        data class FriendRequestViewItem(val friendRequest: FriendRequest) : AdapterItem(ViewType.FriendRequestViewItemType.ordinal)
    }

    private enum class ViewType {
        FriendRequestViewItemType
    }
}