package base.data.network

import base.BuildConfig
import base.data.api.authentication.AuthenticationRetrofitAPI
import base.data.api.authentication.LoggedInUserCache
import base.data.api.authentication.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val loggedInUserCache: LoggedInUserCache
) : Authenticator {
    private val HEADER_AUTHORIZATION = "Authorization"
    private val REFRESH_TOKEN_FAIL = 403
    override fun authenticate(route: Route?, response: Response): Request? {
//        // This is a synchronous call
//        val updatedToken = getUpdatedToken()
//        return response.request.newBuilder()
//            .header("Authorization", "Bearer $updatedToken")
//            .build()

        // We need to have a token in order to refresh it.
        val token: String = loggedInUserCache.getLoginUserToken() ?: return null

        synchronized(this) {
            val newToken: String = loggedInUserCache.getLoginUserToken() ?: return null

            // Check if the request made was previously made as an authenticated request.
            if (response.request.header(HEADER_AUTHORIZATION) != null) {

                // If the token has changed since the request was made, use the new token.
                if (newToken != token) {
                    return response.request
                        .newBuilder()
                        .removeHeader(HEADER_AUTHORIZATION)
                        .addHeader(HEADER_AUTHORIZATION, "Bearer $newToken")
                        .build()
                }
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val tokenResponse = retrofit.create(AuthenticationRetrofitAPI::class.java)
                    .tokenRefresh(RefreshTokenRequest(loggedInUserCache.getLoginUserRefreshToken()))
                    .execute()
                if (tokenResponse.isSuccessful) {
                    val userToken = tokenResponse.body() ?: return null
                    loggedInUserCache.setLoggedInUserToken(userToken.token)
                    // Retry the request with the new token.
                    return response.request
                        .newBuilder()
                        .removeHeader(HEADER_AUTHORIZATION)
                        .addHeader(HEADER_AUTHORIZATION, "Bearer " + userToken.token)
                        .build()
                } else {
                    if (tokenResponse.code() == REFRESH_TOKEN_FAIL) {
                        logoutUser()
                    }
                }
            }
        }
        return null
    }

    private fun logoutUser() {
        loggedInUserCache.userUnauthorized()
    }

//    private fun getUpdatedToken(): String {
//        val refreshToken = loggedInUserCache.getLoginUserRefreshToken()
//        val authTokenResponse = authenticationRetrofitAPI.tokenRefresh(RefreshTokenRequest(refreshToken)).execute().body()?.token
//        loggedInUserCache.setLoggedInUserToken(authTokenResponse)
//        return authTokenResponse ?: ""
//    }
}