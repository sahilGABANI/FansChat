package base.data.api.friendrequest

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.friendrequest.model.*
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.users.model.FansChatUserDetails
import base.data.network.ErrorType
import base.data.network.model.FansChatError
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface FriendRequestRetrofitAPI {

    @POST("friends-requests")
    @ErrorType(FansChatError::class)
    fun sendFriendsRequest(@Body friendRequestRequest: FriendRequestRequest): Single<FriendRequestResponse>

    @GET("friends-requests")
    fun getFriendRequest(@Query("page") page: Int, @Query("perPage") perPage: Int): Single<ListOfFriendRequest>

    @DELETE("friends-requests/{userId}")
    fun declineFriendRequest(@Path("userId") userId: String): Single<ResponseBody>

    @POST("friends-requests/{userId}")
    fun acceptFriendRequest(@Path("userId") userId: String): Single<AcceptFriendRequestResponse>

    @GET("users/{userId}")
    fun getUserDetails(@Path("userId") userId: Int): Single<FansChatUserDetails>

    @DELETE("friends/{userId}")
    fun deleteFriends(@Path("userId") userId: String): Single<ResponseBody>

    @GET("friends/{userId}")
    @ErrorType(FansChatError::class)
    fun getListOfFriends(@Path("userId") userId: String, @Query("page") page: Int, @Query("perPage") perPage: Int): Single<GetListOfFriendsResponse>

    @POST("report/user")
    fun reportUser(@Body reportRequest: ReportRequest): Single<ReportResponse>
}
