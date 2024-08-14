package base.data.api.friendrequest

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class FriendRequestModule {

    @Provides
    @Singleton
    fun provideFriendRequestRetrofitAPI(retrofit: Retrofit): FriendRequestRetrofitAPI {
        return retrofit.create(FriendRequestRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendRequestRepository(
        friendRequestRetrofitAPI: FriendRequestRetrofitAPI
    ): FriendRequestRepository {
        return FriendRequestRepository(friendRequestRetrofitAPI)
    }
}