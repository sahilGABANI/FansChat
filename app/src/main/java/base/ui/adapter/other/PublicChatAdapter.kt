package base.ui.adapter.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.chat.model.CreateChatGroupResponse
import base.util.PublicChatHolder
import base.util.inflate
import base.util.onClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PublicChatAdapter(
    val context: Context,
    private val userIds: String,
    val items: List<CreateChatGroupResponse>,
    val callback: (chat: CreateChatGroupResponse, pos: Int) -> Unit
) : Adapter<PublicChatHolder>() {

    private var foundItems = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicChatHolder {
        return PublicChatHolder(parent.inflate(R.layout.item_public_chat))
    }

    override fun onBindViewHolder(holder: PublicChatHolder, position: Int) {
        val item = foundItems[position]

        holder.apply {
//            image.srcRound = item.image
            Glide.with(context).load(item.avatarUrl).circleCrop()
                .error(R.drawable.avatar_placeholder).placeholder(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(image)
            name.isVisible = true
            name.text = item.name
            tvMemberCount.text = "" + item.usersIds.size
            container.onClick {
                if (!foundItems[adapterPosition].usersIds.contains(userIds)) {
                    callback.invoke(foundItems[adapterPosition], adapterPosition)
                }
//                else
//                    fragment.toast(fragment.getString(R.string.you_are_already_in_grp))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        foundItems = items.filter { it.name?.contains(query, true)!! }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return foundItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePublicChat(items: List<CreateChatGroupResponse>) {
        this.foundItems = items
        notifyDataSetChanged()
    }
}