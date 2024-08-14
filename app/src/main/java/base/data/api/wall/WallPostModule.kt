package base.data.api.wall

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class WallPostModule {

    @Provides
    @Singleton
    fun provideWallPostRetrofitAPI(retrofit: Retrofit): WallPostRetrofitAPI {
        return retrofit.create(WallPostRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideWallRepository(
        wallPostRetrofitAPI: WallPostRetrofitAPI
    ): WallPostsRepository {
        return WallPostsRepository(wallPostRetrofitAPI)
    }

    @Provides
    @Singleton
    fun provideWallCacheRepository(
    ): WallCacheRepository {
        return WallCacheRepository()
    }
}