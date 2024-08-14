package base.data.api.chat.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import base.data.network.model.ErrorMessages
import base.socket.model.Owner
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class CreateChatGroupRequest(
    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @field:SerializedName("isPublic")
    val isPublic: Boolean = false,

    @field:SerializedName("isOfficial")
    val isOfficial: Boolean = false,

    @field:SerializedName("usersIds")
    val usersIds: ArrayList<String>? = null,
)

@Entity
@Parcelize
data class CreateChatGroupResponse(
    @field:SerializedName("success")
    val success: Boolean = false,

    @PrimaryKey
    @field:SerializedName("_id")
    val id: String = "",

    @field:SerializedName("name")
    var name: String? = null,

    @field:SerializedName("avatarUrl")
    var avatarUrl: String? = null,

    @field:SerializedName("isPublic")
    var isPublic: Boolean = false,

    @field:SerializedName("isOfficial")
    val isOfficial: Boolean = false,

    @field:SerializedName("isGlobal")
    val isGlobal: Boolean = false,

    @field:SerializedName("owner")
    val owner: String? = null,

    @field:SerializedName("blockUsers")
    val blockUsers: ArrayList<String> = arrayListOf(),

    @field:SerializedName("mutedList")
    val mutedList: ArrayList<String> = arrayListOf(),

    @field:SerializedName("usersIds")
    val usersIds: ArrayList<String> = arrayListOf(),

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("members")
    val members: ArrayList<Owner> = arrayListOf(),

    @field:SerializedName("message")
    val message: String? = null,
) : Parcelable

data class DeleteChatGroupRequest(
    @field:SerializedName("groupId")
    val id: String? = null,
)

data class DeleteChatMessageRequest(
    @field:SerializedName("id")
    val id: String? = null,
)

data class DeleteChatGroupResponse(
    @field:SerializedName("success")
    val success: Boolean = false,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("errors")
    val param: List<ErrorMessages>?
)

data class UpdateChatGroupRequest(

    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @field:SerializedName("isPublic")
    val isPublic: Boolean = false,

    @field:SerializedName("isOfficial")
    val isOfficial: Boolean = false,

    @field:SerializedName("usersIds")
    val usersIds: ArrayList<String>? = null,
)

data class NewChatGroupCreatedResponse(
    @field:SerializedName("_id")
    val id: String = "",

    @field:SerializedName("name")
    var name: String? = null,
)

data class GetPublicChatGroupListRequest(

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("name")
    val param: String? = null,
)

data class GetPublicChatGroupListResponse(

    @field:SerializedName("success")
    val success: Boolean = false,

    @field:SerializedName("count")
    val count: Int = 0,

    @field:SerializedName("data")
    val data: ArrayList<CreateChatGroupResponse>? = null,

    @field:SerializedName("errors")
    val param: List<ErrorMessages>?
)

data class JoinChatGroupRequest(
    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("page")
    val page: Int = 0,

    @field:SerializedName("perPage")
    val perPage: Int = 0,
)