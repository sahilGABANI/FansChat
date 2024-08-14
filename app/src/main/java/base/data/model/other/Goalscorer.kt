package base.data.model.other

import com.fasterxml.jackson.annotation.JsonProperty

class Goalscorer {

    @JsonProperty("time")
    var minute = ""

    @JsonProperty("home_scorer")
    var homeScorer = ""

    @JsonProperty("away_scorer")
    var awayScorer = ""

    var name = ""
        get() = homeScorer + awayScorer

    var isHome = homeScorer.isNotEmpty()
}