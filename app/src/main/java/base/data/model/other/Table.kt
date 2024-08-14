package base.data.model.other

import com.fasterxml.jackson.annotation.JsonProperty

data class Table(

    @JsonProperty("overall_league_position") val position: Int,

    @JsonProperty("team_name") val teamName: String,

    @JsonProperty("overall_league_payed") val Pl: Int,

    @JsonProperty("overall_league_W") val W: Int,

    @JsonProperty("overall_league_D") val D: Int,

    @JsonProperty("overall_league_L") val L: Int,

    @JsonProperty("overall_league_GA") val GA: Int,

    @JsonProperty("overall_league_GF") val GF: Int,

    @JsonProperty("overall_league_PTS") val points: Int)