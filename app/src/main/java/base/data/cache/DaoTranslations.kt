package base.data.cache

import androidx.room.Dao
import androidx.room.Query
import base.data.model.other.Translation

@Dao
interface DaoTranslations : BaseDao<Translation> {

    /*Title/Body check should be also there as it might be edited*/

    fun insertOrUpdatePost(item: Translation) {
        val itemsFromDB: Translation? = getPostTranslation(item.postId, item.oriTitle, item.oriBodyText)
        if (itemsFromDB == null) insert(listOf(item))
        else update(item)
    }

    fun insertOrUpdateComment(item: Translation) {
        val itemsFromDB: Translation? = getCommentTranslation(item.postId, item.commentId, item.oriTitle)
        if (itemsFromDB == null) insert(listOf(item))
        else update(item)
    }

    @Query("SELECT * FROM Translation WHERE :postId=postId AND :oriTitle=oriTitle AND :oriBodyText=oriBodyText")
    fun getPostTranslation(postId: String, oriTitle: String, oriBodyText: String): Translation?

    @Query("SELECT * FROM Translation WHERE :commentId=commentId AND :postId=postId AND :oriTitle=oriTitle") //comment translation should be post specific
    fun getCommentTranslation(postId: String, commentId: String, oriTitle: String): Translation?

    @Query("DELETE FROM Translation")
    fun deleteAll()
}