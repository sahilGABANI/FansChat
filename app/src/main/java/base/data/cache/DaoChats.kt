package base.data.cache

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import base.data.model.chat.Chat
import io.reactivex.Flowable

@Dao
interface DaoChats {

    @Insert(onConflict = REPLACE)
    fun insertAll(items: List<Chat>)

    @Insert(onConflict = REPLACE)
    fun insert(item: Chat)

    @Query("SELECT * FROM Chat")
    fun getAll(): Flowable<List<Chat>>

    @Query("SELECT * FROM Chat WHERE id = :id")
    fun get(id: String): Flowable<Chat>

    @Query("SELECT * FROM Chat WHERE id = :id")
    fun getImmediately(id: String): Chat?

    @Update
    fun update(item: Chat)

    @Delete
    fun delete(item: Chat)

    @Query("DELETE FROM Chat")
    fun deleteAll()
}