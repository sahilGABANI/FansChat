package base.data.api.users

import base.data.api.users.model.FansChatUserDetails
import base.data.api.users.model.GetUserResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserRetrofitAPI {
    @GET("users")
    fun getUserList(
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("isAdmin") isAdmin: Boolean
    ): Single<GetUserResponse>

    @GET("users")
    fun searchGetUserList(
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("keywords") keywords: String,
        @Query("isAdmin") isAdmin: Boolean
    ): Single<GetUserResponse>
//    fun getUserList(@Body getFriendRequestRequest: GetFriendRequestRequest): Single<GetUserResponse>

    @GET("users/{userId}")
    fun getUserDetails(@Path("userId") userId: String): Single<FansChatUserDetails>
}