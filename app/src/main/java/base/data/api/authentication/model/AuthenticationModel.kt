package base.data.api.authentication.model

import base.data.network.model.ErrorMessages
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("password")
    val password: String? = null,

    @field:SerializedName("fireBaseToken")
    val fireBaseToken: String? = null,
)

data class RegisterRequest(
    @field:SerializedName("password")
    val password: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("fireBaseToken")
    val fireBaseToken: String? = null,
)

data class LoginResponse(
    @field:SerializedName("user")
    val user: FansChatUser,

    @field:SerializedName("token")
    val token: String,

    @field:SerializedName("refreshToken")
    val refreshToken: String,
)

data class FirebaseResponse(
    @field:SerializedName("fireBaseToken")
    val fireBaseToken: String,
)

data class FansChatUser(

    @field:SerializedName("wallPosts")
    val wallPosts: Int? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("daysUsingSSK")
    val daysUsingSSK: Int? = null,

    @field:SerializedName("keywords")
    val keywords: List<String?>? = null,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("receivedLikes")
    val receivedLikes: Int? = null,

    @field:SerializedName("videosWatched")
    val videosWatched: Int? = null,

    @field:SerializedName("capsLvL")
    val capsLvL: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("videoChats")
    val videoChats: Int? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean? = null,

    @field:SerializedName("matches")
    val matches: Int? = null,

//    @field:SerializedName("friends")
//    val friends: List<Any>? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("followers")
    val followers: Int? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("connectedRooms")
    val connectedRooms: List<Any?>? = null,

    @field:SerializedName("commentsCount")
    val commentsCount: Int? = null,

    @field:SerializedName("following")
    val following: Int? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean? = null,

    @field:SerializedName("chats")
    val chats: Int? = null,

    @field:SerializedName("location")
    val location: Location? = null,

    @field:SerializedName("_id")
    val uid: String? = null,

    @field:SerializedName("publicChats")
    val publicChats: Int? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

data class FriendsItem(
    val any: Any? = null
)

data class Location(
    @field:SerializedName("coordinates")
    val coordinates: List<Any>? = null
)


data class FansChatCommonMessage(
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("success")
    val success: Boolean = false,

    @field:SerializedName("errors")
    val errors: List<ErrorMessages>? = null
)

data class UpdateProfileRequest(
    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("country")
    var country: String? = null,

    @field:SerializedName("city")
    var city: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("passwordConfirm")
    val passwordConfirm: String? = null,
)

data class ChangePasswordRequest(
    @field:SerializedName("currentPassword")
    val currentPassword: String? = null,

    @field:SerializedName("password")
    val password: String? = null,

    @field:SerializedName("passwordConfirm")
    val passwordConfirm: String? = null,
)

data class RefreshTokenRequest(
    @field:SerializedName("refreshToken")
    val refreshToken: String? = null,
)

data class RequestPasswordRequest(
    @field:SerializedName("email")
    val email: String? = null,
)

data class RequestPasswordTokenRequest(
    @field:SerializedName("password")
    val password: String? = null,

    @field:SerializedName("passwordRepeat")
    val passwordRepeat: String? = null,
)

data class RefreshTokenResponse(
    @field:SerializedName("token")
    val token: String? = null,
)

data class FacebookLoginRequest(
    @field:SerializedName("token")
    val token: String,

    @field:SerializedName("email")
    var email: String? = null,

    @field:SerializedName("fireBaseToken")
    val fireBaseToken: String? = null,
)

data class UpdateAvatarRequest(
    @field:SerializedName("avatarUrl")
    val avatarUrl: String? = null,
)

data class FaceBookResponse(
    @field:SerializedName("name")
    val userName: String? = null,
    @field:SerializedName("id")
    val facebookId: String? = null,
    @field:SerializedName("email")
    val userEmail: String? = null,
)