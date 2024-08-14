package base.data.model.chat

import androidx.room.Entity
import androidx.room.PrimaryKey
import base.util.json.IdConverter
import base.util.json.IdSerializer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.Serializable

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class ConversationList : Serializable {

    @PrimaryKey
    var id: String = ""

    var name: String = ""

    @JsonProperty("avatarUrl")
    var avatarUrl: String? = null

    @JsonProperty("isPublic")
    var isPublic: Boolean = false

    @JsonProperty("isOfficial")
    var isOfficial: Boolean = false

    @JsonProperty("isGlobal")
    var isGlobal: Boolean = false

    @JsonProperty("owner")
    val owner: String? = null

    @JsonProperty("blockUsers")
    val blockUsers: ArrayList<String> = arrayListOf()

    @JsonProperty("mutedList")
    val mutedList: ArrayList<String> = arrayListOf()

    @JsonProperty("usersIds")
    val usersIds: ArrayList<String> = arrayListOf()

    @JsonProperty("created")
    val created: String? = null

    @JsonProperty("updated")
    val updated: String? = null

    @JsonProperty("message")
    val message: String? = null
}