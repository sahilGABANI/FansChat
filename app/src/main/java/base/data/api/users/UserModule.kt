package base.data.api.users

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class UserModule {

    @Provides
    @Singleton
    fun provideUserRetrofitAPI(retrofit: Retrofit): UserRetrofitAPI {
        return retrofit.create(UserRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userRetrofitAPI: UserRetrofitAPI
    ): UserRepository {
        return UserRepository(userRetrofitAPI)
    }
}