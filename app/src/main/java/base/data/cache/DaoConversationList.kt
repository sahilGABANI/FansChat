package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import base.data.api.chat.model.CreateChatGroupResponse
import io.reactivex.Single

@Dao
interface DaoConversationList: BaseDao<CreateChatGroupResponse> {

    fun insertOrUpdate(items: List<CreateChatGroupResponse>) {
        items.forEach {
            val itemsFromDB: CreateChatGroupResponse? = get(it.id)//moduleType
            if (itemsFromDB == null)
                insert(listOf(it))
            else
                update(it)
        }
    }

    @Query("SELECT * FROM CreateChatGroupResponse")
    fun getAll(): Single<List<CreateChatGroupResponse>>

    @Query("SELECT * FROM CreateChatGroupResponse WHERE id = :id")
    fun get(id: String): CreateChatGroupResponse?

    @Query("DELETE FROM CreateChatGroupResponse")
    fun deleteAll()

    @Query("DELETE FROM CreateChatGroupResponse WHERE id = :groupId")
    fun deleteGroup(groupId: String)
}