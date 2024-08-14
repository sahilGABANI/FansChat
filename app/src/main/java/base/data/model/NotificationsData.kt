package base.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.data.cache.MessageDateConverter
import base.util.json.DateConverterUnix
import base.util.json.DateSerializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class GetNotificationRequest(@field:SerializedName("page") val page: Int? = null,

    @field:SerializedName("perPage") val perPage: Int? = null

)

data class NotificationResponse(
    @field:SerializedName("count") val count: Int = 0,

    @field:SerializedName("data") val listOfNotifications: List<NotificationsData>? = null,
)

@Entity
data class NotificationsData(

    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val senderId: String? = null,

    @TypeConverters(Converters::class) var sender: Sender? = null,

    @JsonDeserialize(converter = DateConverterUnix::class) @JsonSerialize(converter = DateSerializer::class) @TypeConverters(
        MessageDateConverter::class) var created: Date? = null,

    @JsonDeserialize(converter = DateConverterUnix::class) @JsonSerialize(converter = DateSerializer::class) @TypeConverters(
        MessageDateConverter::class) var updated: Date? = null,

    val _id: String = "",
    val ownerId: String? = null,
    val wallId: String? = null,
    val postTitle: String? = null,
    val authorName: String? = null,
    val postId: String = "",
    val title: String? = null,
    val event: String = "",
    val room: String? = null,

    val groupChatId: String = "",
    val type: String? = null,
    val groupChatName: String? = null,
    val senderName: String? = null,
    val message: String = "",
) : Serializable

data class Sender(@field:SerializedName("_id") var id: String? = null,

    var firstName: String = "",

    var lastName: String = "",

    var displayName: String = "",

    var avatarUrl: String? = null, var isOfficial: Boolean? = false,

    var online: Boolean? = false, var points: Int? = 0) : Serializable