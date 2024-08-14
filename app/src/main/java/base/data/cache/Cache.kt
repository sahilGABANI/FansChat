package base.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.users.model.DaoFansChatUserDetails
import base.data.model.CommonFeedItem
import base.data.model.NotificationsData
import base.data.model.User
import base.data.model.chat.Chat
import base.data.model.feed.FeedItem
import base.data.model.other.LiveMatch
import base.data.model.other.Match
import base.data.model.other.Translation
import base.data.model.wall.ContentItem
import base.data.model.wall.FeedSetupItem
import base.data.model.wall.TickerData
import base.socket.model.GroupMessages
import com.facebook.ads.NativeAd
import timber.log.Timber

@Database(entities = [User::class, FeedItem::class, CommonFeedItem::class, Chat::class, LiveMatch::class, Match::class, TickerData::class, FeedSetupItem::class, ContentItem::class, Translation::class, CreateChatGroupResponse::class, GroupMessages::class, DaoFansChatUserDetails::class, NotificationsData::class],
    version = 39,
    exportSchema = true)
@TypeConverters(Converters::class)
abstract class Cache : RoomDatabase() {

    abstract fun daoUsers(): DaoUser
    abstract fun daoFeed(): DaoFeed
    abstract fun daoChats(): DaoChats
    abstract fun daoLiveMatches(): DaoLiveMatches
    abstract fun daoWallTicker(): DaoWallTicker
    abstract fun daoWallModule(): DaoWallModule
    abstract fun daoConversationList(): DaoConversationList
    abstract fun daoConversation(): DaoConversation
    abstract fun daoWallContent(): DaoWallContent
    abstract fun daoNews(): DaoNews
    abstract fun daoTranslation(): DaoTranslations
    abstract fun daoPersonalProfile(): DaoPersonalProfile
    abstract fun daoNotifications(): DaoNotifications

    companion object {
        lateinit var cache: Cache

        fun initCache(context: Context) {
            cache = databaseBuilder(context.applicationContext, Cache::class.java, "cache").addCallback(object :
                Callback() {
                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    Timber.e("onDestructiveMigration")
                }
            }).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        }
    }
}

fun <T> Cache.save(items: List<T>) {
    if (items.isEmpty()) return

    when (items[0]) {
        is Chat -> {
            daoChats().deleteAll()
            daoChats().insertAll(items as List<Chat>)
        }
//        is News -> daoNews().insertAll(items as List<News>)
        is User -> daoUsers().insertAll(items as List<User>)
        is FeedItem -> {
            daoFeed().deleteAll()
            daoFeed().insertAll(items.filter { it !is NativeAd } as List<FeedItem>)

//            daoNews().deleteAll() // force news refresh as well
        }
        is LiveMatch -> {
            daoLiveMatches().deleteAll()
            daoLiveMatches().insertAll(items as List<LiveMatch>)
        }
    }
}

fun <T> Cache.save(item: T) {
    when (item) {
        is User -> daoUsers().insert(item)
//        is News -> daoNews().insert(item)
        is FeedItem -> daoFeed().insert(item)
        is Chat -> daoChats().insert(item)
    }
}

fun <T> Cache.isNotSaved(item: T): Boolean {
    val record = when (item) {
        is User -> daoUsers().getImmediately(item.id)
        is Chat -> daoChats().getImmediately(item.id)
        else -> null
    }
    return record == null
}
