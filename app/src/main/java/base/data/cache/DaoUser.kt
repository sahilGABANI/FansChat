package base.data.cache

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import base.data.model.User
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface DaoUser {

    @Insert(onConflict = REPLACE)
    fun insertAll(users: List<User>)

    @Insert(onConflict = REPLACE)
    fun insert(user: User)

    @Query("SELECT * FROM User WHERE :id = id")
    fun get(id: String): Flowable<User>

    @Query("SELECT * FROM User WHERE :id = id")
    fun getUser(id: String): Single<User>

    @Query("SELECT * FROM User WHERE isMe = 1 ORDER BY id ASC")
    fun getCurrent(): Flowable<User>

    @Query("SELECT * FROM User WHERE isMe = 1 ORDER BY id ASC")
    fun getCurrentImmediately(): User?

    @Query("SELECT * FROM User WHERE :id = id")
    fun getImmediately(id: String): User?

    @Query("SELECT * FROM User WHERE friend = 1")
    fun getFriends(): Flowable<List<User>>

    @Query("SELECT * FROM User ORDER BY id ASC")
    fun getAllUsers(): List<User>

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM User")
    fun deleteAll()
}