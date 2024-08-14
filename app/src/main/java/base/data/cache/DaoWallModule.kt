package base.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import base.data.model.wall.ContentItem
import base.data.model.wall.FeedSetupItem
import io.reactivex.Flowable

@Dao
interface DaoWallModule : BaseDao<FeedSetupItem> {

    @Query("SELECT * FROM FeedSetupItem")
    fun getModules(): Flowable<List<FeedSetupItem>>

    @Query("DELETE FROM FeedSetupItem")
    fun deleteAll()

    fun getDistinctModules(): Flowable<List<FeedSetupItem>> {
        return getModules().distinctUntilChanged()

    }
}