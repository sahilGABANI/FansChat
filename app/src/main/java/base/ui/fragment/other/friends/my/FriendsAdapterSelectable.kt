package base.ui.fragment.other.friends.my

import android.content.Context
import android.view.View
import base.data.api.authentication.LoggedInUserCache
import base.data.api.users.model.FansChatUserDetails
import base.ui.fragment.other.chat.ChatCreateFragment
import base.util.FriendsHolder
import base.util.onClick
import base.util.selectedFriend
import base.util.toggle

class FriendsAdapterSelectable(
    val fragment: ChatCreateFragment,
    private var listMainItems: List<FansChatUserDetails>,
    val loggedInUserCache: LoggedInUserCache,
    override var items: List<FansChatUserDetails>,
    private val context: Context
) : FriendsAdapter(false, true, loggedInUserCache, items, context) {

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.container.onClick {
            selectedFriend.toggle(items[position])
            notifyItemChanged(position)
            fragment.updateBottomList()
        }
        holder.ivChecked.visibility = if (selectedFriend.contains(items[position])) {
            View.VISIBLE
        } else View.GONE
    }

    fun filter(query: String) {
        items = listMainItems.filter { it.displayName!!.contains(query, true) }
        notifyDataSetChanged()
    }

}