package base.data

import base.data.api.Api
import base.data.model.feed.FeedItem
import base.data.model.feed.FeedItems
import base.data.model.other.Match
import base.data.model.other.Table
import base.extension.showToast
import base.ui.base.BaseBottomSheetBaseFragment
import base.ui.base.BaseFragment
import base.util.*

object SyncedStorage {

    fun BaseFragment.setAutoTranslateEnabled(isEnabled: Boolean) {
        saveBoolean(AUTO_TRANSLATE, isEnabled)
    }

    fun BaseFragment.setNotificationsEnabled(type: String, isEnabled: Boolean) {
        saveBoolean(type, isEnabled)

        compositeDisposable.add(
            Api.setNotificationsEnabled(type, isEnabled)
                .inBackground().subscribe({ }, { showToast(it.msg) })
        )
    }

/*
    fun Fragment.translate(
        post: FeedItem, language: String,
        callback: (FeedItem) -> Unit
    ) {
//        val post = (if (item is PostShare) item.referencedItem else item) ?: return
        val translation = post.translation(language)

        if (translation != null && (translation.bodyText.isNotEmpty() && translation.title.isNotEmpty())) {
            post.title = translation.title
            post.body = translation.bodyText
            callback.invoke(post)
        } else {
            disposables.add(
                Api.translate(post, language)
                    .inBackground().subscribe({
                        */
/*post.translations.add(Translation(language, it.title, it.body))
                        post.title = it.title
                        post.body = it.body*//*

                        if (it.author.id.isBlank()) {
                            it.author = post.author
                        }
                        cache.save(it)
                    }, {
                        Timber.e(it)
                        //showToast(it.msg)
                    })
            )
        }
    }
*/

    /*fun Fragment.translate(
        message: Message, language: String,
        callback: (Message) -> Unit
    ) {

        disposables.add(
            Api.translate(message, language)
                .inBackground().subscribe({ callback.invoke(it) }, { showToast(it.msg) })
        )
    }*/

    /*  private fun FeedItem.translation(language: String) =
          translations.find { it.language == language }*/

    fun BaseFragment.getMatches(leagueId: Int, callback: (List<Match>) -> Unit) {
        Api.getMatches(leagueId).inBackground().subscribe({ callback.invoke(it) }, { showToast(it.msg) }).autoDispose()
    }

    fun BaseFragment.getTables(leagueId: Int, callback: (List<Table>) -> Unit) {
        Api.getTable(leagueId).inBackground().subscribe({ callback.invoke(it) }, { showToast(it.msg) }).autoDispose()
    }

    //Match Info

    //Todo: keep it until get the REST API
    fun BaseBottomSheetBaseFragment.submitRepost(item: FeedItem, callback: () -> Unit) {

        var pinPost = FeedItems()
        pinPost.id = item.id
        pinPost.type = item.type
        pinPost.authorId = item.authorId
        pinPost.date = item.date
        pinPost.likes = item.likes
        pinPost.commentsCount = item.commentsCount
        pinPost.shareCount = item.shareCount
        pinPost.sharedMessage = item.sharedMessage
        pinPost.referencedItemId = item.referencedItemId
        pinPost.referencedItemClub = item.referencedItemClub
        pinPost.image = item.image
        pinPost.coverAspectRatio = item.coverAspectRatio
        pinPost.title = item.title
        pinPost.body = item.body
        pinPost.postId = item.postId

//        Api.publish(pinPost)
//            .inBackground().subscribe({
//                cache.save(it.apply {
//                    it.referencedItem = item.referencedItem; it.author = item.author
//                })
//                callback.invoke()
//            }, { showToast(it.msg); cache.delete(it) })
    }

    fun BaseBottomSheetBaseFragment.submitPost(item: Any, callback: (obj: Any?) -> Unit) {

//        when (item) {
//            is FeedItem -> Api.publish(item)
//                .inBackground().subscribe({
//                    cache.save(it.apply {
//                        it.referencedItem = item.referencedItem; it.author = item.author
//                    })
//                    callback.invoke(it)
//                }, { showToast(it.msg); cache.delete(it) })
//
//            is Message -> Api.publish(item)
//                .inBackground().subscribe({
//                    cache.save(it.apply { it.sender = item.sender })
//                    callback.invoke(it)
//                }, {
//                    showToast(it.msg)
//                    cache.delete(it)
//                })
//
//            is Comment -> Api.publish(item)
//                .inBackground().subscribe({
//                    cache.save(it.apply {
//                        it.sender = item.sender; it.message = item.message
//                    }); callback.invoke(it)
//                },
//                    { showToast(it.msg); cache.delete(it) })
//
//            is Chat -> Api.publish(item)
//                .inBackground().subscribe({ cache.save(it); callback.invoke(it) },
//                    {
//                        callback.invoke(null)
//                        showToast(it.msg); cache.delete(it)
//                    })
//        }
    }
}
