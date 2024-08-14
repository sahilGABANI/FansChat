package base.data.cache

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import base.data.model.feed.FeedItem
import io.reactivex.Flowable

@Dao
interface DaoFeed {

    @Insert(onConflict = REPLACE)
    fun insertAll(items: List<FeedItem>)

    @Insert(onConflict = REPLACE)
    fun insert(item: FeedItem)

    @Query("SELECT * FROM FeedItem ORDER BY date DESC")
    fun getAll(): Flowable<List<FeedItem>>

    @Query("SELECT * FROM FeedItem WHERE id = :id")
    fun get(id: String): Flowable<FeedItem>

    @Query("SELECT * FROM FeedItem WHERE id = :id")
    fun getImmediately(id: String): FeedItem?

    @Update
    fun update(item: FeedItem)

    @Query("UPDATE feeditem SET  likes= :likes WHERE id =:id")
    fun updateLikeCount(likes: Int, id: String)

    @Delete
    fun delete(item: FeedItem)

    @Query("DELETE FROM FeedItem")
    fun deleteAll()
}