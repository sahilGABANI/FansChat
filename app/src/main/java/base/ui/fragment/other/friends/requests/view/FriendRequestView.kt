package base.ui.fragment.other.friends.requests.view

import android.content.Context
import android.view.View
import base.R
import base.data.api.friendrequest.model.FriendRequest
import base.databinding.ItemFriendRequestBinding
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.extension.toDate
import base.util.ConstraintLayoutWithLifecycle
import base.util.srcRound
import base.util.toRelative
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.text.ParseException
import java.util.*

class FriendRequestView(context: Context) : ConstraintLayoutWithLifecycle(context) {

    // Scoped to the lifecycle of the view's view (between onCreateView and onDestroyView)
    private var friendRequestBinding: ItemFriendRequestBinding? = null

    private lateinit var friendRequestData: FriendRequest

    private val acceptRequestViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val acceptRequestViewClicks: Observable<FriendRequest> = acceptRequestViewClicksSubject.hide()

    private val declineRequestViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val declineRequestViewClicks: Observable<FriendRequest> = declineRequestViewClicksSubject.hide()

    private val openProfileViewClicksSubject: PublishSubject<FriendRequest> = PublishSubject.create()
    val openProfileViewClicks: Observable<FriendRequest> = openProfileViewClicksSubject.hide()

    init {
        inflateUi()
    }


    private fun inflateUi() {
        val view = View.inflate(context, R.layout.item_friend_request, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        friendRequestBinding = ItemFriendRequestBinding.bind(view)

        friendRequestBinding?.let {
            it.acceptButton.throttleClicks().subscribeAndObserveOnMainThread {
                acceptRequestViewClicksSubject.onNext(friendRequestData)
            }

            it.declineButton.throttleClicks().subscribeAndObserveOnMainThread {
                declineRequestViewClicksSubject.onNext(friendRequestData)
            }

            it.rlMain.throttleClicks().subscribeAndObserveOnMainThread {
                openProfileViewClicksSubject.onNext(friendRequestData)
            }
        }
    }

    fun bind(friendRequest: FriendRequest) {
        this.friendRequestData = friendRequest

        friendRequestBinding?.let { binding ->

            friendRequest.sender?.let {
                binding.name.text = it.displayName
                it.avatar?.let { profile ->
                    binding.avatar.srcRound = profile
                }

//                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                try {
                    val date: Date? = friendRequest.created?.toDate()//format.parse(friendRequest.created)
                    binding.date.text = date?.toRelative() ?: ""
                    binding.date.visibility = if (friendRequest.created.isNullOrEmpty()) View.GONE else View.VISIBLE
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        friendRequestBinding = null
    }
}