package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import base.data.model.wall.ContentItem
import io.reactivex.Flowable

@Dao
interface DaoWallContent : BaseDao<ContentItem> {
    fun insertOrUpdate(items: List<ContentItem>) {
        items.forEach {
            val itemsFromDB: ContentItem? = getPostByModule(it.type, it._id, it.moduleType)//moduleType
            if (itemsFromDB == null) insert(listOf(it))
            else update(it)
        }
    }

    @Query("SELECT * FROM ContentItem WHERE :type = type COLLATE NOCASE AND :moduleType = moduleType COLLATE NOCASE ORDER BY created DESC")
    fun getContentList(type: String, moduleType: String): Flowable<List<ContentItem>>

    //SELECT * FROM ContentItem WHERE 'news' = type COLLATE NOCASE AND 'Gallery' = moduleType COLLATE NOCASE AND title LIKE "%FOOTBALL%" OR bodyText LIKE "%FOOTBALL%" OR subTitle LIKE "%FOOTBALL%" ORDER BY updated DESC
    @RawQuery(observedEntities = [ContentItem::class]) // raw query as it may structured at runtime
    fun getContentListByFilter(type: SimpleSQLiteQuery): Flowable<List<ContentItem>>

    @Query("SELECT * FROM ContentItem WHERE :type = type COLLATE NOCASE AND :id=_id AND :moduleType = moduleType COLLATE NOCASE")
    fun getPostByModule(type: String, id: String, moduleType: String): ContentItem?

    @Query("SELECT * FROM ContentItem WHERE :type = type COLLATE NOCASE AND :id=_id ")
    fun getPost(type: String, id: String): ContentItem?

    @Query("DELETE FROM ContentItem WHERE :type = type COLLATE NOCASE AND :id=_id")
    fun deletePostById(id: String, type: String)

    @Query("DELETE FROM ContentItem")
    fun deleteAll()
}