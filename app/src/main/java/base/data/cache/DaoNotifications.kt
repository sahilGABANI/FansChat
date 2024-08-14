package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import base.data.model.NotificationsData
import io.reactivex.Flowable

@Dao
interface DaoNotifications : BaseDao<NotificationsData> {
    /*fun insertOrUpdate(items: List<NotificationsData>) {
        items.forEach {
            val itemsFromDB: NotificationsData? = getNotificationById(it._id)
            if (itemsFromDB == null) insert(listOf(it))
            else update(it)
        }
    }

    @Query("SELECT * FROM NotificationsData WHERE :id=_id ")
    fun getNotificationById(id: String): NotificationsData?
*/
    @Query("SELECT * FROM NotificationsData")//ORDER BY created DESC
    fun getNotifications(): Flowable<List<NotificationsData>>

    @Query("DELETE FROM NotificationsData")
    fun deleteAll()

}