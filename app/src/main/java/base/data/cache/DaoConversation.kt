package base.data.cache

import androidx.room.*
import base.data.api.chat.model.CreateChatGroupResponse
import base.socket.model.GroupMessages
import io.reactivex.Flowable

@Dao
interface DaoConversation: BaseDao<GroupMessages> {

    fun insertOrUpdate(items: List<GroupMessages>) {
        items.forEach {
            val itemsFromDB: GroupMessages? = get(it.id)//moduleType
            if (itemsFromDB == null)
                insert(listOf(it))
            else
                update(it)
        }
    }

    fun deleteMessage(item: GroupMessages) {
        delete(item)
    }

    @Query("SELECT * FROM GroupMessages WHERE groupId = :groupId")
    fun getAll(groupId: String): List<GroupMessages>

    @Query("SELECT * FROM GroupMessages WHERE groupId = :id")
    fun get(id: String): GroupMessages?

    @Query("SELECT * FROM GroupMessages WHERE groupId = :groupId ORDER BY created DESC")
    fun getAllByOrder(groupId: String): List<GroupMessages>

    @Query("SELECT * FROM GroupMessages WHERE groupId = :groupId ORDER BY updated ASC LIMIT 1")
    fun getLastMsg(groupId: String): GroupMessages?

    @Query("DELETE FROM GroupMessages WHERE id = :id")
    fun deleteMsgById(id: String)

    @Query("DELETE FROM GroupMessages")
    fun deleteAll()

    @Query("DELETE FROM GroupMessages WHERE groupId = :groupId")
    fun deleteMessageByChatId(groupId: String)
}