package base.socket.model

import base.data.api.wall.model.Comment
import base.data.model.CommonFeedItem
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetWallRequest(
    @field:SerializedName("page") val page: Int? = null,

    @field:SerializedName("perPage") val perPage: Int? = null,

    @field:SerializedName("id") val id: String? = null,

    @field:SerializedName("lang") val lang: String? = null,
)

data class GetWallResponse(

    @field:SerializedName("data") val data: List<CommonFeedItem>? = null,

    @field:SerializedName("success") val success: Boolean = false,

    @field:SerializedName("count") val count: Int? = null,

    @field:SerializedName("errors") val errors: Int? = null

)

data class WallPostError(

    @field:SerializedName("message") val message: String? = null,

    @field:SerializedName("param") val param: String? = null,

    @field:SerializedName("success") val success: Boolean = false,
)

data class Hi(

    @field:SerializedName("type") val type: String? = null,

    @field:SerializedName("properties") val properties: Properties? = null) : Serializable

data class Translations(

    @field:SerializedName("type") val type: String? = null,

    @field:SerializedName("properties") val properties: Properties? = null) : Serializable

/*

data class WallFeedItem(

    @field:SerializedName("owner")
    val owner: Owner? = null,

    @field:SerializedName("coverImageUrl")
    val coverImageUrl: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("language")
    val language: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("bodyText")
    val bodyText: String? = null,

    @field:SerializedName("subTitle")
    val subTitle: String? = null,

    @field:SerializedName("videoUrl")
    val videoUrl: String? = null,

    @field:SerializedName("success")
    val success: Int? = null,

    @field:SerializedName("translations")
    val translations: ArrayList<Translations>? = null,

    @field:SerializedName("imageUrl")
    val imageUrl: String? = null,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,

    @field:SerializedName("likeCount")
    val likeCount: Int = 0,

    @field:SerializedName("commentsCount")
    val commentsCount: Int = 0,

    @field:SerializedName("shareCount")
    val shareCount: Int = 0,

    @field:SerializedName("likedByMe")
    val likedByMe: Boolean = false,

    @field:SerializedName("sourceLanguage")
    val sourceLanguage: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("ownerId")
    val ownerId: String? = null,

    @field:SerializedName("type")
    val type: String? = null
) : Serializable
*/

data class Properties(

    @field:SerializedName("hi") val hi: Hi? = null,

    @field:SerializedName("bodyText") val bodyText: String? = null,

    @field:SerializedName("subtitle") val subtitle: String? = null,

    @field:SerializedName("title") val title: String? = null) : Serializable

data class Owner(

    @field:SerializedName("firstName") val firstName: String? = null,

    @field:SerializedName("lastName") val lastName: String? = null,

    @field:SerializedName("displayName") val displayName: String? = null,

    @field:SerializedName("created") val created: String? = null,

    @field:SerializedName("isOnline") val isOnline: Boolean? = null,

    @field:SerializedName("_id") val id: String? = null,

    @field:SerializedName("isAdmin") val isAdmin: Boolean? = null,

    @field:SerializedName("updated") val updated: String? = null,

    @field:SerializedName("email") val email: String? = null,

    @field:SerializedName("avatarUrl") val avatarUrl: String? = null,

    @field:SerializedName("isFriend") val isFriend: Boolean = false,

    @field:SerializedName("isFriendRequestPending") val isFriendRequestPending: Boolean = false,

    @field:SerializedName("clubId") val clubId: String? = null,

    @field:SerializedName("city") val city: String? = null,

    @field:SerializedName("isGlobal") val isGlobal: Boolean = false,

    val wallPosts: Int = 0,
    val receivedLikes: Int = 0,
    val commentsCount: Int = 0,

    ) : Serializable

data class WallItemRequest(
    @field:SerializedName("id") val id: String,
)


data class SpecificItemResponse(
    @field:SerializedName("success") val success: Boolean? = null,

    @field:SerializedName("_id") val id: String? = null,

    @field:SerializedName("title") val title: String? = null,

    @field:SerializedName("subTitle") val subTitle: String? = null,

    @field:SerializedName("bodyText") val bodyText: String? = null,

    @field:SerializedName("coverImageUrl") val coverImageUrl: String? = null,

    @field:SerializedName("imageUrl") val imageUrl: String? = null,

    @field:SerializedName("videoUrl") val videoUrl: String? = null,

    @field:SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,

    @field:SerializedName("language") val language: String? = null,

    @field:SerializedName("translations") val translations: ArrayList<Translations>? = null,

    @field:SerializedName("comments") val comments: List<Comment>? = null,

    @field:SerializedName("wallId") val wallId: String? = null,

    @field:SerializedName("created") val created: String? = null,

    @field:SerializedName("updated") val updated: String? = null,

    @field:SerializedName("sourceLanguage") val sourceLanguage: String? = null,

    @field:SerializedName("url") val url: String? = null,
/*
    @field:SerializedName("likes")
    val likes: ArrayList<String>? = null,*/

    @field:SerializedName("likeCount") val likeCount: Int = 0,

    @field:SerializedName("commentsCount") val commentsCount: Int? = 0,

    @field:SerializedName("shareCount") val shareCount: Int? = 0,
)

data class Author(

    @field:SerializedName("firstName") val firstName: String? = null,

    @field:SerializedName("lastName") val lastName: String? = null,

    @field:SerializedName("displayName") val displayName: String? = null,

    @field:SerializedName("created") val created: String? = null,

    @field:SerializedName("isOnline") val isOnline: Boolean? = null,

    @field:SerializedName("_id") val id: String? = null,

    @field:SerializedName("isAdmin") val isAdmin: Boolean? = null,

    @field:SerializedName("updated") val updated: String? = null,

    @field:SerializedName("email") val email: String? = null)

data class WallId(

    @field:SerializedName("firstName") val firstName: String? = null,

    @field:SerializedName("lastName") val lastName: String? = null,

    @field:SerializedName("displayName") val displayName: String? = null,

    @field:SerializedName("created") val created: String? = null,

    @field:SerializedName("isOnline") val isOnline: Boolean? = null,

    @field:SerializedName("_id") val id: String? = null,

    @field:SerializedName("isAdmin") val isAdmin: Boolean? = null,

    @field:SerializedName("updated") val updated: String? = null,

    @field:SerializedName("email") val email: String? = null)
