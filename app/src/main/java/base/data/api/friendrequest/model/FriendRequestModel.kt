package base.data.api.friendrequest.model

import base.data.api.authentication.model.FansChatUser
import base.data.api.users.model.FansChatUserDetails
import com.google.gson.annotations.SerializedName

data class FriendRequestRequest(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("message")
    val message: String,
)

data class FriendRequestResponse(
    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("receiver")
    val receiver: FansChatUser,
)

data class GetFriendRequestRequest(
    @field:SerializedName("perPage")
    val perPage: Int,

    @field:SerializedName("page")
    val page: Int,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean? = null,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean = false,

    @field:SerializedName("displayName")
    var displayName: String? = null,

    @field:SerializedName("userType")
    val userType: String? = null,

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("country")
    val country: String? = null,

    @field:SerializedName("online")
    val online: Boolean? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("keywords")
    val keywords: String? = null,
)

data class GetFriendRequestResponse(
    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("sender")
    val sender: String,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("created")
    val created: String,

    @field:SerializedName("updated")
    val updated: String,

    @field:SerializedName("receiver")
    val receiver: FansChatUserDetails,
)

data class AcceptFriendRequestResponse(
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("friend")
    val friend: FansChatUserDetails,
)

data class ListOfFriendRequest(
    @field:SerializedName("data")
    val data: List<FriendRequest>? = null,

    @field:SerializedName("count")
    val count: Int? = null
)

data class SenderData(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("avatar")
    val avatar: String? = null,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean? = null,

    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("isAdmin")
    val isAdmin: Boolean? = null
)

data class FriendRequest(

    @field:SerializedName("receiver")
    val receiver: String? = null,

    @field:SerializedName("sender")
    val sender: SenderData? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null
)

data class ReportRequest(
    @field:SerializedName("message")
    val message: String?= null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("reportedUserId")
    val reportedUserId: String? = null

)

data class ReportResponse (

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("clubId")
    var clubId: String? = null,

    @field:SerializedName("reportedUserId")
    var reportedUserId: String? = null,

    @field:SerializedName("complainingUserId")
    var complainingUserId: String? = null,

    @field:SerializedName("message")
    var message: String? = null,

    @field:SerializedName("created")
    var created: String? = null,

    @field:SerializedName("updated")
    var updated: String? = null

)
