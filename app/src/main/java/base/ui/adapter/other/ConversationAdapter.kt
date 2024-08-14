package base.ui.adapter.other

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.cache.Cache
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

const val CONVERSATIONS = 0
const val ADD_CHAT = 1

class ConversationAdapter(
    val context: Context,
    val items: MutableList<CreateChatGroupResponse>,
    val userId: String,
    private var onItemClick: ((addChat: Boolean, CreateChatGroupResponse) -> Unit)
) : Adapter<RecyclerView.ViewHolder>() {

    var currentOpenChat: Int = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == CONVERSATIONS) {
            ChatHolder(parent.inflate(R.layout.item_chat))
        } else {
            AddChatHolder(parent.inflate(R.layout.item_add_chat))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatHolder) {
            holder.container.background = null
            val chat = items[position]

            var imageUrl: String? = null

            if (chat.members.size == 2) {
                var findGroupImage = chat.members.filter { it.id != userId }
                imageUrl = chat.avatarUrl
                if (findGroupImage.isNotEmpty() && chat.avatarUrl.isNullOrEmpty())
                    findGroupImage.forEach {
                        imageUrl = it.avatarUrl
                    }
            } else {
                imageUrl = chat.avatarUrl
            }

            Glide.with(context)
                .load(imageUrl)
                .circleCrop()
                .error(R.drawable.avatar_placeholder)
                .placeholder(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.image)

            holder.name.isVisible = false
            holder.name.text = chat.name
            holder.imageBackground.isVisible = currentOpenChat == position

            holder.notification.isVisible =
                chat.id?.let {
                    Cache.cache.daoConversation().getAll(it)
                        .any { it.readFlag == false && it.sender?.id != userId }
                } ?: run { false }

            holder.container.onClick {
                if (currentOpenChat != -1 && currentOpenChat != position && currentOpenChat <= items.size)
                    notifyItemChanged(currentOpenChat)
                currentOpenChat = position
                notifyItemChanged(currentOpenChat)
                onItemClick.invoke(false, chat) //conversation clicked
            }

//            holder.notification.isVisible == chat.unreadCount > 0
        } else {
            holder.itemView.onClick {
                onItemClick.invoke(true, CreateChatGroupResponse()) //Add chat icon clicked
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) {
            ADD_CHAT
        } else {
            CONVERSATIONS
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

}