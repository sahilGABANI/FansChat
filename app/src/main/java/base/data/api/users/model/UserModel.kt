package base.data.api.users.model

import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetUserResponse(

    @field:SerializedName("count")
    val count: Int = 0,

    @field:SerializedName("data")
    val data: ArrayList<FansChatUserDetails>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    )

data class FansChatUserDetails(
    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("wallPosts")
    val wallPosts: Int? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("daysUsingSSK")
    val daysUsingSSK: Int? = null,

    @field:SerializedName("keywords")
    val keywords: List<String>? = null,

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("displayName")
    var displayName: String? = null,

    @field:SerializedName("county")
    val county: String? = null,

    @field:SerializedName("receivedLikes")
    val receivedLikes: Int? = null,

    @field:SerializedName("videosWatched")
    val videosWatched: Int? = null,

    @field:SerializedName("capsLvL")
    val capsLvL: Int? = null,

    @field:SerializedName("id")
    val idN: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("videoChats")
    val videoChats: Int? = null,

    @field:SerializedName("avatarUrl")
    var avatarUrl: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean? = null,

    @field:SerializedName("matches")
    val matches: Int? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("followers")
    val followers: Int? = null,

    @field:SerializedName("connectedRooms")
    val connectedRooms: ArrayList<String>? = null,

    @field:SerializedName("commentsCount")
    val commentsCount: Int? = null,

    @field:SerializedName("following")
    val following: Int? = null,

    @Nullable
    @field:SerializedName("chats")
    val chats: Int? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean? = null,

    @field:SerializedName("publicChats")
    val publicChats: Int? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("isFriendRequestPending")
    var isFriendRequestPending: Boolean = false,

    @field:SerializedName("isFriend")
    var isFriend: Boolean = false,

    @field:SerializedName("isFriendRequestSent")
    var isFriendRequestSent: Boolean = false,

    @field:SerializedName("refreshToken")
    var refreshToken: String? = null,

    @field:SerializedName("points")
    var points: Int? = null,

    ) : Serializable {
    fun toDaoFansChatUserDetails(): DaoFansChatUserDetails {
        return DaoFansChatUserDetails(
            id = id,
            firstName = firstName,
            wallPosts = wallPosts,
            lastName = lastName,
            daysUsingSSK = daysUsingSSK,
            keywords = keywords,
            city = city,
            displayName = displayName,
            county = county,
            receivedLikes = receivedLikes,
            videosWatched = videosWatched,
            capsLvL = capsLvL,
            idN = idN,
            email = email,
            phone = phone,
            videoChats = videoChats,
            avatarUrl = avatarUrl,
            created = created,
            clubId = clubId,
            isAdmin = isAdmin,
            matches = matches,
            followers = followers,
            connectedRooms = connectedRooms,
            commentsCount = commentsCount,
            following = following,
            chats = chats,
            isGlobal = isGlobal,
            publicChats = publicChats,
            updated = updated,
            isFriendRequestPending = isFriendRequestPending,
            isFriendRequestSent = isFriendRequestSent,
            isFriend = isFriend,
            refreshToken = refreshToken
        )
    }
}

data class Location(

    @field:SerializedName("coordinates")
    val coordinates: ArrayList<String> = arrayListOf()
)

@Entity
data class DaoFansChatUserDetails(
    @PrimaryKey
    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("wallPosts")
    val wallPosts: Int? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("daysUsingSSK")
    val daysUsingSSK: Int? = null,

    @field:SerializedName("keywords")
    val keywords: List<String?>? = null,

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("displayName")
    var displayName: String? = null,

    @field:SerializedName("county")
    val county: String? = null,

    @field:SerializedName("receivedLikes")
    val receivedLikes: Int? = null,

    @field:SerializedName("videosWatched")
    val videosWatched: Int? = null,

    @field:SerializedName("capsLvL")
    val capsLvL: Int? = null,

    @field:SerializedName("id")
    val idN: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @Nullable
    @field:SerializedName("videoChats")
    val videoChats: Int? = null,

    @field:SerializedName("avatarUrl")
    var avatarUrl: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean? = null,

    @field:SerializedName("matches")
    val matches: Int? = null,

//    @Nullable
//    @field:SerializedName("friends")
//    val friends: ArrayList<Any> = arrayListOf(),

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("followers")
    val followers: Int? = null,

    @field:SerializedName("connectedRooms")
    val connectedRooms: ArrayList<String>? = null,

    @field:SerializedName("commentsCount")
    val commentsCount: Int? = null,

    @field:SerializedName("following")
    val following: Int? = null,

    @field:SerializedName("chats")
    val chats: Int? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean? = null,

    @field:SerializedName("publicChats")
    val publicChats: Int? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("isFriendRequestPending")
    var isFriendRequestPending: Boolean = false,

    @field:SerializedName("isFriend")
    var isFriend: Boolean = false,

    @field:SerializedName("isFriendRequestSent")
    var isFriendRequestSent: Boolean = false,

    @field:SerializedName("refreshToken")
    var refreshToken: String? = null,
)
