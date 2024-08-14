package base.data.model.wall

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.data.model.BaseFeedItem
import java.io.Serializable

class WallData : Serializable {
    var ticker: List<String> = listOf()

    @TypeConverters(Converters::class)
    var feedSetup: List<FeedSetupItem> = listOf()
}

@Entity
class TickerData(var ticker: String) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Entity
class FeedSetupItem : Serializable {
    @PrimaryKey
    var title: String = ""

    var type: String = ""

    var titleColor: String? = null
    var seeAll: String? = null
    var background: String? = null
    var seeAllColor: String? = null

    @TypeConverters(Converters::class)
    var filters: List<String> = listOf()
    var filterColor: String? = null
    var filterSelectedColor: String? = null
    var dataSource: String = ""

    @Ignore
    var content: ArrayList<ContentItem> = arrayListOf()

    @Ignore
    var isLastPage: Boolean = false
}

@Entity
class ContentItem : BaseFeedItem() {
    /*@PrimaryKey
    val id: String? = null
    val coverImageUrl: String = ""
    val created: String? = null
    val language: Int? = null
    val ownerId: String? = null
    val title: String = ""
    val type: Int? = null
    val url: String = ""
    val shareCount: Int = 0
    val bodyText: String = ""
    val subTitle: String = ""
    val videoUrl: String = ""
    val commentsCount: Int = 0
    val imageUrl: String = ""
    val updated: String? = null
    val sourceLanguage: Int = 0
    val thumbnailUrl: String = ""
    val likes: List<Any?> = listOf()*/
    @PrimaryKey(autoGenerate = true)
    var idPrimary: Long = 0
    var _id: String = ""

    var moduleType: String = ""

    /*Extras which not coming from api*/
//    var rating: Int = 0

}
