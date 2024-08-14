package base.ui.fragment.other.friends.my

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.authentication.LoggedInUserCache
import base.data.api.users.model.FansChatUserDetails
import base.util.FriendsHolder
import base.util.inflate
import base.util.onClick
import base.util.open
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber

open class FriendsAdapter(
    private val isUserClickable: Boolean = true,
    private val isGrid: Boolean = false,
    private val loggedInUserCache: LoggedInUserCache,
    open var items: List<FansChatUserDetails>,
    private val context: Context,
) :
    Adapter<FriendsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        return FriendsHolder(parent.inflate(if (isGrid) R.layout.item_user_grid else R.layout.item_user))
    }

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        val friend = items[position]
        holder.apply {
            name.text = friend.displayName
            Glide.with(context).load(friend.avatarUrl).apply(
                RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.avatar_placeholder).placeholder(R.drawable.avatar_placeholder)
            ).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(avatar)
            itemView.onClick {
                if (isUserClickable)
                    when {
                        loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                            itemView.open(R.id.authDecideFragment)
                        }
                        friend.id != loggedInUserCache.getLoggedInUserId() -> {
                            itemView.open(R.id.friendDetails, "userId" to friend.id)
                        }
                        friend.id == loggedInUserCache.getLoggedInUserId() -> {
                            itemView.open(R.id.profile)
                        }
                        else -> {
                            Timber.e("Not supported")
                        }
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}