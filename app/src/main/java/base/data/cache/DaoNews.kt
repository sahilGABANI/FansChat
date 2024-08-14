package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import base.data.model.CommonFeedItem
import io.reactivex.Flowable

@Dao
interface DaoNews : BaseDao<CommonFeedItem> {
    fun insertOrUpdate(items: List<CommonFeedItem>) {
        items.forEach {
            val itemsFromDB: CommonFeedItem? = getPost(it.type, it._id)//moduleType
            if (itemsFromDB == null)
                insert(listOf(it))
            else
                update(it)
        }
    }

    @Query("SELECT * FROM CommonFeedItem WHERE :type = type COLLATE NOCASE")//ORDER BY created DESC
    fun getPostList(type: String): Flowable<List<CommonFeedItem>>

    @Query("SELECT * FROM CommonFeedItem WHERE :type = type COLLATE NOCASE AND :id=_id")
    fun getPost(type: String, id: String): CommonFeedItem?

    @Query("DELETE FROM CommonFeedItem WHERE :type = type COLLATE NOCASE")
    fun deletePostsByType(type: String)

    @Query("DELETE FROM CommonFeedItem WHERE :type = type COLLATE NOCASE AND :id=_id")
    fun deletePostById(id: String, type: String)

    @Query("DELETE FROM CommonFeedItem")
    fun deleteAll()

}