package base.ui.fragment.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.data.model.NotificationsData
import base.databinding.LayoutNotificationBinding
import base.socket.SocketService
import base.socket.model.InAppNotificationType
import base.util.getSectionType
import base.util.onClick
import base.util.toChatRelative

class NotificationAdapter(val context: Context, val list: ArrayList<NotificationsData>, private val currentUserId: String?, val callback: (notification: NotificationsData) -> Unit) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_notification, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            binding.tvTitle.text = getTitleString(list[position].event)
            binding.tvDesc.text = getDescString(
                    list[position].event,
                    list[position].sender?.displayName ?: "",
                    list[position].message,
                    list[position].postTitle,
                    list[position].senderId,
                    list[position].wallId,
            )
            binding.tvDateTime.text = list[position].created?.toChatRelative() ?: ""
            binding.cardMain.onClick {
                callback.invoke(list[position])
            }
        }
    }

    private fun getDescString(event: String, senderName: String, message: String, postTitle: String?, senderId: String?, wallId: String?): String {
        return when (event) {
            InAppNotificationType.friendsRequestReceived.name -> context.getString(R.string.friend_request_msg, senderName)
            InAppNotificationType.friendsRequestAccepted.name -> context.getString(R.string.friend_request_accepted_msg, senderName)
            SocketService.EVENT_MESSAGE_UPDATE -> context.getString(R.string.new_media_msg_msg, senderName)
            SocketService.EVENT_SEND_MESSAGE -> context.getString(R.string.new_msg_msg, senderName)
            SocketService.EVENT_UPDATE_POST_LIKE -> context.getString(if (senderId == wallId && wallId != currentUserId) R.string.desc_like_post_owner else if (wallId != currentUserId) R.string.desc_like_post_friend else R.string.desc_like_post, senderName)
            SocketService.EVENT_NEW_WALL_POST -> context.getString(R.string.desc_post_created, senderName)
            SocketService.EVENT_UPDATE_WALL_POST -> context.getString(R.string.desc_post_updated, senderName)
            SocketService.EVENT_NEW_POST_COMMENT -> context.getString(if (senderId == wallId && wallId != currentUserId) R.string.desc_new_comment_owner else if (wallId != currentUserId) R.string.desc_new_comment_friend else R.string.desc_new_comment, senderName)
            SocketService.EVENT_UPDATE_POST_COMMENT -> context.getString(if (senderId == wallId && wallId != currentUserId) R.string.desc_comment_updated_owner else if (wallId != currentUserId) R.string.desc_comment_updated_friend else R.string.desc_comment_updated, senderName)
            SocketService.EVENT_NEW_NEWS, SocketService.EVENT_NEW_SOCIAL, SocketService.EVENT_NEW_VIDEO, SocketService.EVENT_NEW_RUMOR -> context.getString(R.string.desc_club_post_created, context.getString(R.string.club_name), postTitle)
            SocketService.EVENT_UPDATE_NEWS, SocketService.EVENT_UPDATE_SOCIAL, SocketService.EVENT_UPDATE_VIDEO, SocketService.EVENT_UPDATE_RUMOR -> context.getString(R.string.desc_club_post_updated, context.getString(R.string.club_name), postTitle)
            SocketService.EVENT_NEW_NEWS_COMMENT, SocketService.EVENT_NEW_SOCIAL_COMMENT, SocketService.EVENT_NEW_VIDEO_COMMENT, SocketService.EVENT_NEW_RUMOR_COMMENT -> context.getString(R.string.desc_new_club_comment, senderName, postTitle)
            SocketService.EVENT_UPDATE_NEWS_COMMENT, SocketService.EVENT_UPDATE_SOCIAL_COMMENT, SocketService.EVENT_UPDATE_VIDEO_COMMENT, SocketService.EVENT_UPDATE_RUMOR_COMMENT -> context.getString(R.string.desc_club_comment_updated, senderName, postTitle)
            SocketService.EVENT_UPDATE_SOCIAL_LIKE, SocketService.EVENT_UPDATE_NEWS_LIKE, SocketService.EVENT_UPDATE_VIDEO_LIKE, SocketService.EVENT_UPDATE_RUMOR_LIKE -> context.getString(R.string.desc_like_club_post, senderName, postTitle)
            else -> message
        }
    }

    private fun getTitleString(event: String): String {
        return when (event) {
            InAppNotificationType.friendsRequestReceived.name -> context.getString(R.string.received_friend_request)
            InAppNotificationType.friendsRequestAccepted.name -> context.getString(R.string.accept_friend_request)
            SocketService.EVENT_MESSAGE_UPDATE -> "Updated Message"
            SocketService.EVENT_SEND_MESSAGE -> context.getString(R.string.message_received)
            SocketService.EVENT_UPDATE_POST_LIKE -> context.getString(R.string.title_like_post)
            SocketService.EVENT_NEW_WALL_POST -> context.getString(R.string.title_post_created)
            SocketService.EVENT_UPDATE_WALL_POST -> context.getString(R.string.title_post_updated)
            SocketService.EVENT_NEW_POST_COMMENT -> context.getString(R.string.title_new_comment)
            SocketService.EVENT_UPDATE_POST_COMMENT -> context.getString(R.string.title_comment_updated)
            SocketService.EVENT_NEW_NEWS, SocketService.EVENT_NEW_SOCIAL, SocketService.EVENT_NEW_VIDEO, SocketService.EVENT_NEW_RUMOR -> context.getString(R.string.title_club_post_created, context.getSectionType(event))
            SocketService.EVENT_UPDATE_NEWS, SocketService.EVENT_UPDATE_SOCIAL, SocketService.EVENT_UPDATE_VIDEO, SocketService.EVENT_UPDATE_RUMOR -> context.getString(R.string.title_club_post_updated, context.getSectionType(event))
            SocketService.EVENT_NEW_NEWS_COMMENT, SocketService.EVENT_NEW_SOCIAL_COMMENT, SocketService.EVENT_NEW_VIDEO_COMMENT, SocketService.EVENT_NEW_RUMOR_COMMENT -> context.getString(R.string.title_new_club_comment, context.getSectionType(event))
            SocketService.EVENT_UPDATE_NEWS_COMMENT, SocketService.EVENT_UPDATE_SOCIAL_COMMENT, SocketService.EVENT_UPDATE_VIDEO_COMMENT, SocketService.EVENT_UPDATE_RUMOR_COMMENT -> context.getString(R.string.title_club_comment_updated, context.getSectionType(event))
            SocketService.EVENT_UPDATE_NEWS_LIKE, SocketService.EVENT_UPDATE_SOCIAL_LIKE, SocketService.EVENT_UPDATE_VIDEO_LIKE, SocketService.EVENT_UPDATE_RUMOR_LIKE -> context.getString(R.string.title_like_club_post, context.getSectionType(event))
            else -> event
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = LayoutNotificationBinding.bind(view)
    }

}
