package base.ui.adapter.other

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.extension.showToast
import base.extension.toDate
import base.socket.model.GroupMessages
import base.ui.fragment.main.ChatFragment
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class MessagesAdapter(
    private val context: Context,
    val fragment: ChatFragment,
    private val userIds: String,
    val items: ArrayList<GroupMessages>,
    val callback: (pos: Int, GroupMessages) -> Unit
) : Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOADING = 2
    private var isLoaderVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> {
                ProgressHolder(parent.inflate(R.layout.item_progress))
            }
            MY_MESSAGES -> {
                MessageHolder(parent.inflate(R.layout.item_my_message))
            }
            else -> {
                MessageHolder(parent.inflate(R.layout.item_message))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageHolder) {
            val message = items[position]
            if (message.id.isNotBlank()) {
//                if (message.readFlag == false) {
//                    message.readFlag = true
//                    fragment.chatCacheRepository.addNewMessages(arrayListOf(message))
//                    fragment.sortListAndSetAdapter(openChat = false)
//                }
                holder.apply {
                    text.text = message.message
                    message.sender?.let { sender ->
                        date.text =
                            if (sender.id == userIds) fragment.getString(R.string.you) + if (message.created != null) " | " + message.created?.toDate()
                                ?.toChatRelative()
                            else ""
                            else {
                                if (message.created != null) message.created?.toDate()
                                    ?.toChatRelative()
                                else ""
                            }
                    }
                    image.isVisible = message.type == "image" || message.type == "video" || message.type == "audio"
                    text.isVisible = if ((message.type == "image" || message.type == "video" || message.type == "audio")) !(message.message.isNullOrBlank()) else true
                    if (image.isVisible) {
                        val aspectRatio = message.imageAspectRatio
                        image.layoutParams.height =
                            (fragment.requireContext().resources.getDimensionPixelOffset(R.dimen._85sdp) / (if (aspectRatio == null || aspectRatio < 0.66666) {
                                0.66666
                            } else aspectRatio)).roundToInt()

                        image.layoutParams.width = fragment.requireContext().resources.getDimensionPixelOffset(R.dimen._85sdp)
                    }
                    when (message.type) {
                        "image" -> {
                            when {
                                (message.sender?.id ?: 0) == userIds -> {
                                    progressBar.visibility = View.GONE
                                    Glide.with(context)
                                        .load(message.url)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .dontAnimate()
                                        .error(R.drawable.msg_placeholder)
                                        .placeholder(R.drawable.msg_placeholder)
                                        .into(image)
                                }
                                !message.url.isNullOrEmpty() -> {
                                    progressBar.visibility = View.GONE
                                    Glide.with(context)
                                        .load(message.url)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .error(R.drawable.msg_placeholder)
                                        .dontAnimate()
                                        .placeholder(R.drawable.msg_placeholder)
                                        .into(image)
                                }
                                else -> {
                                    Glide.with(context)
                                        .load(R.drawable.msg_placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .error(R.drawable.msg_placeholder)
                                        .dontAnimate()
                                        .placeholder(R.drawable.msg_placeholder)
                                        .into(image)
                                    progressBar.visibility = View.GONE
                                }
                            }
                        }
                        "video" -> when {
                            !message.thumbnailUrl.isNullOrEmpty() -> {
                                progressBar.visibility = View.GONE
                                Glide.with(context)
                                    .load(message.thumbnailUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .dontAnimate()
                                    .error(R.drawable.msg_placeholder)
                                    .placeholder(R.drawable.msg_placeholder)
                                    .into(image)
                            }
                            !message.url.isNullOrEmpty() -> {
                                Glide.with(context)
                                    .load(message.url)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .frame(1500)
                                    .dontAnimate()
                                    .error(R.drawable.msg_placeholder) //
                                    .placeholder(R.drawable.msg_placeholder)
                                    .into(image)
                            }
                            else -> {
                                Glide.with(context)
                                    .load(R.drawable.msg_placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .error(R.drawable.msg_placeholder)
                                    .dontAnimate()
                                    .placeholder(R.drawable.msg_placeholder)
                                    .into(image)
                                progressBar.visibility = View.GONE
                            }
                        }
                        else -> {
                            Glide.with(context)
                                .load( R.drawable.placeholder_audio)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .error(R.drawable.msg_placeholder)
                                .dontAnimate()
                                .placeholder(R.drawable.msg_placeholder)
                                .into(image)
                            progressBar.visibility = View.GONE
                        }
                    }
                    image.onClick {
                        when (message.type) {
                            "image" -> {
                                if (!message.url.isNullOrBlank()) {
                                    hideKeyboards(view)
                                    viewImage(message.url)
                                } else if (message.sender?.id ?: 0 == userIds && !message.localPath.isNullOrBlank()) {
                                    hideKeyboards(view)
                                    if (File(message.localPath!!).exists()) viewImage(/*Uri.fromFile(File(*/
                                        message.localPath!!/*))*/
                                    )
                                    else fragment.showToast(fragment.getString(R.string.this_file_no_longer_exist))
                                }
                            }
                        }
                    }
                    if ((message.sender?.id ?: 0) == userIds) {
                        play.isVisible = (message.type == "video") || (message.type == "audio")
                    } else {
                        play.isVisible = (message.type == "video" && !message.url.isNullOrBlank()) || (message.type == "audio" && !message.url.isNullOrBlank())
                    }
                    play.onClick {
                        hideKeyboards(view)
                        when (message.type) {
                            "video" -> if (!message.url.isNullOrBlank()) itemView.context.viewVideo(
                                message.url
                            )
                            else if (message.sender?.id ?: 0 == userIds && !message.localPath.isNullOrBlank()) {
                                if (File(message.localPath!!).exists()) itemView.context.viewVideo(
                                    message.localPath
                                )
                                else fragment.showToast(fragment.getString(R.string.this_file_no_longer_exist))
                            }
                            "audio" -> if (!message.url.isNullOrBlank()) {
                                if (message.url!!.extension.lowercase(Locale.getDefault()) == "caf") {
                                    fragment.showToast(fragment.getString(R.string.cant_play))
                                } else {
                                    fragment.viewAudio(message.url!!)
                                }
                            } /*else if (message.senderId == userIds && !message.localPath.isNullOrBlank()) {
                                fragment.viewAudio(message.localPath)
                            }*/
                        }
                    }
                    avatar.onClick {
                        hideKeyboards(view)
                        message.sender?.let {
                            itemView.open(R.id.friendDetails, "userId" to it.id.toString())
                        }
                    }
                    if ((message.sender?.id ?: 0) == userIds) {
                        wrapper.onLongClick { callback.invoke(position, message) }
                        container.onLongClick { callback.invoke(position, message) }
                        image.onLongClick { callback.invoke(position, message) }
                    } else {
                        if (message.type == "text") {
                            wrapper.onClick {
                                fragment.dialogTranslate(text, message) {
                                    holder.text.text = it.message
                                }
                            }
                        }
                    }

                    message.sender?.let {
                        name.text = if ((message.sender.id ?: 0) == userIds) {
                                ""
                            } else {
                                it.displayName + " | "
                            }
                        Glide.with(context)
                            .load(it.avatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .error(R.drawable.avatar_placeholder)
                            .placeholder(R.drawable.avatar_placeholder)
                            .dontAnimate()
                            .into(avatar)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (userIds == null) {
            fragment.askToLogin()
            return OTHER_MESSAGES
        }
        val message = items[position]
        return when {
            userIds != null && message.sender?.id ?: 0 == userIds -> {
                MY_MESSAGES
            }
            else -> {
                OTHER_MESSAGES
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}