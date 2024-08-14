package base.data.api.wall

import androidx.sqlite.db.SimpleSQLiteQuery
import base.data.api.wall.model.*
import base.data.cache.Cache
import base.data.model.CommonFeedItem
import base.data.model.other.Translation
import base.data.model.wall.ContentItem
import base.data.model.wall.FeedSetupItem
import base.data.model.wall.TickerData
import base.data.model.wall.WallData
import base.extension.CLUB_ID
import base.socket.model.GetWallRequest
import base.util.Constants
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.ResponseBody


class WallPostsRepository(private val wallPostRetrofitAPI: WallPostRetrofitAPI) {

/*
    fun getWall(): WallData {
        val inputStream: InputStream = FansChat.context.resources.openRawResource(R.raw.wall_reponse)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        inputStream.use { inputStream ->
            val reader: Reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }

        val jsonString: String = writer.toString()
        return Gson().fromJson(jsonString, WallData::class.java)
    }
*/

    fun getWallPostList(getWallRequest: GetWallRequest): Single<ListOfWallPostResponse> {
        return wallPostRetrofitAPI.getWallPost(getWallRequest.page!!, getWallRequest.perPage!!).map { it }
    }

    fun addWallPost(wallPostRequest: WallPostRequest): Single<ContentItem> {
        return wallPostRetrofitAPI.addWallPost(wallPostRequest).map { it }
    }

