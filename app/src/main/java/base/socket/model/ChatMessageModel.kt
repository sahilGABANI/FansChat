package base.socket.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import base.data.api.users.model.FansChatUserDetails
import base.data.network.model.ErrorMessages
import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("uploadStatus")
    val uploadStatus: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
)


data class GetGroupUserChatMessagesResponse(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("data")
    val data: ArrayList<GroupMessages>? = null,
)

@Entity
data class GroupMessages(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @PrimaryKey
    @field:SerializedName("_id")
    val id: String,

    @field:SerializedName("sender")
    val sender: Owner? = null,

    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("message")
    var message: String? = null,

    @field:SerializedName("type")
    var type: String? = null,

    @field:SerializedName("uploadStatus")
    var uploadStatus: String? = null,

    @field:SerializedName("url")
    var url: String? = null,

    @field:SerializedName("localPath")
    var localPath: String? = null,

    @field:SerializedName("created")
    var created: String? = null,

    @field:SerializedName("updated")
    var updated: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Double? = null,

    @field:SerializedName("edited")
    val edited: Boolean = false,

    @field:SerializedName("thumbnailUrl")
    var thumbnailUrl: String? = null,

    var imageAspectRatio: Double? = 0.0,

    var readFlag: Boolean? = false
)


data class SendMessage(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("sender")
    val sender: Owner? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("uploadStatus")
    val uploadStatus: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Double? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("errors")
    val errors: ArrayList<ErrorMessages>? = null,
)

data class UpdateChatMessageRequest(
    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("uploadStatus")
    val uploadStatus: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Double? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
)

data class MessageRes(

    @field:SerializedName("_id")
    val id: String? = null,

//    @field:SerializedName("sender")
//    val sender: String? = null,

    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("uploadStatus")
    val uploadStatus: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Double? = null,

    @field:SerializedName("url")
    val url: String? = null,
)


data class MessageResWithSender(

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("sender")
    val sender: FansChatUserDetails? = null,

    @field:SerializedName("groupId")
    val groupId: String? = null,

    @field:SerializedName("clubId")
    val clubId: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

    @field:SerializedName("uploadStatus")
    val uploadStatus: String? = null,

    @field:SerializedName("imageAspectRadio")
    val imageAspectRadio: Double? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
)