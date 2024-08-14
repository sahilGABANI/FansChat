package base.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.util.json.CountConverter
import base.util.json.DateConverterUnix
import base.util.json.LocationConverter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.Serializable
import java.util.*

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class User : Serializable {

    @PrimaryKey
    @JsonProperty("userId")
    var id: String = ""

    var name: String
        get() {
            return if (firstName.isNotBlank()) {
                "$firstName $lastName".trim()
            } else {
                displayName
            }
        }
        set(name) {
            displayName = name
            firstName = name.split(" ")[0]
            lastName = if (name.contains(" ")) name.split(" ")[1] else ""
        }

    var displayName = ""
    var firstName = ""
    var lastName = ""
    var userType: String? = null

    @JsonProperty("avatarUrl")
    var avatar: String? = null

    @JsonProperty("circularAvatarUrl")
    var circularAvatarUrl: String? = null

    @JsonProperty("userName")
    var email: String = ""

    var phone: String? = null

    @JsonProperty("subscribed")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @TypeConverters(Converters::class)
    var regDate: Date = Date()

    @JsonIgnore
    var isLoggedIn: Boolean = false

    @JsonIgnore
    var isMe: Boolean = false

    @JsonProperty("aFriend")
    var friend: Boolean = false

    @JsonProperty("isFriendPendingRequest")
    var isFriendRequestPending = false

    @JsonDeserialize(converter = LocationConverter::class)
    var location: String = ""

    @JsonProperty("friendsCount")
    var friendsCount: Int = 0

    @JsonProperty("comments")
    var commentsCount: Int = 0

    @JsonProperty("iFollowHim")
    var following = false

    var wallPosts = 0
    var likes = 0
    var videosWatched = 0
    var chats = 0
    var videoChats = 0
    var publicChats = 0
    var matchesHome = 0
    var matchesAway = 0

    @JsonProperty("following")
    var followingCount = 0
    var followersCount = 0

    @JsonProperty("isGlobal")
    var isGlobal: Boolean = false

    @JsonProperty("capsValue")
    @JsonDeserialize(converter = CountConverter::class)
    var points = 0

    @JsonDeserialize(converter = CountConverter::class)
    var level = 0

    var progress = 0.0
    var badgeCount = 0

    override fun equals(other: Any?): Boolean {
        return id == (other as User).id
                && email == other.email // this part fixes feed loading for newly registered users
    }

    override fun hashCode() = id.hashCode()
}