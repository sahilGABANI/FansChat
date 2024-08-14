package base.data.api.wall

import base.data.api.wall.model.*
import base.data.model.CommonFeedItem
import base.data.model.wall.ContentItem
import base.data.model.wall.WallData
import base.data.network.ErrorType
import base.data.network.model.FansChatError
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface WallPostRetrofitAPI {
    @POST("wall")
    @ErrorType(FansChatError::class)
    fun addWallPost(@Body wallPostRequest: WallPostRequest): Single<ContentItem>

    @GET("wall")
    @ErrorType(FansChatError::class)
    fun getWallPost(@Query("page") page: Int, @Query("perPage") perPage: Int): Single<ListOfWallPostResponse>

    @DELETE("posts/{postId}")
    @ErrorType(FansChatError::class)
    fun deleteWallPost(@Path("postId") postId: String): Single<ResponseBody>

    @GET("posts/{postId}")
    @ErrorType(FansChatError::class)
    fun getWallPostData(@Path("postId") postId: String, @Query("lang") language: String): Single<CommonFeedItem>

    @PUT("posts/{postId}")
    @ErrorType(FansChatError::class)
    fun updateWallPost(@Path("postId") postId: String, @Body wallPostRequest: WallPostRequest): Single<CommonFeedItem>

    @GET("posts/{postId}/comments")
    @ErrorType(FansChatError::class)
    fun getWallPostComment(@Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<CommentResponse>

    @POST("wall-comments")
    @ErrorType(FansChatError::class)
    fun addWallPostComment(@Body addCommentRequest: AddCommentRequest): Single<AddCommentsResponse>

    @DELETE("wall-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun deleteWallPostComment(@Path("commentId") commentId: String): Single<ResponseBody>

    @PUT("wall-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun updateWallPostComment(@Path("commentId") commentId: String,
        @Body addCommentRequest: UpdateComment): Single<Comment>

    @PUT("posts/{postId}/likes")
    @ErrorType(FansChatError::class)
    fun likeWallPosts(@Path("postId") postId: String, @Body updateLike: UpdateLike): Single<CommonFeedItem>

    @GET("feed/meta/{clubId}/{lang}")
    @ErrorType(FansChatError::class)
    fun getNewWall(@Path("clubId") clubId: String, @Path("lang") lang: String): Single<WallData>

    @GET("feed/data/{clubId}/{lang}")
    @ErrorType(FansChatError::class)
    fun getNextCarousels(@Path("clubId") clubId: String,
        @Path("lang") language: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<ArrayList<ContentItem>>

    @GET("news/{postId}")
    @ErrorType(FansChatError::class)
    fun getNewsPostData(@Path("postId") postId: String, @Query("lang") language: String): Single<CommonFeedItem>

    @GET("social/{postId}")
    @ErrorType(FansChatError::class)
    fun getSocialPostData(@Path("postId") postId: String, @Query("lang") language: String): Single<CommonFeedItem>

    @GET("rumors/{postId}")
    @ErrorType(FansChatError::class)
    fun getRumoursPostData(@Path("postId") postId: String, @Query("lang") language: String): Single<CommonFeedItem>

    @GET("videos/{postId}")
    @ErrorType(FansChatError::class)
    fun getVideoPostData(@Path("postId") postId: String, @Query("lang") language: String): Single<CommonFeedItem>

    @GET("news/{postId}/comments")
    @ErrorType(FansChatError::class)
    fun getNewsPostComment(@Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<CommentResponse>

    @GET("social/{postId}/comments")
    @ErrorType(FansChatError::class)
    fun getSocialPostComment(@Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<CommentResponse>

    @GET("rumors/{postId}/comments")
    @ErrorType(FansChatError::class)
    fun getRumoursPostComment(@Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<CommentResponse>

    @GET("videos/{postId}/comments")
    @ErrorType(FansChatError::class)
    fun getVideoPostComment(@Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int): Single<CommentResponse>

    @PUT("news/{postId}/likes")
    @ErrorType(FansChatError::class)
    fun likeNewsPosts(@Path("postId") postId: String, @Body updateLike: UpdateLike): Single<CommonFeedItem>

    @PUT("social/{postId}/likes")
    @ErrorType(FansChatError::class)
    fun likeSocialPosts(@Path("postId") postId: String, @Body updateLike: UpdateLike): Single<CommonFeedItem>

    @PUT("rumors/{postId}/likes")
    @ErrorType(FansChatError::class)
    fun likeRumoursPosts(@Path("postId") postId: String, @Body updateLike: UpdateLike): Single<CommonFeedItem>

    @PUT("videos/{postId}/likes")
    @ErrorType(FansChatError::class)
    fun likeVideoPosts(@Path("postId") postId: String, @Body updateLike: UpdateLike): Single<CommonFeedItem>

    @POST("news-comments")
    @ErrorType(FansChatError::class)
    fun addNewsPostComment(@Body addCommentRequest: AddCommentRequest): Single<AddCommentsResponse>

    @POST("social-comments")
    @ErrorType(FansChatError::class)
    fun addSocialPostComment(@Body addCommentRequest: AddCommentRequest): Single<AddCommentsResponse>

    @POST("rumors-comments")
    @ErrorType(FansChatError::class)
    fun addRumoursPostComment(@Body addCommentRequest: AddCommentRequest): Single<AddCommentsResponse>

    @POST("video-comments")
    @ErrorType(FansChatError::class)
    fun addVideoPostComment(@Body addCommentRequest: AddCommentRequest): Single<AddCommentsResponse>

    @DELETE("news-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun deleteNewsPostComment(@Path("commentId") commentId: String): Single<ResponseBody>

    @DELETE("social-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun deleteSocialPostComment(@Path("commentId") commentId: String): Single<ResponseBody>

    @DELETE("rumors-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun deleteRumoursPostComment(@Path("commentId") commentId: String): Single<ResponseBody>

    @DELETE("video-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun deleteVideoPostComment(@Path("commentId") commentId: String): Single<ResponseBody>

    @PUT("news-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun updateNewsPostComment(@Path("commentId") commentId: String,
        @Body addCommentRequest: UpdateComment): Single<Comment>

    @PUT("social-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun updateSocialPostComment(@Path("commentId") commentId: String,
        @Body addCommentRequest: UpdateComment): Single<Comment>

    @PUT("rumors-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun updateRumorsPostComment(@Path("commentId") commentId: String,
        @Body addCommentRequest: UpdateComment): Single<Comment>

    @PUT("video-comments/{commentId}")
    @ErrorType(FansChatError::class)
    fun updateVideoPostComment(@Path("commentId") commentId: String,
        @Body addCommentRequest: UpdateComment): Single<Comment>

    /*----------------------------------- Translation Start----------------------------------*/

    /*Comments*/
    @POST("news-comments/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateNewsComment(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("social-comments/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateSocialComment(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("rumors-comments/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateRumorsComment(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("video-comments/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateVideoComment(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("wall-comments/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateWallComment(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    /*Posts*/
    @POST("news/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateNewsPost(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("social/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateSocialPost(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("rumors/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateRumorsPost(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("videos/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateVideoPost(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>

    @POST("posts/{postId}/translate/{targetLanguage}")
    @ErrorType(FansChatError::class)
    fun translateWallPost(@Path("postId") postId: String,
        @Path("targetLanguage") targetLanguage: String): Single<TranslateResponse>
/*----------------------------------- Translation END----------------------------------*/

/*----------------------------------- Sharing Start----------------------------------*/

    @GET("news/{postId}/share")
    @ErrorType(FansChatError::class)
    fun getShareNewsUrl(@Path("postId") postId: String): Single<SharingResponse>

    @GET("social/{postId}/share")
    @ErrorType(FansChatError::class)
    fun getShareSocialUrl(@Path("postId") postId: String): Single<SharingResponse>

    @GET("rumors/{postId}/share")
    @ErrorType(FansChatError::class)
    fun getShareRumoursUrl(@Path("postId") postId: String): Single<SharingResponse>

    @GET("videos/{postId}/share")
    @ErrorType(FansChatError::class)
    fun getShareVideoUrl(@Path("postId") postId: String): Single<SharingResponse>

    @GET("posts/{postId}/share")
    @ErrorType(FansChatError::class)
    fun getShareWallUrl(@Path("postId") postId: String): Single<SharingResponse>
/*----------------------------------- Sharing END----------------------------------*/


/*----------------------------------- Report Start----------------------------------*/

    @POST("report/news")
    @ErrorType(FansChatError::class)
    fun reportNewsPost(@Body reportPostRequest: ReportPostRequest): Single<ReportPostResponse>

    @POST("report/social")
    @ErrorType(FansChatError::class)
    fun reportSocialPost(@Body reportPostRequest: ReportPostRequest): Single<ReportPostResponse>

    @POST("report/rumors")
    @ErrorType(FansChatError::class)
    fun reportRumoursPost(@Body reportPostRequest: ReportPostRequest): Single<ReportPostResponse>

    @POST("report/videos")
    @ErrorType(FansChatError::class)
    fun reportVideoPost(@Body reportPostRequest: ReportPostRequest): Single<ReportPostResponse>

    @POST("report/posts")
    @ErrorType(FansChatError::class)
    fun reportWallPost(@Body reportPostRequest: ReportPostRequest): Single<ReportPostResponse>
/*----------------------------------- Report END----------------------------------*/

}