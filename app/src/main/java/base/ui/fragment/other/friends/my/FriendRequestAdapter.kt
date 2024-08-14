package base.ui.fragment.other.friends.my

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.data.api.users.model.FansChatUserDetails
import base.util.FriendsHolder
import base.util.inflate
import base.util.onClick
import base.util.open
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class FriendRequestAdapter(
    private val context: Context,
    private val isUserClickable: Boolean = true,
    private val isGrid: Boolean = false,
    private val userId: String? = null,
) : RecyclerView.Adapter<FriendsHolder>() {

    private var listOfUser: List<FansChatUserDetails> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        return FriendsHolder(parent.inflate(if (isGrid) R.layout.item_user_grid else R.layout.item_user))
    }

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        val friend = listOfUser[position]
        holder.apply {
            name.text = friend.displayName
            Glide.with(context)
                .load(friend.avatarUrl)
                .error(R.drawable.avatar_placeholder)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(avatar)
            itemView.onClick {
                if (isUserClickable)
                    when {
                        userId == null -> {
                            itemView.open(R.id.authDecideFragment)
                        }
                        friend.id != userId -> {
                            itemView.open(R.id.friendDetails, "userId" to friend.id)
                        }
                        else -> {
                            itemView.open(R.id.profile)
                        }
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfUser.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(listOfUser: List<FansChatUserDetails>) {
        this.listOfUser = listOfUser
        notifyDataSetChanged()
    }
}