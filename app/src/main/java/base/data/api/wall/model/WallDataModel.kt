package base.data.api.wall.model

import androidx.room.TypeConverters
import base.data.api.users.model.FansChatUserDetails
import base.data.cache.MessageDateConverter
import base.data.model.CommonFeedItem
import base.data.model.other.Translation
import base.extension.CLUB_ID
import base.util.json.DateConverterUnix
import base.util.json.DateSerializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.SerializedName
import java.util.*

data class ListOfWallPostResponse(
    @field:SerializedName("count") val count: Int = 0,

    @field:SerializedName("data") val data: ArrayList<CommonFeedItem>? = null
)

data class WallPostRequest(
    @field:SerializedName("type") val type: String = "",

    @field:SerializedName("title") val title: String? = null,

    @field:SerializedName("subTitle") val subTitle: String? = null,

    @field:SerializedName("bodyText") var bodyText: String? = null,

    @field:SerializedName("coverImageUrl") val coverImageUrl: String? = null,

    @field:SerializedName("imageUrl") var imageUrl: String? = null,

    @field:SerializedName("videoUrl") var videoUrl: String? = null,

    @field:SerializedName("thumbnailUrl") var thumbnailUrl: String? = null,

    @field:SerializedName("language") val language: String? = null,

    @field:SerializedName("contentType") var contentType: String? = null,

    @field:SerializedName("coverAspectRatio") var coverAspectRatio: Double? = null
)

data class CommentResponse(
    @field:SerializedName("count") val count: Int = 0,

    @field:SerializedName("data") val data: ArrayList<Comment>? = null
)

data class AddCommentsResponse(
    @field:SerializedName("post") val post: CommonFeedItem? = null,

    @field:SerializedName("comment") val comment: Comment? = null,
)

data class Comment(
    @field:SerializedName("authorId") val authorId: String? = null,

    @field:SerializedName("comment") var comment: String? = null,

    @field:SerializedName("wallId") val wallId: String? = null,

    @field:SerializedName("postId") val postId: String? = null,

    @field:SerializedName("_id") val id: String? = null,

    @JsonDeserialize(converter = DateConverterUnix::class)
    @JsonSerialize(converter = DateSerializer::class)
    @TypeConverters(MessageDateConverter::class)
    var created: Date? = null,

    @JsonDeserialize(converter = DateConverterUnix::class)
    @JsonSerialize(converter = DateSerializer::class)
    @TypeConverters(MessageDateConverter::class) var updated: Date? = null,

    @field:SerializedName("edited") val edited: Boolean = false,

    @field:SerializedName("author") val author: FansChatUserDetails? = null
)

data class AddCommentRequest(
    @field:SerializedName("comment") val comment: String,

    @field:SerializedName("wallId") val wallId: String,

    @field:SerializedName("postId") val postId: String? = null
)

data class DeleteWallPostCommentRequest(
    @field:SerializedName("commentId") val commentId: String? = null,
)

data class UpdateComment(
    @field:SerializedName("comment") val comment: String,

    @field:SerializedName("postId") val id: String,
)

data class UpdateLike(@field:SerializedName("like") val like: Boolean)

data class SharingResponse(val url: String = "")

data class TranslateResponse(val _id: String = "", val translations: Translation? = null)

data class ReportPostRequest(
    @field:SerializedName("reportedPostId") val reportedPostId: String,
    @field:SerializedName("message") val message: String?,
    @field:SerializedName("clubId") val clubId: String = "" + CLUB_ID
)

data class ReportPostResponse(
    val _id: String,
    val clubId: String,
    val reportedPostId: String,
    val complainingUserId: String,
    val message: String,
    val created: String,
    val updated: String
)