package base.data.model.other

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.util.json.DateConverterString
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity
class Match : Serializable {

    @PrimaryKey
    @JsonProperty("match_date")
    @JsonDeserialize(converter = DateConverterString::class)
    @TypeConverters(Converters::class)
    var date: Date = Date()

    @JsonProperty("match_hometeam_name")
    var nameFirst = ""

    @JsonProperty("match_awayteam_name")
    var nameSecond = ""

    @JsonProperty("league_name")
    var league = ""

    @JsonProperty("match_time")
    var time = ""

    @JsonProperty("match_hometeam_score")
    var firstTeamScored = ""
    @JsonProperty("match_awayteam_score")
    var secondTeamScored = ""

    var score = ""
        get() = "$firstTeamScored - $secondTeamScored"

    @JsonProperty("goalscorer")
    @TypeConverters(Converters::class)
    var goalscorers = ArrayList<Goalscorer>()

    fun getDateTag(): String {
        val dateTagFormatter = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault())
        return dateTagFormatter.format(date)
    }
}