package base.data.api.news

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class NewsPostModule {

    @Provides
    @Singleton
    fun provideNewsPostRetrofitAPI(retrofit: Retrofit): NewsPostRetrofitAPI {
        return retrofit.create(NewsPostRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        newsPostRetrofitAPI: NewsPostRetrofitAPI
    ): NewsPostRepository {
        return NewsPostRepository(newsPostRetrofitAPI)
    }

    @Provides
    @Singleton
    fun provideNewsCacheRepository(): NewsCacheRepository {
        return NewsCacheRepository()
    }
}