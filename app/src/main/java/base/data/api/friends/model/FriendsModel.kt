package base.data.api.friends.model

import base.data.api.users.model.FansChatUserDetails
import com.google.gson.annotations.SerializedName

data class GetListOfFriendsResponse(
    @field:SerializedName("count")
    val count: Int = 0,

    @field:SerializedName("data")
    val listOfUser: List<FansChatUserDetails>? = null,
)

data class FriendsResponse(
    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("online")
    val online: Boolean = false,

    @field:SerializedName("userType")
    val userType: String? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean = false,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean = false,

    @field:SerializedName("country")
    val country: String? = null,

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("location")
    val location: ArrayList<Int>? = null,

    @field:SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    )

data class DeleteFriendsRequest(
    @field:SerializedName("userId")
    val userId: String? = null,
)

data class TranslateMessage(
    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("translations")
    val translations: Translations? = null,
)

data class Translations(
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("translated")
    val translated: String? = null,

    @field:SerializedName("checkSum")
    val checkSum: String? = null,

    @field:SerializedName("language")
    val language: String? = null,
)

data class ProfileToggleRequest(val mute: String? = null, val unmute: String? = null)
