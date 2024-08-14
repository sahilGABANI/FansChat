package base.data.model.other

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Translation : Serializable {
    var translated: String = ""
    var language: String = ""
    var title: String = ""
    var bodyText: String = ""
    var subtitle: String = ""
    var comment: String = ""

    var commentId: String = ""
    var postId: String = ""
    var oriTitle: String = ""
    var oriBodyText: String = ""

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}