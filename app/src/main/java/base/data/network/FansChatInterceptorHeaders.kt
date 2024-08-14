package base.data.network

import base.data.api.authentication.LoggedInUserCache
import base.util.Constants
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

class FansChatInterceptorHeaders(
    private val loggedInUserCache: LoggedInUserCache
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()
        requestBuilder.header("Content-Type", "application/json")
        requestBuilder.header("Accept", "application/json")

        val token = loggedInUserCache.getLoginUserToken() ?: ""
        //Timber.i("Token %s", isAccessTokenValid())
        if (original.url.host == "api.vimeo.com") {
            requestBuilder.header("Authorization", Credentials.basic(Constants.VIMEO_CLIENT_ID, Constants.VIMEO_CLIENT_SECRET))
        } else {
            if (token.isNotEmpty()) {
                // authenticated user
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        val response: Response
        try {
            response = chain.proceed(requestBuilder.build())
        } catch (t: Throwable) {
            Timber.e("error in FansChatInterceptorHeaders:\n${t.message}")
            throw IOException(t.message)
        }
        return response
    }
}
