package base.socket.model

import base.data.api.users.model.FansChatUserDetails
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SomeOneLeftGroupPayload(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("sender")
    val sender: String? = null,

    @field:SerializedName("room")
    val room: String? = null,

    @field:SerializedName("event")
    val event: String? = null,

    @field:SerializedName("message")
    val message: MessageRes? = null,

    @field:SerializedName("messageId")
    val messageId: String? = null
)

data class MessageGroupPayload(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("sender")
    val sender: FansChatUserDetails? = null,

    @field:SerializedName("groupChatName")
    val groupChatName: String? = null,

    @field:SerializedName("groupChatId")
    val groupChatId: String? = null,

    @field:SerializedName("room")
    val room: String? = null,

    @field:SerializedName("event")
    val event: String? = null,

    @field:SerializedName("message")
    val message: Any? = null
) {
    val safeMessage: MessageResWithSender?
        get() = if (message is String) null else Gson().fromJson(Gson().toJson(message), MessageResWithSender::class.java)
}

data class NotificationWallPost(
    @field:SerializedName("postId")
    val postId: String? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,
)

data class UpdateWallPostLikePayload(
    @field:SerializedName("postId")
    val postId: String? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,

    @field:SerializedName("liked")
    val liked: Boolean = false,

    @field:SerializedName("userId")
    val userId: String? = null,
)

data class AddNewWallPostCommentNotification(
    @field:SerializedName("comment")
    val comment: AddNewWallPostComment? = null,
)

data class AddNewWallPostComment(
    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("comment")
    val comment: String? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,

    @field:SerializedName("postId")
    val postId: String? = null,

    @field:SerializedName("authorId")
    val authorId: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("author")
    val author: Owner? = null,

    @field:SerializedName("post")
    val post: PostDetails? = null,
)

data class PostDetails(
    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("commentsCount")
    val commentsCount: Int? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,
)

data class DeleteWalPostDetails(
    @field:SerializedName("postId")
    val postId: String? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,

    @field:SerializedName("commentId")
    val commentId: String? = null,
)

data class NotificationPayload(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("sender")
    val sender: String? = null,

    @field:SerializedName("data")
    val data: ArrayList<NotificationData>? = null
)

data class NotificationData(
    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("ownerId")
    val ownerId: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("event")
    val event: InAppNotificationType? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("sender")
    val sender: String? = null
)

data class AddNewPostNotification(

    @field:SerializedName("_id")
    val _id: String? = null,

    @field:SerializedName("wallId")
    val wallId: String? = null,

    @field:SerializedName("postTitle")
    val postTitle: String? = null,

    @field:SerializedName("authorName")
    val authorName: String? = null,

    @field:SerializedName("event")
    val event: String? = null,

    val like: Boolean = false,
    val postId: String? = null,
    val commentId: String? = null,
    val senderId: String? = null,
    val likeCount: Int = 0
)

data class PostData(
    @field:SerializedName("_id")
    val _id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("subTitle")
    val subTitle: String? = null,

    @field:SerializedName("bodyText")
    val bodyText: String? = null,
)

data class ChatMessageNotification(
    @field:SerializedName("groupChatName")
    val groupChatName: String? = null,
    @field:SerializedName("room")
    val room: String? = null,
    @field:SerializedName("event")
    val event: String? = null,
    @field:SerializedName("groupChatId")
    val groupChatId: String? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("senderName")
    val senderName: String? = null,
    @field:SerializedName("senderId")
    val senderId: String? = null,
)

enum class InAppNotificationType {
    @SerializedName("friendsRequestDecline")
    friendsRequestDecline,

    @SerializedName("friendsRequestAccepted")
    friendsRequestAccepted,

    @SerializedName("friendsRequestReceived")
    friendsRequestReceived,

    @SerializedName("friendRemove")
    friendRemove,

}
