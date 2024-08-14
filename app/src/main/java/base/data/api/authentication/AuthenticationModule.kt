package base.data.api.authentication

import base.data.prefs.LocalPrefs
import base.socket.SocketDataManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class AuthenticationModule {

    @Provides
    @Singleton
    fun provideLoggedInUserCache(prefs: LocalPrefs): LoggedInUserCache {
        return LoggedInUserCache(prefs)
    }

    @Provides
    @Singleton
    fun provideAuthenticationRetrofitAPI(retrofit: Retrofit): AuthenticationRetrofitAPI {
        return retrofit.create(AuthenticationRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        authenticationRetrofitAPI: AuthenticationRetrofitAPI,
        loggedInUserCache: LoggedInUserCache,
        socketDataManager: SocketDataManager
    ): AuthenticationRepository {
        return AuthenticationRepository(authenticationRetrofitAPI, loggedInUserCache, socketDataManager)
    }

    @Provides
    @Singleton
    fun provideProfileCacheRepository(): ProfileCacheDataRepository {
        return ProfileCacheDataRepository()
    }
}