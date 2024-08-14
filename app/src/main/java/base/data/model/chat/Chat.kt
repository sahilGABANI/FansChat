package base.data.model.chat

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.util.json.IdConverter
import base.util.json.IdSerializer
import base.util.json.IntConverter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.Serializable

/*"owner" -> "605b75d70c5b0be97fd6661a"
"avatarUrl" -> "https://airmap.s3.us-east-2.amazonaws.com/BAL-avatar.png"
"club_id" -> "2000"
"mutedList" -> {JSONArray@8472}  size = 0
"name" -> "BAL"
"isGlobal" -> {Boolean@8435} true
"isPublic" -> {Boolean@8435} true
"unreadCount" -> {Double@8478} 0.0
"_id" -> {JSONObject@8480}  size = 1
"usersIds" -> {JSONArray@8482}  size = 64
"isMuted" -> {Boolean@8484} false
"timestamp" -> "1526614409"*/
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class Chat : Serializable {

    @PrimaryKey
    @JsonProperty("_id")
    @JsonSerialize(converter = IdSerializer::class)//todo need to check
    @JsonDeserialize(converter = IdConverter::class)
    var id: String = ""

    var name: String = ""

    @JsonProperty("avatarUrl")
    var image: String? = null

    @JsonDeserialize(converter = IntConverter::class)
    var unreadCount = 0

    @JsonProperty("isPublic")
    var public: Boolean = false

    @JsonProperty("isMuted")
    var muted: Boolean = false

    @JsonProperty("owner")
    var ownerId: String = ""

    @TypeConverters(Converters::class)
    var usersIds: List<String> = emptyList()

    @TypeConverters(Converters::class)
    var mutedList: List<String> = emptyList()
    var club_id: String? = null
    var timestamp: String? = null

    @JsonProperty("isGlobal")
    var isGlobal: Boolean? = false
    var blockUsers: Boolean = false

    @JsonProperty("isOfficial")
    var official: Boolean? = false
}