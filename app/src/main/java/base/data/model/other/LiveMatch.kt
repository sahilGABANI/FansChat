package base.data.model.other

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.util.json.DateConverterUnix
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.Serializable
import java.util.*

@Entity
class LiveMatch : Serializable {

    @PrimaryKey
    @JsonProperty("matchDate")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @TypeConverters(Converters::class)
    var date: Date = Date()

    @JsonProperty("firstClubName")
    var nameFirst: String = ""

    @JsonProperty("secondClubName")
    var nameSecond: String = ""

    @JsonProperty("firstClubUrl")
    var imageFirst: String = ""

    @JsonProperty("secondClubUrl")
    var imageSecond: String = ""

    var ticketsUrl: String? = null
}