    fun deleteWallPost(postId: String): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteWallPost(postId).map { it }
    }

    fun getWallPostData(postId: String, lang: String): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.getWallPostData(postId, lang).map { it }
    }

    fun updateWallPost(postId: String, wallPostRequest: WallPostRequest): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.updateWallPost(postId, wallPostRequest).map { it }
    }

    fun addWallPostComment(addCommentRequest: AddCommentRequest): Single<AddCommentsResponse> {
        return wallPostRetrofitAPI.addWallPostComment(addCommentRequest).map { it }
    }

    fun deleteWallPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteWallPostComment(deleteWallPostRequest.commentId.toString())
    }

    fun getWallPostComment(postId: String, page: Int, perPage: Int): Single<CommentResponse> {
        return wallPostRetrofitAPI.getWallPostComment(postId, page, perPage).map { it }
    }

    fun updateWallPostComment(
        commentId: String,
        addCommentRequest: UpdateComment,
    ): Single<Comment> {
        return wallPostRetrofitAPI.updateWallPostComment(commentId, addCommentRequest).map { it }
    }

    fun likeWallPosts(postId: String, updateLike: UpdateLike): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.likeWallPosts(postId, updateLike).map { it }
    }

    fun getNewWall(lang: String): Single<WallData> {
        return wallPostRetrofitAPI.getNewWall("" + CLUB_ID, lang).map { it }
    }

    fun getNextCarousels(type: String, page: Int, perPage: Int, lang: String): Single<ArrayList<ContentItem>> {
        return wallPostRetrofitAPI.getNextCarousels("" + CLUB_ID, lang, type, page, perPage).map { it }
    }

    fun getNewsPostData(postId: String, lang: String): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.getNewsPostData(postId, lang).map { it }
    }

    fun getSocialPostData(postId: String, lang: String): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.getSocialPostData(postId, lang).map { it }
    }

    fun getRumoursPostData(postId: String, lang: String): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.getRumoursPostData(postId, lang).map { it }
    }

    fun getVideoPostData(postId: String, lang: String): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.getVideoPostData(postId, lang).map { it }
    }

    fun getNewsPostComment(postId: String, page: Int, perPage: Int): Single<CommentResponse> {
        return wallPostRetrofitAPI.getNewsPostComment(postId, page, perPage).map { it }
    }

    fun getSocialPostComment(postId: String, page: Int, perPage: Int): Single<CommentResponse> {
        return wallPostRetrofitAPI.getSocialPostComment(postId, page, perPage).map { it }
    }

    fun getRumoursPostComment(postId: String, page: Int, perPage: Int): Single<CommentResponse> {
        return wallPostRetrofitAPI.getRumoursPostComment(postId, page, perPage).map { it }
    }

    fun getVideoPostComment(postId: String, page: Int, perPage: Int): Single<CommentResponse> {
        return wallPostRetrofitAPI.getVideoPostComment(postId, page, perPage).map { it }
    }

    fun likeNewsPosts(postId: String, updateLike: UpdateLike): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.likeNewsPosts(postId, updateLike).map { it }
    }

    fun likeSocialPosts(postId: String, updateLike: UpdateLike): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.likeSocialPosts(postId, updateLike).map { it }
    }

    fun likeRumoursPosts(postId: String, updateLike: UpdateLike): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.likeRumoursPosts(postId, updateLike).map { it }
    }

    fun likeVideoPosts(postId: String, updateLike: UpdateLike): Single<CommonFeedItem> {
        return wallPostRetrofitAPI.likeVideoPosts(postId, updateLike).map { it }
    }

    fun addNewsPostComment(addCommentRequest: AddCommentRequest): Single<AddCommentsResponse> {
        return wallPostRetrofitAPI.addNewsPostComment(addCommentRequest).map { it }
    }

    fun addSocialPostComment(addCommentRequest: AddCommentRequest): Single<AddCommentsResponse> {
        return wallPostRetrofitAPI.addSocialPostComment(addCommentRequest).map { it }
    }

    fun addRumoursPostComment(addCommentRequest: AddCommentRequest): Single<AddCommentsResponse> {
        return wallPostRetrofitAPI.addRumoursPostComment(addCommentRequest).map { it }
    }

    fun addVideoPostComment(addCommentRequest: AddCommentRequest): Single<AddCommentsResponse> {
        return wallPostRetrofitAPI.addVideoPostComment(addCommentRequest).map { it }
    }

    fun deleteNewsPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteNewsPostComment(deleteWallPostRequest.commentId.toString())
    }

    fun deleteSocialPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteSocialPostComment(deleteWallPostRequest.commentId.toString())
    }

    fun deleteRumoursPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteRumoursPostComment(deleteWallPostRequest.commentId.toString())
    }

    fun deleteVideoPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest): Single<ResponseBody> {
        return wallPostRetrofitAPI.deleteVideoPostComment(deleteWallPostRequest.commentId.toString())
    }

    fun updateNewsPostComment(
        commentId: String,
        addCommentRequest: UpdateComment,
    ): Single<Comment> {
        return wallPostRetrofitAPI.updateNewsPostComment(commentId, addCommentRequest).map { it }
    }

    fun updateSocialPostComment(
        commentId: String,
        addCommentRequest: UpdateComment,
    ): Single<Comment> {
        return wallPostRetrofitAPI.updateSocialPostComment(commentId, addCommentRequest).map { it }
    }

    fun updateRumorsPostComment(
        commentId: String,
        addCommentRequest: UpdateComment,
    ): Single<Comment> {
        return wallPostRetrofitAPI.updateRumorsPostComment(commentId, addCommentRequest).map { it }
    }

    fun updateVideoPostComment(
        commentId: String,
        addCommentRequest: UpdateComment,
    ): Single<Comment> {
        return wallPostRetrofitAPI.updateVideoPostComment(commentId, addCommentRequest).map { it }
    }

    /*Translation*/

    fun translateNewsComment(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateNewsComment(commentId, targetLanguage).map { it }
    }

    fun translateSocialComment(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateSocialComment(commentId, targetLanguage).map { it }
    }

    fun translateRumorsComment(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateRumorsComment(commentId, targetLanguage).map { it }
    }

    fun translateVideoComment(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateVideoComment(commentId, targetLanguage).map { it }
    }

    fun translateWallComment(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateWallComment(commentId, targetLanguage).map { it }
    }

    fun translateNewsPost(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateNewsPost(commentId, targetLanguage).map { it }
    }

    fun translateRumorsPost(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateRumorsPost(commentId, targetLanguage).map { it }
    }

    fun translateSocialPost(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateSocialPost(commentId, targetLanguage).map { it }
    }

    fun translateVideoPost(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateVideoPost(commentId, targetLanguage).map { it }
    }

    fun translateWallPost(commentId: String, targetLanguage: String): Single<TranslateResponse> {
        return wallPostRetrofitAPI.translateWallPost(commentId, targetLanguage).map { it }
    }

    /*Sharing*/

    fun getShareNewsUrl(postId: String): Single<SharingResponse> {
        return wallPostRetrofitAPI.getShareNewsUrl(postId).map { it }
    }

    fun getShareSocialUrl(postId: String): Single<SharingResponse> {
        return wallPostRetrofitAPI.getShareSocialUrl(postId).map { it }
    }

    fun getShareRumoursUrl(postId: String): Single<SharingResponse> {
        return wallPostRetrofitAPI.getShareRumoursUrl(postId).map { it }
    }

    fun getShareVideoUrl(postId: String): Single<SharingResponse> {
        return wallPostRetrofitAPI.getShareVideoUrl(postId).map { it }
    }

    fun getShareWallUrl(postId: String): Single<SharingResponse> {
        return wallPostRetrofitAPI.getShareWallUrl(postId).map { it }
    }

    /*Report*/

    fun reportNewsPost(reportPostRequest: ReportPostRequest): Single<ReportPostResponse> {
        return wallPostRetrofitAPI.reportNewsPost(reportPostRequest).map { it }
    }

    fun reportSocialPost(reportPostRequest: ReportPostRequest): Single<ReportPostResponse> {
        return wallPostRetrofitAPI.reportSocialPost(reportPostRequest).map { it }
    }

    fun reportRumoursPost(reportPostRequest: ReportPostRequest): Single<ReportPostResponse> {
        return wallPostRetrofitAPI.reportRumoursPost(reportPostRequest).map { it }
    }

    fun reportVideoPost(reportPostRequest: ReportPostRequest): Single<ReportPostResponse> {
        return wallPostRetrofitAPI.reportVideoPost(reportPostRequest).map { it }
    }

    fun reportWallPost(reportPostRequest: ReportPostRequest): Single<ReportPostResponse> {
        return wallPostRetrofitAPI.reportWallPost(reportPostRequest).map { it }
    }
}

