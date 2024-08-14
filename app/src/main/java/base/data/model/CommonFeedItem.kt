package base.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.data.cache.Converters
import base.data.cache.MessageDateConverter
import base.socket.model.Owner
import base.util.Constants
import base.util.json.DateConverterUnix
import base.util.json.DateSerializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.Serializable
import java.util.*

@Entity
class CommonFeedItem : BaseFeedItem() {
    @PrimaryKey
    var _id: String = ""
}

open class BaseFeedItem : Serializable {
    @TypeConverters(Converters::class)
    var owner: Owner? = null

    var coverImageUrl: String? = ""

    @JsonDeserialize(converter = DateConverterUnix::class)
    @JsonSerialize(converter = DateSerializer::class)
    @TypeConverters(MessageDateConverter::class)
    var created: Date? = null

    @JsonDeserialize(converter = DateConverterUnix::class)
    @JsonSerialize(converter = DateSerializer::class)
    @TypeConverters(MessageDateConverter::class)
    var updated: Date? = null

    var language: String? = null

    var title: String? = ""

    var bodyText: String? = ""

    var subTitle: String? = ""

    var videoUrl: String? = ""
    /*get() {
        return "https://vimeo.com/652677424"
    }*/

    var success: Int? = null

    //    @JsonDeserialize(converter = TranslationsConverter::class)
    //    @TypeConverters(Converters::class)
    //    var translations = ArrayList<Translation>()

    var imageUrl: String? = ""

    var thumbnailUrl: String? = null

    var likeCount: Int = 0

    var commentsCount: Int = 0

    var shareCount: Int = 0

    var likedByMe: Boolean = false

    var sourceLanguage: String? = null

    var url: String = ""

    var ownerId: String? = null

    var type: String = "0"

    var contentType: String? = null

    var coverAspectRatio: Double = -0.5625

    var wallId: String? = null

    var source: String? = null

    /*Later added params*/
    var watchCount: Int = 0

    /*
        @TypeConverters(Converters::class)
        var likes: ArrayList<Any> = arrayListOf()*/

    /*imageUrl.isNullOrBlank() && videoUrl!!.isNotEmpty()*/
    fun isVideoPost(): Boolean {
        return contentType.equals(Constants.POST_TYPE_VIDEO, true)/*videoUrl.isNotBlank()*/
    }
}