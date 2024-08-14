package base.data.api.news

import base.data.cache.Cache
import base.data.model.CommonFeedItem
import base.socket.model.GetWallRequest
import base.socket.model.GetWallResponse
import io.reactivex.Flowable
import io.reactivex.Single

class NewsPostRepository(private val newsPostRetrofitAPI: NewsPostRetrofitAPI) {
    fun getNewsPostList(getWallRequest: GetWallRequest): Single<GetWallResponse> {
        return newsPostRetrofitAPI.getNewsPost(
            getWallRequest.page!!,
            getWallRequest.perPage!!,
            getWallRequest.lang!!
        )
            .map { it }
    }

    fun getSocialPostList(getWallRequest: GetWallRequest): Single<GetWallResponse> {
        return newsPostRetrofitAPI.getSocialPost(
            getWallRequest.page!!,
            getWallRequest.perPage!!,
            getWallRequest.lang!!
        )
            .map { it }
    }

    fun getRumourPostList(getWallRequest: GetWallRequest): Single<GetWallResponse> {
        return newsPostRetrofitAPI.getRumourPost(
            getWallRequest.page!!,
            getWallRequest.perPage!!,
            getWallRequest.lang!!
        )
            .map { it }
    }

    fun getClubTvPostList(getWallRequest: GetWallRequest): Single<GetWallResponse> {
        return newsPostRetrofitAPI.getClubTvPost(
            getWallRequest.page!!,
            getWallRequest.perPage!!,
            getWallRequest.lang!!
        )
            .map { it }
    }
}

class NewsCacheRepository {

    fun deletePostsByType(type: String) {
        Cache.cache.daoNews().deletePostsByType(type)
    }

    fun addPostList(list: List<CommonFeedItem>) {
        Cache.cache.daoNews().insert(list)
    }

    fun getPostList(type: String): Flowable<List<CommonFeedItem>> {
        return Cache.cache.daoNews().getPostList(type).map { it }
    }

    fun getPost(moduleType: String, id: String): CommonFeedItem? {
        return Cache.cache.daoNews().getPost(moduleType, id)
    }

    fun updatePost(commonFeedItem: CommonFeedItem) {
        return Cache.cache.daoNews().update(commonFeedItem)
    }

    fun deletePost(commonFeedItem: CommonFeedItem) {
        return Cache.cache.daoNews().delete(commonFeedItem)
    }

    fun deletePostById(id: String, type: String) {
        return Cache.cache.daoNews().deletePostById(id, type)
    }
}