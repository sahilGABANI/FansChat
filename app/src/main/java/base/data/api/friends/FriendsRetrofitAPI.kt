package base.data.api.friends

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.friendrequest.model.*
import base.data.api.friends.model.DeleteFriendsRequest
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.friends.model.TranslateMessage

import io.reactivex.Single
import retrofit2.http.*

interface FriendsRetrofitAPI {

    @GET("friends")
    fun getListOfFriends(@Query("page") page: Int, @Query("perPage") perPage: Int): Single<GetListOfFriendsResponse>

    @GET("friends/{id}/mutual")
    fun getListOfMutualFriends(@Path("id") id: String, @Body getFriendRequestRequest: GetFriendRequestRequest): Single<GetListOfFriendsResponse>

    @DELETE("friends/{id}")
    fun deleteFriend(@Path("id") id: String, @Body deleteFriendsRequest: DeleteFriendsRequest): Single<FansChatCommonMessage>

    @POST("ims/messages/{postId}/translate/{targetLanguage}")
    fun getTranslateMessage(@Path("postId") postId: String, @Path("targetLanguage") targetLanguage: String): Single<TranslateMessage>
}
