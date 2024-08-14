package base.data.api.authentication

import base.data.api.authentication.model.*
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.friends.model.ProfileToggleRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.model.NotificationSettings
import base.data.network.ErrorType
import base.data.network.model.FansChatError
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface AuthenticationRetrofitAPI {
    @POST("auth/login")
    @ErrorType(FansChatError::class)
    fun loginUser(@Body loginRequest: LoginRequest): Single<LoginResponse>

    @POST("auth/register")
    @ErrorType(FansChatError::class)
    fun registerUser(@Body registerRequest: RegisterRequest): Single<LoginResponse>

    @PUT("auth/firebase-token")
    @ErrorType(FansChatError::class)
    fun updateFirebaseToken(@Body firebaseResponse: FirebaseResponse): Single<FirebaseResponse>

    @GET("auth/profile")
    @ErrorType(FansChatError::class)
    fun getUserProfile(): Single<FansChatUserDetails>

    @PUT("auth/profile")
    @ErrorType(FansChatError::class)
    fun updateUserProfile(@Body updateProfileRequest: UpdateProfileRequest): Single<FansChatUserDetails>

    @PUT("auth/password-change")
    @ErrorType(FansChatError::class)
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Single<FansChatCommonMessage>

    @POST("auth/token-refresh")
    @ErrorType(FansChatError::class)
    fun tokenRefresh(@Body refreshTokenRequest: RefreshTokenRequest): Call<RefreshTokenResponse>

    @POST("auth/password-reset")
    @ErrorType(FansChatError::class)
    fun resetPassword(@Body requestPasswordRequest: RequestPasswordRequest): Single<ResponseBody>

    @POST("auth/password-reset/{token}")
    @ErrorType(FansChatError::class)
    fun resetPasswordToken(
        @Path("token") token: String,
        @Body requestPasswordTokenRequest: RequestPasswordTokenRequest
    ): Single<FansChatCommonMessage>

    @POST("auth/facebook")
    @ErrorType(FansChatError::class)
    fun facebookLoginUser(@Body facebookLoginRequest: FacebookLoginRequest): Single<LoginResponse>

    @POST("avatar")
    fun updateAvatar(@Body updateAvatarRequest: UpdateAvatarRequest): Single<FansChatUserDetails>

    @DELETE("avatar")
    fun deleteAvatar(): Single<FansChatUserDetails>

    @GET("friends/{userId}")
    @ErrorType(FansChatError::class)
    fun getListOfFriends(@Path("userId") userId: String): Single<GetListOfFriendsResponse>

    @PUT("auth/settings/notifications")
    @ErrorType(FansChatError::class)
    fun updateNotificationToggles(@Body profileToggleRequest: ProfileToggleRequest): Single<NotificationSettings>

    @GET("auth/settings/notifications")
    @ErrorType(FansChatError::class)
    fun getNotificationToggles(): Single<NotificationSettings>

    @POST("auth/logout")
    @ErrorType(FansChatError::class)
    fun logout(): Single<FansChatCommonMessage>
}

