package base.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import base.data.model.other.LiveMatch
import io.reactivex.Flowable

@Dao
interface DaoLiveMatches {

    @Insert(onConflict = REPLACE)
    fun insertAll(items: List<LiveMatch>)

    @Query("SELECT * FROM LiveMatch ORDER BY date DESC")
    fun getAll(): Flowable<List<LiveMatch>>

    @Query("DELETE FROM LiveMatch")
    fun deleteAll()
}