package base.ui.fragment.other.friends.requests

import base.data.model.User
import base.util.json.DateConverterUnix
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.Serializable
import java.util.*

data class FriendRequest(

    val id: String,

    val sender: User,

    @JsonProperty("timestamp")
    @JsonDeserialize(converter = DateConverterUnix::class)
    var date: Date?
) : Serializable