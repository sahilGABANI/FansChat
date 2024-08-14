package base.ui.adapter.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.cache.Cache.Companion.cache
import base.data.model.User
import base.extension.toDate
import base.socket.model.GroupMessages
import base.util.*

class ChatsSearchAdapter(
    val context: Context?,
    var items: List<CreateChatGroupResponse>,
    private val userIds: String,
    val callback: (chat: CreateChatGroupResponse, pos: Int) -> Unit
) : Adapter<ChatSearchHolder>() {

    private var lastIndex = -1
    private var foundItems = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSearchHolder {
        return ChatSearchHolder(parent.inflate(R.layout.item_chat_search))
    }

    override fun onBindViewHolder(holder: ChatSearchHolder, position: Int) {
        val item = foundItems[position]

        holder.apply {
            if (item.id?.let { cache.daoConversation().getLastMsg(it) } == null) {
//                getMessages(item, 0, 1) {
//                    if (it.isNullOrEmpty()) {
//                        linDateTime.visibility = View.GONE
//                    } else {
//                        linDateTime.visibility = View.VISIBLE
//                        tvDateTime.text = it.first().date!!.toRelative()
//                        if (cache.isNotSaved(it)) cache.save(it)
//                    }
//                }
            } else {
                val msg: GroupMessages? = cache.daoConversation().getLastMsg(item.id)
                if (msg == null) {
                    linDateTime.visibility = View.GONE
                } else {
                    linDateTime.visibility = View.VISIBLE
                    tvDateTime.text = msg.created?.toDate()!!.toRelative()
                }
            }

//            image.srcRound = item.image

            name.text = item.name
            tvFrndMemberCount.text =
                ("" /*+ item.usersIds.size + " Friends "  */ + item.usersIds.size + " Members")

            btnJoinChat.onClick {
                if (!foundItems[adapterPosition].usersIds.contains(userIds))
                    callback.invoke(foundItems[adapterPosition], adapterPosition)
                else
                    Toast.makeText(
                        context,
                        context?.resources?.getString(R.string.you_are_already_in_grp),
                        Toast.LENGTH_SHORT
                    ).show()
            }
            linTop.onClick {
                /* if (lastIndex != -1)
                     notifyItemChanged(lastIndex)*/
                val lastOpened = lastIndex

                lastIndex = if (lastIndex == adapterPosition) {
                    -1
                } else {
                    adapterPosition
                }
                if (lastOpened != -1)
                    notifyItemChanged(lastOpened)
                if (lastIndex != -1)
                    notifyItemChanged(lastIndex)

                /*

                 lastIndex = if (lastIndex == adapterPosition)
                     -1
                 else {
                     adapterPosition
                 }
                 if (lastIndex != -1)
                     notifyItemChanged(lastIndex)*/
                if (context != null) {
                    hideKeyboards(view)
                }
            }
            linBottom.visibility = if (lastIndex == position) View.VISIBLE else View.GONE
            linTop.setBackgroundColor(ContextCompat.getColor(context!!, if (lastIndex == position) R.color.white else R.color.gray))

            val listUsers: ArrayList<User> = ArrayList()

            for (userId in item.usersIds) {
                val user = User()
                user.id = userId
                listUsers.add(user)
            }

            rvFrndList.adapter = ChatsSearchUsersAdapter(context, listUsers)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        foundItems = if (query.isEmpty()) {
            emptyList()
        } else {
            items.filter { it.name?.contains(query, true)!! }
        }
        lastIndex = -1
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return foundItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePublicChat(items: List<CreateChatGroupResponse>) {
        this.items = items
        notifyDataSetChanged()
    }
}