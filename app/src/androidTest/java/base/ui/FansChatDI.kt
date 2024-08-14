package base.ui

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
import base.di.FansChatAppComponent
import base.di.FansChatAppModule
import base.socket.SocketManagerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [FansChatAppModule::class, PrefsModule::class, NetworkModule::class, AuthenticationModule::class, FansChatViewModelProvider::class, SocketManagerModule::class, FriendRequestModule::class, UserModule::class, WallPostModule::class, ChatModule::class, FriendsModule::class, NewsPostModule::class, NotificationsModule::class]
)
interface TestAppComponent : FansChatAppComponent {
    fun inject(test: MainActivityEspressoTest)
}