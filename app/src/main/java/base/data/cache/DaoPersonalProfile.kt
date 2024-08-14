package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import base.data.api.users.model.DaoFansChatUserDetails
import base.data.api.users.model.FansChatUserDetails
import io.reactivex.Single

@Dao
interface DaoPersonalProfile: BaseDao<DaoFansChatUserDetails> {
    fun insertOrUpdate(items: DaoFansChatUserDetails) {
        items.let {
            val itemsFromDB: FansChatUserDetails? = get(it.id)
            if (itemsFromDB == null)
                insert(listOf(it))
            else
                update(it)
        }
    }

    @Query("SELECT * FROM DaoFansChatUserDetails WHERE id = :uid")
    fun get(uid: String): FansChatUserDetails?

    @Query("SELECT * FROM DaoFansChatUserDetails WHERE id = :userId")
    fun getUser(userId: String): Single<FansChatUserDetails>

}