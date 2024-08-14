package base.data.model.feed

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import base.extension.CLUB_ID
import base.data.cache.Converters
import base.data.model.User
import base.data.model.feed.FeedItem.Type.post
import base.data.model.other.Translation
import base.util.emptyUser
import base.util.findSplitPosition
import base.util.json.*
import base.util.randomId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.Serializable
import java.util.*

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
open class FeedItem : Serializable {

    @PrimaryKey
    @JsonProperty("_id")
    @JsonDeserialize(converter = IdConverter::class)
    @JsonSerialize(converter = IdSerializer::class)
    var id = ""

    var postId = randomId()

    @JsonProperty("videoID")
    var video = ""

    @JsonProperty("vidUrl")
    var videoUrl = ""

    @JsonProperty("timestamp")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @TypeConverters(Converters::class)
    var date: Date = Date()

    @JsonProperty("pubDate")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @Ignore
    var dateForNews: Date = Date()
        set(value) {
            date = value
        }

    @JsonProperty("created_time")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @Ignore
    var dateForSocial: Date = Date()
        set(value) {
            date = value
        }

    @JsonProperty("publishedDate")
    @JsonDeserialize(converter = DateConverterString::class)
    @Ignore
    var dateForVideo: Date = Date()
        set(value) {
            date = value
        }

    @JsonProperty("wallId")
    var authorId: String = ""

    @JsonIgnore
    @TypeConverters(Converters::class)
    var author: User = emptyUser

    @JsonDeserialize(converter = ItemTypeConverter::class)
    @JsonSerialize(converter = ItemTypeSerializer::class)
    @TypeConverters(Converters::class)
    var type: Type = post

    @JsonProperty("likeCount")
    @JsonDeserialize(converter = CountConverter::class)
    var likes = 0

    @JsonProperty("likedByUser")
    var likedByMe: Boolean = false
    var commentsCount = 0

    @JsonDeserialize(converter = CountConverter::class)
    var shareCount = 0

    @JsonProperty("bodyText")
    var body = ""

    @Ignore
    @JsonProperty("description")
    var bodyForVideos = ""
        set(value) {
            body = value
            field = value
        }

    var title = ""

    @JsonProperty("coverImageUrl")
    var image: String? = null

    @JsonProperty("contentType")
    var contentType: String = ""

    @JsonProperty("contentUrl")
    var contentUrl: String = ""

    @Ignore
    @JsonProperty("image")
    var imageForNews: String? = null
        set(value) {
            image = value
            field = value
        }

    @Ignore
    @JsonProperty("thumbnail")
    var imageForVideo: String? = null
        set(value) {
            image = value
            field = value
        }

    @JsonProperty("coverAspectRatio")
    var coverAspectRatio: Float? = null

    @JsonProperty("sharedComment")
    var sharedMessage = ""
    var translatedTo: String? = null

    @JsonProperty("content")
    @Ignore
    var bodyForNews = ""
        set(value) {
            body = value
        }

    var referencedItemId = ""
    var referencedItemClub = CLUB_ID

    @TypeConverters(Converters::class)
    var referencedItem: FeedItem? = null

    @JsonProperty("message")
    @Ignore
    var titleAndBodyForSocial = ""
        set(value) {
            if (value.isEmpty()) return

            val titleEndPosition = value.findSplitPosition()

            title = value.substring(0, titleEndPosition)

            val bodyStartPosition = titleEndPosition + 1
            val bodyEndPosition = value.length - 1

            body = if (bodyStartPosition < bodyEndPosition) {
                value.substring(bodyStartPosition, bodyEndPosition)
            } else ""
        }

    var url = ""

    @JsonDeserialize(converter = TranslationsConverter::class)
    @TypeConverters(Converters::class)
    var translations = ArrayList<Translation>()

    override fun equals(other: Any?): Boolean {
        return id == (other as FeedItem).id
    }

    override fun hashCode() = id.hashCode()

    enum class Type {
        post,
        newsShare,
        betting,
        stats,
        rumor,
        wallStoreItem,
        newsOfficial,
        newsUnOfficial,
        wallComment,
        rumourShare,
        postShare,
        socialShare,
        social,
        clubtv,
        clubtvShared
    }

    fun getPostVideoUrl(): String {
        if (video.isNotEmpty()) {
            return video
        }
        if (videoUrl.isNotEmpty()) {
            return videoUrl
        }
        if (contentUrl.isNotEmpty()) {
            return contentUrl
        }
        return ""
    }
}



@Entity
open class FeedItems : Serializable {

    @PrimaryKey
    @JsonProperty("_id")
    @JsonDeserialize(converter = IdConverter::class)
    @JsonSerialize(converter = IdSerializer::class)
    var id = ""

    @JsonDeserialize(converter = ItemTypeConverter::class)
    @JsonSerialize(converter = ItemTypeSerializer::class)
    @TypeConverters(Converters::class)
    var type: FeedItem.Type = post

    @JsonProperty("wallId")
    var authorId: String = ""

    @JsonProperty("timestamp")
    @JsonDeserialize(converter = DateConverterUnix::class)
    @TypeConverters(Converters::class)
    var date: Date = Date()

    @JsonProperty("likeCount")
    @JsonDeserialize(converter = CountConverter::class)
    var likes = 0

    var commentsCount = 0

    @JsonDeserialize(converter = CountConverter::class)
    var shareCount = 0

    @JsonProperty("sharedComment")
    var sharedMessage = ""

    var referencedItemId = ""
    var referencedItemClub = CLUB_ID

    @JsonProperty("coverImageUrl")
    var image: String? = null

    @JsonProperty("coverAspectRatio")
    var coverAspectRatio: Float? = null

    var title = ""

    @JsonProperty("bodyText")
    var body = ""

    @JsonProperty("subTitle")
    var subTitle = ""

    var postId = randomId()
}