package base.ui.adapter.other

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.Api
import base.data.cache.Cache.Companion.cache
import base.data.cache.isNotSaved
import base.data.cache.save
import base.data.model.User
import base.util.FriendsHolder
import base.util.inflate
import base.util.srcRound

class ChatsSearchUsersAdapter(val fragment: Context?, val items: ArrayList<User>) :
    Adapter<FriendsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        return FriendsHolder(parent.inflate(R.layout.item_user))
    }

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        var friend = items[position]

        holder.apply {

            if (cache.daoUsers().getImmediately(friend.id) == null) {
                Api.getUser(friend.id)
                    .subscribe({
                        if (cache.isNotSaved(it)) cache.save(it)
                        friend = it
                        name.text = friend.name
                        avatar.srcRound = friend.avatar
                    }, { it.printStackTrace() })
            } else {
                friend = cache.daoUsers().getImmediately(friend.id)!!
                name.text = friend.name
                avatar.srcRound =
                    if (friend.avatar.isNullOrBlank()) friend.circularAvatarUrl else friend.avatar
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}