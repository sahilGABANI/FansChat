package base.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import base.data.model.wall.TickerData
import io.reactivex.Flowable

@Dao
interface DaoWallTicker {

    @Insert(onConflict = REPLACE)
    fun insert(items: List<TickerData>)

    @Query("SELECT * FROM TickerData")
    fun getTicker(): Flowable<List<TickerData>>

    @Query("DELETE FROM TickerData")
    fun deleteAll()
}