class WallCacheRepository {

    fun getTicker(): Flowable<List<TickerData>> {
        return Cache.cache.daoWallTicker().getTicker().map { it }
    }

    fun addTicker(list: List<TickerData>) {
        Cache.cache.daoWallTicker().deleteAll()
        Cache.cache.daoWallTicker().insert(list)
    }

    fun addModuleList(list: List<FeedSetupItem>) {
        Cache.cache.daoWallModule().deleteAll()
        Cache.cache.daoWallContent().deleteAll()

        val listContent = arrayListOf<ContentItem>()
        //Insert directly as table is blank
        Cache.cache.daoWallContent().insert(list.let {
            list.forEach { module ->
                module.content.forEach {
                    it.moduleType = module.title//type
//                    it.filterApplied = if (module.filters.isNotEmpty()) module.filters[0] else ""
                }
                listContent.addAll(module.content)
            }
            listContent
        })

        Cache.cache.daoWallModule().insert(list)
    }

    fun getModuleList(): Flowable<List<FeedSetupItem>> {
        return Cache.cache.daoWallModule().getDistinctModules().map { it }
    }

    fun addContentList(listContent: List<ContentItem>, moduleType: String) {
        //Insert or Update as table may have same data
        Cache.cache.daoWallContent().insertOrUpdate(listContent.let {
            it.forEach { contentItem ->
                contentItem.moduleType = moduleType
//                contentItem.filterApplied = filterApplied
            }
            listContent
        })
    }

    fun getContentList(dataSource: String,
        moduleType: String,
        listFilters: ArrayList<String>): Flowable<List<ContentItem>> {
        if (listFilters.isNullOrEmpty()) {
            return Cache.cache.daoWallContent().getContentList(if (dataSource.equals(Constants.POST_TYPE_STREAMING,
                    true)) Constants.POST_ORIGINAL_TYPE_CLUB_TV else dataSource, moduleType).map { it }
        } else {
            var strQuery = ""
            val selValues = arrayListOf(if (dataSource.equals(Constants.POST_TYPE_STREAMING,
                    true)) Constants.POST_ORIGINAL_TYPE_CLUB_TV else dataSource, moduleType)
            listFilters.forEach {
                strQuery = "$strQuery title LIKE ? OR bodyText LIKE ? OR subTitle LIKE ? OR"
                selValues.add("%$it%")
                selValues.add("%$it%")
                selValues.add("%$it%")
            }
            strQuery = strQuery.substringBeforeLast("OR")

            val query =
                SimpleSQLiteQuery("SELECT * FROM ContentItem WHERE type = ? COLLATE NOCASE AND moduleType = ? COLLATE NOCASE AND ($strQuery) ORDER BY created DESC",
                    selValues.toArray())
            return Cache.cache.daoWallContent().getContentListByFilter(query).map { it }
        }
    }

    fun getPost(type: String, id: String): ContentItem? {
        return Cache.cache.daoWallContent().getPost(if (type.equals(Constants.POST_TYPE_STREAMING,
                true)) Constants.POST_ORIGINAL_TYPE_CLUB_TV else type, id/*, type*/)
    }

    fun updatePost(contentItem: ContentItem) {
        return Cache.cache.daoWallContent().update(contentItem)
    }

    fun deletePost(contentItem: ContentItem) {
        return Cache.cache.daoWallContent().delete(contentItem)
    }

    fun deletePostById(id: String, type: String) {
        return Cache.cache.daoWallContent().deletePostById(id, type)
    }

    fun addPostTranslation(item: Translation) {
        Cache.cache.daoTranslation().insertOrUpdatePost(item)
    }

    fun addCommentTranslation(item: Translation) {
        Cache.cache.daoTranslation().insertOrUpdateComment(item)
    }

    fun getPostTranslation(item: Translation): Translation? {
        return Cache.cache.daoTranslation().getPostTranslation(item.postId, item.oriTitle, item.oriBodyText)
    }

    fun getCommentTranslation(item: Translation): Translation? {
        return Cache.cache.daoTranslation().getCommentTranslation(item.postId, item.commentId, item.oriTitle)
    }

    fun deleteTranslation(item: Translation) {
        return Cache.cache.daoTranslation().delete(item)
    }
}