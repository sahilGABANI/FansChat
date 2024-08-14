package base.data.network

import android.content.Context
import base.BuildConfig
import base.data.api.authentication.LoggedInUserCache
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideEmptyInterceptorHeaders(): EmptyBodyInterceptor {
        return EmptyBodyInterceptor()
    }

    @Provides
    @Singleton
    fun provideFansChatInterceptorHeaders(loggedInUserCache: LoggedInUserCache): FansChatInterceptorHeaders {
        return FansChatInterceptorHeaders(loggedInUserCache)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        loggedInUserCache: LoggedInUserCache
    ): TokenAuthenticator {
        return TokenAuthenticator(loggedInUserCache)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        context: Context,
        fansChatInterceptorHeaders: FansChatInterceptorHeaders,
        emptyBodyInterceptor: EmptyBodyInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cacheDir = File(context.cacheDir, "HttpCache")
        val cache = Cache(cacheDir, cacheSize.toLong())
        val builder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(240, TimeUnit.SECONDS)
            .cache(cache)
            .authenticator(tokenAuthenticator)
            .addInterceptor(emptyBodyInterceptor)
            .addInterceptor(fansChatInterceptorHeaders)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.createAsync()) // Using create async means all api calls are automatically created asynchronously using OkHttp's thread pool
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .enableComplexMapKeySerialization()
                        .create()
                )
            )
            .build()
    }
}