package base.data.api

import android.content.Context
import base.data.model.User
import base.data.model.other.Match
import base.data.model.other.Table
import base.util.Ignorable
import base.util.json.FileUploader
import io.reactivex.Flowable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

object Api {

    var previousUser: User? = null

    fun getUser(userId: String): Flowable<User> {
        Timber.tag("tagx").d("load user from api")
        return Flowable.empty()
        /* return requestUser<User>(
             "getUserInfoById",
             "userId" to userId
         ).map { it.fillDataIfOfficialAccount() }*/
    }

    fun upload(ctx: Context, file: File): Single<String> {
        return FileUploader(ctx).amplifyFileUpload(file)
    }

    fun setNotificationsEnabled(type: String, isEnabled: Boolean): Flowable<Ignorable> {
        return Flowable.empty()
/*
        return when (type) {
            WALL_NOTIFS -> request(
                "wallSetMuteValue",
                "value" to isEnabled.toString()
            )
            else -> {
                request(
                    if (isEnabled)
                        "unmutePushNotifications" else "mutePushNotifications",
                    "type" to type
                )
            }
        }
*/
    }
/*
    fun translate(post: FeedItem, language: String): Flowable<FeedItem> {

        val endpoint = when (post.type) {
            newsOfficial, newsUnOfficial -> "translateNewsItem"
            social -> "translateSocialItem"
            else -> "translateWallPost"
        }
        return request(
            endpoint, hashMapOf(
                "post" to post,
                "toLanguage" to language
            )
        )
    }*/

/*
    fun translate(message: Message, language: String): Flowable<Message> {
        return Flowable.empty()
        */
/*
            return request(
                "translateIM", hashMapOf(
                    "msgId" to message.id,
                    "toLanguage" to language
                )
            )*//*

    }
*/

/*
    fun translate(comment: Comment, language: String): Flowable<Comment> {

        return request(
            "translateComment", hashMapOf(
                "id" to comment.id,
                "toLanguage" to language
            )
        )
    }
*/

    fun getMatches(leagueId: Int): Flowable<List<Match>> {
        /* return requestList(
             "getMatchesByLeague", hashMapOf(
                 "league_id" to leagueId.toString()
             )
         )*/
        return Flowable.empty()
    }

    fun getTable(leagueId: Int): Flowable<List<Table>> {
/*
        return requestList(
            "getLeagueById", hashMapOf(
                "league_id" to leagueId.toString()
            )
        )
*/
        return Flowable.empty()
    }
}