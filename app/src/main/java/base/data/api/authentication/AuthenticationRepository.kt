package base.data.api.authentication

import base.data.api.authentication.model.*
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.friends.model.ProfileToggleRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.cache.Cache
import base.data.model.NotificationSettings
import base.socket.SocketDataManager
import io.reactivex.Single
import okhttp3.ResponseBody

class AuthenticationRepository(
    private val authenticationRetrofitAPI: AuthenticationRetrofitAPI,
    private val loggedInUserCache: LoggedInUserCache,
    private val socketDataManager: SocketDataManager
) {

    fun login(loginRequest: LoginRequest): Single<FansChatUser> {
        return authenticationRetrofitAPI.loginUser(loginRequest).doAfterSuccess { loginResponse ->
                loggedInUserCache.setLoggedInUserToken(loginResponse.token)
                loggedInUserCache.setLoggedInUserRefreshToken(loginResponse.refreshToken)
                loggedInUserCache.setLoggedInUserId(loginResponse.user.id)
                loggedInUserCache.setLoggedInPassword(loginRequest.password)
                loggedInUserCache.setUserName(loginResponse.user.displayName)
                loggedInUserCache.setLoggedInUserProfile(loginResponse.user.avatarUrl)
                socketDataManager.connect()
            }.map { it.user }
    }

    fun registerUser(registerRequest: RegisterRequest): Single<FansChatUser> {
        return authenticationRetrofitAPI.registerUser(registerRequest).doAfterSuccess { loginResponse ->
                loggedInUserCache.setLoggedInUserToken(loginResponse.token)
                loggedInUserCache.setLoggedInUserRefreshToken(loginResponse.refreshToken)
                loggedInUserCache.setLoggedInUserId(loginResponse.user.id)
                loggedInUserCache.setLoggedInPassword(registerRequest.password)
                loggedInUserCache.setUserName(loginResponse.user.displayName)
                socketDataManager.connect()
            }.map { it.user }
    }

    fun updateFirebaseToken(firebaseResponse: FirebaseResponse): Single<FirebaseResponse> {
        return authenticationRetrofitAPI.updateFirebaseToken(firebaseResponse).map { it }
    }

    fun resetPassword(requestPasswordRequest: RequestPasswordRequest): Single<ResponseBody> {
        return authenticationRetrofitAPI.resetPassword(requestPasswordRequest).map { it }
    }

    fun resetPasswordToken(
        token: String, requestPasswordTokenRequest: RequestPasswordTokenRequest
    ): Single<FansChatCommonMessage> {
        return authenticationRetrofitAPI.resetPasswordToken(token, requestPasswordTokenRequest).map { it }
    }

    fun getUserProfile(): Single<FansChatUserDetails> {
        loggedInUserCache.getLoggedInUserId() ?: return Single.error(Exception("User not login"))
        return authenticationRetrofitAPI.getUserProfile().map {
            loggedInUserCache.setLoggedInUserProfile(it.avatarUrl)
            loggedInUserCache.setUserName(it.displayName)
            it
        }
    }

    fun updateUserProfile(updateProfileRequest: UpdateProfileRequest): Single<FansChatUserDetails> {
        return authenticationRetrofitAPI.updateUserProfile(updateProfileRequest).map {
                loggedInUserCache.setLoggedInUserProfile(it.avatarUrl)
                loggedInUserCache.setLoggedInUserRefreshToken(it.refreshToken)
                it
            }
    }

    fun changePassword(changePasswordRequest: ChangePasswordRequest): Single<FansChatCommonMessage> {
        return authenticationRetrofitAPI.changePassword(changePasswordRequest).map { it }
    }


    fun updateNotificationToggles(notificationToggleRequest: ProfileToggleRequest): Single<NotificationSettings> {
        return authenticationRetrofitAPI.updateNotificationToggles(notificationToggleRequest).map { it }
    }

    fun getNotificationToggles(): Single<NotificationSettings> {
        return authenticationRetrofitAPI.getNotificationToggles().map { it }
    }

    fun facebookLoginUser(facebookLoginRequest: FacebookLoginRequest): Single<FansChatUser> {
        return authenticationRetrofitAPI.facebookLoginUser(facebookLoginRequest).doAfterSuccess { loginResponse ->
                loggedInUserCache.setLoggedInUserToken(loginResponse.token)
                loggedInUserCache.setLoggedInUserRefreshToken(loginResponse.refreshToken)
                loggedInUserCache.setLoggedInUserId(loginResponse.user.id)
                loggedInUserCache.setUserName(loginResponse.user.displayName)
                socketDataManager.connect()
            }.map { it.user }
    }

    fun updateAvatar(updateAvatarRequest: UpdateAvatarRequest): Single<FansChatUserDetails> {
        return authenticationRetrofitAPI.updateAvatar(updateAvatarRequest).map { it }
    }

    fun deleteAvatar(): Single<FansChatUserDetails> {
        return authenticationRetrofitAPI.deleteAvatar().map { it }
    }

    fun getListOfFriends(userId: String): Single<GetListOfFriendsResponse> {
        return authenticationRetrofitAPI.getListOfFriends(userId).map { it }
    }

    fun logout(): Single<FansChatCommonMessage> {
        return authenticationRetrofitAPI.logout()
    }
}

class ProfileCacheDataRepository {
    fun getOwnProfile(userId: String): Single<FansChatUserDetails> {
        return Cache.cache.daoPersonalProfile().getUser(userId).map { it }
    }

    fun addProfile(userDetails: FansChatUserDetails) {
        return Cache.cache.daoPersonalProfile().insertOrUpdate(userDetails.toDaoFansChatUserDetails())
    }
}