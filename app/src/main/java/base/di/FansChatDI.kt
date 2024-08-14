package base.di

import android.app.Application
import android.content.Context
import base.application.FansChat
import base.data.api.authentication.AuthenticationModule
import base.data.api.chat.ChatModule
import base.data.api.friendrequest.FriendRequestModule
import base.data.api.friends.FriendsModule
import base.data.api.news.NewsPostModule
import base.data.api.notifications.NotificationsModule
import base.data.api.users.UserModule
import base.data.api.wall.WallPostModule
import base.data.network.NetworkModule
import base.data.prefs.PrefsModule
import base.data.viewmodelmodule.FansChatViewModelProvider
import base.socket.SocketManagerModule
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FansChatAppModule(val app: Application) {
    @Provides
    @Singleton
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @Singleton
    fun provideContext(): Context {
        return app
    }
}

@Singleton
@Component(
    modules = [
        FansChatAppModule::class,
        PrefsModule::class,
        NetworkModule::class,
        AuthenticationModule::class,
        FansChatViewModelProvider::class,
        SocketManagerModule::class,
        FriendRequestModule::class,
        UserModule::class,
        WallPostModule::class,
        ChatModule::class,
        FriendsModule::class,
        NewsPostModule::class,
        NotificationsModule::class
    ]

)
interface FansChatAppComponent : BaseAppComponent {
    fun inject(app: FansChat)
}