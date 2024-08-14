package base.data.api.friends

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class FriendsModule {

    @Provides
    @Singleton
    fun provideFriendRequestRetrofitAPI(retrofit: Retrofit): FriendsRetrofitAPI {
        return retrofit.create(FriendsRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendRequestRepository(
        friendsRetrofitAPI: FriendsRetrofitAPI
    ): FriendsRepository {
        return FriendsRepository(friendsRetrofitAPI)
    }
}