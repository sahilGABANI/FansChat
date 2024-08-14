package base.data.api.news

import base.data.network.ErrorType
import base.data.network.model.FansChatError
import base.socket.model.GetWallResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsPostRetrofitAPI {
    @GET("news")
    @ErrorType(FansChatError::class)
    fun getNewsPost(@Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lang") language: String): Single<GetWallResponse>

    @GET("social")
    @ErrorType(FansChatError::class)
    fun getSocialPost(@Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lang") language: String): Single<GetWallResponse>

    @GET("rumors")
    @ErrorType(FansChatError::class)
    fun getRumourPost(@Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lang") language: String): Single<GetWallResponse>

    @GET("videos")
    @ErrorType(FansChatError::class)
    fun getClubTvPost(@Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("lang") language: String): Single<GetWallResponse>
}